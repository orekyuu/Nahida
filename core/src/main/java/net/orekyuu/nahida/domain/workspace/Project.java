package net.orekyuu.nahida.domain.workspace;

import lombok.extern.log4j.Log4j2;
import net.orekyuu.nahida.domain.Source;
import net.orekyuu.nahida.domain.structure.Class;
import net.orekyuu.nahida.domain.structure.ClassFQN;
import net.orekyuu.nahida.domain.structure.Method;
import net.orekyuu.nahida.domain.structure.MethodSignature;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Log4j2
public class Project {
    Map<ClassFQN, Class> loadedClasses = new HashMap<>();

    public void load(Source source) throws IOException {
        for (Class clazz : source.load()) {
            registerClass(clazz);
        }
    }

    private void registerClass(Class clazz) {
        loadedClasses.put(clazz.fqn(), clazz);
        log.info("register {}", clazz.fqn());
    }

    public List<Class> findSubclasses(ClassFQN classFQN) {
        return loadedClasses.values().stream()
                .filter(it -> it.isInstanceof(classFQN, this))
                .filter(it -> !it.fqn().equals(classFQN))
                .toList();
    }

    public List<Class> allClass() {
        return loadedClasses.values().stream().toList();
    }

    public Optional<Class> findClass(ClassFQN clazz) {
        return Optional.ofNullable(loadedClasses.get(clazz));
    }

    public Optional<Method> findMethod(MethodSignature methodSignature) {
        return findClass(methodSignature.classFQN())
                .flatMap(clazz -> clazz.methods().stream()
                    .filter(it -> it.signature().equals(methodSignature))
                    .findFirst());
    }

    public CallerGraph createMethodCallGraph() {
        CallerGraph graph = new CallerGraph();
        loadedClasses.values().stream()
                .flatMap(clazz -> clazz.methods().stream())
                .forEach(m -> graph.addMethod(this, m));
        return graph;
    }
}
