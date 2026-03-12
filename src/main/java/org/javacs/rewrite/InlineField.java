package org.javacs.rewrite;

import java.nio.file.Path;
import java.util.Map;
import java.util.logging.Logger;
import org.javacs.CompilerProvider;
import org.javacs.lsp.TextEdit;

class InlineField implements Rewrite {
    private static final Logger LOG = Logger.getLogger("main");
    final String className, fieldName;

    InlineField(String className, String fieldName) {
        this.className = className;
        this.fieldName = fieldName;
    }

    @Override
    public Map<Path, TextEdit[]> rewrite(CompilerProvider compiler) {
        LOG.warning("InlineField.rewrite is not yet implemented");
        return Rewrite.CANCELLED;
    }
}
