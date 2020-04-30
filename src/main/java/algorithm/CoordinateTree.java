package algorithm;

import tree.AbstractTree;

import java.awt.geom.Point2D;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class CoordinateTree<DATA, TREE extends AbstractTree<DATA, TREE>> extends AbstractTree<DATA, CoordinateTree<DATA, TREE>> {

    Point2D.Float location;

    public float preliminaryX;
    public float modifier;

    private CoordinateTree(TREE tree) {
        super(tree.data(), null, null);
        this.location = new Point2D.Float();
    }

    private CoordinateTree(DATA data, CoordinateTree<DATA, TREE> parent) {
        super(data, parent, null);
        this.location = new Point2D.Float();
    }

    @Override
    protected CoordinateTree<DATA, TREE> createTreeNode(DATA data) {
        return new CoordinateTree<>(data, this);
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

    public Optional<CoordinateTree<DATA, TREE>> getLeftNeighbor(CoordinateTree<DATA, TREE> node) {
        AtomicBoolean nodeSeen = new AtomicBoolean(false);
        List<CoordinateTree<DATA, TREE>> maybeResult = new ArrayList<>(1);
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

        CoordinateTree<DATA, TREE> leftNeighbor = maybeResult.get(0);
        return leftNeighbor == null
                ? Optional.empty()
                : Optional.of(leftNeighbor);
    }

    public static <D, T extends AbstractTree<D, T>> CoordinateTree<D, T> from(T tree) {
        CoordinateTree<D, T> root = new CoordinateTree<>(tree);
        Map<T, CoordinateTree<D, T>> treeMapping = new HashMap<>();
        treeMapping.put(tree, root);

        tree.traverse(TraverseStrategy.BREADTH_FIRST, (t) -> {
            if (t.children() != null) {
                t.children().forEach((child) -> {
                    CoordinateTree<D, T> copyParent = treeMapping.get(child.parent());
                    CoordinateTree<D, T> copyChild = copyParent.addChild(child.data());
                    treeMapping.put(child, copyChild);
                });
            }
            return true;
        });
        return root;
    }
}
