package io.github.stairman06.arewepatchworkyet.analyze;

public class ClassMember implements Comparable<ClassMember> {
    public String name;
    public String descriptor;
    public String ownerClass;
    public String caller;
    public Type type;

    public enum Type {
        METHOD,
        FIELD
    }

    public ClassMember(Type type, String name, String descriptor, String owner, String caller) {
        this.type = type;
        this.name = name;
        this.descriptor = descriptor;
        this.ownerClass = owner;
        this.caller = caller;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ClassMember) {
            return ((ClassMember) obj).name.equals(this.name) && ((ClassMember) obj).descriptor.equals(this.descriptor);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.name.hashCode() + this.descriptor.hashCode();
    }

    @Override
    public int compareTo(ClassMember o) {
        // Sorted by method name
        return this.name.compareTo(((ClassMember) o).name);
    }
}
