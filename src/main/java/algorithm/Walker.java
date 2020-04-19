package algorithm;

import tree.AbstractTree;
import tree.AbstractTree.TraverseStrategy;

import java.util.Optional;

public class Walker<D, T extends AbstractTree<D, T>> {

    public static final float SIBLING_SPACING = 1.0f;

    private final CoordinateWrappedTree<D, T> tree;

    public Walker(T tree) {
        this.tree = CoordinateWrappedTree.from(tree);
    }

    public CoordinateWrappedTree<D, T> compute() {
        firstWalk();
        setLocations();
        return tree;
    }

    private void firstWalk() {
        tree.traverse(TraverseStrategy.POST_ORDER, (t) -> {
            if (t.children() == null) {
                if (t.leftSibling != null) {
                    t.preliminaryX = t.leftSibling.preliminaryX + SIBLING_SPACING;
                } else {
                    t.preliminaryX = 0.0f;
                }
            } else {
                float midPoint = (t.getLeftChild().preliminaryX + t.getRightChild().preliminaryX) / 2;
                if (t.leftSibling == null) {
                    t.preliminaryX = midPoint;
                } else {
                    t.preliminaryX = t.leftSibling.preliminaryX;
                    t.modifier = t.preliminaryX + midPoint;
                    // apportion
                }
            }
            return true;
        });
    }

    private void apportion(CoordinateWrappedTree<D, T> treeNode) {
        Optional<CoordinateWrappedTree<D, T>> maybeLeftNeighbor = tree.getLeftNeighbor(treeNode);
        if (maybeLeftNeighbor.isPresent() && treeNode.children() != null) {
            CoordinateWrappedTree<D, T> leftMost = treeNode.getLeftChild();


        }
    }

    private void setLocations() {
        tree.traverse(TraverseStrategy.BREADTH_FIRST, (t) -> {
            t.setLocation(t.preliminaryX + t.modifier, t.level());
            return true;
        });
    }
}
