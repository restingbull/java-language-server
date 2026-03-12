#!/usr/bin/env bash
# start_lsp.sh - Build and run java-language-server LSP in a given directory.
# Usage: ./scripts/start_lsp.sh [workspace-dir] [-- extra-java-args...]
# Communicates via stdin/stdout (LSP JSON-RPC protocol).

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

WORKSPACE_DIR="${1:-.}"
WORKSPACE_DIR="$(cd "$WORKSPACE_DIR" && pwd)"

DEPLOY_JAR="$REPO_DIR/bazel-bin/java-language-server_deploy.jar"

# Build if deploy jar is missing
if [ ! -f "$DEPLOY_JAR" ]; then
    echo "Building java-language-server..." >&2
    cd "$REPO_DIR"
    bazel build //:java-language-server
fi

# Resolve java executable
if [ -n "$JAVA_HOME" ]; then
    JAVA="$JAVA_HOME/bin/java"
else
    JAVA="java"
fi

# Run LSP server with cwd set to the provided workspace directory
cd "$WORKSPACE_DIR"
exec "$JAVA" \
    --add-exports jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED \
    --add-exports jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED \
    --add-exports jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED \
    --add-exports jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED \
    --add-exports jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED \
    --add-exports jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED \
    --add-exports jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED \
    --add-opens jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED \
    -jar "$DEPLOY_JAR" \
    "${@:2}"
