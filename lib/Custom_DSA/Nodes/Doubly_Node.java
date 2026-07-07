package Tour_Nodes;

public class Doubly_Node<T> extends Node<T> {

    private Node<T> next;
    private Node<T> prev;

    public Doubly_Node(T data) {
        super(data);
        this.next = null;
        this.prev = null;
    }

    @Override
    public Node<T> get_next() {
        return this.next;
    }

    @Override
    public void set_next(Node<T> next) {
        this.next = next;
    }

    @Override
    public Node<T> get_prev() {
        return this.prev;
    }

    @Override
    public void set_prev(Node<T> prev) {
        this.prev = prev;
    }
}
