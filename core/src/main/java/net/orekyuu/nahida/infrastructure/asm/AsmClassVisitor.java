package net.orekyuu.nahida.infrastructure.asm;

import lombok.extern.log4j.Log4j2;
import net.orekyuu.nahida.domain.structure.*;
import net.orekyuu.nahida.domain.structure.Class;
import org.objectweb.asm.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Log4j2
public class AsmClassVisitor extends ClassVisitor {
    Class.ClassBuilder builder;

    List<Class> results = new ArrayList<>();
    List<Method> methods;
    ClassFQN currentClass;

    public AsmClassVisitor() {
        super(Opcodes.ASM9);
    }

    Visibility toVisibility(int access) {
        if ((access & Opcodes.ACC_PUBLIC) != 0) {
            return Visibility.PUBLIC;
        }
        if ((access & Opcodes.ACC_PRIVATE) != 0) {
            return Visibility.PRIVATE;
        }
        if ((access & Opcodes.ACC_PROTECTED) != 0) {
            return Visibility.PROTECTED;
        }
        return Visibility.PACKAGE;
    }

    MethodSignature toMethodSignature(ClassFQN owner, String name, String descriptor) {
        var args = Arrays.stream(Type.getArgumentTypes(descriptor))
                .map(it -> ClassFQN.fromClassFQN(it.getClassName()))
                .map(Argument::new)
                .toList();
        return MethodSignature.builder()
                .args(args)
                .name(name)
                .classFQN(owner)
                .build();
    }
    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        if (name.equals("module-info") || name.equals("package-info")) {
            builder = null;
            super.visit(version, access, name, signature, superName, interfaces);
            return;
        }
        currentClass = ClassFQN.fromClassFQN(name);
        methods = new ArrayList<>();
        builder = Class.builder()
                .visibility(toVisibility(access))
                .fqn(currentClass)
                .superFqn(ClassFQN.fromClassFQN(superName))
                .interfaces(Arrays.stream(interfaces).map(ClassFQN::fromClassFQN).toList());
        log.debug("visit {}, {}, {}, {}, {}, {}", version, access, name, signature, superName, interfaces);
        super.visit(version, access, name, signature, superName, interfaces);
    }


    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        List<Statement> statements = new ArrayList<>();
        var methodBuilder = Method.builder()
                .statements(statements)
                .signature(toMethodSignature(currentClass, name, descriptor))
                .throwsFqn(Arrays.stream(Objects.requireNonNullElse(exceptions, new String[0])).map(ClassFQN::fromClassFQN).toList())
                .visibility(toVisibility(access));
        log.debug("visitMethod {}, {}, {}, {}, {}", access, name, descriptor, signature, exceptions);
        return new MethodVisitor(Opcodes.ASM9) {
            @Override
            public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                log.debug("visitMethodInsn {}, {}, {}, {}, {}", opcode, owner, name, descriptor, isInterface);
                ClassFQN ownerFqn = ClassFQN.fromClassFQN(owner);
                statements.add(Statement.builder()
                        .owner(ownerFqn)
                        .signature(toMethodSignature(ownerFqn, name, descriptor))
                        .build());
                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
            }
            @Override
            public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
                log.debug("visitInvokeDynamicInsn {}, {}, {}, {}", name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
                for (Object bootstrapMethodArgument : bootstrapMethodArguments) {
                    if (bootstrapMethodArgument instanceof Handle handle) {
                        if (isMethodRef(handle)) {
                            ClassFQN owner = ClassFQN.fromClassFQN(handle.getOwner());
                            statements.add(Statement.builder()
                                            .owner(owner)
                                            .signature(toMethodSignature(owner, handle.getName(), handle.getDesc()))
                                            .build());
                        }
                    }
                }
                super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
            }

            private boolean isMethodRef(Handle handle) {
                return switch (handle.getTag()) {
                    case Opcodes.H_INVOKEVIRTUAL,
                            Opcodes.H_INVOKESTATIC,
                            Opcodes.H_INVOKESPECIAL,
                            Opcodes.H_NEWINVOKESPECIAL,
                            Opcodes.H_INVOKEINTERFACE -> true;
                    default -> false;
                };
            }

            @Override
            public void visitEnd() {
                methods.add(methodBuilder.build());
                super.visitEnd();
            }
        };
    }

    @Override
    public void visitEnd() {
        if (builder == null) {
            super.visitEnd();
            return;
        }
        Class aClass = builder
                .methods(methods)
                .build();
        log.debug("visitEnd {}", aClass);
        results.add(aClass);
        super.visitEnd();
    }

    public List<Class> results() {
        return results;
    }
}
