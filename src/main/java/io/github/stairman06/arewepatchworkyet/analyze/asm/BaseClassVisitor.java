package io.github.stairman06.arewepatchworkyet.analyze.asm;

import io.github.stairman06.arewepatchworkyet.AreWePatchworkYet;
import io.github.stairman06.arewepatchworkyet.analyze.Analyzer;
import io.github.stairman06.arewepatchworkyet.analyze.Method;
import io.github.stairman06.arewepatchworkyet.analyze.asm.util.AnnotationStringArrayVisitor;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.function.Consumer;

/**
 * This visits every class defined.
 * It writes all defined methods to {@link Analyzer#implementedMethods}
 */
public class BaseClassVisitor extends ClassVisitor {
    private String name;

    // Array containing each class that the methods defined here need to be "applied" to
    // This is because of Mixin methods, where methods will be applied to a different class then the one they're in
    private ArrayList<String> classesToApply = new ArrayList<>();

    private ArrayList<String> extraClassesToApplyTo = new ArrayList<>();
    public BaseClassVisitor() {
        super(Opcodes.ASM9);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        // Here we handle mixin'd classes
        if(descriptor.equals("Lorg/spongepowered/asm/mixin/Mixin;")) {
            return new MixinAnnotationVisitor(super.visitAnnotation(descriptor, visible), (extra) -> {
                extraClassesToApplyTo = extra;

                HashSet<String> thisSuperCache = Analyzer.superCache.getOrDefault(this.name, new HashSet<>());

                // evil nested for
                for (String extraClass : extraClassesToApplyTo) {
                    HashSet<String> extraSuperCache = Analyzer.superCache.getOrDefault(extraClass, new HashSet<>());
                    for (String superClass : thisSuperCache) {
                        extraSuperCache.add(superClass);
                    }

                    Analyzer.superCache.put(extraClass, extraSuperCache);
                }
            });
        }

        return super.visitAnnotation(descriptor, visible);
    }

    private void addImplementedSet() {
        for (String className : classesToApply) {
            Analyzer.implementedMethods.putIfAbsent(className, new HashSet<>());
        }
    }

    private static class MixinAnnotationVisitor extends AnnotationVisitor {
        private ArrayList<String> mixinedClasses = new ArrayList<>();
        private Consumer<ArrayList<String>> consumer;
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

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        classesToApply.add(name);
        this.name = name;
        this.addImplementedSet();


        HashSet<String> superSet = Analyzer.superCache.getOrDefault(name, new HashSet<>());
        superSet.add(superName);
        superSet.addAll(Arrays.asList(interfaces));

        Analyzer.superCache.put(name, superSet);
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        for (String className : classesToApply) {
            Analyzer.implementedMethods.get(className).add(new Method(name,descriptor));
        }

        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }
}
