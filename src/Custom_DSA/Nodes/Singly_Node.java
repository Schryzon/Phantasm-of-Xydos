package Custom_DSA.Nodes;

public class Singly_Node<T> extends Node<T> {

    private Node<T> next;

    public Singly_Node(T data) {
        super(data);
        this.next = null;
    }

    @Override
    public Node<T> get_next() {
        return this.next;
    }

    @Override
    public void set_next(Node<T> next) {
        this.next = next;
    }
}
