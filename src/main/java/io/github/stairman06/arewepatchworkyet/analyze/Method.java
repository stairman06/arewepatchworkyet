package io.github.stairman06.arewepatchworkyet.analyze;

public class Method implements Comparable {
    public String name;
    public String descriptor;
    public Method(String name, String descriptor) {
        this.name = name;
        this.descriptor = descriptor;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Method) {
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
    public int compareTo(Object o) {
        if(o instanceof Method) {
            return this.name.compareTo(((Method) o).name);
        }

        return 0;
    }
}
