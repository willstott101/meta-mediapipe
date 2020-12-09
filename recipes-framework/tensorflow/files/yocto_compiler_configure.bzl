# -*- Python -*-
"""Yocto rule for yocto compiler autoconfiguration."""

def _tpl(repository_ctx, tpl, substitutions={}, out=None):
  if not out:
    out = tpl
  repository_ctx.template(
      out,
      Label("//third_party/toolchains/yocto:%s.tpl" % tpl),
      substitutions)

def _yocto_compiler_configure_impl(repository_ctx):
    # We need to find a cross-compilation include directory for Python, so look
    # for an environment variable. Be warned, this crosstool template is only
    # regenerated on the first run of Bazel, so if you change the variable after
    # it may not be reflected in later builds. Doing a shutdown and clean of Bazel
    # doesn't fix this, you'll need to delete the generated file at something like:
    # external/local_config_arm_compiler/CROSSTOOL in your Bazel install.
    if "CROSSTOOL_PYTHON_INCLUDE_PATH" in repository_ctx.os.environ:
        python_include_path = repository_ctx.os.environ["CROSSTOOL_PYTHON_INCLUDE_PATH"]
    else:
        python_include_path = "/buildarea/raid5/hjia/wrlinux-20/build_master-wr_ts_intel_2020120722/build/tmp-glibc/work/corei7-64-wrs-linux/tensorflow/2.4.0-r0/recipe-sysroot/usr/include/python3.9"

    if "CT_NAME" in repository_ctx.os.environ:
        cross_tool_name = repository_ctx.os.environ["CT_NAME"]
    else:
        cross_tool_name = "x86_64-wrs-linux"

    _tpl(repository_ctx, "cc_config.bzl", {
        "%{YOCTO_COMPILER_PATH}%": str(repository_ctx.path(
            repository_ctx.attr.remote_config_repo,
        )),
        "%{PYTHON_INCLUDE_PATH}%": python_include_path,
        "%{CT_NAME}%": cross_tool_name,
    })
    repository_ctx.symlink(repository_ctx.attr.build_file, "BUILD")

yocto_compiler_configure = repository_rule(
    implementation = _yocto_compiler_configure_impl,
    attrs = {
        "remote_config_repo": attr.string(mandatory = False, default =""),
        "build_file": attr.label(),
    },
)
