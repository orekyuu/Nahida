package net.orekyuu.nahida.domain.structure;

import javax.annotation.CheckForNull;

public record ClassFQN(@CheckForNull String packageName, String name) {

    public boolean isPrimitive() {
        return packageName == null;
    }

    @Override
    public String toString() {
        if (packageName != null) {
            return packageName + "." + name;
        }
        return name;
    }

    public static ClassFQN fromClassFQN(String fqn) {
        String replace = fqn.replace('/', '.');
        int index = replace.lastIndexOf(".");
        if (0 > index) {
            return new ClassFQN(null, fqn);
        }
        return new ClassFQN(replace.substring(0, index), replace.substring(index + 1));
    }
}
