package io.github.stairman06.arewepatchworkyet.analyze;

public class Method implements Comparable<Method> {
    public String name;
    public String descriptor;
    public String ownerClass;

    public Method(String name, String descriptor, String owner) {
        this.name = name;
        this.descriptor = descriptor;
        this.ownerClass = owner;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Method) {
            return ((Method) obj).name.equals(this.name) && ((Method) obj).descriptor.equals(this.descriptor);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.name.hashCode() + this.descriptor.hashCode();
    }

    @Override
    public int compareTo(Method o) {
        // Sorted by method name
        return this.name.compareTo(((Method) o).name);
    }
}
