include tensorflow.inc

inherit native

export PYTHON_BIN_PATH="${PYTHON}"
export PYTHON_LIB_PATH="${PYTHON_SITEPACKAGES_DIR}"

do_configure_append () {
    ${TF_CONFIG} \
    ./configure
}

do_compile () {
    unset CC
    ${BAZEL} build \
        ${TF_ARGS_EXTRA} \
        -c opt \
        --subcommands --explain=${T}/explain.log \
        --verbose_explanations --verbose_failures \
        --verbose_failures \
        //tensorflow/tools/pip_package:build_pip_package
}

do_install() {
    export TMPDIR="${WORKDIR}"
    echo "Generating pip package"
    BDIST_OPTS="--universal" \
        ${S}/bazel-bin/tensorflow/tools/pip_package/build_pip_package ${WORKDIR}

    echo "Installing pip package"
    install -d ${D}/${PYTHON_SITEPACKAGES_DIR}
    ${STAGING_BINDIR_NATIVE}/pip3 install --disable-pip-version-check -v --no-deps \
        -t ${D}/${PYTHON_SITEPACKAGES_DIR} --no-cache-dir ${WORKDIR}/tensorflow-${PV}-*.whl

    install -d ${D}${sbindir}
    (
        cd ${D}${PYTHON_SITEPACKAGES_DIR}/bin;
        for app in `ls`; do
            mv $app ${D}${sbindir}
        done

    )

}
