package net.orekyuu.nahida;

import net.orekyuu.nahida.domain.ClassFileSource;
import net.orekyuu.nahida.domain.JarFileSource;
import net.orekyuu.nahida.domain.structure.Argument;
import net.orekyuu.nahida.domain.structure.ClassFQN;
import net.orekyuu.nahida.domain.structure.MethodSignature;
import net.orekyuu.nahida.domain.workspace.CallerGraph;
import net.orekyuu.nahida.domain.workspace.MethodCallNode;
import net.orekyuu.nahida.domain.workspace.Project;
import net.orekyuu.nahida.infrastructure.asm.AsmClassVisitor;
import org.objectweb.asm.ClassReader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        Project project = new Project();
        project.load(new ClassFileSource(Path.of("/Users/orekyuu/repos/Nahida/build/classes/java/main")));
        CallerGraph graph = project.createMethodCallGraph();
        MethodCallNode node = graph.getNode(MethodSignature.builder()
                .name("addChild")
                .classFQN(ClassFQN.fromClassFQN("net.orekyuu.nahida.domain.workspace.MethodCallNode"))
                .args(List.of(new Argument(ClassFQN.fromClassFQN("net.orekyuu.nahida.domain.workspace.MethodCallNode"))))
                .build());
        System.out.println(node);
    }
}
