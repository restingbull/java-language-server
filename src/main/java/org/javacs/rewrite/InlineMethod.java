package org.javacs.rewrite;

import java.nio.file.Path;
import java.util.Map;
import java.util.logging.Logger;
import org.javacs.CompilerProvider;
import org.javacs.lsp.TextEdit;

class InlineMethod implements Rewrite {
    private static final Logger LOG = Logger.getLogger(InlineMethod.class.getName());
    final String className, methodName;
    final String[] erasedParameterTypes;

    InlineMethod(String className, String methodName, String[] erasedParameterTypes) {
        this.className = className;
        this.methodName = methodName;
        this.erasedParameterTypes = erasedParameterTypes;
    }

    @Override
    public Map<Path, TextEdit[]> rewrite(CompilerProvider compiler) {
        LOG.warning("InlineMethod.rewrite is not yet implemented");
        return Rewrite.CANCELLED;
    }
}
