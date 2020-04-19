package tree;

import java.util.ArrayList;
import java.util.List;

public class Tree<D> extends AbstractTree<D, Tree<D>> {

    public Tree(D data) {
        this(data, null, null);
    }

    public Tree(D data, Tree<D> parent, List<Tree<D>> children) {
        super(data, parent, children);
    }

    @Override
    public Tree<D> addChild(D data) {
        if (this.children == null) {
            this.children = new ArrayList<>();
        }
        Tree<D> child = new Tree<>(data, this, null);
        this.children.add(child);
        return child;
    }
}
