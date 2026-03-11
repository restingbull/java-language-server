#!/usr/bin/env bash

set -e

# Needed once
if [ ! -e node_modules ]; then
    npm install
fi

# Build standalone java
if [ ! -e jdks/linux/jdk-25.0.2 ]; then
    ./scripts/download_linux_jdk.sh
fi
if [ ! -e jdks/windows/jdk-25.0.2 ]; then
    ./scripts/download_windows_jdk.sh
fi
if [ ! -e dist/linux/bin/java ]; then
    ./scripts/link_linux.sh
fi
if [ ! -e dist/windows/bin/java.exe ]; then
    ./scripts/link_windows.sh
fi
if [ ! -e dist/mac/bin/java ]; then
    ./scripts/link_mac.sh
fi

# Compile sources
if [ ! -e src/main/java/com/google/devtools/build/lib/analysis/AnalysisProtos.java ]; then
    ./scripts/gen_proto.sh
fi

bazel build //:java-language-server

# Assemble dist/classpath for VSCode extension
mkdir -p dist/classpath
cp bazel-bin/java-language-server.jar dist/classpath/
cp ~/.m2/repository/com/google/code/gson/gson/2.8.9/gson-2.8.9.jar dist/classpath/
cp ~/.m2/repository/com/google/protobuf/protobuf-java/3.19.6/protobuf-java-3.19.6.jar dist/classpath/

# Build vsix
npm run-script vscode:build

code --install-extension build.vsix --force

echo 'Reload VSCode to update extension'
