package net.orekyuu.nahida.domain.structure;

import lombok.Builder;

@Builder
public record Statement(ClassFQN owner, MethodSignature signature) {
}
