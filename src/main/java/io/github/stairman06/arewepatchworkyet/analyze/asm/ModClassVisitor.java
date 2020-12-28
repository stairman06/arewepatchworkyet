package io.github.stairman06.arewepatchworkyet.analyze.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Visits all classes defined in a mod.
 */
public class ModClassVisitor extends ClassVisitor {
    public ModClassVisitor() {
        super(Opcodes.ASM9);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        return new ModMethodVisitor(super.visitMethod(access, name, descriptor, signature, exceptions));
    }
}
