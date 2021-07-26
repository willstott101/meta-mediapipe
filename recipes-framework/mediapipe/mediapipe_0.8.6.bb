DESCRIPTION = "Mediapipe C++ Library"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=86d3f3a95c324c9479bd8986968f4327"

DEPENDS = "bazel-native protobuf-native util-linux-native protobuf"
SRCREV = "374f5e2e7e818bde5289fb3cffa616705cec6f73"
SRC_URI = "git://github.com/google/mediapipe.git;branch=master \
           file://BUILD.in \
           file://BUILD.yocto_compiler \
           file://cc_config.bzl.tpl \
           file://yocto_compiler_configure.bzl \
           "

S = "${WORKDIR}/git"

inherit python3native bazel

do_configure_append () {
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
    ${BAZEL} build \
        ${MP_ARGS_EXTRA} \
        -c opt \
        --cpu=${BAZEL_TARGET_CPU} \
        --subcommands --explain=${T}/explain.log \
        --verbose_explanations --verbose_failures \
        --crosstool_top=@local_config_yocto_compiler//:toolchain \
        --host_crosstool_top=@bazel_tools//tools/cpp:toolchain \
        --verbose_failures \
        --copt -DMESA_EGL_NO_X11_HEADERS \
        --copt -DEGL_NO_X11 \
        mediapipe/examples/desktop/hello_world:hello_world
        ${MP_TARGET_EXTRA}
}


# do_compile_append() {
#     chmod a+w ${BAZEL_DIR}/output_base/execroot/org_tensorflow/bazel-out/*/bin/tensorflow/lite/python/schema_py_srcs_no_include_all
#     chmod a+w ${BAZEL_DIR}/output_base/execroot/org_tensorflow/bazel-out/*/bin/tensorflow/lite/python/schema_py_srcs_no_include_all/tflite
# }
