package Tour_Graph;

import Tour_Lists.Linked_List;
import Tour_Nodes.Node;
import Tour_Stack_Queue.Linked_Queue;
import Tour_Stack_Queue.Linked_Stack;

@SuppressWarnings("unchecked")
public class Generic_Graph<T extends Comparable<T>> {

    /* ------------------------- Basic inner data types ---------------------- */

    public interface PrevResult<V extends Comparable<V>> {
        boolean isSuccess();
        double[] getDistances();
        int[] getPrev();
        Vertex<V>[] getIndexMap();
    }

    public static class Vertex<E extends Comparable<E>>
        implements Comparable<Vertex<E>> {

        public E value;
        public Edge<E> first_edge; // head of edge singly-linked list
        public int indegree; // for topological sort

        Vertex(E value) {
            this.value = value;
            this.first_edge = null;
            this.indegree = 0;
        }

        @Override
        public int compareTo(Vertex<E> other) {
            // Compare by stored value
            return this.value.compareTo(other.value);
        }
    }

    public static class Edge<E extends Comparable<E>> {

        public Vertex<E> to;
        public double weight;
        public Edge<E> next;

        Edge(Vertex<E> to, double weight, Edge<E> next) {
            this.to = to;
            this.weight = weight;
            this.next = next;
        }
    }

    public static class VertexNode<V extends Comparable<V>> {

        public Vertex<V> v;
        public VertexNode<V> next;

        VertexNode(Vertex<V> v) {
            this.v = v;
            this.next = null;
        }
    }

    public VertexNode<T> vertices; // head for vertices linked list
    public int vertex_count;

    public Generic_Graph() {
        this.vertices = null;
        this.vertex_count = 0;
    }

    private Vertex<T> find_vertex_node(T value) {
        VertexNode<T> cur = this.vertices;
        while (cur != null) {
            if (cur.v.value.compareTo(value) == 0) return cur.v;
            cur = cur.next;
        }
        return null;
    }

    /* ------------------------- Small helpers using ADTs --------------- */

    // Pair and map implemented using Linked_Queue/Linked_List
    private static class VBPair implements Comparable<VBPair> {

        Object key;
        boolean val;

        VBPair(Object k, boolean v) {
            key = k;
            val = v;
        }

        @Override
        public int compareTo(VBPair other) {
            return 0;
        }
    }

    private static class SimpleBoolMap {

        private Linked_Queue<VBPair> list;

        SimpleBoolMap() {
            list = new Linked_Queue<>();
        }

        void put(Object k, boolean v) {
            Node<VBPair> cur = list.get_head();
            while (cur != null) {
                VBPair p = cur.get_data();
                if (p.key == k) {
                    p.val = v;
                    return;
                }
                cur = cur.get_next();
            }
            list.add(new VBPair(k, v));
        }

        boolean get(Object k) {
            Node<VBPair> cur = list.get_head();
            while (cur != null) {
                VBPair p = cur.get_data();
                if (p.key == k) return p.val;
                cur = cur.get_next();
            }
            return false;
        }
    }

    /* ------------------------- Public API ------------------------------ */
    public boolean add_vertex(T value) {
        if (find_vertex_node(value) != null) return false;
        Vertex<T> v = new Vertex<>(value);
        VertexNode<T> vn = new VertexNode<>(v);
        vn.next = this.vertices;
        this.vertices = vn;
        this.vertex_count++;
        return true;
    }

    public boolean remove_vertex(T value) {
        VertexNode<T> prev = null;
        VertexNode<T> cur = this.vertices;
        while (cur != null) {
            if (cur.v.value.compareTo(value) == 0) break;
            prev = cur;
            cur = cur.next;
        }
        if (cur == null) return false;
        VertexNode<T> walker = this.vertices;
        while (walker != null) {
            remove_edge_internal(walker.v, cur.v, false);
            walker = walker.next;
        }
        if (prev == null) this.vertices = cur.next;
        else prev.next = cur.next;
        this.vertex_count--;
        return true;
    }

    public boolean add_edge(T from, T to, double weight, boolean directed) {
        Vertex<T> vf = find_vertex_node(from);
        Vertex<T> vt = find_vertex_node(to);
        if (vf == null || vt == null) return false;
        Edge<T> e = vf.first_edge;
        while (e != null) {
            if (e.to == vt) {
                e.weight = weight;
                return true;
            }
            e = e.next;
        }
        vf.first_edge = new Edge<>(vt, weight, vf.first_edge);
        vt.indegree++;
        if (!directed) {
            Edge<T> rev = vt.first_edge;
            while (rev != null) {
                if (rev.to == vf) {
                    rev.weight = weight;
                    return true;
                }
                rev = rev.next;
            }
            vt.first_edge = new Edge<>(vf, weight, vt.first_edge);
            vf.indegree++;
        }
        return true;
    }

    private boolean remove_edge_internal(
        Vertex<T> src,
        Vertex<T> dst,
        boolean adjust_indegree
    ) {
        if (src == null || dst == null) return false;
        Edge<T> prev = null;
        Edge<T> cur = src.first_edge;
        while (cur != null) {
            if (cur.to == dst) break;
            prev = cur;
            cur = cur.next;
        }
        if (cur == null) return false;
        if (prev == null) src.first_edge = cur.next;
        else prev.next = cur.next;
        if (adjust_indegree) dst.indegree--;
        return true;
    }

    public boolean remove_edge(T from, T to, boolean directed) {
        Vertex<T> vf = find_vertex_node(from);
        Vertex<T> vt = find_vertex_node(to);
        if (vf == null || vt == null) return false;
        boolean removed = remove_edge_internal(vf, vt, true);
        if (!directed) remove_edge_internal(vt, vf, true);
        return removed;
    }

    public boolean contains_vertex(T v) {
        return find_vertex_node(v) != null;
    }

    public boolean contains_edge(T from, T to) {
        Vertex<T> vf = find_vertex_node(from);
        Vertex<T> vt = find_vertex_node(to);
        if (vf == null || vt == null) return false;
        Edge<T> e = vf.first_edge;
        while (e != null) {
            if (e.to == vt) return true;
            e = e.next;
        }
        return false;
    }

    /* ------------------------- Traversals (using user ADTs) -------------- */
    public Linked_Queue<T> dfs_recursive(T start) {
        Vertex<T> s = find_vertex_node(start);
        Linked_Queue<T> res = new Linked_Queue<>();
        if (s == null) return res;
        SimpleBoolMap seen = new SimpleBoolMap();
        dfs_rec_helper(s, res, seen);
        return res;
    }

    private void dfs_rec_helper(
        Vertex<T> v,
        Linked_Queue<T> out,
        SimpleBoolMap seen
    ) {
        if (seen.get(v)) return;
        seen.put(v, true);
        out.add(v.value);
        Edge<T> e = v.first_edge;
        while (e != null) {
            dfs_rec_helper(e.to, out, seen);
            e = e.next;
        }
    }

    public Linked_Queue<T> dfs_iterative(T start) {
        Vertex<T> s = find_vertex_node(start);
        Linked_Queue<T> res = new Linked_Queue<>();
        if (s == null) return res;
        Linked_Stack<Vertex<T>> st = new Linked_Stack<>();
        SimpleBoolMap seen = new SimpleBoolMap();
        st.add(s);
        while (!st.is_empty()) {
            Vertex<T> cur = st.pop();
            if (seen.get(cur)) continue;
            seen.put(cur, true);
            res.add(cur.value);
            Edge<T> e = cur.first_edge;
            while (e != null) {
                if (!seen.get(e.to)) st.add(e.to);
                e = e.next;
            }
        }
        return res;
    }

    public Linked_Queue<T> bfs(T start) {
        Vertex<T> s = find_vertex_node(start);
        Linked_Queue<T> res = new Linked_Queue<>();
        if (s == null) return res;
        Linked_Queue<Vertex<T>> q = new Linked_Queue<>();
        SimpleBoolMap seen = new SimpleBoolMap();
        q.add(s);
        seen.put(s, true);
        while (!q.is_empty()) {
            Vertex<T> cur = q.dequeue();
            res.add(cur.value);
            Edge<T> e = cur.first_edge;
            while (e != null) {
                if (!seen.get(e.to)) {
                    seen.put(e.to, true);
                    q.add(e.to);
                }
                e = e.next;
            }
        }
        return res;
    }

    public boolean has_path(T a, T b) {
        Vertex<T> va = find_vertex_node(a);
        Vertex<T> vb = find_vertex_node(b);
        if (va == null || vb == null) return false;
        Linked_Queue<Vertex<T>> q = new Linked_Queue<>();
        SimpleBoolMap seen = new SimpleBoolMap();
        q.add(va);
        seen.put(va, true);
        while (!q.is_empty()) {
            Vertex<T> cur = q.dequeue();
            if (cur == vb) return true;
            Edge<T> e = cur.first_edge;
            while (e != null) {
                if (!seen.get(e.to)) {
                    seen.put(e.to, true);
                    q.add(e.to);
                }
                e = e.next;
            }
        }
        return false;
    }

    /* ------------------------- Shortest paths & helpers ------------------ */
    // MinHeap and UnionFind reused from earlier lightweight implementations
    private static class MinHeap {

        private HeapNode[] data;
        private int size;

        private static class HeapNode {

            int vertex_index;
            double dist;

            HeapNode(int v, double d) {
                vertex_index = v;
                dist = d;
            }
        }

        MinHeap(int cap) {
            this.data = new HeapNode[Math.max(4, cap)];
            this.size = 0;
        }

        void push(int vertex_index, double dist) {
            if (this.size >= this.data.length) resize();
            this.data[this.size] = new HeapNode(vertex_index, dist);
            sift_up(this.size);
            this.size++;
        }

        HeapNode pop() {
            if (this.size == 0) return null;
            HeapNode res = this.data[0];
            this.size--;
            this.data[0] = this.data[this.size];
            this.data[this.size] = null;
            sift_down(0);
            return res;
        }

        boolean is_empty() {
            return this.size == 0;
        }

        private void sift_up(int i) {
            while (i > 0) {
                int p = (i - 1) / 2;
                if (this.data[p].dist <= this.data[i].dist) break;
                HeapNode tmp = this.data[p];
                this.data[p] = this.data[i];
                this.data[i] = tmp;
                i = p;
            }
        }

        private void sift_down(int i) {
            while (true) {
                int l = 2 * i + 1,
                    r = 2 * i + 2,
                    smallest = i;
                if (
                    l < this.size &&
                    this.data[l].dist < this.data[smallest].dist
                ) smallest = l;
                if (
                    r < this.size &&
                    this.data[r].dist < this.data[smallest].dist
                ) smallest = r;
                if (smallest == i) break;
                HeapNode tmp = this.data[i];
                this.data[i] = this.data[smallest];
                this.data[smallest] = tmp;
                i = smallest;
            }
        }

        private void resize() {
            HeapNode[] n = new HeapNode[this.data.length * 2];
            for (int i = 0; i < this.data.length; i++) n[i] = this.data[i];
            this.data = n;
        }
    }

    private static class UnionFind {

        private int[] parent;
        private int[] rank;

        UnionFind(int n) {
            parent = new int[n];
            rank = new int[n];
            for (int i = 0; i < n; i++) parent[i] = i;
        }

        int find(int x) {
            if (parent[x] != x) parent[x] = find(parent[x]);
            return parent[x];
        }

        void union(int a, int b) {
            int ra = find(a),
                rb = find(b);
            if (ra == rb) return;
            if (rank[ra] < rank[rb]) parent[ra] = rb;
            else if (rank[rb] < rank[ra]) parent[rb] = ra;
            else {
                parent[rb] = ra;
                rank[ra]++;
            }
        }
    }

    public static class PathResult<V extends Comparable<V>>
        implements PrevResult<V> {

        public boolean success;
        public double[] distances;
        public int[] prev;
        public Vertex<V>[] idx_to_vertex;

        public PathResult(boolean ok, double[] d, int[] p, Vertex<V>[] map) {
            success = ok;
            distances = d;
            prev = p;
            idx_to_vertex = map;
        }

        @Override
        public boolean isSuccess() {
            return success;
        }

        @Override
        public double[] getDistances() {
            return distances;
        }

        @Override
        public int[] getPrev() {
            return prev;
        }

        @Override
        public Vertex<V>[] getIndexMap() {
            return idx_to_vertex;
        }
    }

    public static class BellmanResult<V extends Comparable<V>>
        implements PrevResult<V> {

        public boolean success;
        public boolean no_negative_cycle;
        public double[] distances;
        public int[] prev;
        public Vertex<V>[] idx_to_vertex;

        public BellmanResult(
            boolean ok,
            boolean nc,
            double[] d,
            int[] p,
            Vertex<V>[] map
        ) {
            success = ok;
            no_negative_cycle = nc;
            distances = d;
            prev = p;
            idx_to_vertex = map;
        }

        @Override
        public boolean isSuccess() {
            return success;
        }

        @Override
        public double[] getDistances() {
            return distances;
        }

        @Override
        public int[] getPrev() {
            return prev;
        }

        @Override
        public Vertex<V>[] getIndexMap() {
            return idx_to_vertex;
        }
    }

    private Vertex<T>[] index_vertices() {
        int n = this.vertex_count;
        VertexNode<T> cur = this.vertices;
        Vertex<T>[] arr = new Vertex[n];
        int i = 0;
        while (cur != null) {
            arr[i++] = cur.v;
            cur = cur.next;
        }
        return arr;
    }

    public PathResult<T> dijkstra(T source) {
        int n = this.vertex_count;
        Vertex<T>[] idx_to_v = index_vertices();
        double[] dist = new double[n];
        int[] prev = new int[n];
        for (int i = 0; i < n; i++) {
            dist[i] = Double.POSITIVE_INFINITY;
            prev[i] = -1;
        }
        int src_idx = -1;
        for (int i = 0; i < n; i++) if (
            idx_to_v[i].value.compareTo(source) == 0
        ) {
            src_idx = i;
            break;
        }
        if (src_idx == -1) return new PathResult<T>(false, null, null, null);
        dist[src_idx] = 0.0;
        MinHeap heap = new MinHeap(n);
        heap.push(src_idx, 0.0);
        boolean[] visited = new boolean[n];
        while (!heap.is_empty()) {
            MinHeap.HeapNode hn = heap.pop();
            int u = hn.vertex_index;
            if (visited[u]) continue;
            visited[u] = true;
            Edge<T> e = idx_to_v[u].first_edge;
            while (e != null) {
                int v_idx = -1;
                for (int j = 0; j < n; j++) if (idx_to_v[j] == e.to) {
                    v_idx = j;
                    break;
                }
                if (v_idx == -1) {
                    e = e.next;
                    continue;
                }
                double alt = dist[u] + e.weight;
                if (alt < dist[v_idx]) {
                    dist[v_idx] = alt;
                    prev[v_idx] = u;
                    heap.push(v_idx, alt);
                }
                e = e.next;
            }
        }
        return new PathResult<>(true, dist, prev, idx_to_v);
    }

    public BellmanResult<T> bellman_ford(T source) {
        int n = this.vertex_count;
        Vertex<T>[] idx_to_v = index_vertices();
        double[] dist = new double[n];
        int[] prev = new int[n];
        for (int i = 0; i < n; i++) {
            dist[i] = Double.POSITIVE_INFINITY;
            prev[i] = -1;
        }
        int src_idx = -1;
        for (int i = 0; i < n; i++) if (
            idx_to_v[i].value.compareTo(source) == 0
        ) {
            src_idx = i;
            break;
        }
        if (src_idx == -1) return new BellmanResult<>(
            false,
            true,
            null,
            null,
            null
        );
        dist[src_idx] = 0.0;
        for (int k = 0; k < n - 1; k++) {
            boolean any = false;
            for (int u = 0; u < n; u++) {
                Edge<T> e = idx_to_v[u].first_edge;
                while (e != null) {
                    int v_idx = -1;
                    for (int j = 0; j < n; j++) if (idx_to_v[j] == e.to) {
                        v_idx = j;
                        break;
                    }
                    if (v_idx >= 0 && dist[u] + e.weight < dist[v_idx]) {
                        dist[v_idx] = dist[u] + e.weight;
                        prev[v_idx] = u;
                        any = true;
                    }
                    e = e.next;
                }
            }
            if (!any) break;
        }
        for (int u = 0; u < n; u++) {
            Edge<T> e = idx_to_v[u].first_edge;
            while (e != null) {
                int v_idx = -1;
                for (int j = 0; j < n; j++) if (idx_to_v[j] == e.to) {
                    v_idx = j;
                    break;
                }
                if (
                    v_idx >= 0 && dist[u] + e.weight < dist[v_idx]
                ) return new BellmanResult<>(
                    false,
                    false,
                    null,
                    null,
                    idx_to_v
                );
                e = e.next;
            }
        }
        return new BellmanResult<>(true, true, dist, prev, idx_to_v);
    }

    /* ------------------------- MSTs, topological, cycle ------------------ */
    private static class EdgeItem {

        int u;
        int v;
        double weight;

        EdgeItem(int u, int v, double w) {
            this.u = u;
            this.v = v;
            this.weight = w;
        }
    }

    private EdgeItem[] gather_edges(Vertex<T>[] idx_to_v) {
        int n = idx_to_v.length;
        int cnt = 0;
        for (int i = 0; i < n; i++) {
            Edge<T> e = idx_to_v[i].first_edge;
            while (e != null) {
                cnt++;
                e = e.next;
            }
        }
        EdgeItem[] list = new EdgeItem[cnt];
        int p = 0;
        for (int i = 0; i < n; i++) {
            Edge<T> e = idx_to_v[i].first_edge;
            while (e != null) {
                int j = -1;
                for (int k = 0; k < n; k++) if (idx_to_v[k] == e.to) {
                    j = k;
                    break;
                }
                if (j >= 0) list[p++] = new EdgeItem(i, j, e.weight);
                e = e.next;
            }
        }
        return list;
    }

    public Linked_Queue<String> prim_mst() {
        int n = this.vertex_count;
        Linked_Queue<String> out = new Linked_Queue<>();
        if (n == 0) return out;
        Vertex<T>[] idx_to_v = index_vertices();
        boolean[] in_mst = new boolean[n];
        double[] key = new double[n];
        int[] parent = new int[n];
        for (int i = 0; i < n; i++) {
            key[i] = Double.POSITIVE_INFINITY;
            parent[i] = -1;
        }
        key[0] = 0.0;
        MinHeap heap = new MinHeap(n);
        heap.push(0, 0.0);
        while (!heap.is_empty()) {
            MinHeap.HeapNode hn = heap.pop();
            int u = hn.vertex_index;
            if (in_mst[u]) continue;
            in_mst[u] = true;
            Edge<T> e = idx_to_v[u].first_edge;
            while (e != null) {
                int v_idx = -1;
                for (int j = 0; j < n; j++) if (idx_to_v[j] == e.to) {
                    v_idx = j;
                    break;
                }
                if (v_idx >= 0 && !in_mst[v_idx] && e.weight < key[v_idx]) {
                    key[v_idx] = e.weight;
                    parent[v_idx] = u;
                    heap.push(v_idx, key[v_idx]);
                }
                e = e.next;
            }
        }
        for (int i = 1; i < n; i++) if (parent[i] != -1) out.add(
            idx_to_v[parent[i]].value +
                " -> " +
                idx_to_v[i].value +
                " : " +
                key[i]
        );
        return out;
    }

    public Linked_Queue<String> kruskal_mst() {
        int n = this.vertex_count;
        Linked_Queue<String> out = new Linked_Queue<>();
        if (n == 0) return out;
        Vertex<T>[] idx_to_v = index_vertices();
        EdgeItem[] edges = gather_edges(idx_to_v);
        for (int i = 0; i < edges.length - 1; i++) {
            int best = i;
            for (int j = i + 1; j < edges.length; j++) if (
                edges[j].weight < edges[best].weight
            ) best = j;
            EdgeItem tmp = edges[i];
            edges[i] = edges[best];
            edges[best] = tmp;
        }
        UnionFind uf = new UnionFind(n);
        for (int i = 0; i < edges.length; i++) {
            EdgeItem ei = edges[i];
            if (uf.find(ei.u) != uf.find(ei.v)) {
                uf.union(ei.u, ei.v);
                out.add(
                    idx_to_v[ei.u].value +
                        " -- " +
                        idx_to_v[ei.v].value +
                        " : " +
                        ei.weight
                );
            }
        }
        return out;
    }

    public Linked_Queue<T> topological_sort() {
        Linked_Queue<T> out = new Linked_Queue<>();
        int n = this.vertex_count;
        if (n == 0) return out;
        Vertex<T>[] idx_to_v = index_vertices();
        int[] indeg = new int[n];
        for (int i = 0; i < n; i++) indeg[i] = idx_to_v[i].indegree;
        Linked_Queue<Integer> q = new Linked_Queue<>();
        for (int i = 0; i < n; i++) if (indeg[i] == 0) q.add(i);
        int removed = 0;
        while (!q.is_empty()) {
            int u = q.dequeue();
            out.add(idx_to_v[u].value);
            removed++;
            Edge<T> e = idx_to_v[u].first_edge;
            while (e != null) {
                int v = -1;
                for (int j = 0; j < n; j++) if (idx_to_v[j] == e.to) {
                    v = j;
                    break;
                }
                if (v >= 0) {
                    indeg[v]--;
                    if (indeg[v] == 0) q.add(v);
                }
                e = e.next;
            }
        }
        if (removed != n) return new Linked_Queue<>();
        return out;
    }

    public boolean detect_cycle_directed() {
        int n = this.vertex_count;
        if (n == 0) return false;
        Vertex<T>[] idx_to_v = index_vertices();
        int[] state = new int[n];
        for (int i = 0; i < n; i++) {
            if (state[i] == 0 && dfs_cycle_d(i, idx_to_v, state)) return true;
        }
        return false;
    }

    private boolean dfs_cycle_d(int u_idx, Vertex<T>[] idx_to_v, int[] state) {
        state[u_idx] = 1;
        Edge<T> e = idx_to_v[u_idx].first_edge;
        while (e != null) {
            int v = -1;
            for (int j = 0; j < idx_to_v.length; j++) if (idx_to_v[j] == e.to) {
                v = j;
                break;
            }
            if (v >= 0) {
                if (state[v] == 1) return true;
                if (
                    state[v] == 0 && dfs_cycle_d(v, idx_to_v, state)
                ) return true;
            }
            e = e.next;
        }
        state[u_idx] = 2;
        return false;
    }

    public boolean detect_cycle_undirected() {
        int n = this.vertex_count;
        if (n == 0) return false;
        Vertex<T>[] idx_to_v = index_vertices();
        boolean[] seen = new boolean[n];
        for (int i = 0; i < n; i++) if (
            !seen[i] && dfs_cycle_u(i, -1, idx_to_v, seen)
        ) return true;
        return false;
    }

    private boolean dfs_cycle_u(
        int u,
        int parent,
        Vertex<T>[] idx_to_v,
        boolean[] seen
    ) {
        seen[u] = true;
        Edge<T> e = idx_to_v[u].first_edge;
        while (e != null) {
            int v = -1;
            for (int j = 0; j < idx_to_v.length; j++) if (idx_to_v[j] == e.to) {
                v = j;
                break;
            }
            if (v >= 0) {
                if (!seen[v]) {
                    if (dfs_cycle_u(v, u, idx_to_v, seen)) return true;
                } else if (v != parent) return true;
            }
            e = e.next;
        }
        return false;
    }

    public double[][] to_adjacency_matrix() {
        int n = this.vertex_count;
        double[][] mat = new double[n][n];
        for (int i = 0; i < n; i++) for (int j = 0; j < n; j++) mat[i][j] =
            Double.POSITIVE_INFINITY;
        Vertex<T>[] idx_to_v = index_vertices();
        for (int i = 0; i < n; i++) {
            Edge<T> e = idx_to_v[i].first_edge;
            while (e != null) {
                int j = -1;
                for (int k = 0; k < n; k++) if (idx_to_v[k] == e.to) {
                    j = k;
                    break;
                }
                if (j >= 0) mat[i][j] = e.weight;
                e = e.next;
            }
        }
        return mat;
    }

    public Generic_Graph<T> transpose() {
        Generic_Graph<T> g = new Generic_Graph<>();
        VertexNode<T> cur = this.vertices;
        while (cur != null) {
            g.add_vertex(cur.v.value);
            cur = cur.next;
        }
        VertexNode<T> walker = this.vertices;
        while (walker != null) {
            Edge<T> e = walker.v.first_edge;
            while (e != null) {
                g.add_edge(e.to.value, walker.v.value, e.weight, true);
                e = e.next;
            }
            walker = walker.next;
        }
        return g;
    }

    public Generic_Graph<T> clone_graph() {
        Generic_Graph<T> g = new Generic_Graph<>();
        VertexNode<T> cur = this.vertices;
        while (cur != null) {
            g.add_vertex(cur.v.value);
            cur = cur.next;
        }
        cur = this.vertices;
        while (cur != null) {
            Edge<T> e = cur.v.first_edge;
            while (e != null) {
                g.add_edge(cur.v.value, e.to.value, e.weight, true);
                e = e.next;
            }
            cur = cur.next;
        }
        return g;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        VertexNode<T> cur = this.vertices;
        while (cur != null) {
            sb.append(cur.v.value).append(" -> ");
            Edge<T> e = cur.v.first_edge;
            boolean first = true;
            while (e != null) {
                if (!first) sb.append(", ");
                sb.append(e.to.value).append("(").append(e.weight).append(")");
                first = false;
                e = e.next;
            }
            sb.append('\n');
            cur = cur.next;
        }
        return sb.toString();
    }

    /* ------------------------- Path builder ------------------------------ */
    public Linked_Queue<T> build_path_from_prev(PrevResult<T> pr, T target) {
        Linked_Queue<T> out = new Linked_Queue<>();
        if (pr == null || !pr.isSuccess()) return out;

        Vertex<T>[] map = pr.getIndexMap();
        int[] prev = pr.getPrev();

        int n = map.length;
        int tidx = -1;

        for (int i = 0; i < n; i++) if (map[i].value.compareTo(target) == 0) {
            tidx = i;
            break;
        }

        if (tidx == -1) return out;

        Linked_Stack<T> st = new Linked_Stack<>();
        int cur = tidx;

        while (cur != -1) {
            st.add(map[cur].value);
            cur = prev[cur];
        }

        while (!st.is_empty()) out.add(st.pop());

        return out;
    }

    public static String format(double d) {
        return Double.isInfinite(d) ? "∞" : Double.toString(d);
    }

    /* ================== Automata API (DFA / ε-NFA lite) ================== */
    public static double sym(char c) {
        if (c == 'a') return 1.0;
        if (c == 'b') return 2.0;
        return 3.0; // treat others as epsilon if ever needed
    }

    /** Acceptance predicate */
    public interface AcceptFn<U extends Comparable<U>> {
        boolean is_accept(U v);
    }

    /** Result for one run */
    public static class AutomataResult<V extends Comparable<V>> {

        public boolean accepted;
        public Linked_Queue<V> path;
        public String input;

        AutomataResult(boolean ok, Linked_Queue<V> p, String s) {
            accepted = ok;
            path = p;
            input = s;
        }
    }

    /** Follow exactly one outgoing edge by weight (returns next vertex value or null). */
    private T step_by_weight(T current, double w) {
        Vertex<T> v = find_vertex_node(current);
        if (v == null) return null;
        Edge<T> e = v.first_edge;
        while (e != null) {
            if (e.weight == w) return e.to.value;
            e = e.next;
        }
        return null;
    }

    /** Greedy ε-closure walk (weight == 3). Appends nodes to path if moved. */
    private T walk_epsilon(T start, Linked_Queue<T> path) {
        T cur = start;
        while (true) {
            T nxt = step_by_weight(cur, 3.0); // ε
            if (nxt == null) break;
            path.add(nxt);
            cur = nxt;
        }
        return cur;
    }

    /** Run DFA/ε-NFA from a start vertex over `input`. `use_epsilon=true` enables ε edges (w=3). */
    public AutomataResult<T> run_automata(
        T start,
        String input,
        AcceptFn<T> acceptor,
        boolean use_epsilon
    ) {
        Linked_Queue<T> path = new Linked_Queue<>();
        if (start == null) return new AutomataResult<>(false, path, input);

        // Enter start, then greedily take ε if enabled
        path.add(start);
        T cur = start;
        if (use_epsilon) cur = walk_epsilon(cur, path);

        // Consume input
        for (int i = 0; i < input.length(); i++) {
            double w = sym(input.charAt(i));
            T nxt = step_by_weight(cur, w);
            if (nxt == null) {
                // Dead end -> reject (but keep path)
                return new AutomataResult<>(false, path, input);
            }
            path.add(nxt);
            cur = nxt;
            if (use_epsilon) cur = walk_epsilon(cur, path);
        }

        // After input, maybe a final ε-tail
        if (use_epsilon) cur = walk_epsilon(cur, path);

        boolean ok = acceptor != null && acceptor.is_accept(cur);
        return new AutomataResult<>(ok, path, input);
    }
}
