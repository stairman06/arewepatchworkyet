package io.github.stairman06.arewepatchworkyet.mappings;

import io.github.stairman06.arewepatchworkyet.MappingUtils;
import org.objectweb.asm.commons.Remapper;

public class AsmRemapper extends Remapper {
    @Override
    public String map(String internalName) {
        return MappingUtils.getYarnClassName(internalName);
    }
}
