package algorithm;

import tree.AbstractTree;
import tree.AbstractTree.TraverseStrategy;

public class Walker<D, T extends AbstractTree<D, T>> {

    public static final float SIBLING_SPACING = 4.0f;
    public static final float SUBTREE_SEPARATION = 4.0f;
    public static final float MEAN_NODE_SIZE = 2.0f;

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
                if (t.leftSibling() != null) {
                    t.preliminaryX = t.leftSibling().preliminaryX +
                            SIBLING_SPACING +
                            MEAN_NODE_SIZE;
                } else {
                    t.preliminaryX = 0.0f;
                }
            } else {
                float midPoint = (t.getLeftChild().preliminaryX + t.getRightChild().preliminaryX) / 2;
                if (t.leftSibling() == null) {
                    t.preliminaryX = midPoint;
                } else {
                    t.preliminaryX = t.leftSibling().preliminaryX + SIBLING_SPACING + MEAN_NODE_SIZE;
                    t.modifier = t.preliminaryX - midPoint;
                    apportion(t);
                }
            }
            return true;
        });
    }

    private void apportion(CoordinateWrappedTree<D, T> treeNode) {
        CoordinateWrappedTree<D, T> leftMost = treeNode.getLeftChild();
        CoordinateWrappedTree<D, T> leftNeighbor;

        int baseLevel = treeNode.level();
        for (int level = baseLevel; level < tree.maxDepth(); level++) {
            leftNeighbor = tree.getLeftNeighbor(leftMost).orElse(null);
            if (leftMost == null || leftNeighbor == null) return;
            float leftModsum = 0.0f;
            float rightModsum = 0.0f;
            CoordinateWrappedTree<D, T> ancestorLeftmost = leftMost;
            CoordinateWrappedTree<D, T> ancestorNeighbor = leftNeighbor;
            while(ancestorLeftmost != treeNode) {
                ancestorLeftmost = ancestorLeftmost.parent();
                ancestorNeighbor = ancestorNeighbor.parent();
                rightModsum += ancestorLeftmost.modifier;
                leftModsum += ancestorNeighbor.modifier;
            }

            float moveDistance = (leftNeighbor.preliminaryX +
                    leftModsum +
                    SUBTREE_SEPARATION +
                    MEAN_NODE_SIZE) -
                    (leftMost.preliminaryX +
                    rightModsum);

            if (moveDistance > 0) {
                CoordinateWrappedTree<D, T> tempNode = treeNode;
                int numLeftSiblings = 0;
                while (tempNode != null && tempNode != ancestorNeighbor) {
                    numLeftSiblings++;
                    tempNode = tempNode.leftSibling();
                }
                if (tempNode != null) {
                    float portion = moveDistance / numLeftSiblings;
                    tempNode = treeNode;
                    while(tempNode != ancestorNeighbor) {
                        tempNode.preliminaryX += moveDistance;
                        tempNode.modifier += moveDistance;
                        moveDistance -= portion;
                        tempNode = tempNode.leftSibling();
                    }
                } else {
                    return;
                }
            }

            if (leftMost.children() == null) {
                leftMost = treeNode.getLeftmost(level + 2).orElse(null);
            } else {
                leftMost = leftMost.getLeftChild();
            }
        }
    }

    private void setLocations() {
        tree.traverse(TraverseStrategy.DEPTH_FIRST, (t) -> {
            float modifier = 0.0f;
            if (t.parent() != null) {
                modifier += t.parent().modifier;
                t.modifier += modifier;
            }
            t.setLocation(t.preliminaryX + modifier, t.level());
            return true;
        });
    }
}
