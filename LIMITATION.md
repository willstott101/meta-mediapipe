# Limitation
```
* Target arch only supports 32 bit arm and 32 bit x86, 64 bit arm and
  64 bit x86. BSP (MACHINE) incluced in above archs should be supported.

* Bazel build takes lots of time, it has own rules and builds everything
  from scratch. Currently bazel could not reuse Yocto DEPENDS/RDEPENDS.

* In order to run tensorflow cases in a reasonable time, although it builds
  successfully on qemuarm, qemuarm64, qemux86 and qemux86-64, only qemux86-64
  with kvm for runtime test.

* It failed to use pre-build model to do predict/inference on big-endian
  platform, since upstream does not support big-endian very well
  https://github.com/tensorflow/tensorflow/issues/16364

* If host(build system) is not x86_64, please add meta-java to BBLAYERS in
  conf/bblayers.conf (git://git.yoctoproject.org/meta-java)
```
