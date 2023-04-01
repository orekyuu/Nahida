package net.orekyuu.nahida.domain;

import net.orekyuu.nahida.domain.structure.Class;
import net.orekyuu.nahida.infrastructure.asm.AsmClassVisitor;
import org.objectweb.asm.ClassReader;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassFileSource implements Source {

    final Path path;

    public ClassFileSource(Path path) {
        this.path = path;
    }

    @Override
    public List<Class> load() throws IOException {
        if (Files.isRegularFile(path)) {
            return load(path);
        }

        ArrayList<Class> result = new ArrayList<>();
        Files.walkFileTree(path, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                result.addAll(load(file));
                return FileVisitResult.CONTINUE;
            }
        });
        return result;
    }

    private List<Class> load(Path path) throws IOException {
        if (!path.toString().endsWith(".class")) {
            return List.of();
        }

        try(var in = Files.newInputStream(path)) {
            AsmClassVisitor visitor = new AsmClassVisitor();
            ClassReader reader = new ClassReader(in);
            reader.accept(visitor, ClassReader.SKIP_DEBUG);
            return visitor.results();
        }
    }

    private boolean isClassFile(Path p) {
        return p.endsWith(".class");
    }
}
