package net.orekyuu.nahida.server;

import net.orekyuu.nahida.domain.structure.MethodSignature;

import java.util.Objects;

public class MethodCallNodeJson {
    public MethodSignatureJson method;
    public int depth;

    public MethodCallNodeJson(MethodSignatureJson method, int depth) {
        this.method = method;
        this.depth = depth;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodCallNodeJson that = (MethodCallNodeJson) o;
        return Objects.equals(method, that.method);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method);
    }
}
