DESCRIPTION = "Bazel build and test tool"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3b83ef96387f14655fc854ddc3c6bd57"

SRC_URI[md5sum] = "381ca27503c566ce5e489d1ba07d1d25"
SRC_URI[sha256sum] = "d7f40d0cac95a06cea6cb5b7f7769085257caebc3ee84269dd9298da760d5615"

SRC_URI = "https://github.com/bazelbuild/bazel/releases/download/${PV}/bazel-${PV}-dist.zip \
           file://0001-HttpDownloader-save-download-tarball-to-distdir.patch \
           file://0001-fix-unzip-command-not-found.patch \
           file://0001-python3.patch \
           file://0001-add-Yocto-native-sysroot-dir-to-the-default-Bazel-to.patch \
"

inherit native python3native

INHIBIT_SYSROOT_STRIP = "1"

CCACHE_DISABLE = "1"

DEPENDS = "coreutils-native \
           zip-native \
           openjdk-8-native \
          "

S="${WORKDIR}"

TS_DL_DIR ??= "${DL_DIR}"

BAZEL_JOBS ??= "4"
EXTRA_BAZEL_ARGS = " \
    --host_javabase=@local_jdk//:jdk \
    --python_path=python3 \
    --jobs=${BAZEL_JOBS} \
    --local_ram_resources=4096 \
    --local_cpu_resources=${BAZEL_JOBS} \
"

do_compile () {
    export JAVA_HOME="${STAGING_LIBDIR_NATIVE}/jvm/openjdk-8-native"
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
