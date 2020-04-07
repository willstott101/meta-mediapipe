DESCRIPTION = "TensorFlow C/C++ Libraries"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=01e86893010a1b87e69a213faa753ebd"

DEPENDS = "bazel-native protobuf-native util-linux-native protobuf"
SRCREV = "c8875cbb1341f6ca14dd0ec908f1dde7d67f7808"
SRC_URI = "git://github.com/tensorflow/tensorflow.git;branch=r1.13 \
           file://0001-SyntaxError-around-async-keyword-on-Python-3.7.patch \
           file://0001-support-musl.patch \
           file://0001-use-local-bazel-to-workaround-bazel-paralle-issue.patch \
           file://0001-fix-compilation-error.patch \
           file://0001-Fix-TensorFlow-on-Python-3.8-logger-issue.patch \
           file://0001-Rename-gettid-functions.patch \
           file://0001-third_party-eigen_archive-workaround-ice-failure-whi.patch \
          "

S = "${WORKDIR}/git"

DEPENDS += " \
    python3 \
    python3-numpy-native \
    python3-keras-applications-native \
    python3-keras-preprocessing-native \
    python3-pip-native \
    python3-wheel-native \
"

inherit python3native bazel

TF_CONFIG ?= " \
    TF_NEED_CUDA=0 \
    TF_NEED_OPENCL_SYCL=0 \
    TF_NEED_OPENCL=0 \
    TF_CUDA_CLANG=0 \
    TF_DOWNLOAD_CLANG=0 \
    TF_ENABLE_XLA=0 \
    TF_NEED_MPI=0 \
    TF_SET_ANDROID_WORKSPACE=0 \
"
