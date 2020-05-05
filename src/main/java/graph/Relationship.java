package graph;

public class Relationship<DATA> {

    Node<DATA> source;
    Node<DATA> target;

    public Relationship(Node<DATA> source, Node<DATA> target) {
        this.source = source;
        this.target = target;
    }

    public Node<DATA> source() {
        return source;
    }

    public Node<DATA> target() {
        return target;
    }
}
