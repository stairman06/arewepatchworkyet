package io.github.stairman06.arewepatchworkyet.ui;

public class ResultListItem {
    private final Object object;
    private final Type type;
    public enum Type {
        CLASS,
        METHOD
    }

    public ResultListItem(Type type, Object object) {
        this.type = type;
        this.object = object;
    }

    public Type getType() {
        return type;
    }

    public Object getObject() {
        return object;
    }
}
