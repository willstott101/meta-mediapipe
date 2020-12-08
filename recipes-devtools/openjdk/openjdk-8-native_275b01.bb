DESCRIPTION = "Java runtime based upon the OpenJDK Project, the community \
builds using source code from OpenJDK project"
LICENSE = "GPL-2.0-with-classpath-exception"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3e0b59f8fac05c3c03d4a26bbda13f8f"

SRC_URI[md5sum] = "52c1f769ab67b58b4300713fb5d46a47"
SRC_URI[sha256sum] = "fccaa6cc14571813dbb427ac08d5acd034782a2654e6090ad4d63e7200011ac4"
SRC_URI = " \
    https://github.com/ojdkbuild/contrib_jdk8u-ci/releases/download/jdk8u275-b01/jdk-8u275-ojdkbuild-linux-x64.zip \
"

S = "${WORKDIR}/jdk-8u275-ojdkbuild-linux-x64"

do_patch[noexec] = "1"
do_configure[noexec] = "1"
do_compile[noexec] = "1"

do_install () {
	install -d ${D}${libdir}/jvm/openjdk-8-native
	cp -rf ${S}/* ${D}${libdir}/jvm/openjdk-8-native
}

inherit native
INHIBIT_SYSROOT_STRIP = "1"

python __anonymous() {
    if d.getVar("BUILD_ARCH") != "x86_64":
        msg =  "\nThe pre-build openjdk-8-native does not support %s host," % d.getVar("BUILD_ARCH")
        msg += "\nplease use the one in meta-java to replace,"
        msg += "\nadd meta-java to BBLAYERS in conf/bblayers.conf"
        raise bb.parse.SkipPackage(msg)
}
