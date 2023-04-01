package net.orekyuu.nahida.domain;

import net.orekyuu.nahida.domain.structure.Class;
import net.orekyuu.nahida.infrastructure.asm.AsmClassVisitor;
import org.objectweb.asm.ClassReader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarFileSource implements Source {

    final Path path;

    public JarFileSource(Path path) {
        this.path = path;
    }

    @Override
    public List<Class> load() throws IOException {
        ArrayList<Class> result = new ArrayList<>();
        try (var jar = new JarFile(path.toFile())) {
            var entries = new Iterable<JarEntry>() {

                @Override
                public Iterator<JarEntry> iterator() {
                    return jar.entries().asIterator();
                }
            };
            for (JarEntry entry : entries) {
                if (!entry.getName().endsWith(".class") || entry.isDirectory()) {
                    continue;
                }

                try(var in = jar.getInputStream(entry)) {
                    AsmClassVisitor visitor = new AsmClassVisitor();
                    ClassReader reader = new ClassReader(in);
                    reader.accept(visitor, ClassReader.SKIP_DEBUG);
                    result.addAll(visitor.results());
                }
            }
        }
        return result;
    }
}
