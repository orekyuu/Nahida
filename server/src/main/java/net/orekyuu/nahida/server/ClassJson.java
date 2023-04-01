package net.orekyuu.nahida.server;

import net.orekyuu.nahida.domain.structure.Class;
import net.orekyuu.nahida.domain.structure.ClassFQN;

public class ClassJson {
    public String fqn;
    public String simple;
    public static ClassJson from(Class clazz) {
        return new ClassJson(clazz.fqn());
    }

    public ClassJson(ClassFQN fqn) {
        this.fqn = fqn.toString();
        this.simple = fqn.name();
    }

    public ClassJson() {
    }
}
