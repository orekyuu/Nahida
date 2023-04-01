package net.orekyuu.nahida.domain.workspace;

import net.orekyuu.nahida.domain.structure.Class;
import net.orekyuu.nahida.domain.structure.ClassFQN;
import net.orekyuu.nahida.domain.structure.Method;
import net.orekyuu.nahida.domain.structure.MethodSignature;
import net.orekyuu.nahida.domain.structure.Statement;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CallerGraph {
    private final Map<MethodSignature, MethodCallNode> nodes = new HashMap<>();

    public void addMethod(Project project, Method method) {
        MethodSignature key = method.signature();
        MethodCallNode currentNode = getNode(key);
        for (Statement statement : method.statements()) {
            MethodCallNode callNode = getNode(statement.signature());
            // 呼び出し先のノードをつなぐ
            currentNode.addChild(callNode);
            // 呼び出し元のノードをつなぐ
            callNode.addParent(currentNode);

            // 子供がいる場合は実装まで追いかける
            ClassFQN clazz = statement.signature().classFQN();
            if (Objects.equals(clazz.packageName(), "java.lang")) {
                continue;
            }
            for (Class subclass : project.findSubclasses(clazz)) {
                for (Method m : subclass.methods()) {
                    // オーバーライドしている場合はつなぐ
                    MethodSignature callSignature = statement.signature();
                    MethodSignature subclassSignature = m.signature();
                    if (subclassSignature.name().equals(callSignature.name()) && subclassSignature.args().equals(callSignature.args())) {
                        // 呼び出し元 -> サブクラスのメソッド
                        getNode(callSignature).addChild(getNode(subclassSignature));
                        // 呼び出し元 <- サブクラスのメソッド
                        getNode(subclassSignature).addParent(getNode(callSignature));
                    }
                }
            }
        }
    }

    public MethodCallNode getNode(MethodSignature sig) {
        return nodes.computeIfAbsent(sig, MethodCallNode::new);
    }
}
