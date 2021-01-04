package io.github.stairman06.arewepatchworkyet.analyze.asm.util;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * Visits String arrays in Annotations
 */
public class AnnotationStringArrayVisitor extends AnnotationVisitor {
    private final ArrayList<String> items = new ArrayList<>();
    private final Consumer<ArrayList<String>> consumer;

    public AnnotationStringArrayVisitor(AnnotationVisitor parent, Consumer<ArrayList<String>> consumer) {
        super(Opcodes.ASM9, parent);
        this.consumer = consumer;
    }

    @Override
    public void visit(String name, Object value) {
        try {
            items.add(((Type) value).getClassName().replace('.', '/'));
        } catch (Exception e) {
            // Cannot be casted to Type for an unknown reason
        }
        super.visit(name, value);
    }

    @Override
    public void visitEnd() {
        consumer.accept(items);
        super.visitEnd();
    }
}
