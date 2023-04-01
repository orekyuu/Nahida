package net.orekyuu.nahida.server;

import java.util.Objects;

public class MethodCallEdgeJson {
    public MethodSignatureJson from;
    public MethodSignatureJson to;
    public String id;

    public MethodCallEdgeJson(MethodSignatureJson from, MethodSignatureJson to) {
        this.from = from;
        this.to = to;
        this.id = String.join("-", from.displayString, to.displayString);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodCallEdgeJson that = (MethodCallEdgeJson) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
