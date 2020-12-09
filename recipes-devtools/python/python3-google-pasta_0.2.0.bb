SUMMARY = "The AST-based Python refactoring library"
HOMEPAGE = "https://github.com/google/pasta"
SECTION = "devel/python"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://PKG-INFO;md5=a10102394a800f3fa4a3df0934d57bb1"

SRC_URI[md5sum] = "7c218a4a0d84303b9319352040fbfce6"
SRC_URI[sha256sum] = "c9f2c8dfc8f96d0d5808299920721be30c9eec37f2389f28904f454565c8a16e"

inherit pypi setuptools3

BBCLASSEXTEND = "native"
