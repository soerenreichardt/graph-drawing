package algorithm;

import tree.AbstractTree;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CoordinateWrappedTree<DATA, TREE extends AbstractTree<DATA, TREE>> extends AbstractTree<DATA, CoordinateWrappedTree<DATA, TREE>> {

    Point2D.Float location;

    private CoordinateWrappedTree(TREE tree) {
        super(tree.data(), null, null);
        this.location = new Point2D.Float();
    }

    private CoordinateWrappedTree(DATA data, CoordinateWrappedTree<DATA, TREE> parent) {
        super(data, parent, null);
        this.location = new Point2D.Float();
    }

    @Override
    public CoordinateWrappedTree<DATA, TREE> addChild(DATA data) {
        CoordinateWrappedTree<DATA, TREE> child = new CoordinateWrappedTree<>(data, this);
        if (this.children == null) {
            this.children = new ArrayList<>();
        }
        this.children().add(child);
        return child;
    }

    public Point2D.Float location() {
        return this.location;
    }

    public void setLocation(Point2D.Float location) {
        this.location = location;
    }

    static <D, T extends AbstractTree<D, T>> CoordinateWrappedTree<D, T> from(T tree) {
        CoordinateWrappedTree<D, T> root = new CoordinateWrappedTree<>(tree);
        Map<T, CoordinateWrappedTree<D, T>> treeMapping = new HashMap<>();
        treeMapping.put(tree, root);

        tree.traverse(TraverseStrategy.BREADTH_FIRST, (t) -> {
            if (t.children() != null) {
                t.children().forEach((child) -> {
                    T treeChild = (T) child;
                    CoordinateWrappedTree<D, T> copyParent = treeMapping.get(treeChild.parent());
                    CoordinateWrappedTree<D, T> copyChild = copyParent.addChild(treeChild.data());
                    treeMapping.put(treeChild, copyChild);
                });
            }
        });
        return root;
    }
}
