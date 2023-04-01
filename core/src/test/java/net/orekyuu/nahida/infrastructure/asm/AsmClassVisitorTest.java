package net.orekyuu.nahida.infrastructure.asm;

import net.orekyuu.nahida.domain.structure.*;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;

import java.io.IOException;
import java.lang.Class;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AsmClassVisitorTest {

    AsmClassVisitor visitor = new AsmClassVisitor();
    ClassReader reader;

    void setup(Class<?> clazz) throws IOException {
        visitor = new AsmClassVisitor();
        reader = new ClassReader(clazz.getCanonicalName());
        reader.accept(visitor, ClassReader.SKIP_DEBUG);
    }

    @Test
    void testLocalDateTime() throws IOException {
        setup(LocalDateTime.class);
        assertThat(visitor.results())
                .hasSize(1)
                .first().satisfies(clazz -> {
                    assertThat(clazz.fqn()).isEqualTo(new ClassFQN("java.time", "LocalDateTime"));
                    List<Method> methods = clazz.findMethodByName("plusWithOverflow");
                    assertThat(methods).hasSize(1);
                    List<Statement> statements = methods.get(0).statements();
                    matchSignature(statements.get(0), ClassFQN.fromClassFQN("java.time.LocalDateTime"), "with", ClassFQN.fromClassFQN("java.time.LocalDate"), ClassFQN.fromClassFQN("java.time.LocalTime"));
                    matchSignature(statements.get(1), ClassFQN.fromClassFQN("java.time.LocalTime"), "toNanoOfDay");
                    matchSignature(statements.get(2), ClassFQN.fromClassFQN("java.lang.Math"), "floorDiv", ClassFQN.fromClassFQN("long"), ClassFQN.fromClassFQN("long"));
                    matchSignature(statements.get(3), ClassFQN.fromClassFQN("java.lang.Math"), "floorMod", ClassFQN.fromClassFQN("long"), ClassFQN.fromClassFQN("long"));
                    matchSignature(statements.get(4), ClassFQN.fromClassFQN("java.time.LocalTime"), "ofNanoOfDay", ClassFQN.fromClassFQN("long"));
                    matchSignature(statements.get(5), ClassFQN.fromClassFQN("java.time.LocalDate"), "plusDays", ClassFQN.fromClassFQN("long"));
                    matchSignature(statements.get(6), ClassFQN.fromClassFQN("java.time.LocalDateTime"), "with", ClassFQN.fromClassFQN("java.time.LocalDate"), ClassFQN.fromClassFQN("java.time.LocalTime"));
                });
    }

    private void matchSignature(Statement statement, ClassFQN owner, String name, ClassFQN... args) {
        var expect = Statement.builder()
                .owner(owner)
                .signature(MethodSignature.builder()
                        .classFQN(owner)
                        .name(name)
                        .args(Arrays.stream(args).map(Argument::new).toList())
                        .build())
                .build();

        assertThat(statement).isEqualTo(expect);
    }
}