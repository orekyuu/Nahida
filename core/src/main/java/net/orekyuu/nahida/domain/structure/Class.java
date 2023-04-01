package net.orekyuu.nahida.domain.structure;

import lombok.Builder;
import net.orekyuu.nahida.domain.workspace.Project;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Builder
public record Class(
        Visibility visibility,
        ClassFQN fqn,
        ClassFQN superFqn,
        List<ClassFQN> interfaces,
        List<Method> methods) {

    public List<Method> findMethodByName(String methodName) {
        return methods.stream()
                .filter(m -> m.signature().name().equals(methodName))
                .toList();
    }

    public boolean isInstanceof(ClassFQN fqn, Project project) {
        if (fqn().equals(fqn) || superFqn.equals(fqn) || interfaces.stream().anyMatch(it -> it.equals(fqn))) {
            return true;
        }
        if (project.findClass(superFqn).map(clazz -> clazz.isInstanceof(fqn, project)).orElse(false)) {
            return true;
        }
        return interfaces.stream()
                .map(i -> project.findClass(i).orElse(null))
                .filter(Objects::nonNull)
                .anyMatch(c -> c.isInstanceof(fqn, project));
    }
}
