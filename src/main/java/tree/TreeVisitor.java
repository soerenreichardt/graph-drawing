package tree;

@FunctionalInterface
public interface TreeVisitor<DATA, TREE extends AbstractTree<DATA, TREE>> {

    void accept(TREE tree);
}
