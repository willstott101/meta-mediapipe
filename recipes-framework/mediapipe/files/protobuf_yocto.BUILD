# Bazel (https://bazel.build/) BUILD file for Protobuf modified for use within a Yocto layer
    # copied from Protobuf 3.11.4
    # Updated libprotobuf(-lite) to pull from a patched-in pre-built .so
    # Updated libprotoc to pull from a patched-in pre-built .so
    # Changed protoc - probably broken

load("@rules_cc//cc:defs.bzl", "cc_binary", "cc_library", "cc_test", "objc_library", native_cc_proto_library = "cc_proto_library")
load("@rules_java//java:defs.bzl", "java_library")
load("@rules_proto//proto:defs.bzl", "proto_lang_toolchain", "proto_library")
load("@rules_proto//proto/private:native.bzl", "native_proto_common")
load("@rules_python//python:defs.bzl", "py_library")
load(":cc_proto_blacklist_test.bzl", "cc_proto_blacklist_test")

licenses(["notice"])

exports_files(["LICENSE"])

################################################################################
# Java 9 configuration
################################################################################

config_setting(
    name = "jdk9",
    values = {
        "java_toolchain": "@bazel_tools//tools/jdk:toolchain_jdk9",
    },
)

################################################################################
# Protobuf Runtime Library
################################################################################

COPTS = select({
    "//conditions:default": [
        "-DHAVE_PTHREAD",
        "-DHAVE_ZLIB",
        "-Woverloaded-virtual",
        "-Wno-sign-compare",
        "-Wno-unused-function",
        # Prevents ISO C++ const string assignment warnings for pyext sources.
        "-Wno-write-strings",
        "-Wno-deprecated-declarations",
    ],
})

load(":compiler_config_setting.bzl", "create_compiler_config_setting")

create_compiler_config_setting(
    name = "msvc",
    value = "msvc-cl",
)

config_setting(
    name = "android",
    values = {
        "crosstool_top": "//external:android/crosstool",
    },
)

config_setting(
    name = "android-libcpp",
    values = {
        "crosstool_top": "@androidndk//:toolchain-libcpp",
    },
)

config_setting(
    name = "android-gnu-libstdcpp",
    values = {
        "crosstool_top": "@androidndk//:toolchain-gnu-libstdcpp",
    },
)

# Android and MSVC builds do not need to link in a separate pthread library.
LINK_OPTS = select({
    ":android": [],
    ":android-libcpp": [],
    ":android-gnu-libstdcpp": [],
    ":msvc": [
        # Suppress linker warnings about files with no symbols defined.
        "-ignore:4221",
    ],
    "//conditions:default": [
        "-lpthread",
        "-lm",
    ],
})

load(
    ":protobuf.bzl",
    "cc_proto_library",
    "internal_copied_filegroup",
    "internal_gen_well_known_protos_java",
    "internal_protobuf_py_tests",
    "py_proto_library",
)

config_setting(
    name = "yocto_armeabi",
    values = {
        "cpu": "armeabi",
    },
    visibility = ["//visibility:public"],
)

config_setting(
    name = "yocto_armv8",
    values = {
        "cpu": "armv8",
    },
    visibility = ["//visibility:public"],
)

config_setting(
    name = "yocto_aarch64",
    values = {
        "cpu": "aarch64",
    },
    visibility = ["//visibility:public"],
)

cc_library(
    name = "protobuf_lite",
    srcs = select({
        "//conditions:default": ["yocto/native/libprotobuf-lite.so"],
        ":yocto_armeabi": ["yocto/libprotobuf-lite.so"],
        ":yocto_armv8": ["yocto/libprotobuf-lite.so"],
        ":yocto_aarch64": ["yocto/libprotobuf-lite.so"],
    }),
    hdrs = glob([
        "yocto/include/google/protobuf/*.h",
        "yocto/include/google/protobuf/*/*.h",
        "yocto/include/google/protobuf/*/*/*.h",
    ]),
    includes = ["include"],
    linkopts = [
        "-lprotobuf-lite",
    ],
    linkstatic = 1,
    visibility = ["//visibility:public"],
)

cc_library(
    name = "protobuf",
    srcs = select({
        "//conditions:default": ["yocto/native/libprotobuf.so"],
        ":yocto_armeabi": ["yocto/libprotobuf.so"],
        ":yocto_armv8": ["yocto/libprotobuf.so"],
        ":yocto_aarch64": ["yocto/libprotobuf.so"],
    }),
    hdrs = glob([
        "yocto/include/google/protobuf/*.h",
        "yocto/include/google/protobuf/*/*.h",
        "yocto/include/google/protobuf/*/*/*.h",
    ]),
    includes = ["include"],
    linkopts = [
        "-lprotobuf",
    ],
    linkstatic = 1,
    visibility = ["//visibility:public"],
)

# This provides just the header files for use in projects that need to build
# shared libraries for dynamic loading. This target is available until Bazel
# adds native support for such use cases.
# TODO(keveman): Remove this target once the support gets added to Bazel.
cc_library(
    name = "protobuf_headers",
    hdrs = glob([
        "yocto/include/google/protobuf/*.h",
        "yocto/include/google/protobuf/*/*.h",
        "yocto/include/google/protobuf/*/*/*.h",
        "yocto/include/google/protobuf/*.inc",
        "yocto/include/google/protobuf/*/*.inc",
        "yocto/include/google/protobuf/*/*/*.inc",
    ]),
    includes = ["src/"],
    visibility = ["//visibility:public"],
)

# Map of all well known protos.
# name => (include path, imports)
WELL_KNOWN_PROTO_MAP = {
    "any": ("src/google/protobuf/any.proto", []),
    "api": (
        "src/google/protobuf/api.proto",
        [
            "source_context",
            "type",
        ],
    ),
    "compiler_plugin": (
        "src/google/protobuf/compiler/plugin.proto",
        ["descriptor"],
    ),
    "descriptor": ("src/google/protobuf/descriptor.proto", []),
    "duration": ("src/google/protobuf/duration.proto", []),
    "empty": ("src/google/protobuf/empty.proto", []),
    "field_mask": ("src/google/protobuf/field_mask.proto", []),
    "source_context": ("src/google/protobuf/source_context.proto", []),
    "struct": ("src/google/protobuf/struct.proto", []),
    "timestamp": ("src/google/protobuf/timestamp.proto", []),
    "type": (
        "src/google/protobuf/type.proto",
        [
            "any",
            "source_context",
        ],
    ),
    "wrappers": ("src/google/protobuf/wrappers.proto", []),
}

WELL_KNOWN_PROTOS = [value[0] for value in WELL_KNOWN_PROTO_MAP.values()]

filegroup(
    name = "well_known_protos",
    srcs = WELL_KNOWN_PROTOS,
    visibility = ["//visibility:public"],
)

cc_proto_library(
    name = "cc_wkt_protos",
    srcs = WELL_KNOWN_PROTOS,
    include = "src",
    default_runtime = ":protobuf",
    internal_bootstrap_hack = 1,
    protoc = ":protoc",
    visibility = ["//visibility:public"],
)

################################################################################
# Well Known Types Proto Library Rules
#
# These proto_library rules can be used with one of the language specific proto
# library rules i.e. java_proto_library:
#
# java_proto_library(
#   name = "any_java_proto",
#   deps = ["@com_google_protobuf//:any_proto],
# )
################################################################################

[proto_library(
    name = proto[0] + "_proto",
    srcs = [proto[1][0]],
    strip_import_prefix = "src",
    visibility = ["//visibility:public"],
    deps = [dep + "_proto" for dep in proto[1][1]],
) for proto in WELL_KNOWN_PROTO_MAP.items()]

[native_cc_proto_library(
    name = proto + "_cc_proto",
    deps = [proto + "_proto"],
    visibility = ["//visibility:private"],
) for proto in WELL_KNOWN_PROTO_MAP.keys()]

cc_proto_blacklist_test(
    name = "cc_proto_blacklist_test",
    deps = [proto + "_cc_proto" for proto in WELL_KNOWN_PROTO_MAP.keys()]
)

################################################################################
# Protocol Buffers Compiler
################################################################################

cc_library(
    name = "protoc_lib",
    srcs = [
        # IDEA: Use select to seperate .so for native and target
        "yocto/libprotoc.so",
    ],
    hdrs = glob([
        "yocto/include/google/protobuf/*.h",
        "yocto/include/google/protobuf/*/*.h",
        "yocto/include/google/protobuf/*/*/*.h",
    ]),
    includes = ["include"],
    linkopts = [
        "-lprotoc",
    ],
    linkstatic = 1,
    visibility = ["//visibility:public"],
)

# Doesn't work in yocto! We instead patch the bazel stuff to run the protoc on the PATH
sh_binary(
    name = "protoc",
    srcs = ["yocto/native/protoc-native"],
    visibility = ["//visibility:public"],
    data = glob(["yocto/native/*"]),
)

################################################################################
# Tests
################################################################################

RELATIVE_LITE_TEST_PROTOS = [
    # AUTOGEN(lite_test_protos)
    "google/protobuf/map_lite_unittest.proto",
    "google/protobuf/unittest_import_lite.proto",
    "google/protobuf/unittest_import_public_lite.proto",
    "google/protobuf/unittest_lite.proto",
    "google/protobuf/unittest_no_arena_lite.proto",
]

LITE_TEST_PROTOS = ["src/" + s for s in RELATIVE_LITE_TEST_PROTOS]

RELATIVE_TEST_PROTOS = [
    # AUTOGEN(test_protos)
    "google/protobuf/any_test.proto",
    "google/protobuf/compiler/cpp/cpp_test_bad_identifiers.proto",
    "google/protobuf/compiler/cpp/cpp_test_large_enum_value.proto",
    "google/protobuf/map_proto2_unittest.proto",
    "google/protobuf/map_unittest.proto",
    "google/protobuf/unittest.proto",
    "google/protobuf/unittest_arena.proto",
    "google/protobuf/unittest_custom_options.proto",
    "google/protobuf/unittest_drop_unknown_fields.proto",
    "google/protobuf/unittest_embed_optimize_for.proto",
    "google/protobuf/unittest_empty.proto",
    "google/protobuf/unittest_enormous_descriptor.proto",
    "google/protobuf/unittest_import.proto",
    "google/protobuf/unittest_import_public.proto",
    "google/protobuf/unittest_lazy_dependencies.proto",
    "google/protobuf/unittest_lazy_dependencies_custom_option.proto",
    "google/protobuf/unittest_lazy_dependencies_enum.proto",
    "google/protobuf/unittest_lite_imports_nonlite.proto",
    "google/protobuf/unittest_mset.proto",
    "google/protobuf/unittest_mset_wire_format.proto",
    "google/protobuf/unittest_no_arena.proto",
    "google/protobuf/unittest_no_arena_import.proto",
    "google/protobuf/unittest_no_field_presence.proto",
    "google/protobuf/unittest_no_generic_services.proto",
    "google/protobuf/unittest_optimize_for.proto",
    "google/protobuf/unittest_preserve_unknown_enum.proto",
    "google/protobuf/unittest_preserve_unknown_enum2.proto",
    "google/protobuf/unittest_proto3.proto",
    "google/protobuf/unittest_proto3_arena.proto",
    "google/protobuf/unittest_proto3_arena_lite.proto",
    "google/protobuf/unittest_proto3_lite.proto",
    "google/protobuf/unittest_well_known_types.proto",
    "google/protobuf/util/internal/testdata/anys.proto",
    "google/protobuf/util/internal/testdata/books.proto",
    "google/protobuf/util/internal/testdata/default_value.proto",
    "google/protobuf/util/internal/testdata/default_value_test.proto",
    "google/protobuf/util/internal/testdata/field_mask.proto",
    "google/protobuf/util/internal/testdata/maps.proto",
    "google/protobuf/util/internal/testdata/oneofs.proto",
    "google/protobuf/util/internal/testdata/proto3.proto",
    "google/protobuf/util/internal/testdata/struct.proto",
    "google/protobuf/util/internal/testdata/timestamp_duration.proto",
    "google/protobuf/util/internal/testdata/wrappers.proto",
    "google/protobuf/util/json_format.proto",
    "google/protobuf/util/json_format_proto3.proto",
    "google/protobuf/util/message_differencer_unittest.proto",
]

TEST_PROTOS = ["src/" + s for s in RELATIVE_TEST_PROTOS]

cc_proto_library(
    name = "cc_test_protos",
    srcs = LITE_TEST_PROTOS + TEST_PROTOS,
    include = "src",
    default_runtime = ":protobuf",
    protoc = ":protoc",
    deps = [":cc_wkt_protos"],
)

COMMON_TEST_SRCS = [
    # AUTOGEN(common_test_srcs)
    "src/google/protobuf/arena_test_util.cc",
    "src/google/protobuf/map_test_util.inc",
    "src/google/protobuf/test_util.cc",
    "src/google/protobuf/test_util.inc",
    "src/google/protobuf/testing/file.cc",
    "src/google/protobuf/testing/googletest.cc",
]

cc_binary(
    name = "test_plugin",
    srcs = [
        # AUTOGEN(test_plugin_srcs)
        "src/google/protobuf/compiler/mock_code_generator.cc",
        "src/google/protobuf/compiler/test_plugin.cc",
        "src/google/protobuf/testing/file.cc",
    ],
    deps = [
        ":protobuf",
        ":protoc_lib",
        "//external:gtest",
    ],
)

cc_test(
    name = "win32_test",
    srcs = ["src/google/protobuf/io/io_win32_unittest.cc"],
    tags = [
        "manual",
        "windows",
    ],
    deps = [
        ":protobuf_lite",
        "//external:gtest_main",
    ],
)

cc_test(
    name = "protobuf_test",
    srcs = COMMON_TEST_SRCS + [
        # AUTOGEN(test_srcs)
        "src/google/protobuf/any_test.cc",
        "src/google/protobuf/arena_unittest.cc",
        "src/google/protobuf/arenastring_unittest.cc",
        "src/google/protobuf/compiler/annotation_test_util.cc",
        "src/google/protobuf/compiler/cpp/cpp_bootstrap_unittest.cc",
        "src/google/protobuf/compiler/cpp/cpp_move_unittest.cc",
        "src/google/protobuf/compiler/cpp/cpp_plugin_unittest.cc",
        "src/google/protobuf/compiler/cpp/cpp_unittest.cc",
        "src/google/protobuf/compiler/cpp/cpp_unittest.inc",
        "src/google/protobuf/compiler/cpp/metadata_test.cc",
        "src/google/protobuf/compiler/csharp/csharp_bootstrap_unittest.cc",
        "src/google/protobuf/compiler/csharp/csharp_generator_unittest.cc",
        "src/google/protobuf/compiler/importer_unittest.cc",
        "src/google/protobuf/compiler/java/java_doc_comment_unittest.cc",
        "src/google/protobuf/compiler/java/java_plugin_unittest.cc",
        "src/google/protobuf/compiler/mock_code_generator.cc",
        "src/google/protobuf/compiler/objectivec/objectivec_helpers_unittest.cc",
        "src/google/protobuf/compiler/parser_unittest.cc",
        "src/google/protobuf/compiler/python/python_plugin_unittest.cc",
        "src/google/protobuf/compiler/ruby/ruby_generator_unittest.cc",
        "src/google/protobuf/descriptor_database_unittest.cc",
        "src/google/protobuf/descriptor_unittest.cc",
        "src/google/protobuf/drop_unknown_fields_test.cc",
        "src/google/protobuf/dynamic_message_unittest.cc",
        "src/google/protobuf/extension_set_unittest.cc",
        "src/google/protobuf/generated_message_reflection_unittest.cc",
        "src/google/protobuf/io/coded_stream_unittest.cc",
        "src/google/protobuf/io/io_win32_unittest.cc",
        "src/google/protobuf/io/printer_unittest.cc",
        "src/google/protobuf/io/tokenizer_unittest.cc",
        "src/google/protobuf/io/zero_copy_stream_unittest.cc",
        "src/google/protobuf/map_field_test.cc",
        "src/google/protobuf/map_test.cc",
        "src/google/protobuf/message_unittest.cc",
        "src/google/protobuf/message_unittest.inc",
        "src/google/protobuf/no_field_presence_test.cc",
        "src/google/protobuf/preserve_unknown_enum_test.cc",
        "src/google/protobuf/proto3_arena_lite_unittest.cc",
        "src/google/protobuf/proto3_arena_unittest.cc",
        "src/google/protobuf/proto3_lite_unittest.cc",
        "src/google/protobuf/proto3_lite_unittest.inc",
        "src/google/protobuf/reflection_ops_unittest.cc",
        "src/google/protobuf/repeated_field_reflection_unittest.cc",
        "src/google/protobuf/repeated_field_unittest.cc",
        "src/google/protobuf/stubs/bytestream_unittest.cc",
        "src/google/protobuf/stubs/common_unittest.cc",
        "src/google/protobuf/stubs/int128_unittest.cc",
        "src/google/protobuf/stubs/status_test.cc",
        "src/google/protobuf/stubs/statusor_test.cc",
        "src/google/protobuf/stubs/stringpiece_unittest.cc",
        "src/google/protobuf/stubs/stringprintf_unittest.cc",
        "src/google/protobuf/stubs/structurally_valid_unittest.cc",
        "src/google/protobuf/stubs/strutil_unittest.cc",
        "src/google/protobuf/stubs/template_util_unittest.cc",
        "src/google/protobuf/stubs/time_test.cc",
        "src/google/protobuf/text_format_unittest.cc",
        "src/google/protobuf/unknown_field_set_unittest.cc",
        "src/google/protobuf/util/delimited_message_util_test.cc",
        "src/google/protobuf/util/field_comparator_test.cc",
        "src/google/protobuf/util/field_mask_util_test.cc",
        "src/google/protobuf/util/internal/default_value_objectwriter_test.cc",
        "src/google/protobuf/util/internal/json_objectwriter_test.cc",
        "src/google/protobuf/util/internal/json_stream_parser_test.cc",
        "src/google/protobuf/util/internal/protostream_objectsource_test.cc",
        "src/google/protobuf/util/internal/protostream_objectwriter_test.cc",
        "src/google/protobuf/util/internal/type_info_test_helper.cc",
        "src/google/protobuf/util/json_util_test.cc",
        "src/google/protobuf/util/message_differencer_unittest.cc",
        "src/google/protobuf/util/time_util_test.cc",
        "src/google/protobuf/util/type_resolver_util_test.cc",
        "src/google/protobuf/well_known_types_unittest.cc",
        "src/google/protobuf/wire_format_unittest.cc",
    ] + select({
        "//conditions:default": [
            # AUTOGEN(non_msvc_test_srcs)
            "src/google/protobuf/compiler/command_line_interface_unittest.cc",
        ],
        ":msvc": [],
    }),
    copts = COPTS,
    data = [
        ":test_plugin",
    ] + glob([
        "src/google/protobuf/**/*",
        # Files for csharp_bootstrap_unittest.cc.
        "conformance/**/*",
        "csharp/src/**/*",
    ]),
    includes = [
        "src/",
    ],
    linkopts = LINK_OPTS,
    deps = [
        ":cc_test_protos",
        ":protobuf",
        ":protoc_lib",
        "//external:gtest_main",
    ],
)

################################################################################
# Java support
################################################################################
internal_gen_well_known_protos_java(
    srcs = WELL_KNOWN_PROTOS,
)

java_library(
    name = "protobuf_java",
    srcs = glob([
        "java/core/src/main/java/com/google/protobuf/*.java",
    ]) + [
        ":gen_well_known_protos_java",
    ],
    javacopts = select({
        "//:jdk9": ["--add-modules=jdk.unsupported"],
        "//conditions:default": [
            "-source 7",
            "-target 7",
        ],
    }),
    visibility = ["//visibility:public"],
)

java_library(
    name = "protobuf_javalite",
    srcs = [
        # Keep in sync with java/lite/pom.xml
        "java/core/src/main/java/com/google/protobuf/AbstractMessageLite.java",
        "java/core/src/main/java/com/google/protobuf/AbstractParser.java",
        "java/core/src/main/java/com/google/protobuf/AbstractProtobufList.java",
        "java/core/src/main/java/com/google/protobuf/AllocatedBuffer.java",
        "java/core/src/main/java/com/google/protobuf/Android.java",
        "java/core/src/main/java/com/google/protobuf/ArrayDecoders.java",
        "java/core/src/main/java/com/google/protobuf/BinaryReader.java",
        "java/core/src/main/java/com/google/protobuf/BinaryWriter.java",
        "java/core/src/main/java/com/google/protobuf/BooleanArrayList.java",
        "java/core/src/main/java/com/google/protobuf/BufferAllocator.java",
        "java/core/src/main/java/com/google/protobuf/ByteBufferWriter.java",
        "java/core/src/main/java/com/google/protobuf/ByteOutput.java",
        "java/core/src/main/java/com/google/protobuf/ByteString.java",
        "java/core/src/main/java/com/google/protobuf/CodedInputStream.java",
        "java/core/src/main/java/com/google/protobuf/CodedInputStreamReader.java",
        "java/core/src/main/java/com/google/protobuf/CodedOutputStream.java",
        "java/core/src/main/java/com/google/protobuf/CodedOutputStreamWriter.java",
        "java/core/src/main/java/com/google/protobuf/DoubleArrayList.java",
        "java/core/src/main/java/com/google/protobuf/ExperimentalApi.java",
        "java/core/src/main/java/com/google/protobuf/ExtensionLite.java",
        "java/core/src/main/java/com/google/protobuf/ExtensionRegistryFactory.java",
        "java/core/src/main/java/com/google/protobuf/ExtensionRegistryLite.java",
        "java/core/src/main/java/com/google/protobuf/ExtensionSchema.java",
        "java/core/src/main/java/com/google/protobuf/ExtensionSchemaLite.java",
        "java/core/src/main/java/com/google/protobuf/ExtensionSchemas.java",
        "java/core/src/main/java/com/google/protobuf/FieldInfo.java",
        "java/core/src/main/java/com/google/protobuf/FieldSet.java",
        "java/core/src/main/java/com/google/protobuf/FieldType.java",
        "java/core/src/main/java/com/google/protobuf/FloatArrayList.java",
        "java/core/src/main/java/com/google/protobuf/GeneratedMessageInfoFactory.java",
        "java/core/src/main/java/com/google/protobuf/GeneratedMessageLite.java",
        "java/core/src/main/java/com/google/protobuf/IntArrayList.java",
        "java/core/src/main/java/com/google/protobuf/Internal.java",
        "java/core/src/main/java/com/google/protobuf/InvalidProtocolBufferException.java",
        "java/core/src/main/java/com/google/protobuf/IterableByteBufferInputStream.java",
        "java/core/src/main/java/com/google/protobuf/JavaType.java",
        "java/core/src/main/java/com/google/protobuf/LazyField.java",
        "java/core/src/main/java/com/google/protobuf/LazyFieldLite.java",
        "java/core/src/main/java/com/google/protobuf/LazyStringArrayList.java",
        "java/core/src/main/java/com/google/protobuf/LazyStringList.java",
        "java/core/src/main/java/com/google/protobuf/ListFieldSchema.java",
        "java/core/src/main/java/com/google/protobuf/LongArrayList.java",
        "java/core/src/main/java/com/google/protobuf/ManifestSchemaFactory.java",
        "java/core/src/main/java/com/google/protobuf/MapEntryLite.java",
        "java/core/src/main/java/com/google/protobuf/MapFieldLite.java",
        "java/core/src/main/java/com/google/protobuf/MapFieldSchema.java",
        "java/core/src/main/java/com/google/protobuf/MapFieldSchemaLite.java",
        "java/core/src/main/java/com/google/protobuf/MapFieldSchemas.java",
        "java/core/src/main/java/com/google/protobuf/MessageInfo.java",
        "java/core/src/main/java/com/google/protobuf/MessageInfoFactory.java",
        "java/core/src/main/java/com/google/protobuf/MessageLite.java",
        "java/core/src/main/java/com/google/protobuf/MessageLiteOrBuilder.java",
        "java/core/src/main/java/com/google/protobuf/MessageLiteToString.java",
        "java/core/src/main/java/com/google/protobuf/MessageSchema.java",
        "java/core/src/main/java/com/google/protobuf/MessageSetSchema.java",
        "java/core/src/main/java/com/google/protobuf/MutabilityOracle.java",
        "java/core/src/main/java/com/google/protobuf/NewInstanceSchema.java",
        "java/core/src/main/java/com/google/protobuf/NewInstanceSchemaLite.java",
        "java/core/src/main/java/com/google/protobuf/NewInstanceSchemas.java",
        "java/core/src/main/java/com/google/protobuf/NioByteString.java",
        "java/core/src/main/java/com/google/protobuf/OneofInfo.java",
        "java/core/src/main/java/com/google/protobuf/Parser.java",
        "java/core/src/main/java/com/google/protobuf/PrimitiveNonBoxingCollection.java",
        "java/core/src/main/java/com/google/protobuf/ProtoSyntax.java",
        "java/core/src/main/java/com/google/protobuf/Protobuf.java",
        "java/core/src/main/java/com/google/protobuf/ProtobufArrayList.java",
        "java/core/src/main/java/com/google/protobuf/ProtobufLists.java",
        "java/core/src/main/java/com/google/protobuf/ProtocolStringList.java",
        "java/core/src/main/java/com/google/protobuf/RawMessageInfo.java",
        "java/core/src/main/java/com/google/protobuf/Reader.java",
        "java/core/src/main/java/com/google/protobuf/RopeByteString.java",
        "java/core/src/main/java/com/google/protobuf/Schema.java",
        "java/core/src/main/java/com/google/protobuf/SchemaFactory.java",
        "java/core/src/main/java/com/google/protobuf/SchemaUtil.java",
        "java/core/src/main/java/com/google/protobuf/SmallSortedMap.java",
        "java/core/src/main/java/com/google/protobuf/StructuralMessageInfo.java",
        "java/core/src/main/java/com/google/protobuf/TextFormatEscaper.java",
        "java/core/src/main/java/com/google/protobuf/UninitializedMessageException.java",
        "java/core/src/main/java/com/google/protobuf/UnknownFieldSchema.java",
        "java/core/src/main/java/com/google/protobuf/UnknownFieldSetLite.java",
        "java/core/src/main/java/com/google/protobuf/UnknownFieldSetLiteSchema.java",
        "java/core/src/main/java/com/google/protobuf/UnmodifiableLazyStringList.java",
        "java/core/src/main/java/com/google/protobuf/UnsafeUtil.java",
        "java/core/src/main/java/com/google/protobuf/Utf8.java",
        "java/core/src/main/java/com/google/protobuf/WireFormat.java",
        "java/core/src/main/java/com/google/protobuf/Writer.java",
    ],
    javacopts = select({
        "//:jdk9": ["--add-modules=jdk.unsupported"],
        "//conditions:default": [
            "-source 7",
            "-target 7",
        ],
    }),
    visibility = ["//visibility:public"],
)

java_library(
    name = "protobuf_java_util",
    srcs = glob([
        "java/util/src/main/java/com/google/protobuf/util/*.java",
    ]),
    javacopts = [
        "-source 7",
        "-target 7",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "protobuf_java",
        "//external:error_prone_annotations",
        "//external:gson",
        "//external:guava",
    ],
)

################################################################################
# Python support
################################################################################

py_library(
    name = "python_srcs",
    srcs = glob(
        [
            "python/google/**/*.py",
        ],
        exclude = [
            "python/google/protobuf/**/__init__.py",
            "python/google/protobuf/internal/*_test.py",
            "python/google/protobuf/internal/test_util.py",
        ],
    ),
    imports = ["python"],
    srcs_version = "PY2AND3",
)

cc_binary(
    name = "python/google/protobuf/internal/_api_implementation.so",
    srcs = ["python/google/protobuf/internal/api_implementation.cc"],
    copts = COPTS + [
        "-DPYTHON_PROTO2_CPP_IMPL_V2",
    ],
    linkshared = 1,
    linkstatic = 1,
    deps = select({
        "//conditions:default": [],
        ":use_fast_cpp_protos": ["//external:python_headers"],
    }),
)

cc_binary(
    name = "python/google/protobuf/pyext/_message.so",
    srcs = glob([
        "python/google/protobuf/pyext/*.cc",
        "python/google/protobuf/pyext/*.h",
    ]),
    copts = COPTS + [
        "-DGOOGLE_PROTOBUF_HAS_ONEOF=1",
    ] + select({
        "//conditions:default": [],
        ":allow_oversize_protos": ["-DPROTOBUF_PYTHON_ALLOW_OVERSIZE_PROTOS=1"],
    }),
    includes = [
        "python/",
        "src/",
    ],
    linkshared = 1,
    linkstatic = 1,
    deps = [
        ":protobuf",
        ":proto_api",
    ] + select({
        "//conditions:default": [],
        ":use_fast_cpp_protos": ["//external:python_headers"],
    }),
)

config_setting(
    name = "use_fast_cpp_protos",
    values = {
        "define": "use_fast_cpp_protos=true",
    },
)

config_setting(
    name = "allow_oversize_protos",
    values = {
        "define": "allow_oversize_protos=true",
    },
)

# Copy the builtin proto files from src/google/protobuf to
# python/google/protobuf. This way, the generated Python sources will be in the
# same directory as the Python runtime sources. This is necessary for the
# modules to be imported correctly since they are all part of the same Python
# package.
internal_copied_filegroup(
    name = "protos_python",
    srcs = WELL_KNOWN_PROTOS,
    dest = "python",
    strip_prefix = "src",
)

# TODO(dzc): Remove this once py_proto_library can have labels in srcs, in
# which case we can simply add :protos_python in srcs.
COPIED_WELL_KNOWN_PROTOS = ["python/" + s[4:] for s in WELL_KNOWN_PROTOS]

py_proto_library(
    name = "protobuf_python",
    srcs = COPIED_WELL_KNOWN_PROTOS,
    include = "python",
    data = select({
        "//conditions:default": [],
        ":use_fast_cpp_protos": [
            ":python/google/protobuf/internal/_api_implementation.so",
            ":python/google/protobuf/pyext/_message.so",
        ],
    }),
    default_runtime = "",
    protoc = ":protoc",
    py_extra_srcs = glob(["python/**/__init__.py"]),
    py_libs = [
        ":python_srcs",
        "@six//:six",
    ],
    srcs_version = "PY2AND3",
    visibility = ["//visibility:public"],
)

# Copy the test proto files from src/google/protobuf to
# python/google/protobuf. This way, the generated Python sources will be in the
# same directory as the Python runtime sources. This is necessary for the
# modules to be imported correctly by the tests since they are all part of the
# same Python package.
internal_copied_filegroup(
    name = "protos_python_test",
    srcs = LITE_TEST_PROTOS + TEST_PROTOS,
    dest = "python",
    strip_prefix = "src",
)

# TODO(dzc): Remove this once py_proto_library can have labels in srcs, in
# which case we can simply add :protos_python_test in srcs.
COPIED_LITE_TEST_PROTOS = ["python/" + s for s in RELATIVE_LITE_TEST_PROTOS]

COPIED_TEST_PROTOS = ["python/" + s for s in RELATIVE_TEST_PROTOS]

py_proto_library(
    name = "python_common_test_protos",
    srcs = COPIED_LITE_TEST_PROTOS + COPIED_TEST_PROTOS,
    include = "python",
    default_runtime = "",
    protoc = ":protoc",
    srcs_version = "PY2AND3",
    deps = [":protobuf_python"],
)

py_proto_library(
    name = "python_specific_test_protos",
    srcs = glob([
        "python/google/protobuf/internal/*.proto",
        "python/google/protobuf/internal/import_test_package/*.proto",
    ]),
    include = "python",
    default_runtime = ":protobuf_python",
    protoc = ":protoc",
    srcs_version = "PY2AND3",
    deps = [":python_common_test_protos"],
)

py_library(
    name = "python_tests",
    srcs = glob(
        [
            "python/google/protobuf/internal/*_test.py",
            "python/google/protobuf/internal/test_util.py",
            "python/google/protobuf/internal/import_test_package/__init__.py",
        ],
    ),
    imports = ["python"],
    srcs_version = "PY2AND3",
    deps = [
        ":protobuf_python",
        ":python_common_test_protos",
        ":python_specific_test_protos",
    ],
)

internal_protobuf_py_tests(
    name = "python_tests_batch",
    data = glob([
        "src/google/protobuf/**/*",
    ]),
    modules = [
        "descriptor_database_test",
        "descriptor_pool_test",
        "descriptor_test",
        "generator_test",
        "json_format_test",
        "message_factory_test",
        "message_test",
        "proto_builder_test",
        "reflection_test",
        "service_reflection_test",
        "symbol_database_test",
        "text_encoding_test",
        "text_format_test",
        "unknown_fields_test",
        "wire_format_test",
    ],
    deps = [":python_tests"],
)

cc_library(
    name = "proto_api",
    hdrs = ["python/google/protobuf/proto_api.h"],
    visibility = ["//visibility:public"],
    deps = [
        "//external:python_headers",
    ],
)

# Note: We use `native_proto_common` here because we depend on an implementation-detail of
# `proto_lang_toolchain`, which may not be available on `proto_common`.
reject_blacklisted_files = hasattr(native_proto_common, "proto_lang_toolchain_rejects_files_do_not_use_or_we_will_break_you_without_mercy")
cc_toolchain_blacklisted_protos = [proto + "_proto" for proto in WELL_KNOWN_PROTO_MAP.keys()] if reject_blacklisted_files else [":well_known_protos"]
proto_lang_toolchain(
    name = "cc_toolchain",
    blacklisted_protos = cc_toolchain_blacklisted_protos,
    command_line = "--cpp_out=$(OUT)",
    runtime = ":protobuf",
    visibility = ["//visibility:public"],
)

proto_lang_toolchain(
    name = "java_toolchain",
    command_line = "--java_out=$(OUT)",
    runtime = ":protobuf_java",
    visibility = ["//visibility:public"],
)

proto_lang_toolchain(
    name = "javalite_toolchain",
    command_line = "--java_out=lite:$(OUT)",
    runtime = ":protobuf_javalite",
    visibility = ["//visibility:public"],
)

alias(
    name = "objectivec",
    actual = ":protobuf_objc",
    visibility = ["//visibility:public"],
)

objc_library(
    name = "protobuf_objc",
    hdrs = [
        "objectivec/GPBArray.h",
        "objectivec/GPBBootstrap.h",
        "objectivec/GPBCodedInputStream.h",
        "objectivec/GPBCodedOutputStream.h",
        "objectivec/GPBDescriptor.h",
        "objectivec/GPBDictionary.h",
        "objectivec/GPBExtensionInternals.h",
        "objectivec/GPBExtensionRegistry.h",
        "objectivec/GPBMessage.h",
        "objectivec/GPBProtocolBuffers.h",
        "objectivec/GPBProtocolBuffers_RuntimeSupport.h",
        "objectivec/GPBRootObject.h",
        "objectivec/GPBRuntimeTypes.h",
        "objectivec/GPBUnknownField.h",
        "objectivec/GPBUnknownFieldSet.h",
        "objectivec/GPBUtilities.h",
        "objectivec/GPBWellKnownTypes.h",
        "objectivec/GPBWireFormat.h",
        "objectivec/google/protobuf/Any.pbobjc.h",
        "objectivec/google/protobuf/Api.pbobjc.h",
        "objectivec/google/protobuf/Duration.pbobjc.h",
        "objectivec/google/protobuf/Empty.pbobjc.h",
        "objectivec/google/protobuf/FieldMask.pbobjc.h",
        "objectivec/google/protobuf/SourceContext.pbobjc.h",
        "objectivec/google/protobuf/Struct.pbobjc.h",
        "objectivec/google/protobuf/Timestamp.pbobjc.h",
        "objectivec/google/protobuf/Type.pbobjc.h",
        "objectivec/google/protobuf/Wrappers.pbobjc.h",
        # Package private headers, but exposed because the generated sources
        # need to use them.
        "objectivec/GPBArray_PackagePrivate.h",
        "objectivec/GPBCodedInputStream_PackagePrivate.h",
        "objectivec/GPBCodedOutputStream_PackagePrivate.h",
        "objectivec/GPBDescriptor_PackagePrivate.h",
        "objectivec/GPBDictionary_PackagePrivate.h",
        "objectivec/GPBMessage_PackagePrivate.h",
        "objectivec/GPBRootObject_PackagePrivate.h",
        "objectivec/GPBUnknownFieldSet_PackagePrivate.h",
        "objectivec/GPBUnknownField_PackagePrivate.h",
        "objectivec/GPBUtilities_PackagePrivate.h",
    ],
    copts = [
        "-Wno-vla",
    ],
    includes = [
        "objectivec",
    ],
    non_arc_srcs = [
        "objectivec/GPBArray.m",
        "objectivec/GPBCodedInputStream.m",
        "objectivec/GPBCodedOutputStream.m",
        "objectivec/GPBDescriptor.m",
        "objectivec/GPBDictionary.m",
        "objectivec/GPBExtensionInternals.m",
        "objectivec/GPBExtensionRegistry.m",
        "objectivec/GPBMessage.m",
        "objectivec/GPBRootObject.m",
        "objectivec/GPBUnknownField.m",
        "objectivec/GPBUnknownFieldSet.m",
        "objectivec/GPBUtilities.m",
        "objectivec/GPBWellKnownTypes.m",
        "objectivec/GPBWireFormat.m",
        "objectivec/google/protobuf/Any.pbobjc.m",
        "objectivec/google/protobuf/Api.pbobjc.m",
        "objectivec/google/protobuf/Duration.pbobjc.m",
        "objectivec/google/protobuf/Empty.pbobjc.m",
        "objectivec/google/protobuf/FieldMask.pbobjc.m",
        "objectivec/google/protobuf/SourceContext.pbobjc.m",
        "objectivec/google/protobuf/Struct.pbobjc.m",
        "objectivec/google/protobuf/Timestamp.pbobjc.m",
        "objectivec/google/protobuf/Type.pbobjc.m",
        "objectivec/google/protobuf/Wrappers.pbobjc.m",
    ],
    visibility = ["//visibility:public"],
)
