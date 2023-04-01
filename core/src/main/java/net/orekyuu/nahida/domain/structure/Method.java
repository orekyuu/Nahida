package net.orekyuu.nahida.domain.structure;

import lombok.Builder;

import java.util.List;

@Builder
public record Method(
        MethodSignature signature,
        Visibility visibility,
        ClassFQN returnFqn,
        List<ClassFQN> throwsFqn,
        List<Statement> statements
) {
}
