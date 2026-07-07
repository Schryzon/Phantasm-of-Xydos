package Custom_DSA.Lists;

import Custom_DSA.Nodes.*;

public abstract class Linked_List<T extends Comparable<T>> {

    public Node<T> head;
    public Node<T> tail;
    public int size;

    // Enum the sort type
    public enum Sort_Type {
        BUBBLE,
        MERGE,
        SELECTION,
        INSERTION,
        QUICK_RELINK,
        QUICK_SWAP,
    }

    public enum Search_Type {
        LINEAR,
        BINARY,
        BOUNDARY_LOW,
        BOUNDARY_HIGH,
    }

    // Algorithm measurement
    public static class Metrics {

        public long comparisons; // a vs b compares
        public long swaps; // data swaps (bubble/selection)
        public long relinks; // pointer moves (insertion/quick relink)
        public long recursive_calls; // merge/quick recursion depth counter

        public void reset() {
            comparisons = swaps = relinks = recursive_calls = 0;
        }
    }

    protected final Metrics metrics = new Metrics();

    public void reset_metrics() {
        metrics.reset();
    }

    public Metrics get_metrics() {
        return metrics;
    }

    public Linked_List() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    // Can be append or push
    public abstract void add(T data);

    // Remove by first occurence
    public abstract boolean remove(T data);

    // Find first equal
    public abstract Node<T> find(T data);

    public int get_size() {
        return this.size;
    }

    public boolean is_empty() {
        return this.size == 0;
    }

    // Iterate and apply a visitor lambda
    public void for_each(Visitor<T> visitor) {
        Node<T> current = this.head;
        int iter_count = 0;
        while (current != null && iter_count < this.size) {
            visitor.visit(current.get_data());
            current = current.get_next();
            iter_count++;
        }
    }

    public Node<T> get_head() {
        return this.head;
    }

    public interface Visitor<T> {
        void visit(T item);
    }

    protected abstract Linked_List<T> create_empty_like_this();

    public Node<T> find_object(T data) {
        Node<T> current = this.head;
        while (current != null) {
            if (data.compareTo(current.get_data()) == 0) {
                return current;
            }
            current = current.get_next();
        }
        return null;
    }

    public Node<T> node_at(int index) {
        if (index < 0 || index >= this.size) {
            throw new IndexOutOfBoundsException(
                "Index: " + index + ", Size: " + this.size
            );
        }
        Node<T> current = this.head;
        for (int i = 0; i < index; i++) {
            current = current.get_next();
        }
        return current;
    }

    // SORTING ALGORITHMSSSSSSSSSS
    public void sort(Sort_Type type) {
        switch (type) {
            case BUBBLE:
                bubble_sort(null);
                break;
            case SELECTION:
                selection_sort(null);
                break;
            case INSERTION:
                insertion_sort_relink(null);
                break;
            case QUICK_RELINK:
                quick_sort_relink(null);
                break;
            case QUICK_SWAP:
                quick_sort_swap(null);
                break;
            case MERGE: // Let fall-through
            default:
                // default to merge O(n log n)
                this.head = merge_sort(this.head, null);
                recompute_tail();
                break;
        }
    }

    public void sort(Sort_Type type, Compare<T> cmp) {
        switch (type) {
            case BUBBLE:
                bubble_sort(cmp);
                break;
            case SELECTION:
                selection_sort(cmp);
                break;
            case INSERTION:
                insertion_sort_relink(cmp);
                break;
            case QUICK_RELINK:
                quick_sort_relink(cmp);
                break;
            case QUICK_SWAP:
                quick_sort_swap(cmp);
                break;
            case MERGE:
            default:
                this.head = merge_sort(this.head, cmp);
                recompute_tail();
                break;
        }
    }

    private void bubble_sort(Compare<T> cmp) {
        if (this.head == null || this.size <= 1) return;
        boolean swapped;
        Node<T> current,
            last = null;
        do {
            swapped = false;
            current = this.head;
            while (current.get_next() != last) {
                Node<T> next = current.get_next();
                if (cmp(cmp, current.get_data(), next.get_data()) > 0) {
                    T tmp = current.get_data();
                    current.set_data(next.get_data());
                    next.set_data(tmp);
                    metrics.swaps++;
                    swapped = true;
                }
                current = current.get_next();
            }
            last = current;
        } while (swapped);
    }

    private void selection_sort(Compare<T> cmp) {
        if (this.head == null || this.head.get_next() == null) return;
        for (Node<T> i = this.head; i != null; i = i.get_next()) {
            Node<T> min = i;
            for (Node<T> j = i.get_next(); j != null; j = j.get_next()) {
                if (cmp(cmp, j.get_data(), min.get_data()) < 0) {
                    min = j;
                }
            }
            if (min != i) {
                T tmp = i.get_data();
                i.set_data(min.get_data());
                min.set_data(tmp);
                metrics.swaps++;
            }
        }
    }

    private void insertion_sort_relink(Compare<T> cmp) {
        if (this.head == null || this.head.get_next() == null) return;
        Node<T> unsorted = this.head,
            sorted_head = null;
        while (unsorted != null) {
            Node<T> next = unsorted.get_next();
            if (
                sorted_head == null ||
                cmp(cmp, unsorted.get_data(), sorted_head.get_data()) <= 0
            ) {
                unsorted.set_next(sorted_head);
                sorted_head = unsorted;
                metrics.relinks++;
            } else {
                Node<T> cur = sorted_head;
                while (
                    cur.get_next() != null &&
                    cmp(cmp, cur.get_next().get_data(), unsorted.get_data()) < 0
                ) {
                    cur = cur.get_next();
                }
                unsorted.set_next(cur.get_next());
                cur.set_next(unsorted);
                metrics.relinks++;
            }
            unsorted = next;
        }
        this.head = sorted_head;
        recompute_tail();
    }

    private void quick_sort_relink(Compare<T> cmp) {
        if (this.head == null || this.head.get_next() == null) return;
        this.head = quick_sort_relink_rec(this.head, cmp);
        recompute_tail();
    }

    private Node<T> quick_sort_relink_rec(Node<T> head, Compare<T> cmp) {
        metrics.recursive_calls++;
        if (head == null || head.get_next() == null) return head;

        T pivot = head.get_data();
        Node<T> less_h = null,
            less_t = null;
        Node<T> eq_h = null,
            eq_t = null;
        Node<T> gr_h = null,
            gr_t = null;

        for (Node<T> cur = head; cur != null; ) {
            Node<T> nxt = cur.get_next();
            cur.set_next(null);
            int c = cmp(cmp, cur.get_data(), pivot);
            if (c < 0) {
                if (less_h == null) less_h = less_t = cur;
                else {
                    less_t.set_next(cur);
                    less_t = cur;
                    metrics.relinks++;
                }
            } else if (c == 0) {
                if (eq_h == null) eq_h = eq_t = cur;
                else {
                    eq_t.set_next(cur);
                    eq_t = cur;
                }
            } else {
                if (gr_h == null) gr_h = gr_t = cur;
                else {
                    gr_t.set_next(cur);
                    gr_t = cur;
                }
            }
            cur = nxt;
        }

        less_h = quick_sort_relink_rec(less_h, cmp);
        gr_h = quick_sort_relink_rec(gr_h, cmp);
        return concat_three(less_h, eq_h, gr_h);
    }

    /* Quick sort by swapping DATA only (no relinking). */
    private void quick_sort_swap(Compare<T> cmp) {
        if (this.head == null || this.head.get_next() == null) return;
        quick_sort_swap_rec(this.head, this.tail, cmp);
        // sizes/ tail unchanged by data-swaps; no recompute needed.
    }

    /** Recursive quicksort on inclusive window [start, end]. Uses data swaps only. */
    private void quick_sort_swap_rec(
        Node<T> start,
        Node<T> end,
        Compare<T> cmp
    ) {
        metrics.recursive_calls++;
        if (start == null || end == null || start == end) return;
        if (!segment_valid(start, end)) return; // protects against bad bounds

        Part<T> part = partition_swap(start, end, cmp);
        Node<T> left_end = part.pivot_prev;
        Node<T> right_start = part.pivot_next;

        if (left_end != null && start != left_end.get_next()) {
            quick_sort_swap_rec(start, left_end, cmp);
        }
        if (right_start != null && right_start != end) {
            quick_sort_swap_rec(right_start, end, cmp);
        }
    }

    // Return pivot neighborhood so we can recurse
    private Part<T> partition_swap(Node<T> start, Node<T> end, Compare<T> cmp) {
        T pivot_val = end.get_data();

        Node<T> i_prev = null; // last node of the <= region
        Node<T> i = start; // first node of the >  region
        Node<T> j = start; // scanner

        while (j != end) {
            if (cmp(cmp, j.get_data(), pivot_val) <= 0) {
                // swap data of i and j
                T tmp = i.get_data();
                i.set_data(j.get_data());
                j.set_data(tmp);
                metrics.swaps++;

                i_prev = i;
                if (i != end) i = i.get_next(); // do NOT step past end
            }
            j = j.get_next();
        }

        T tmp = i.get_data(); // i is never null here
        i.set_data(end.get_data());
        end.set_data(tmp);

        Node<T> pivot = i;
        Node<T> pivot_prev = i_prev;
        Node<T> pivot_next = pivot.get_next();

        return new Part<>(pivot_prev, pivot, pivot_next);
    }

    // Verify that 'end' is reachable from 'start' following next pointers.
    private boolean segment_valid(Node<T> start, Node<T> end) {
        Node<T> cur = start;
        while (cur != null) {
            if (cur == end) return true;
            cur = cur.get_next();
        }
        return false;
    }

    // Small tuple for partition results
    private static final class Part<E> {

        final Node<E> pivot_prev, pivot, pivot_next;

        Part(Node<E> a, Node<E> b, Node<E> c) {
            this.pivot_prev = a;
            this.pivot = b;
            this.pivot_next = c;
        }
    }

    private Node<T> merge_sort(Node<T> node_head, Compare<T> cmp) {
        metrics.recursive_calls++;
        if (node_head == null || node_head.get_next() == null) return node_head;
        Node<T> mid = get_middle(node_head);
        Node<T> right = mid.get_next();
        mid.set_next(null);
        Node<T> left_sorted = merge_sort(node_head, cmp);
        Node<T> right_sorted = merge_sort(right, cmp);
        return sorted_merge(left_sorted, right_sorted, cmp);
    }

    private Node<T> sorted_merge(Node<T> a, Node<T> b, Compare<T> cmp) {
        if (a == null) return b;
        if (b == null) return a;
        Node<T> res;
        if (cmp(cmp, a.get_data(), b.get_data()) <= 0) {
            res = a;
            res.set_next(sorted_merge(a.get_next(), b, cmp));
        } else {
            res = b;
            res.set_next(sorted_merge(a, b.get_next(), cmp));
        }
        return res;
    }

    private Node<T> get_middle(Node<T> head) {
        if (head == null) return head;
        Node<T> slow = head;
        Node<T> fast = head.get_next();
        while (fast != null) {
            fast = fast.get_next();
            if (fast != null) {
                slow = slow.get_next();
                fast = fast.get_next();
            }
        }
        return slow;
    }

    private void recompute_tail() {
        Node<T> cur = this.head;
        Node<T> last = null;
        int counter = 0;
        while (cur != null) {
            last = cur;
            cur = cur.get_next();
            counter++;
        }
        this.tail = last;
        this.size = counter;
    }

    // Concatenate up to three chains
    private Node<T> concat_three(Node<T> a, Node<T> b, Node<T> c) {
        Node<T> head_new = null,
            tail_new = null;

        if (a != null) {
            head_new = a;
            tail_new = a;
            while (tail_new.get_next() != null) tail_new = tail_new.get_next();
        }
        if (b != null) {
            if (head_new == null) {
                head_new = b;
                tail_new = b;
            } else {
                tail_new.set_next(b);
            }
            while (tail_new.get_next() != null) tail_new = tail_new.get_next();
        }
        if (c != null) {
            if (head_new == null) {
                head_new = c;
            } else {
                tail_new.set_next(c);
            }
        }
        return head_new;
    }

    // SEARCHING ALGORITHMSSSSSSSSSSSS
    public Node<T> search(T data, Search_Type type) {
        switch (type) {
            case LINEAR:
                return linear_search(data, null);
            case BINARY:
                return binary_search_list(data, null);
            case BOUNDARY_LOW:
                return lower_bound_list(data, null);
            case BOUNDARY_HIGH:
                return upper_bound_list(data, null);
            default:
                return linear_search(data, null);
        }
    }

    // Overload with Compare
    public Node<T> search(T data, Search_Type type, Compare<T> cmp) {
        switch (type) {
            case LINEAR:
                return linear_search(data, cmp);
            case BINARY:
                return binary_search_list(data, cmp);
            case BOUNDARY_LOW:
                return lower_bound_list(data, cmp);
            case BOUNDARY_HIGH:
                return upper_bound_list(data, cmp);
            default:
                return linear_search(data, cmp);
        }
    }

    // Comparator-aware
    public Node<T> linear_search(T key, Compare<T> cmp) {
        Node<T> cur = this.head;
        while (cur != null) {
            if (cmp(cmp, key, cur.get_data()) == 0) return cur;
            cur = cur.get_next();
        }
        return null;
    }

    // FAST/SLOW POINTERSSSS
    private Node<T> mid_between(Node<T> start, Node<T> end) {
        if (start == null) return null;
        Node<T> slow = start,
            fast = start;
        while (fast != end && fast.get_next() != end) {
            fast = fast.get_next();
            if (fast != end) {
                slow = slow.get_next();
                fast = fast.get_next();
            }
        }
        return slow;
    }

    public Node<T> binary_search_list(T key, Compare<T> cmp) {
        Node<T> start = this.head,
            end = null; // [start, end)
        while (start != end) {
            Node<T> mid = mid_between(start, end);
            if (mid == null) return null;
            int c = cmp(cmp, key, mid.get_data());
            if (c == 0) return mid;
            if (c < 0) end = mid;
            else start = mid.get_next();
        }
        return null;
    }

    public Node<T> lower_bound_list(T key, Compare<T> cmp) {
        Node<T> start = this.head,
            end = null,
            ans = null;
        while (start != end) {
            Node<T> mid = mid_between(start, end);
            if (mid == null) break;
            if (cmp(cmp, mid.get_data(), key) >= 0) {
                ans = mid;
                end = mid;
            } else {
                start = mid.get_next();
            }
        }
        return ans;
    }

    public Node<T> upper_bound_list(T key, Compare<T> cmp) {
        Node<T> start = this.head,
            end = null,
            ans = null;
        while (start != end) {
            Node<T> mid = mid_between(start, end);
            if (mid == null) break;
            if (cmp(cmp, mid.get_data(), key) > 0) {
                ans = mid;
                end = mid;
            } else {
                start = mid.get_next();
            }
        }
        return ans;
    }

    // For PPG searching version
    public Node<T> lower_bound_by_double(double min_val, Key_Double<T> key) {
        Node<T> start = this.head;
        Node<T> end = null; // exclusive
        Node<T> ans = null;

        while (start != end) {
            Node<T> mid = mid_between(start, end);
            if (mid == null) break;
            double mid_key = key.get(mid.get_data());
            if (mid_key >= min_val) {
                ans = mid;
                end = mid; // tighten left half
            } else {
                start = mid.get_next(); // go right
            }
        }
        return ans; // null if none >= min_val
    }

    public Node<T> upper_bound_by_double(double max_val, Key_Double<T> key) {
        Node<T> start = this.head;
        Node<T> end = null;
        Node<T> ans = null;

        while (start != end) {
            Node<T> mid = mid_between(start, end);
            if (mid == null) break;
            double mid_key = key.get(mid.get_data());
            if (mid_key > max_val) {
                ans = mid;
                end = mid;
            } else {
                start = mid.get_next();
            }
        }
        return ans; // null if none > max_val
    }

    public Linked_List<T> range_ge_to_list(double min_val, Key_Double<T> key) {
        Linked_List<T> result = create_empty_like_this();
        Node<T> start = lower_bound_by_double(min_val, key);
        Node<T> cur = start;

        while (cur != null) {
            double v = key.get(cur.get_data());
            if (v < min_val) break; // safety
            result.add(cur.get_data()); // copies data reference, not new object
            cur = cur.get_next();
        }

        return result;
    }

    public Linked_List<T> range_lt_to_list(double max_val, Key_Double<T> key) {
        Linked_List<T> result = create_empty_like_this();
        Node<T> boundary = lower_bound_by_double(max_val, key);
        Node<T> cur = this.head;

        while (cur != boundary) {
            if (cur == null) break;
            double v = key.get(cur.get_data());
            if (v >= max_val) break;
            result.add(cur.get_data());
            cur = cur.get_next();
        }

        return result;
    }

    private int dec1(double v) {
        return (int) Math.round(v * 10.0);
    }

    public Node<T> find_eq_by_double_linear(double target, Key_Double<T> key) {
        int want = dec1(target);
        Node<T> cur = this.head;
        while (cur != null) {
            if (dec1(key.get(cur.get_data())) == want) return cur; // first match
            cur = cur.get_next();
        }
        return null;
    }

    public Node<T> find_eq_by_double_binary(double target, Key_Double<T> key) {
        int want = dec1(target);
        Node<T> start = this.head,
            end = null;
        Node<T> ans = null;
        while (start != end) {
            Node<T> mid = mid_between(start, end);
            if (mid == null) break;
            int mv = dec1(key.get(mid.get_data()));
            if (mv >= want) {
                if (mv == want) ans = mid; // remember match, keep going left
                end = mid; // tighten to left half
            } else {
                start = mid.get_next(); // go right
            }
        }
        return ans; // null if no exact match
    }

    // UTILITIESSSSSSS
    public int index_of(T key) {
        int idx = 0;
        Node<T> cur = this.head;
        while (cur != null) {
            if (key.compareTo(cur.get_data()) == 0) return idx;
            cur = cur.get_next();
            idx++;
        }
        return -1;
    }

    public int last_index_of(T key) {
        int idx = 0,
            last = -1;
        Node<T> cur = this.head;
        while (cur != null) {
            if (key.compareTo(cur.get_data()) == 0) last = idx;
            cur = cur.get_next();
            idx++;
        }
        return last;
    }

    public T min() {
        if (this.head == null) return null;
        T best = this.head.get_data();
        for (
            Node<T> cur = this.head.get_next();
            cur != null;
            cur = cur.get_next()
        ) {
            if (cur.get_data().compareTo(best) < 0) best = cur.get_data();
        }
        return best;
    }

    public T max() {
        if (this.head == null) return null;
        T best = this.head.get_data();
        for (
            Node<T> cur = this.head.get_next();
            cur != null;
            cur = cur.get_next()
        ) {
            if (cur.get_data().compareTo(best) > 0) best = cur.get_data();
        }
        return best;
    }

    public boolean is_sorted() {
        if (this.head == null) return true;
        Node<T> cur = this.head;
        while (cur != null && cur.get_next() != null) {
            if (
                cur.get_data().compareTo(cur.get_next().get_data()) > 0
            ) return false;
            cur = cur.get_next();
        }
        return true;
    }

    private int cmp(Compare<T> c, T a, T b) {
        metrics.comparisons++;
        if (c != null) return c.cmp(a, b);
        return a.compareTo(b);
    }

    public interface Compare<U> {
        int cmp(U a, U b);
    }

    public interface Equals<U> {
        boolean eq(U a, U b);
    }

    public interface Key_Double<K> {
        double get(K item);
    }
}
