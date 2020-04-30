package algorithm;

import tree.AbstractTree;

import java.awt.geom.Point2D;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class CoordinateTree<DATA, TREE extends AbstractTree<DATA, TREE>> extends AbstractTree<DATA, CoordinateTree<DATA, TREE>> {

    Point2D.Float location;

    public float preliminaryX;
    public float modifier;

    public float shift;
    public float change;

    public CoordinateTree<DATA, TREE> thread;
    public CoordinateTree<DATA, TREE> ancestor;

    protected CoordinateTree(TREE tree) {
        this(tree.data(), null);
    }

    protected CoordinateTree(DATA data, CoordinateTree<DATA, TREE> parent) {
        super(data, parent, null);
        this.location = new Point2D.Float();

        this.modifier = 0.0f;

        this.shift = 0.0f;
        this.change = 0.0f;

        this.thread = null;
        this.ancestor = this;
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

    public static <D, T extends AbstractTree<D, T>> CoordinateTree<D, T> from(T tree) {
        CoordinateTree<D, T> root = new CoordinateTree<>(tree);
        return from(tree, root);
    }
}
