package io.github.stairman06.arewepatchworkyet.analyze.asm;

import io.github.stairman06.arewepatchworkyet.analyze.asm.util.AnnotationStringArrayVisitor;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * An {@link AnnotationVisitor} that visits Mixin annotations.
 */
class MixinAnnotationVisitor extends AnnotationVisitor {
    private ArrayList<String> mixinedClasses = new ArrayList<>();
    private final Consumer<ArrayList<String>> consumer;

    /**
     * Default constructor for MixinAnnotationVisitor
     * @param parent Parent annotation
     * @param consumer This consumer is called after the annotations are visited.
     */
    public MixinAnnotationVisitor(AnnotationVisitor parent, Consumer<ArrayList<String>> consumer) {
        super(Opcodes.ASM9, parent);
        this.consumer = consumer;
    }

    @Override
    public AnnotationVisitor visitArray(String name) {
        return new AnnotationStringArrayVisitor(super.visitArray(name), (items) -> {
            this.mixinedClasses = items;
        });
    }

    @Override
    public void visitEnd() {
        this.consumer.accept(this.mixinedClasses);
        super.visitEnd();
    }
}
