package io.github.stairman06.arewepatchworkyet.analyze.asm;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class BaseMethodVisitor extends MethodVisitor {
    private boolean stubbed = false;
    private final Runnable callback;

    public BaseMethodVisitor(MethodVisitor mv, Runnable callback) {
        super(Opcodes.ASM9, mv);
        this.callback = callback;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        if (descriptor.equals("Lnet/patchworkmc/annotations/Stubbed;")) {
            stubbed = true;
        }
        return super.visitAnnotation(descriptor, visible);
    }

    @Override
    public void visitEnd() {
        if (!stubbed) {
            callback.run();
        }
    }
}