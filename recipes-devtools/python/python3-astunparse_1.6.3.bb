SUMMARY = "An AST unparser for Python"
HOMEPAGE = "https://github.com/simonpercivall/astunparse"
SECTION = "devel/python"
LICENSE = "BSD"
LIC_FILES_CHKSUM = "file://LICENSE;md5=7a7c771110c28a37480b73d07ad6d2a1"

SRC_URI[md5sum] = "2cea4d8e49beba7684bac890e73d6a40"
SRC_URI[sha256sum] = "5ad93a8456f0d084c3456d059fd9a92cce667963232cbf763eac3bc5b7940872"

inherit pypi setuptools3

BBCLASSEXTEND = "native"
