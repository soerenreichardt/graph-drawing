package graph;

import java.util.Objects;

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

    @Override
    public String toString() {
        return "(" + source + ")-[ ]->(" + target + ")";
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, target);
    }

    @Override
    public boolean equals(Object obj) {
        var other = (Relationship<DATA>) obj;
        return source.equals(other.source) && target.equals(other.target);
    }
}
