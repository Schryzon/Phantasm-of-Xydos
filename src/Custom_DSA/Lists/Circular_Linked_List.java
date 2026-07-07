package Custom_DSA.Lists;

import Custom_DSA.Nodes.*;

public class Circular_Linked_List<T extends Comparable<T>>
    extends Linked_List<T> {

    public Circular_Linked_List() {
        super();
    }

    @Override
    public void add(T data) {
        Node<T> node = new Singly_Node<>(data);
        if (this.head == null) {
            this.head = node;
            this.tail = node;
            this.tail.set_next(this.head); // Circular link
        } else {
            this.tail.set_next(node);
            this.tail = node;
            this.tail.set_next(this.head); // Keep circular
        }
        this.size++;
    }

    @Override
    protected Linked_List<T> create_empty_like_this() {
        return new Circular_Linked_List<T>();
    }

    @Override
    public boolean remove(T data) {
        if (this.head == null) {
            return false;
        }
        Node<T> current = this.head;
        Node<T> prev = this.tail;
        int iter_count = 0;
        while (iter_count < this.size) {
            if (Node.equals_safe(current.get_data(), data)) {
                if (current == this.head) {
                    this.head = this.head.get_next();
                    this.tail.set_next(this.head);
                    if (this.size == 1) {
                        this.head = null;
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
            iter_count++;
        }
        return false;
    }

    @Override
    public Node<T> find(T data) {
        if (this.head == null) {
            return null;
        }
        Node<T> current = this.head;
        int iter_count = 0;
        while (iter_count < this.size) {
            if (Node.equals_safe(current.get_data(), data)) {
                return current;
            }
            current = current.get_next();
            iter_count++;
        }
        return null;
    }

    @Override
    public void for_each(Visitor<T> visitor) {
        Node<T> current = this.head;
        int iter_count = 0;
        while (current != null && iter_count < this.size) {
            visitor.visit(current.get_data());
            current = current.get_next();
            iter_count++;
        }
    }

    @Override
    public void sort(Sort_Type type) {
        if (this.tail != null) this.tail.set_next(null); // break ring
        super.sort(type);
        if (this.tail != null) this.tail.set_next(this.head); // reconnect
    }
}
