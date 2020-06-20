package graph;

public class Node<DATA> implements Comparable<Node<DATA>> {

    long id;
    DATA data;

    public Node(long id, DATA data) {
        this.id = id;
        this.data = data;
    }

    public long id() {
        return id;
    }

    public DATA data() {
        return data;
    }

    @Override
    public String toString() {
        return data().toString();
    }

    @Override
    public int compareTo(Node<DATA> other) {
        return Long.compare(id, other.id());
    }

    @Override
    public boolean equals(Object other) {
        return id == ((Node<DATA>) other).id();
    }
}
