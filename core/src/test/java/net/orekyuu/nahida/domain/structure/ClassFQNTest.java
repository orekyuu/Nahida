package net.orekyuu.nahida.domain.structure;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ClassFQNTest {

    @Nested
    class FromClassFQNTest {

        @Test
        void testSlash() {
            assertThat(ClassFQN.fromClassFQN("java/time/LocalDateTime"))
                    .isEqualTo(new ClassFQN("java.time", "LocalDateTime"));
        }

        @Test
        void testDot() {
            assertThat(ClassFQN.fromClassFQN("java.time.LocalDateTime"))
                    .isEqualTo(new ClassFQN("java.time", "LocalDateTime"));
        }

        @Test
        void testPrimitive() {
            assertThat(ClassFQN.fromClassFQN("int"))
                    .isEqualTo(new ClassFQN(null, "int"));
        }
    }
}