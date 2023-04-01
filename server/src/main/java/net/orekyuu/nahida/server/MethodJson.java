package net.orekyuu.nahida.server;

import net.orekyuu.nahida.domain.structure.Argument;
import net.orekyuu.nahida.domain.structure.ClassFQN;
import net.orekyuu.nahida.domain.structure.Method;
import net.orekyuu.nahida.domain.structure.MethodSignature;

import java.util.List;

public class MethodJson {
    public String name;
    public String visibility;

    public List<String> args;
    public MethodSignatureJson signature;

    public static MethodJson from(Method method) {
        MethodJson json = new MethodJson();
        MethodSignature signature = method.signature();
        json.name = signature.name();
        json.visibility = method.visibility().name();
        json.args = signature.args().stream().map(Argument::fqn).map(ClassFQN::toString).toList();
        json.signature = MethodSignatureJson.from(signature);
        return json;
    }
}
