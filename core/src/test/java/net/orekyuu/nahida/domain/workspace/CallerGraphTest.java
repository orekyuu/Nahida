package net.orekyuu.nahida.domain.workspace;

import net.orekyuu.nahida.domain.ClassFileSource;
import net.orekyuu.nahida.domain.structure.Argument;
import net.orekyuu.nahida.domain.structure.ClassFQN;
import net.orekyuu.nahida.domain.structure.MethodSignature;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class CallerGraphTest {

    @Test
    void testParent() throws IOException {
        Project project = new Project();
        project.load(new ClassFileSource(Path.of("/Users/orekyuu/repos/Nahida/build/classes/java/main")));
        CallerGraph graph = project.createMethodCallGraph();

        MethodCallNode node = graph.getNode(MethodSignature.builder()
                .name("load")
                .classFQN(ClassFQN.fromClassFQN("net.orekyuu.nahida.domain.Source"))
                .args(List.of())
                .build());


        assertThat(node.getParentNodes())
                .extracting(MethodCallNode::getCurrent)
                .contains(MethodSignature.builder()
                        .classFQN(ClassFQN.fromClassFQN("net.orekyuu.nahida.domain.structure.Project"))
                        .name("load")
                        .args(List.of(new Argument(ClassFQN.fromClassFQN("net.orekyuu.nahida.domain.Source"))))
                        .build());
    }

}