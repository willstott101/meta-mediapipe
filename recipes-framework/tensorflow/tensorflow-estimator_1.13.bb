DESCRIPTION = "A high-level TensorFlow API that greatly simplifies machine \
learning programming."
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=01e86893010a1b87e69a213faa753ebd"

SRC_URI = "git://github.com/tensorflow/estimator.git;branch=r2.4 \
           file://0001-customize-for-yocto.patch \
          "
SRCREV = "c3e7f2b5bbcc35185ef71797955a28cadce28f60"
S = "${WORKDIR}/git"

inherit python3native bazel

DEPENDS += " \
    python3-pip-native \
    python3-wheel-native \
    python3-six-native \
    python3-protobuf-native \
    python3-absl-native \
    python3-astor-native \
    python3-gast-native \
    python3-termcolor-native \
    python3-wrapt-native \
    python3-opt-einsum-native \
    python3-astunparse-native \
    flatbuffers-native \
    tensorflow-native \
"

do_compile () {
    unset CC
    export TMPDIR="${WORKDIR}"
    export PYTHON_BIN_PATH="${PYTHON}"

    ${BAZEL} build \
        --subcommands --explain=${T}/explain.log \
        --verbose_explanations --verbose_failures \
        --verbose_failures \
        --python_path="${PYTHON}" \
        //tensorflow_estimator/tools/pip_package:build_pip_package

    ${S}/bazel-bin/tensorflow_estimator/tools/pip_package/build_pip_package \
        ${WORKDIR}/estimator_pip
}

do_install () {
    echo "Installing pip package"
    install -d ${D}${PYTHON_SITEPACKAGES_DIR}
    ${STAGING_BINDIR_NATIVE}/pip3 install --disable-pip-version-check -v --no-deps \
        -t ${D}/${PYTHON_SITEPACKAGES_DIR} --no-cache-dir ${WORKDIR}/estimator_pip/*.whl

}

FILES_${PN} += "${libdir}/*"

BBCLASSEXTEND = "native"
