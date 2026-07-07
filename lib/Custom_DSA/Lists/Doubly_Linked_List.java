package Custom_DSA.Lists;

import Custom_DSA.Nodes.*;

public class Doubly_Linked_List<T extends Comparable<T>>
    extends Linked_List<T> {

    public Doubly_Linked_List() {
        super();
    }

    @Override
    public void add(T data) {
        Doubly_Node<T> node = new Doubly_Node<>(data);
        if (this.head == null) {
            this.head = node;
            this.tail = node;
        } else {
            this.tail.set_next(node);
            node.set_prev(this.tail);
            this.tail = node;
        }
        this.size++;
    }

    @Override
    protected Linked_List<T> create_empty_like_this() {
        return new Doubly_Linked_List<T>();
    }

    @Override
    public boolean remove(T data) {
        Node<T> current = this.head;
        while (current != null) {
            if (Node.equals_safe(current.get_data(), data)) {
                Node<T> prev = current.get_prev();
                Node<T> next = current.get_next();
                if (prev != null) {
                    prev.set_next(next);
                } else {
                    this.head = next;
                }

                if (next != null) {
                    next.set_prev(prev);
                } else {
                    this.tail = prev;
                }
                this.size--;
                return true;
            }
            current = current.get_next();
        }
        return false;
    }

    @Override
    public Node<T> find(T data) {
        Node<T> current = this.head;
        while (current != null) {
            if (Node.equals_safe(current.get_data(), data)) {
                return current;
            }
            current = current.get_next();
        }
        return null;
    }
}
