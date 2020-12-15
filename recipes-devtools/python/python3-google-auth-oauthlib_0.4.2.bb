SUMMARY = "The oauthlib integration for Google Auth"
HOMEPAGE = "https://github.com/googleapis/google-auth-library-python-oauthlib"
DESCRIPTION = "This library provides oauthlib integration with google-auth."
SECTION = "devel/python"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=7b7e367c7e1664e6565ee7d16cccc58a"

inherit pypi

SRC_URI[md5sum] = "5d3cb6250dd05fff0b7bc0912a3e8f0a"
SRC_URI[sha256sum] = "65b65bc39ad8cab15039b35e5898455d3d66296d0584d96fe0e79d67d04c51d9"

BBCLASSEXTEND = "native"

RDEPENDS_${PN} += " \
    python3-requests-oauthlib \
    python3-oauthlib \
"
inherit setuptools3
