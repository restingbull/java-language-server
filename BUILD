load("@rules_java//java:defs.bzl", "java_binary", "java_library")

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
