#!/usr/bin/env bash
# start_lsp.sh - Build and run java-language-server LSP in a given directory.
# Usage: ./scripts/start_lsp.sh [workspace-dir] [-- extra-java-args...]
# Communicates via stdin/stdout (LSP JSON-RPC protocol).

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

WORKSPACE_DIR="${1:-.}"
WORKSPACE_DIR="$(cd "$WORKSPACE_DIR" && pwd)"

EXECUTABLE="$REPO_DIR/bazel-bin/java-language-server"

# Build if executable is missing
if [ ! -f "$EXECUTABLE" ]; then
    echo "Building java-language-server..." >&2
    (
        cd "$REPO_DIR"
        bazel build //:java-language-server
    )
fi

# Run LSP server with cwd set to the provided workspace directory
cd "$WORKSPACE_DIR"
exec "${EXECUTABLE}" \
    "${@:2}"
