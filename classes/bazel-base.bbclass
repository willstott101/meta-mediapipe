export JAVA_HOME="${STAGING_LIBDIR_NATIVE}/jvm/openjdk-8-native"

BAZEL_JOBS ??= "4"

# Memory 4GB
BAZEL_MEM ??= "4096"

TS_DL_DIR ??= "${DL_DIR}"

CCACHE_DISABLE = "1"

