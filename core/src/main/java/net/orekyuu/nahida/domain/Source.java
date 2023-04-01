package net.orekyuu.nahida.domain;

import net.orekyuu.nahida.domain.structure.Class;

import java.io.IOException;
import java.util.List;

public interface Source {

    List<Class> load() throws IOException;
}
