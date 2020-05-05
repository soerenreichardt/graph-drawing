package graph;

@FunctionalInterface
public interface RelationshipVisitor<DATA> {

    void accept(Node<DATA> source, Node<DATA> target);
}
