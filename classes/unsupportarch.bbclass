UNSUPPORTED_TARGET_ARCH ??= "powerpc"
python __anonymous() {
    target_arch = d.getVar("TARGET_ARCH")
    if target_arch in d.getVar("UNSUPPORTED_TARGET_ARCH").split():
        raise bb.parse.SkipPackage("TensorFlow does not support Target Arch '%s'" % target_arch)
}
