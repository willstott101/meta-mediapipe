DESCRIPTION = "Java runtime based upon the OpenJDK Project, the community \
builds using source code from OpenJDK project"
LICENSE = "GPL-2.0-with-classpath-exception"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3e0b59f8fac05c3c03d4a26bbda13f8f"

SRC_URI[md5sum] = "88a336086a8f2442022ebbe7aa4beacf"
SRC_URI[sha256sum] = "36f561577928027c04c93db73ed03bda89416e4a0f681f7757adab89280ead08"
SRC_URI = " \
    https://github.com/ojdkbuild/contrib_jdk8u-ci/releases/download/jdk8u292-b10/jdk-8u292-ojdkbuild-linux-x64.zip \
"

S = "${WORKDIR}/jdk-8u292-ojdkbuild-linux-x64"

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
