package graph;

public class Node<DATA> {

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
}
