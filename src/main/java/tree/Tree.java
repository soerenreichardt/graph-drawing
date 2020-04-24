package tree;

import java.util.List;

public class Tree<D> extends AbstractTree<D, Tree<D>> {

    public Tree(D data) {
        this(data, null, null);
    }

    public Tree(D data, Tree<D> parent, List<Tree<D>> children) {
        super(data, parent, children);
    }

    @Override
    protected Tree<D> createTreeNode(D data) {
        return new Tree<>(data, this, null);
    }
}
