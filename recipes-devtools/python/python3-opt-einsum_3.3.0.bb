SUMMARY = "A tensor contraction order optimizer"
HOMEPAGE = "https://github.com/dgasmith/opt_einsum"
SECTION = "devel/python"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=5ab423c88cf3e69553decf93419f53ac"

SRC_URI[md5sum] = "acf0a3997aab84b4e9a854296cc34971"
SRC_URI[sha256sum] = "59f6475f77bbc37dcf7cd748519c0ec60722e91e63ca114e68821c0c54a46549"

inherit pypi setuptools3

SRCNAME = "opt_einsum"
PYPI_SRC_URI = "https://files.pythonhosted.org/packages/source/o/${PYPI_PACKAGE}/${SRCNAME}-${PV}.tar.gz"
S = "${WORKDIR}/${SRCNAME}-${PV}"

BBCLASSEXTEND = "native"
