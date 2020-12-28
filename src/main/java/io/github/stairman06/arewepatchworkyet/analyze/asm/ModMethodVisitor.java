package io.github.stairman06.arewepatchworkyet.analyze.asm;

import io.github.stairman06.arewepatchworkyet.analyze.Analyzer;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ModMethodVisitor extends MethodVisitor {
    public ModMethodVisitor(MethodVisitor mv) {
        super(Opcodes.ASM9, mv);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        Analyzer.analyzeMethodName(owner, name, descriptor);
    }
}
