# Limitation
```
* Target arch only supports 32 bit arm and 32 bit x86, 64 bit arm and
  64 bit x86. BSP (MACHINE) incluced in above archs should be supported.

* If host(build system) is not x86_64, please add meta-java to BBLAYERS in
  conf/bblayers.conf (git://git.yoctoproject.org/meta-java)

* Mediapipe is so far only tested as a c++ library on RPi 4 Aarch64 using the GPU.

* The only dependencies we can share with the Yocto/OE build system is java (only
  used for building bazel), Mesa (egl/opengl), OpenCV and FFMPEG, bazel wants to
  build everything else itself right now.
```
