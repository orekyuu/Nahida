package net.orekyuu.nahida.server;

import net.orekyuu.nahida.domain.structure.Argument;
import net.orekyuu.nahida.domain.structure.ClassFQN;
import net.orekyuu.nahida.domain.structure.MethodSignature;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MethodSignatureJson {
    public ClassJson clazz;
    public String name;
    public List<ClassJson> args;
    public String displayString;

    public String simpleName;

    public static MethodSignatureJson from(MethodSignature signature) {
        MethodSignatureJson json = new MethodSignatureJson();
        json.clazz = new ClassJson(signature.classFQN());
        json.name = signature.name();
        json.args = signature.args().stream().map(Argument::fqn).map(ClassJson::new).toList();
        json.displayString = json.clazz.fqn + "#" + json.name + "(" + json.args.stream().map(it -> it.fqn).collect(Collectors.joining(",")) + ")";
        json.simpleName = json.clazz.simple + "#" + json.name + "(" + json.args.stream().map(it -> it.simple).collect(Collectors.joining(",")) + ")";
        return json;
    }

    public MethodSignature toMethodSignature() {
        return MethodSignature.builder()
                .classFQN(ClassFQN.fromClassFQN(clazz.fqn))
                .name(name)
                .args(args.stream().map(it -> ClassFQN.fromClassFQN(it.fqn)).map(Argument::new).toList())
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodSignatureJson that = (MethodSignatureJson) o;
        return Objects.equals(displayString, that.displayString);
    }

    @Override
    public int hashCode() {
        return Objects.hash(displayString);
    }
}
