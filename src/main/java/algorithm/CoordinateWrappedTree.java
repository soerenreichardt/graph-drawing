package algorithm;

import tree.AbstractTree;

import java.awt.geom.Point2D;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class CoordinateWrappedTree<DATA, TREE extends AbstractTree<DATA, TREE>> extends AbstractTree<DATA, CoordinateWrappedTree<DATA, TREE>> {

    Point2D.Float location;

    public float preliminaryX;
    public float modifier;

    private CoordinateWrappedTree(TREE tree) {
        super(tree.data(), null, null);
        this.location = new Point2D.Float();
    }

    private CoordinateWrappedTree(DATA data, CoordinateWrappedTree<DATA, TREE> parent) {
        super(data, parent, null);
        this.location = new Point2D.Float();
    }

    @Override
    protected CoordinateWrappedTree<DATA, TREE> createTreeNode(DATA data) {
        return new CoordinateWrappedTree<>(data, this);
    }

    public Point2D.Float location() {
        return this.location;
    }

    public void setLocation(float x, float y) {
        setLocation(new Point2D.Float(x, y));
    }

    public void setLocation(Point2D.Float location) {
        this.location = location;
    }

    public Optional<CoordinateWrappedTree<DATA, TREE>> getLeftNeighbor(CoordinateWrappedTree<DATA, TREE> node) {
        AtomicBoolean nodeSeen = new AtomicBoolean(false);
        List<CoordinateWrappedTree<DATA, TREE>> maybeResult = new ArrayList<>(1);
        maybeResult.add(null);
        traverse(TraverseStrategy.BREADTH_FIRST_REVERSE, (t) -> {
            if (node == null) return false;
            if (t.level() == node.level() && t != node && nodeSeen.get()) {
                maybeResult.set(0, t);
                return false;
            }
            if (t == node) {
                nodeSeen.set(true);
            }

            return t.level() <= node.level();
        });

        CoordinateWrappedTree<DATA, TREE> leftNeighbor = maybeResult.get(0);
        return leftNeighbor == null
                ? Optional.empty()
                : Optional.of(leftNeighbor);
    }

    public static <D, T extends AbstractTree<D, T>> CoordinateWrappedTree<D, T> from(T tree) {
        CoordinateWrappedTree<D, T> root = new CoordinateWrappedTree<>(tree);
        Map<T, CoordinateWrappedTree<D, T>> treeMapping = new HashMap<>();
        treeMapping.put(tree, root);

        tree.traverse(TraverseStrategy.BREADTH_FIRST, (t) -> {
            if (t.children() != null) {
                t.children().forEach((child) -> {
                    CoordinateWrappedTree<D, T> copyParent = treeMapping.get(child.parent());
                    CoordinateWrappedTree<D, T> copyChild = copyParent.addChild(child.data());
                    treeMapping.put(child, copyChild);
                });
            }
            return true;
        });
        return root;
    }
}
