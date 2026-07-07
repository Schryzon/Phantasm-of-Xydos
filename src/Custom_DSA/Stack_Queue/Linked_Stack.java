package Custom_DSA.Stack_Queue;

import Custom_DSA.Lists.*;
import Custom_DSA.Nodes.*;

/**
 * Linked stack (LIFO) implemented on top of previous Linked_List ADT.
 * Push/pop/peek are O(1). Inherits everything from Linked_List.
 */
public class Linked_Stack<T extends Comparable<T>> extends Linked_List<T> {

    public Linked_Stack() {
        super();
    }

    /**
     * Add behaves as push: insert at head (LIFO).
     */
    @Override
    public void add(T data) {
        Node<T> node = new Singly_Node<>(data);
        if (this.head == null) {
            this.head = node;
            this.tail = node;
        } else {
            node.set_next(this.head);
            this.head = node;
        }
        this.size++;
    }

    /**
     * Pop removes and returns the top element (head).
     * Returns null if empty.
     */
    public T pop() {
        if (this.head == null) return null;
        Node<T> node = this.head;
        this.head = this.head.get_next();
        if (this.head == null) this.tail = null;
        this.size--;
        return node.get_data();
    }

    /**
     * Peek returns the top element without removing it.
     */
    public T peek() {
        return this.head == null ? null : this.head.get_data();
    }

    /**
     * Remove by first occurrence (same logic as singly list).
     * Don't think I'll be using this anytime soon, though.
     * Still gotta include 'cause ADT.
     */
    @Override
    public boolean remove(T data) {
        Node<T> prev = null;
        Node<T> current = this.head;
        while (current != null) {
            if (Node.equals_safe(current.get_data(), data)) {
                if (prev == null) {
                    this.head = current.get_next();
                    if (this.head == null) this.tail = null;
                } else {
                    prev.set_next(current.get_next());
                    if (current == this.tail) this.tail = prev;
                }
                this.size--;
                return true;
            }
            prev = current;
            current = current.get_next();
        }
        return false;
    }

    /**
     * Find first equal (same as singly).
     * Don't think I'll be using this anytime soon, though.
     * Still gotta include 'cause ADT.
     */
    @Override
    public Node<T> find(T data) {
        Node<T> current = this.head;
        while (current != null) {
            if (Node.equals_safe(current.get_data(), data)) return current;
            current = current.get_next();
        }
        return null;
    }

    @Override
    protected Linked_List<T> create_empty_like_this() {
        return new Linked_Stack<T>();
    }
}
