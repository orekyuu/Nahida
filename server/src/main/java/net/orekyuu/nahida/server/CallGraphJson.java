package net.orekyuu.nahida.server;

import net.orekyuu.nahida.domain.structure.MethodSignature;
import net.orekyuu.nahida.domain.workspace.CallerGraph;
import net.orekyuu.nahida.domain.workspace.MethodCallNode;

import java.util.*;

public class CallGraphJson {
    public Set<MethodCallNodeJson> nodes = new HashSet<>();
    public Set<MethodCallEdgeJson> edges = new HashSet<>();
    public static CallGraphJson from(MethodCallNode node) {
        CallGraphJson json = new CallGraphJson();

        json.nodes.add(new MethodCallNodeJson(MethodSignatureJson.from(node.getCurrent()), 0));
        processParentNode(json, node, 0, new HashSet<>());
        processChildNode(json, node, 0, new HashSet<>());
        return json;
    }

    private static void processParentNode(CallGraphJson json, MethodCallNode node, int depth, Set<MethodSignature> memo) {
        if (memo.contains(node.getCurrent())) {
            return;
        }
        memo.add(node.getCurrent());
        List<MethodCallNode> parentNodes = node.getParentNodes();
        for (MethodCallNode parentNode : parentNodes) {
            json.nodes.add(new MethodCallNodeJson(MethodSignatureJson.from(parentNode.getCurrent()), depth - 1));
            json.edges.add(new MethodCallEdgeJson(
                    MethodSignatureJson.from(parentNode.getCurrent()),
                    MethodSignatureJson.from(node.getCurrent())));
            processParentNode(json, parentNode, depth - 1, memo);
        }
    }

    private static void processChildNode(CallGraphJson json, MethodCallNode node, int depth, Set<MethodSignature> memo) {
        if (memo.contains(node.getCurrent())) {
            return;
        }
        memo.add(node.getCurrent());
        List<MethodCallNode> childNodes = node.getChildNodes();
        for (MethodCallNode childNode : childNodes) {
            MethodSignature current = childNode.getCurrent();
            json.nodes.add(new MethodCallNodeJson(MethodSignatureJson.from(current), depth + 1));
            json.edges.add(new MethodCallEdgeJson(
                    MethodSignatureJson.from(node.getCurrent()),
                    MethodSignatureJson.from(childNode.getCurrent())));
            processChildNode(json, childNode, depth + 1, memo);
        }
    }
}
