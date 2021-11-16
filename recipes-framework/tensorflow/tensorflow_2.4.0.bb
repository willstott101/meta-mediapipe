include tensorflow.inc

SRC_URI += " \
           file://0001-add-yocto-toolchain-to-support-cross-compiling.patch \
           file://0001-fix-build-tensorflow-lite-examples-label_image-label.patch \
           file://0001-label_image-tweak-default-model-location.patch \
           file://0001-label_image.lite-tweak-default-model-location.patch \
           file://0001-CheckFeatureOrDie-use-warning-to-avoid-die.patch \
           file://0001-support-32-bit-x64-and-arm-for-yocto.patch \
           file://BUILD.in \
           file://BUILD.yocto_compiler \
           file://cc_config.bzl.tpl \
           file://yocto_compiler_configure.bzl \
          "

SRC_URI += "https://storage.googleapis.com/download.tensorflow.org/models/inception_v3_2016_08_28_frozen.pb.tar.gz;name=model-inv3"
SRC_URI[model-inv3.md5sum] = "a904ddf15593d03c7dd786d552e22d73"
SRC_URI[model-inv3.sha256sum] = "7045b72a954af4dce36346f478610acdccbf149168fa25c78e54e32f0c723d6d"

SRC_URI += "https://storage.googleapis.com/download.tensorflow.org/models/tflite/mobilenet_v1_1.0_224_quant_and_labels.zip;name=model-mobv1"
SRC_URI[model-mobv1.md5sum] = "38ac0c626947875bd311ef96c8baab62"
SRC_URI[model-mobv1.sha256sum] = "2f8054076cf655e1a73778a49bd8fd0306d32b290b7e576dda9574f00f186c0f"

RDEPENDS:${PN} += " \
    python3 \
    python3-core \
    python3-numpy \
    python3-keras-applications \
    python3-keras-preprocessing \
    python3-protobuf \
    python3-grpcio \
    python3-absl \
    python3-astor \
    python3-astunparse \
    python3-gast \
    python3-termcolor \
    python3-wrapt \
    python3-opt-einsum \
    python3-google-pasta \
    python3-typing-extensions \
    flatbuffers-python3 \
    tensorboard \
    tensorflow-estimator \
"

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

    CT_NAME=$(echo ${HOST_PREFIX} | rev | cut -c 2- | rev)
    SED_COMMAND="s#%%CT_NAME%%#${CT_NAME}#g"
    SED_COMMAND="${SED_COMMAND}; s#%%WORKDIR%%#${WORKDIR}#g"
    SED_COMMAND="${SED_COMMAND}; s#%%YOCTO_COMPILER_PATH%%#${BAZEL_OUTPUTBASE_DIR}/external/yocto_compiler#g"

    sed -i "${SED_COMMAND}" ${S}/BUILD.yocto_compiler \
                            ${S}/WORKSPACE

    ${TF_CONFIG} \
    ./configure
}

TF_TARGET_EXTRA ??= ""
do_compile () {
    export CT_NAME=$(echo ${HOST_PREFIX} | rev | cut -c 2- | rev)
    unset CC
    ${BAZEL} build \
        ${TF_ARGS_EXTRA} \
        -c opt \
        --cpu=${BAZEL_TARGET_CPU} \
        --subcommands --explain=${T}/explain.log \
        --verbose_explanations --verbose_failures \
        --crosstool_top=@local_config_yocto_compiler//:toolchain \
        --host_crosstool_top=@bazel_tools//tools/cpp:toolchain \
        --verbose_failures \
        --copt -DTF_LITE_DISABLE_X86_NEON \
        //tensorflow:libtensorflow.so \
        //tensorflow:libtensorflow_cc.so \
        //tensorflow:libtensorflow_framework.so \
        //tensorflow/tools/benchmark:benchmark_model \
        //tensorflow/tools/pip_package:build_pip_package \
        tensorflow/examples/label_image/... \
        //tensorflow/lite/examples/label_image:label_image \
        ${TF_TARGET_EXTRA}
}

do_install() {
    install -d ${D}${libdir}
    install -m 644 ${S}/bazel-bin/tensorflow/libtensorflow.so \
        ${D}${libdir}
    install -m 644 ${S}/bazel-bin/tensorflow/libtensorflow_cc.so \
        ${D}${libdir}

    install -d ${D}${sbindir}
    install -m 755 ${S}/bazel-bin/tensorflow/tools/benchmark/benchmark_model \
        ${D}${sbindir}

    install -m 755 ${S}/bazel-bin/tensorflow/examples/label_image/label_image \
        ${D}${sbindir}

    install -m 755 ${S}/bazel-bin/tensorflow/lite/examples/label_image/label_image \
        ${D}${sbindir}/label_image.lite

    install -d ${D}${datadir}/label_image
    install -m 644 ${WORKDIR}/imagenet_slim_labels.txt ${D}${datadir}/label_image
    install -m 644 ${WORKDIR}/inception_v3_2016_08_28_frozen.pb \
        ${D}${datadir}/label_image
    install -m 644 ${S}/tensorflow/examples/label_image/data/grace_hopper.jpg \
        ${D}${datadir}/label_image

    install -m 644 ${WORKDIR}/labels_mobilenet_quant_v1_224.txt ${D}${datadir}/label_image
    install -m 644 ${WORKDIR}/mobilenet_v1_1.0_224_quant.tflite \
        ${D}${datadir}/label_image
    install -m 644 ${S}/tensorflow/lite/examples/label_image/testdata/grace_hopper.bmp \
        ${D}${datadir}/label_image


    export TMPDIR="${WORKDIR}"
    echo "Generating pip package"
    BDIST_OPTS="--universal" \
        ${S}/bazel-bin/tensorflow/tools/pip_package/build_pip_package ${WORKDIR}

    echo "Installing pip package"
    install -d ${D}/${PYTHON_SITEPACKAGES_DIR}
    ${STAGING_BINDIR_NATIVE}/pip3 install --disable-pip-version-check -v \
        -t ${D}/${PYTHON_SITEPACKAGES_DIR} --no-cache-dir --no-deps \
         ${WORKDIR}/tensorflow-${PV}-*.whl

    (
        cd ${D}${PYTHON_SITEPACKAGES_DIR}/bin;
        for app in `ls`; do
            sed -i "s:${STAGING_BINDIR_NATIVE}/nativepython3:/usr/bin/python3:g" $app
            mv $app ${D}${sbindir}
        done

        mv ${D}${PYTHON_SITEPACKAGES_DIR}/tensorflow/libtensorflow_framework.so*  ${D}${libdir}
    )
}

FILES:${PN}-dev = ""
INSANE_SKIP:${PN} += "dev-so \
                      already-stripped \
                     "

PACKAGE_BEFORE_PN += "libtensorflow-c libtensorflow-framework label-image label-image-lite python3-tensorflow"

RDEPENDS:label-image += "libtensorflow-framework"
RDEPENDS:python3-tensorflow += "libtensorflow-framework"
RDEPENDS:${PN} += "libtensorflow-c libtensorflow-framework label-image label-image-lite python3-tensorflow"

ALLOW_EMPTY:${PN} = "1"

FILES:python3-tensorflow += "${libdir}/* ${datadir}/* ${sbindir}/*"
FILES:libtensorflow-c = "${libdir}/libtensorflow.so ${libdir}/libtensorflow_cc.so"
FILES:libtensorflow-framework = "${libdir}/libtensorflow.so ${libdir}/libtensorflow_framework.so*"
FILES:label-image = "${sbindir}/label_image"
FILES:label-image-lite = "${sbindir}/label_image.lite"

inherit siteinfo unsupportarch
python __anonymous() {
    if d.getVar("SITEINFO_ENDIANNESS") == 'be':
        msg =  "\nIt failed to use pre-build model to do predict/inference on big-endian platform"
        msg += "\n(such as qemumips), since upstream does not support big-endian very well."
        msg += "\nDetails: https://github.com/tensorflow/tensorflow/issues/16364"
        bb.warn(msg)
}
