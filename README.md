# meta-mediapipe

## Introduction
This is a hackey copy-paste-smash-together of the meta-tensorflow layer at http://git.yoctoproject.org/cgit/cgit.cgi/meta-tensorflow
to _instead_ support building Mediapipe. Tensorflow(-lite at least) is a dependency of Mediapipe but I have made no
effort to re-use the tensorflow built by yocto in the mediapipe build.

The `tensorflow` recipes probably don't even work right now in this layer.

## TODO
* Test and implement building the mediapipe python library
* Achieve compatiblity with the meta-tensorflow layer
** Test and fix the existing tensorflow build along-side the mediapipe build.
** Re-use the tensorflow parts built by yocto in the mediapipe build
** Merge meta-tensorflow and meta-mediapipe
* Use bazel to accumulate more of the library
** Use Bazel to gather only the needed header files
** Use Bazel to gather only the needed tflite files
* Support a more configurable build for the c++ library
** Use bitbake features to enable/disable certain calculators and modules

## Overview
Mediapipe is a Media-processing (mainly for ML inference) pipelineing library.

Enabling easy use of hardware acceleration, protobufs, and tflite for feature/object detection and pose inference.

See: https://github.com/google/mediapipe https://github.com/tensorflow

This repository is build configuration for cross-compiling mediapipe using the Open-Embedded/Yocto ecosystem.

See: https://www.yoctoproject.org/ https://github.com/siemens/kas

## Usage
I'm not going to go into great detail here but the kas config I used to build a mediapipe c++ library for RPi 4 was:

```yaml
header:
  version: 10

machine: raspberrypi4-64

distro: our-own-poky-based-distro

repos:
  poky:
    url: https://git.yoctoproject.org/git/poky
    # refspec: master
    refspec: df2a1f37f77d19e62a5fbdb462a9b24e104f0448
    layers:
      meta:
      meta-poky:
      meta-yocto-bsp:
  raspberrypi:
    url: git://git.yoctoproject.org/meta-raspberrypi
    # refspec: master
    refspec: 0e48785e15d864438cade6e526056dbc92e7a690
  private-system-layer:
    # our own layer with OS configuration and networking bits
  private-app-layer:
    # our own software which made use of the c++ library
  oe:
    url: https://github.com/openembedded/meta-openembedded
    # refspec: master
    refspec: e418ee4657e084c8b4d42aabf76ff6df99253e91
    layers:
      meta-oe:
      meta-python:
      meta-multimedia:
      meta-networking:
  tensorflow:
    url: THIS REPOSITORY
    refspec: master

local_conf_header:
  meta-custom: |
    PACKAGE_CLASSES ?= "package_deb"
    PREFERRED_VERSION_protobuf = "3.12.3"
    PREFERRED_VERSION_protobuf-native = "3.12.3"
    LICENSE_FLAGS_WHITELIST += "commercial"
    ENABLE_DWC2_HOST = "1"
    PRSERV_HOST = "localhost:0"
    BB_NUMBER_THREADS = "4"

target:
  # Our own system image targets from `private-system-layer`
```