package Tour_Nodes;

public abstract class Node<T> {

    // Generic data, using getters & setters
    protected T data;

    public Node(T data) {
        this.data = data;
    }

    public T get_data() {
        return this.data;
    }

    public void set_data(T data) {
        this.data = data;
    }

    public static <T> boolean equals_safe(T a, T b) {
        return (a == null && b == null) || (a != null && a.equals(b));
    }

    public abstract Node<T> get_next();

    public abstract void set_next(Node<T> next);

    public Node<T> get_prev() {
        // If not supported, give null
        return null;
    }

    public void set_prev(Node<T> prev) {
        // No operation if not Doubly
    }
}
