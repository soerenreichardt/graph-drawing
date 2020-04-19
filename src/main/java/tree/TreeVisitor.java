package tree;

@FunctionalInterface
public interface TreeVisitor<DATA, TREE extends AbstractTree<DATA, TREE>> {

    boolean accept(TREE tree);
}
