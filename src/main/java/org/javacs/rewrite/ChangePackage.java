package org.javacs.rewrite;

import com.sun.source.util.Trees;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.javacs.CompilerProvider;
import org.javacs.lsp.Position;
import org.javacs.lsp.Range;
import org.javacs.lsp.TextEdit;

public class ChangePackage implements Rewrite {
    private final Path file;
    private final String newPackage;

    public ChangePackage(Path file, String newPackage) {
        this.file = file;
        this.newPackage = newPackage;
    }

    @Override
    public Map<Path, TextEdit[]> rewrite(CompilerProvider compiler) {
        var task = compiler.parse(file);
        var pkg = task.root.getPackage();
        if (pkg == null) {
            LOG.warning("No package declaration in " + file);
            return CANCELLED;
        }

        var oldPackage = pkg.getPackageName().toString();
        var simpleName = simpleName(file);
        var oldFqn = oldPackage + "." + simpleName;
        var newFqn = newPackage + "." + simpleName;

        var result = new HashMap<Path, TextEdit[]>();

        // Edit the package declaration in the source file
        var pos = Trees.instance(task.task).getSourcePositions();
        var lines = task.root.getLineMap();
        var pkgStart = pos.getStartPosition(task.root, pkg);
        var pkgEnd = pos.getEndPosition(task.root, pkg);
        var startLine = (int) lines.getLineNumber(pkgStart);
        var startCol = (int) lines.getColumnNumber(pkgStart);
        var endLine = (int) lines.getLineNumber(pkgEnd);
        var endCol = (int) lines.getColumnNumber(pkgEnd);
        var pkgRange = new Range(
                new Position(startLine - 1, startCol - 1),
                new Position(endLine - 1, endCol - 1));
        var pkgEdit = new TextEdit(pkgRange, "package " + newPackage);
        result.put(file, new TextEdit[]{pkgEdit});

        // Find all files that import the old FQN and update them
        var importers = compiler.findTypeReferences(oldFqn);
        if (importers.length > 0) {
            try (var compile = compiler.compile(importers)) {
                for (var root : compile.roots) {
                    var rootPath = Path.of(root.getSourceFile().toUri());
                    var edits = new ArrayList<TextEdit>();
                    var rootLines = root.getLineMap();
                    var rootPos = Trees.instance(compile.task).getSourcePositions();
                    for (var imp : root.getImports()) {
                        if (imp.isStatic()) continue;
                        var impName = imp.getQualifiedIdentifier().toString();
                        if (!impName.equals(oldFqn)) continue;
                        var impStart = rootPos.getStartPosition(root, imp);
                        var impEnd = rootPos.getEndPosition(root, imp);
                        var impStartLine = (int) rootLines.getLineNumber(impStart);
                        var impStartCol = (int) rootLines.getColumnNumber(impStart);
                        var impEndLine = (int) rootLines.getLineNumber(impEnd);
                        var impEndCol = (int) rootLines.getColumnNumber(impEnd);
                        var impRange = new Range(
                                new Position(impStartLine - 1, impStartCol - 1),
                                new Position(impEndLine - 1, impEndCol - 1));
                        edits.add(new TextEdit(impRange, "import " + newFqn));
                    }
                    if (!edits.isEmpty()) {
                        result.put(rootPath, edits.toArray(new TextEdit[0]));
                    }
                }
            }
        }

        return result;
    }

    private static String simpleName(Path file) {
        var name = file.getFileName().toString();
        if (name.endsWith(".java")) {
            return name.substring(0, name.length() - 5);
        }
        return name;
    }

    private static final Logger LOG = Logger.getLogger("main");
}
