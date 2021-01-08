DEPENDS += "bazel-native \
           openjdk-8-native \
          "
DEPENDS_append_class-target = " python3"

inherit bazel-base

BAZEL_DIR ?= "${WORKDIR}/bazel"
BAZEL_OUTPUTBASE_DIR ?= "${BAZEL_DIR}/output_base"
export BAZEL_ARGS="--output_user_root=${BAZEL_DIR}/user_root \
                   --output_base=${BAZEL_OUTPUTBASE_DIR} \
                   --bazelrc=${S}/bazelrc \
                   --batch  \
                  "

BAZEL ?= "${BAZEL_DIR}/bazel"

do_prepare_recipe_sysroot[postfuncs] += "do_install_bazel"
do_install_bazel() {
    mkdir -p ${BAZEL_DIR}
    install -m 0755 ${STAGING_BINDIR_NATIVE}/bazel ${BAZEL_DIR}
    create_cmdline_wrapper ${BAZEL} \$BAZEL_ARGS
    zip -A ${BAZEL}.real
}

def bazel_get_target_flags(d):
    flags = ""
    for i in d.getVar("CC").split()[1:]:
        flags += "# From CC\n"
        flags += "build --conlyopt=%s --cxxopt=%s --linkopt=%s\n" % (i, i, i)

    for i in d.getVar("CFLAGS").split():
        if i == "-g":
            continue
        flags += "# From CFLAGS\n"
        flags += "build --conlyopt=%s\n" % i

    for i in d.getVar("BUILD_CFLAGS").split():
        flags += "# From BUILD_CFLAGS\n"
        flags += "build --host_conlyopt=%s\n" % i

    for i in d.getVar("CXXFLAGS").split():
        if i == "-g":
            continue
        flags += "# From CXXFLAGS\n"
        flags += "build --cxxopt=%s\n" % i

    for i in d.getVar("BUILD_CXXFLAGS").split():
        flags += "# From BUILD_CXXFLAGS\n"
        flags += "build --host_cxxopt=%s\n" % i

    for i in d.getVar("CPPFLAGS").split():
        if i == "-g":
            continue
        flags += "# From CPPFLAGS\n"
        flags += "build --conlyopt=%s --cxxopt=%s\n" % (i, i)

    for i in d.getVar("BUILD_CPPFLAGS").split():
        flags += "# From BUILD_CPPFLAGS\n"
        flags += "build --host_conlyopt=%s --host_cxxopt=%s\n" % (i, i)

    for i in d.getVar("LDFLAGS").split():
        if i == "-Wl,--as-needed":
            continue
        flags += "# From LDFLAGS\n"
        flags += "build --linkopt=%s\n" % i

    for i in d.getVar("BUILD_LDFLAGS").split():
        if i == "-Wl,--as-needed":
            continue
        flags += "# From BUILD_LDFLAGS\n"
        flags += "build --host_linkopt=%s\n" % i

    for i in d.getVar("TOOLCHAIN_OPTIONS").split():
        if i == "-Wl,--as-needed":
            continue
        flags += "# From TOOLCHAIN_OPTIONS\n"
        flags += "build --linkopt=%s\n" % i

    return flags

def bazel_get_flags(d):
    flags = ""

    if d.getVar("BAZEL_JOBS"):
        flags += "# From BAZEL_JOBS\n"
        flags += "build --jobs=%s --local_cpu_resources=%s\n" % (d.getVar("BAZEL_JOBS"), d.getVar("BAZEL_JOBS"))

    if d.getVar("BAZEL_MEM"):
        flags += "# From BAZEL_MEM\n"
        flags += "build --local_ram_resources=%s\n" % (d.getVar("BAZEL_MEM"))

    return flags

bazel_do_configure () {
    cat > "${S}/bazelrc" <<-EOF
build --verbose_failures
build --spawn_strategy=standalone --genrule_strategy=standalone
test --verbose_failures --verbose_test_summary
test --spawn_strategy=standalone --genrule_strategy=standalone

build --linkopt=-Wl,--no-as-needed
build --host_linkopt=-Wl,--no-as-needed

build --host_conlyopt=-D_PYTHON_INCLUDE_NATIVE --host_cxxopt=-D_PYTHON_INCLUDE_NATIVE
build --conlyopt=-D_PYTHON_INCLUDE_TARGET --cxxopt=-D_PYTHON_INCLUDE_TARGET

build --strip=never

build --python_path=python3

fetch --distdir=${TS_DL_DIR}
build --distdir=${TS_DL_DIR}

${@bazel_get_flags(d)}

EOF

}

bazel_do_configure_append_class-target () {
    cat >> "${S}/bazelrc" <<-EOF
# FLAGS begin
${@bazel_get_target_flags(d)}
# FLAGS end

build --linkopt=-Wl,-latomic

EOF

    sed -i "s:${WORKDIR}:${BAZEL_OUTPUTBASE_DIR}/external/yocto_compiler:g" ${S}/bazelrc
}

EXPORT_FUNCTIONS do_configure

PSEUDO_IGNORE_PATHS .= ",${WORKDIR}/bazel"

inherit unsupportarch

export YOCTO_NATIVE_SYSROOT="${BAZEL_OUTPUTBASE_DIR}/external/yocto_compiler/recipe-sysroot-native"

do_rm_work[prefuncs] += "clean_bazel"
do_clean[prefuncs] += "clean_bazel"
clean_bazel() {
    if [ -d ${S} ]; then
        cd ${S}
        if [ -e ${BAZEL} ] && [ -e ${S}/bazelrc ]; then
            ${BAZEL} clean
        fi
    fi
    rm ${BAZEL_DIR} -rf
}
