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
"
# Copied from opencv_linux.BUILD
RDEPENDS_${PN} = " \
    libopencv-core \
    libopencv-calib3d \
    libopencv-features2d \
    libopencv-highgui \
    libopencv-imgcodecs \
    libopencv-imgproc \
    libopencv-video \
    libopencv-videoio \
"


SRCREV = "374f5e2e7e818bde5289fb3cffa616705cec6f73"
SRC_URI = "git://github.com/google/mediapipe.git;branch=master \
           file://BUILD.in \
           file://BUILD.yocto_compiler \
           file://cc_config.bzl.tpl \
           file://yocto_compiler_configure.bzl \
           file://0001-Yocto-patches.patch \
           file://0001-Fix-openCV-library-path.patch \
           "

S = "${WORKDIR}/git"

inherit python3native bazel

export PYTHON_BIN_PATH="${PYTHON}"
export PYTHON_LIB_PATH="${STAGING_LIBDIR_NATIVE}/${PYTHON_DIR}/site-packages"

export CROSSTOOL_PYTHON_INCLUDE_PATH="${STAGING_INCDIR}/python${PYTHON_BASEVERSION}${PYTHON_ABI}"

do_configure_append () {
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

    CT_NAME=$(echo ${HOST_PREFIX} | rev | cut -c 2- | rev)
    SED_COMMAND="s#%%CT_NAME%%#${CT_NAME}#g"
    SED_COMMAND="${SED_COMMAND}; s#%%WORKDIR%%#${WORKDIR}#g"
    SED_COMMAND="${SED_COMMAND}; s#%%YOCTO_COMPILER_PATH%%#${BAZEL_OUTPUTBASE_DIR}/external/yocto_compiler#g"

    sed -i "${SED_COMMAND}" ${S}/BUILD.yocto_compiler \
                            ${S}/WORKSPACE
}

MP_TARGET_EXTRA ??= ""
do_compile () {
    export CT_NAME=$(echo ${HOST_PREFIX} | rev | cut -c 2- | rev)
    unset CC
    # DEGL_NO_X11 is due to x11 headers being gammy - it doesn't actually prevent
    # EGL from using X11, via "default display" type stuff.
    ${BAZEL} build \
        ${MP_ARGS_EXTRA} \
        -c opt \
        --copt -DEGL_NO_X11 \
        --subcommands --explain=${T}/explain.log \
        --cpu=${BAZEL_TARGET_CPU} \
        --crosstool_top=@local_config_yocto_compiler//:toolchain \
        --host_crosstool_top=@bazel_tools//tools/cpp:toolchain \
        --verbose_explanations --verbose_failures \
        --verbose_failures \
        mediapipe/examples/desktop/face_detection:face_detection_gpu \
        ${MP_TARGET_EXTRA}
}

do_install() {
    install -d ${D}${bindir}
    install -m 755 ${S}/bazel-bin/mediapipe/examples/desktop/face_detection/face_detection_gpu \
        ${D}${bindir}
    # runtime data
    install -d ${D}/opt/mediapipe/
    cp -r ${S}/mediapipe/graphs/ ${D}/opt/mediapipe/
    chmod 644 -R ${D}/opt/mediapipe/graphs
    find ${D}/opt/mediapipe/graphs -type d -exec chmod 0755 {} +
}

FILES_${PN} = "/opt/mediapipe/* ${bindir}/*"

# do_compile_append() {
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
