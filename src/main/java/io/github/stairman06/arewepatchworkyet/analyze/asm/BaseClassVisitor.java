package io.github.stairman06.arewepatchworkyet.analyze.asm;

import io.github.stairman06.arewepatchworkyet.analyze.Analyzer;
import io.github.stairman06.arewepatchworkyet.analyze.ClassMember;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

/**
 * This visits every class defined.
 * It writes all defined methods to {@link Analyzer#implementedClassMembers}
 */
public class BaseClassVisitor extends ClassVisitor {
    // Array containing each class that the methods defined here need to be "applied" to
    // This is because of Mixin methods, where methods will be applied to a different class then the one they're in
    private final ArrayList<String> classesToApply = new ArrayList<>();

    public BaseClassVisitor() {
        super(Opcodes.ASM9);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        // Here we handle Mixin'd classes
        if (descriptor.equals("Lorg/spongepowered/asm/mixin/Mixin;")) {
            return new MixinAnnotationVisitor(super.visitAnnotation(descriptor, visible), (extra) -> {
                classesToApply.addAll(extra);

                this.addImplementedSet();
            });
        }

        return super.visitAnnotation(descriptor, visible);
    }

    private void addImplementedSet() {
        for (String className : classesToApply) {
            Analyzer.implementedClassMembers.putIfAbsent(className, new HashSet<>());
        }
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        classesToApply.add(name);
        Analyzer.implementedClasses.add(name);
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
            Analyzer.implementedClassMembers.get(className).add(new ClassMember(ClassMember.Type.METHOD, name, descriptor, className, null));
        }

        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        for (String className : classesToApply) {
            Analyzer.implementedClassMembers.get(className).add(new ClassMember(ClassMember.Type.FIELD, name, descriptor, className, null));
        }
        return super.visitField(access, name, descriptor, signature, value);
    }
}
