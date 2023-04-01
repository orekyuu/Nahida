package net.orekyuu.nahida.domain.workspace;

import net.orekyuu.nahida.domain.structure.MethodSignature;

import java.util.ArrayList;
import java.util.List;

public class MethodCallNode {
    final MethodSignature current;
    final List<MethodCallNode> childNodes = new ArrayList<>();
    final List<MethodCallNode> parentNodes = new ArrayList<>();

    public MethodCallNode(MethodSignature current) {
        this.current = current;
    }

    public void addChild(MethodCallNode node) {
        childNodes.add(node);
    }

    public void addParent(MethodCallNode node) {
        parentNodes.add(node);
    }

    public MethodSignature getCurrent() {
        return current;
    }

    public List<MethodCallNode> getChildNodes() {
        return childNodes;
    }

    public List<MethodCallNode> getParentNodes() {
        return parentNodes;
    }
}
