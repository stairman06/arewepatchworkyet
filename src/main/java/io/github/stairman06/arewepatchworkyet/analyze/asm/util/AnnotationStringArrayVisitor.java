package io.github.stairman06.arewepatchworkyet.analyze.asm.util;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.function.Consumer;

public class AnnotationStringArrayVisitor extends AnnotationVisitor {
    private ArrayList<String> items = new ArrayList<>();
    private Consumer<ArrayList<String>> consumer;
    public AnnotationStringArrayVisitor(AnnotationVisitor parent, Consumer<ArrayList<String>> consumer) {
        super(Opcodes.ASM9, parent);
        this.consumer = consumer;
    }

    @Override
    public void visit(String name, Object value) {
        items.add(((Type)value).getClassName().replace('.', '/'));
        super.visit(name, value);
    }

    @Override
    public void visitEnd() {
        consumer.accept(items);
        super.visitEnd();
    }
}
