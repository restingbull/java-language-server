package org.javacs.markup;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

class WarnWrongType extends TreePathScanner<Void, List<WarnWrongType.Finding>> {

    static class Finding {
        final TreePath path;
        final String methodName;
        final TypeMirror argType;
        final TypeMirror elementType;

        Finding(TreePath path, String methodName, TypeMirror argType, TypeMirror elementType) {
            this.path = path;
            this.methodName = methodName;
            this.argType = argType;
            this.elementType = elementType;
        }
    }

    private static final Set<String> CHECKED_METHODS = Set.of("contains", "indexOf", "remove");

    private final Trees trees;
    private final Types types;

    WarnWrongType(JavacTask task) {
        this.trees = Trees.instance(task);
        this.types = task.getTypes();
    }

    @Override
    public Void visitMethodInvocation(MethodInvocationTree t, List<Finding> findings) {
        var methodSelect = t.getMethodSelect();
        if (!(methodSelect instanceof MemberSelectTree)) {
            return super.visitMethodInvocation(t, findings);
        }
        var memberSelect = (MemberSelectTree) methodSelect;
        var methodName = memberSelect.getIdentifier().toString();
        if (!CHECKED_METHODS.contains(methodName)) {
            return super.visitMethodInvocation(t, findings);
        }
        if (t.getArguments().size() != 1) {
            return super.visitMethodInvocation(t, findings);
        }

        // Verify the resolved method takes Object (not int index overload of List.remove)
        var methodEl = trees.getElement(getCurrentPath());
        if (!(methodEl instanceof ExecutableElement)) {
            return super.visitMethodInvocation(t, findings);
        }
        var execEl = (ExecutableElement) methodEl;
        var params = execEl.getParameters();
        if (params.size() != 1) {
            return super.visitMethodInvocation(t, findings);
        }
        var paramType = params.get(0).asType();
        // Skip primitive overloads (e.g. List.remove(int))
        if (paramType.getKind() == TypeKind.INT || paramType.getKind().isPrimitive()) {
            return super.visitMethodInvocation(t, findings);
        }

        // Get receiver type
        var receiverExpr = memberSelect.getExpression();
        var receiverPath = new TreePath(getCurrentPath(), receiverExpr);
        var receiverType = trees.getTypeMirror(receiverPath);
        if (!(receiverType instanceof DeclaredType)) {
            return super.visitMethodInvocation(t, findings);
        }
        var declaredReceiver = (DeclaredType) receiverType;

        // Find the Collection element type parameter
        var elementType = collectionElementType(declaredReceiver);
        if (elementType == null) {
            return super.visitMethodInvocation(t, findings);
        }

        // Get argument type
        ExpressionTree argExpr = t.getArguments().get(0);
        var argPath = new TreePath(getCurrentPath(), argExpr);
        var argType = trees.getTypeMirror(argPath);
        if (argType == null || argType.getKind() == TypeKind.ERROR) {
            return super.visitMethodInvocation(t, findings);
        }

        // Box primitives for comparison
        TypeMirror boxedArg;
        if (argType.getKind().isPrimitive()) {
            boxedArg = types.boxedClass((javax.lang.model.type.PrimitiveType) argType).asType();
        } else {
            boxedArg = argType;
        }

        if (!types.isAssignable(boxedArg, types.erasure(elementType))) {
            findings.add(new Finding(getCurrentPath(), methodName, argType, elementType));
        }

        return super.visitMethodInvocation(t, findings);
    }

    /**
     * Returns the element type E of the first Collection<E> supertype found, or null if the type
     * does not implement Collection.
     */
    private TypeMirror collectionElementType(DeclaredType type) {
        // Check the type itself and all supertypes for Collection<E>
        for (TypeMirror supertype : types.directSupertypes(type)) {
            var result = collectionElementTypeOf(supertype);
            if (result != null) return result;
        }
        return collectionElementTypeOf(type);
    }

    private TypeMirror collectionElementTypeOf(TypeMirror type) {
        if (!(type instanceof DeclaredType)) return null;
        var declared = (DeclaredType) type;
        var el = declared.asElement();
        var qualifiedName = ((javax.lang.model.element.TypeElement) el).getQualifiedName().toString();
        if (qualifiedName.equals("java.util.Collection") && declared.getTypeArguments().size() == 1) {
            return declared.getTypeArguments().get(0);
        }
        if (!declared.getTypeArguments().isEmpty()) {
            for (TypeMirror supertype : types.directSupertypes(declared)) {
                var result = collectionElementTypeOf(supertype);
                if (result != null) return result;
            }
        }
        return null;
    }
}
