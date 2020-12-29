package io.github.stairman06.arewepatchworkyet.analyze.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Visits all classes defined in a mod.
 */
public class ModClassVisitor extends ClassVisitor {
    private String name;

    public ModClassVisitor() {
        super(Opcodes.ASM9);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.name = name;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        return new ModMethodVisitor(super.visitMethod(access, name, descriptor, signature, exceptions), this.name);
    }


}
