DESCRIPTION = "Bazel build and test tool"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3b83ef96387f14655fc854ddc3c6bd57"

SRC_URI[md5sum] = "cc255121586e849c4de2483a8a5814b6"
SRC_URI[sha256sum] = "c9244e5905df6b0190113e26082c72d58b56b1b0dec66d076f083ce4089b0307"

SRC_URI = "https://github.com/bazelbuild/bazel/releases/download/${PV}/bazel-${PV}-dist.zip \
           file://0001-HttpDownloader-save-download-tarball-to-distdir.patch \
           file://0001-fix-unzip-command-not-found.patch \
           file://0001-python3.patch \
           file://0001-add-Yocto-native-sysroot-dir-to-the-default-Bazel-to.patch \
"

inherit python3native

INHIBIT_SYSROOT_STRIP = "1"

CCACHE_DISABLE = "1"

DEPENDS = "coreutils-native \
           zip-native \
           openjdk-8-native \
          "

S="${WORKDIR}"

inherit bazel-base

EXTRA_BAZEL_ARGS = " \
    --host_javabase=@local_jdk//:jdk \
    --python_path=python3 \
    ${@oe.utils.conditional("BAZEL_JOBS", "", "", "--jobs=${BAZEL_JOBS}", d )} \
    ${@oe.utils.conditional("BAZEL_JOBS", "", "", "--local_cpu_resources=${BAZEL_JOBS}", d )} \
    ${@oe.utils.conditional("BAZEL_MEM", "", "", "--local_ram_resources=${BAZEL_MEM}", d )} \
"

do_compile () {
    TMPDIR="${TOPDIR}/bazel" \
    VERBOSE=yes \
    EXTRA_BAZEL_ARGS="${EXTRA_BAZEL_ARGS}" \
    ./compile.sh
}

do_install () {
    install -d ${D}${bindir}
    install -m 0755 ${S}/output/bazel ${D}${bindir}
}

# Explicitly disable uninative
UNINATIVE_LOADER = ""

inherit native
