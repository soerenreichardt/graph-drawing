package graph;

@FunctionalInterface
public interface NodeVisitor<DATA> {

    void accept(Node<DATA> node);
}
