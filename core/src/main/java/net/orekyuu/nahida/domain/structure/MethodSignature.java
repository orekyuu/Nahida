package net.orekyuu.nahida.domain.structure;

import lombok.Builder;

import java.util.List;

@Builder
public record MethodSignature(ClassFQN classFQN, String name, List<Argument> args) {
}
