SUMMARY = "Google Authentication Library"
HOMEPAGE = "https://github.com/googleapis/google-auth-library-python"
DESCRIPTION = "This library simplifies using Google’s various \
server-to-server authentication mechanisms to access Google APIs."
SECTION = "devel/python"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=86d3f3a95c324c9479bd8986968f4327"

inherit pypi

SRC_URI[md5sum] = "d1f8a9284283b8db69e84c41b36db501"
SRC_URI[sha256sum] = "0b0e026b412a0ad096e753907559e4bdb180d9ba9f68dd9036164db4fdc4ad2e"

RDEPENDS_${PN} += " \
    python3-cachetools \
    python3-rsa \
    python3-pyasn1-modules \
"

BBCLASSEXTEND = "native"
