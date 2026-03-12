package org.javacs.rewrite;

import java.nio.file.Path;
import java.util.Map;
import java.util.logging.Logger;
import org.javacs.CompilerProvider;
import org.javacs.lsp.TextEdit;

class InlineVariable implements Rewrite {
    private static final Logger LOG = Logger.getLogger(InlineVariable.class.getName());
    final String className;
    final int position;

    InlineVariable(String className, int position) {
        this.className = className;
        this.position = position;
    }

    @Override
    public Map<Path, TextEdit[]> rewrite(CompilerProvider compiler) {
        LOG.warning("InlineVariable.rewrite is not yet implemented");
        return Rewrite.CANCELLED;
    }
}
