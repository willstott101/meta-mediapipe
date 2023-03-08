DESCRIPTION = "Mediapipe C++ Library"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=86d3f3a95c324c9479bd8986968f4327"

DEPENDS = " \
    bazel-native \
    protobuf-native \
    util-linux-native \
    protobuf \
    mesa \
    opencv \
    ffmpeg \
    python3 \
    python3-numpy-native \
    python3-keras-applications-native \
    python3-keras-preprocessing-native \
    python3-pip-native \
    python3-wheel-native \
    xxd-native \
    libusb1 \
"
# Copied from opencv_linux.BUILD
RDEPENDS:${PN} = " \
    libopencv-core \
    libopencv-calib3d \
    libopencv-features2d \
    libopencv-highgui \
    libopencv-imgcodecs \
    libopencv-imgproc \
    libopencv-video \
    libopencv-videoio \
"

SRCREV = "33d683c67100ef3db37d9752fcf65d30bea440c4"

SRC_URI = "git://github.com/google/mediapipe.git;branch=master \
           file://BUILD.in \
           file://BUILD.yocto_compiler \
           file://cc_config.bzl.tpl \
           file://yocto_compiler_configure.bzl \
           file://0001-Yocto-patches.patch \
           file://0001-Fix-opencv-ffmpeg-library-paths.patch \
           file://mediapipe-config-version.cmake \
           file://mediapipe-config.cmake \
           file://mediapipe-targets-release.cmake \
           file://mediapipe-targets.cmake \
           file://0002-Build-library.patch \
           file://0003-Use-yocto-protobuf.patch \
           file://protobuf_yocto.BUILD \
           file://com_google_protobuf_use_protoc_on_path.diffforbazeltoapply \
           file://0001-instruct-bazel-to-patch-libedgetpu.patch \
           file://org_tensorflow_py310_fixes.diffforbazeltoapply \
           file://0001-patches-for-py310.patch \
           file://0007-Bugfix-for-broken-cc-rules-upstream.patch \
           "

S = "${WORKDIR}/git"

inherit python3native bazel

export PYTHON_BIN_PATH="${PYTHON}"
export PYTHON_LIB_PATH="${STAGING_LIBDIR_NATIVE}/${PYTHON_DIR}/site-packages"

export CROSSTOOL_PYTHON_INCLUDE_PATH="${STAGING_INCDIR}/python${PYTHON_BASEVERSION}${PYTHON_ABI}"

do_configure:append () {
    if [ ! -e ${CROSSTOOL_PYTHON_INCLUDE_PATH}/pyconfig-target.h ];then
        mv ${CROSSTOOL_PYTHON_INCLUDE_PATH}/pyconfig.h ${CROSSTOOL_PYTHON_INCLUDE_PATH}/pyconfig-target.h
    fi

    install -m 644 ${STAGING_INCDIR_NATIVE}/python${PYTHON_BASEVERSION}${PYTHON_ABI}/pyconfig.h \
        ${CROSSTOOL_PYTHON_INCLUDE_PATH}/pyconfig-native.h

    cat > ${CROSSTOOL_PYTHON_INCLUDE_PATH}/pyconfig.h <<ENDOF
#if defined (_PYTHON_INCLUDE_TARGET)
#include "pyconfig-target.h"
#elif defined (_PYTHON_INCLUDE_NATIVE)
#include "pyconfig-native.h"
#else
#error "_PYTHON_INCLUDE_TARGET or _PYTHON_INCLUDE_NATIVE is not defined"
#endif // End of #if defined (_PYTHON_INCLUDE_TARGET)

ENDOF

    mkdir -p ${S}/third_party/toolchains/yocto/
    sed "s#%%CPU%%#${BAZEL_TARGET_CPU}#g" ${WORKDIR}/BUILD.in  > ${S}/third_party/toolchains/yocto/BUILD
    chmod 644 ${S}/third_party/toolchains/yocto/BUILD
    install -m 644 ${WORKDIR}/cc_config.bzl.tpl ${S}/third_party/toolchains/yocto/
    install -m 644 ${WORKDIR}/yocto_compiler_configure.bzl ${S}/third_party/toolchains/yocto/
    install -m 644 ${WORKDIR}/BUILD.yocto_compiler ${S}

    # We let bazel download protobuf - but we replace it's build file to use our local dynamic libs and headers.
    install -m 644 ${WORKDIR}/protobuf_yocto.BUILD ${S}
    # We patch it's bazel scripts to just use the "protoc" on path rather then get the path from bazel.
    install -m 644 ${WORKDIR}/com_google_protobuf_use_protoc_on_path.diffforbazeltoapply ${S}
    # Fixes for distutils.sysconfig being deprecated
    install -m 644 ${WORKDIR}/org_tensorflow_py310_fixes.diffforbazeltoapply ${S}

    CT_NAME=$(echo ${HOST_PREFIX} | rev | cut -c 2- | rev)
    SED_COMMAND="s#%%CT_NAME%%#${CT_NAME}#g"
    SED_COMMAND="${SED_COMMAND}; s#%%WORKDIR%%#${WORKDIR}#g"
    SED_COMMAND="${SED_COMMAND}; s#%%YOCTO_COMPILER_PATH%%#${BAZEL_OUTPUTBASE_DIR}/external/yocto_compiler#g"

    sed -i "${SED_COMMAND}" ${S}/BUILD.yocto_compiler \
                            ${S}/WORKSPACE
}

# MP_TARGET ??= "mediapipe/examples/desktop/face_detection:face_detection_gpu"
MP_TARGET ??= "mediapipe/examples/desktop/libmediapipe:libmediapipe.so"
do_compile () {
    export CT_NAME=$(echo ${HOST_PREFIX} | rev | cut -c 2- | rev)
    unset CC

    protoc --version
    which protoc

    # DEGL_NO_X11 is due to x11 headers being gammy - it doesn't actually prevent
    # EGL from using X11, via "default display" apis.
    ${BAZEL} build \
        ${MP_ARGS_EXTRA} \
        -c opt \
        --copt -DEGL_NO_X11 \
        --define darwinn_portable=1 \
        --define MEDIAPIPE_EDGE_TPU=usb \
        --linkopt=-l:libusb-1.0.so \
        --copt -DMEDIAPIPE_EDGE_TPU \
        --copt=-flax-vector-conversions \
        --subcommands --explain=${T}/explain.log \
        --cpu=${BAZEL_TARGET_CPU} \
        --crosstool_top=@local_config_yocto_compiler//:toolchain \
        --host_crosstool_top=@bazel_tools//tools/cpp:toolchain \
        --verbose_explanations \
        --verbose_failures \
        ${MP_TARGET}
}

do_install() {
    # There are loads of .so files we don't want... so we have to filter by directory
    install -d ${D}${libdir}
    install -m 755 ${S}/bazel-bin/mediapipe/examples/desktop/libmediapipe/libmediapipe.so \
        ${D}${libdir}/libmediapipe.so.${PV}
    # TODO: Don't hardcode .0 here and match it to the cmake ref too
    ln -s -r ${D}${libdir}/libmediapipe.so.${PV} ${D}${libdir}/libmediapipe.so.0

    install -d ${D}${bindir}
    # There are loads of executable files we don't want... so take any with no '.'' in the path...
    find ${S}/bazel-bin/mediapipe/ -type f -executable ! -path '*.*' -exec install -D -m 755 {} ${D}/${bindir} \;

    # QDH: Just include ALL graph definitions
    for f in $( find ${S}/mediapipe/graphs/ -name '*.pbtxt' -printf "%P\n" );
    do
        install -D -m 644 ${S}/mediapipe/graphs/${f} ${D}/opt/mediapipe/graphs/${f}
    done

    # QDH: Just include ALL tflite models
    for f in $( find ${S}/mediapipe/ -name '*.tflite' -printf "%P\n" );
    do
        install -D -m 644 ${S}/mediapipe/${f} ${D}/opt/mediapipe/${f}
    done


    # QDH: Just include ALL headers from the source - there seems no built-in way to get only
    # the relevant headers out from bazel.
    for f in $( find ${BAZEL_OUTPUTBASE_DIR}/external/com_google_absl/ -regex ".*\.\(inc\|h\)\$" -printf "%P\n" );
    do
        # Save third-party headers to mediapipe-external to avoid any potential clashes.
        install -D -m 644 ${BAZEL_OUTPUTBASE_DIR}/external/com_google_absl/${f} \
            ${D}${includedir}/mediapipe-external/${f}
    done

    for f in $( find ${BAZEL_OUTPUTBASE_DIR}/execroot/mediapipe/bazel-out/host/bin/mediapipe/ -name '*.pb.h' -printf "%P\n" );
    do
        install -D -m 644 ${BAZEL_OUTPUTBASE_DIR}/execroot/mediapipe/bazel-out/host/bin/mediapipe/${f} \
            ${D}${includedir}/mediapipe/${f}
    done
    for f in $( find ${S}/mediapipe -name '*.h' -printf "%P\n" );
    do
        install -D -m 644 ${S}/mediapipe/${f} ${D}${includedir}/mediapipe/${f}
    done

    # We've copy-pasted these cmake-generated files and modified them - since we're not actually using cmake to build..
    # It'd probably be neater to use cmake to generate these files from our .so
    CMAKE_DEST=${D}${libdir}/cmake/mediapipe
    install -d ${CMAKE_DEST}
    sed "s#%%PV%%#${PV}#g" ${WORKDIR}/mediapipe-config-version.cmake > ${CMAKE_DEST}/mediapipe-config-version.cmake
    install -m 644 ${WORKDIR}/mediapipe-config.cmake ${CMAKE_DEST}
    sed "s#%%PV%%#${PV}#g" ${WORKDIR}/mediapipe-targets-release.cmake > ${CMAKE_DEST}/mediapipe-targets-release.cmake
    install -m 644 ${WORKDIR}/mediapipe-targets.cmake ${CMAKE_DEST}
    chmod 644 ${CMAKE_DEST}/*

    find ${D} -type d -empty -delete
}

FILES:${PN} = "/opt/mediapipe/* ${bindir}/* ${libdir}/*"
INSANE_SKIP:${PN}-dev += " dev-elf "

# do_compile:append() {
#     chmod a+w ${BAZEL_DIR}/output_base/execroot/org_tensorflow/bazel-out/*/bin/tensorflow/lite/python/schema_py_srcs_no_include_all
#     chmod a+w ${BAZEL_DIR}/output_base/execroot/org_tensorflow/bazel-out/*/bin/tensorflow/lite/python/schema_py_srcs_no_include_all/tflite
# }

inherit siteinfo unsupportarch
python __anonymous() {
    if d.getVar("SITEINFO_ENDIANNESS") == 'be':
        msg =  "\nIt failed to use pre-build model to do predict/inference on big-endian platform"
        msg += "\n(such as qemumips), since upstream does not support big-endian very well."
        msg += "\nDetails: https://github.com/tensorflow/tensorflow/issues/16364"
        bb.warn(msg)
}
