load("@rules_java//java:defs.bzl", "java_binary", "java_library", "java_test")

java_library(
    name = "lib",
    srcs = glob(["src/main/java/**/*.java"]),
    deps = [
        "@maven//:com_google_code_gson_gson",
        "@maven//:com_google_protobuf_protobuf_java",
    ],
)

java_binary(
    name = "java-language-server",
    main_class = "org.javacs.Main",
    runtime_deps = [":lib"],
)

_TEST_DEPS = [
    ":lib",
    "@maven//:com_google_code_gson_gson",
    "@maven//:com_google_protobuf_protobuf_java",
    "@maven//:junit_junit",
    "@maven//:org_hamcrest_hamcrest_core",
    "@maven//:org_hamcrest_hamcrest_all",
]

_TEST_SRCS = glob(
    ["src/test/java/**/*.java"],
    exclude = [
        "src/test/java/**/Benchmark*.java",
        "src/test/java/**/JavaCompilerServiceTest.java",
    ],
)

_TEST_DATA = glob(["src/test/examples/**"]) + ["pom.xml"]

java_test(
    name = "ArtifactTest",
    srcs = _TEST_SRCS,
    data = _TEST_DATA,
    local = True,
    test_class = "org.javacs.ArtifactTest",
    deps = _TEST_DEPS,
)

java_test(
    name = "ClassesTest",
    srcs = _TEST_SRCS,
    data = _TEST_DATA,
    local = True,
    test_class = "org.javacs.ClassesTest",
    deps = _TEST_DEPS,
)

java_test(
    name = "CodeActionTest",
    srcs = _TEST_SRCS,
    data = _TEST_DATA,
    local = True,
    test_class = "org.javacs.CodeActionTest",
    deps = _TEST_DEPS,
)

java_test(
    name = "CodeLensTest",
    srcs = _TEST_SRCS,
    data = _TEST_DATA,
    local = True,
    test_class = "org.javacs.CodeLensTest",
    deps = _TEST_DEPS,
)

java_test(
    name = "CompletionsScopesTest",
    srcs = _TEST_SRCS,
    data = _TEST_DATA,
    local = True,
    test_class = "org.javacs.CompletionsScopesTest",
    deps = _TEST_DEPS,
)

java_test(
    name = "CompletionsTest",
    srcs = _TEST_SRCS,
    data = _TEST_DATA,
    local = True,
    test_class = "org.javacs.CompletionsTest",
    deps = _TEST_DEPS,
)

java_test(
    name = "FileEncodingTest",
    srcs = _TEST_SRCS,
    data = _TEST_DATA,
    local = True,
    test_class = "org.javacs.FileEncodingTest",
    deps = _TEST_DEPS,
)

java_test(
    name = "FileStoreTest",
    srcs = _TEST_SRCS,
    data = _TEST_DATA,
    local = True,
    test_class = "org.javacs.FileStoreTest",
    deps = _TEST_DEPS,
)

java_test(
    name = "FindReferencesTest",
    srcs = _TEST_SRCS,
    data = _TEST_DATA,
    local = True,
    test_class = "org.javacs.FindReferencesTest",
    deps = _TEST_DEPS,
)

java_test(
    name = "FindSrcZipTest",
    srcs = _TEST_SRCS,
    data = _TEST_DATA,
    local = True,
    test_class = "org.javacs.FindSrcZipTest",
    deps = _TEST_DEPS,
)

java_test(
    name = "GotoTest",
    srcs = _TEST_SRCS,
    data = _TEST_DATA,
    local = True,
    test_class = "org.javacs.GotoTest",
    deps = _TEST_DEPS,
)

java_test(
    name = "HoverTest",
    srcs = _TEST_SRCS,
    data = _TEST_DATA,
    local = True,
    test_class = "org.javacs.HoverTest",
    deps = _TEST_DEPS,
)

java_test(
    name = "IncrementalCompileTest",
    srcs = _TEST_SRCS,
    data = _TEST_DATA,
    local = True,
    test_class = "org.javacs.IncrementalCompileTest",
    deps = _TEST_DEPS,
)

java_test(
    name = "InferBazelConfigTest",
    srcs = _TEST_SRCS,
    data = _TEST_DATA,
    local = True,
    test_class = "org.javacs.InferBazelConfigTest",
    deps = _TEST_DEPS,
)

java_test(
    name = "InferConfigTest",
    srcs = _TEST_SRCS,
    data = _TEST_DATA,
    local = True,
    test_class = "org.javacs.InferConfigTest",
    deps = _TEST_DEPS,
)

java_test(
    name = "JavaDebugServerTest",
    srcs = _TEST_SRCS,
    data = _TEST_DATA,
    local = True,
    test_class = "org.javacs.JavaDebugServerTest",
    deps = _TEST_DEPS,
)

java_test(
    name = "JavaLanguageServerTest",
    srcs = _TEST_SRCS,
    data = _TEST_DATA,
    local = True,
    test_class = "org.javacs.JavaLanguageServerTest",
    deps = _TEST_DEPS,
)

java_test(
    name = "LanguageServerTest",
    srcs = _TEST_SRCS,
    data = _TEST_DATA,
    local = True,
    test_class = "org.javacs.lsp.LanguageServerTest",
    deps = _TEST_DEPS,
)

java_test(
    name = "LspTest",
    srcs = _TEST_SRCS,
    data = _TEST_DATA,
    local = True,
    test_class = "org.javacs.lsp.LspTest",
    deps = _TEST_DEPS,
)

java_test(
    name = "MarkdownHelperTest",
    srcs = _TEST_SRCS,
    data = _TEST_DATA,
    local = True,
    test_class = "org.javacs.MarkdownHelperTest",
    deps = _TEST_DEPS,
)

java_test(
    name = "RewriteTest",
    srcs = _TEST_SRCS,
    data = _TEST_DATA,
    local = True,
    test_class = "org.javacs.rewrite.RewriteTest",
    deps = _TEST_DEPS,
)

java_test(
    name = "SearchTest",
    srcs = _TEST_SRCS,
    data = _TEST_DATA,
    local = True,
    test_class = "org.javacs.SearchTest",
    deps = _TEST_DEPS,
)

java_test(
    name = "SemanticColorsTest",
    srcs = _TEST_SRCS,
    data = _TEST_DATA,
    local = True,
    test_class = "org.javacs.SemanticColorsTest",
    deps = _TEST_DEPS,
)

java_test(
    name = "SignatureHelpTest",
    srcs = _TEST_SRCS,
    data = _TEST_DATA,
    local = True,
    test_class = "org.javacs.SignatureHelpTest",
    deps = _TEST_DEPS,
)

java_test(
    name = "SourceFileManagerTest",
    srcs = _TEST_SRCS,
    data = _TEST_DATA,
    local = True,
    # Fails under Bazel: StandardJavaFileManager doesn't resolve runfiles symlinks on macOS.
    # Passes with Maven (real paths). Tag manual to exclude from //... CI runs.
    tags = ["manual"],
    test_class = "org.javacs.SourceFileManagerTest",
    deps = _TEST_DEPS,
)

java_test(
    name = "StringSearchTest",
    srcs = _TEST_SRCS,
    data = _TEST_DATA,
    local = True,
    test_class = "org.javacs.StringSearchTest",
    deps = _TEST_DEPS,
)

java_test(
    name = "WarningsTest",
    srcs = _TEST_SRCS,
    data = _TEST_DATA,
    local = True,
    test_class = "org.javacs.WarningsTest",
    deps = _TEST_DEPS,
)
