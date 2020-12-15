SUMMARY = "ASN.1 modules for Python"
HOMEPAGE = "https://github.com/etingof/pyasn1-modules"
DESCRIPTION = "The pyasn1-modules package contains a collection of \
ASN.1 data structures expressed as Python classes based on \
pyasn1 data model."
SECTION = "devel/python"

LICENSE = "BSD"
LIC_FILES_CHKSUM = "file://LICENSE.txt;md5=a14482d15c2249de3b6f0e8a47e021fd"

inherit pypi

SRC_URI[md5sum] = ""
SRC_URI[sha256sum] = ""

RDEPENDS_${PN} += " \
    python3-pyasn1 \
"

BBCLASSEXTEND = "native"

inherit setuptools3
