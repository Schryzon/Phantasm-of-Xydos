package Tour_Lists;

import Tour_Nodes.*;

public class Singly_Linked_List<T extends Comparable<T>>
    extends Linked_List<T> {

    public Singly_Linked_List() {
        super();
    }

    @Override
    public void add(T data) {
        Node<T> node = new Singly_Node<>(data);
        if (this.head == null) {
            this.head = node;
            this.tail = node;
        } else {
            this.tail.set_next(node);
            this.tail = node;
        }
        this.size++;
    }

    @Override
    protected Linked_List<T> create_empty_like_this() {
        return new Singly_Linked_List<T>();
    }

    @Override
    public boolean remove(T data) {
        Node<T> prev = null;
        Node<T> current = this.head;
        while (current != null) {
            if (Node.equals_safe(current.get_data(), data)) {
                if (prev == null) {
                    this.head = current.get_next();
                    if (this.head == null) {
                        this.tail = null;
                    }
                } else {
                    prev.set_next(current.get_next());
                    if (current == this.tail) {
                        this.tail = prev;
                    }
                }
                this.size--;
                return true;
            }
            prev = current;
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
