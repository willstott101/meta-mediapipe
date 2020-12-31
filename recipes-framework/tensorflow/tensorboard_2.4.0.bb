DESCRIPTION = "A suite of web applications for inspecting and understanding \
your TensorFlow runs and graphs."
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://tensorboard-2.4.0.dist-info/LICENSE;md5=6d39b719635a13cfc32655534903ca63"

SRC_URI = " \
    https://files.pythonhosted.org/packages/02/83/179c8f76e5716030cc3ee9433721161cfcc1d854e9ba20c9205180bb100a/tensorboard-2.4.0-py3-none-any.whl \
"

do_unpack[depends] += "python3-pip-native:do_populate_sysroot"

SRC_URI[md5sum] = "26f82806bfe1f354233de9ef29e36fe0"
SRC_URI[sha256sum] = "cde0c663a85609441cb4d624e7255fd8e2b6b1d679645095aac8a234a2812738"

RDEPENDS_${PN} += "python3 \
           python3-core \
           python3-numpy \
           python3-protobuf \
           python3-grpcio \
           python3-werkzeug \
           python3-six \
           python3-markdown \
           python3-absl \
           python3-google-auth \
           python3-google-auth-oauthlib \
           python3-requests \
"

inherit python3native

do_unpack () {
    echo "Installing pip package"
    ${STAGING_BINDIR_NATIVE}/pip3 install --disable-pip-version-check -v \
        -t ${S} --no-cache-dir --no-deps \
         ${DL_DIR}/tensorboard-2.4.0-py3-none-any.whl
}

do_install () {
    install -d ${D}${PYTHON_SITEPACKAGES_DIR}
    cp -rf ${S}/* ${D}${PYTHON_SITEPACKAGES_DIR}/
    rm ${D}/${PYTHON_SITEPACKAGES_DIR}/bin ${D}/${PYTHON_SITEPACKAGES_DIR}/tensorboard-2.4.0.dist-info  -rf
    rm ${D}/${PYTHON_SITEPACKAGES_DIR}/bin -rf
}

do_configure[noexec] = "1"
do_compile[noexec] = "1"

FILES_${PN} += "${libdir}/*"
