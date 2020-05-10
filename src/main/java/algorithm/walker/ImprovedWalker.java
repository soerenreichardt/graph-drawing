package algorithm.walker;

import algorithm.CoordinateTree;
import tree.AbstractTree;

import java.util.List;

import static algorithm.walker.Walker.*;

public class ImprovedWalker<D, T extends AbstractTree<D, T>> {

    private final CoordinateTree<D, T> tree;

    public ImprovedWalker(T tree) {
        this.tree = CoordinateTree.from(tree);
    }

    public CoordinateTree<D, T> compute() {
        firstWalk(tree);
        secondWalk(tree, 0.0f);
        return tree;
    }

    private void firstWalk(CoordinateTree<D, T> currentRoot) {
        if (!currentRoot.hasChildren()) {
            if (currentRoot.leftSibling() != null) {
                currentRoot.preliminaryX = currentRoot.leftSibling().preliminaryX + SIBLING_SPACING + MEAN_NODE_SIZE;
            } else {
                currentRoot.preliminaryX = 0.0f;
            }
        } else {
            var defaultAncestor = currentRoot.getLeftChild();
            for (var child : currentRoot.children()) {
                firstWalk(child);
                defaultAncestor = apportion(child, defaultAncestor);
            }
            executeShifts(currentRoot);

            float midPoint = (currentRoot.getLeftChild().preliminaryX + currentRoot.getRightChild().preliminaryX) * 0.5f;
            if (currentRoot.leftSibling() != null) {
                currentRoot.preliminaryX = currentRoot.leftSibling().preliminaryX + SIBLING_SPACING + MEAN_NODE_SIZE;
                currentRoot.modifier = currentRoot.preliminaryX - midPoint;
            } else {
                currentRoot.preliminaryX = midPoint;
            }
        }
    }

    private CoordinateTree<D, T> apportion(CoordinateTree<D, T> currentRoot, CoordinateTree<D, T> defaultAncestor) {
        if (currentRoot.leftSibling() != null) {
            var insideRight = currentRoot;
            var outsideRight = currentRoot;
            var insideLeft = currentRoot.leftSibling();
            var outsideLeft = insideRight.leftmostSibling();

            float modInsideRight = insideRight.modifier;
            float modOutsideRight = outsideRight.modifier;
            float modInsideLeft = insideLeft.modifier;
            float modOutsideLeft = outsideLeft.modifier;

            while (nextRight(insideLeft) != null && nextLeft(insideRight) != null) {
                insideLeft = nextRight(insideLeft);
                insideRight = nextLeft(insideRight);
                outsideLeft = nextLeft(outsideLeft);
                outsideRight = nextRight(outsideRight);

                outsideRight.ancestor = currentRoot;

                float shift = (insideLeft.preliminaryX + modInsideLeft) -
                        (insideRight.preliminaryX + modInsideRight) +
                        SUBTREE_SEPARATION +
                        MEAN_NODE_SIZE;
                if (shift > 0) {
                    moveSubTree(ancestor(insideLeft, currentRoot, defaultAncestor), currentRoot, shift);
                    modInsideRight += shift;
                    modOutsideRight += shift;
                }
                modInsideLeft += insideLeft.modifier;
                modInsideRight += insideRight.modifier;
                modOutsideLeft += outsideLeft.modifier;
                modOutsideRight += outsideRight.modifier;
            }

            if (nextRight(insideLeft) != null && nextRight(outsideRight) == null) {
                outsideRight.thread = nextRight(insideLeft);
                outsideRight.modifier += modInsideLeft - modOutsideRight;
            }
            if (nextLeft(insideRight) != null && nextLeft(outsideLeft) == null) {
                outsideLeft.thread = nextLeft(insideRight);
                outsideLeft.modifier += modInsideRight - modOutsideLeft;
                return currentRoot;
            }
        }

        return defaultAncestor;
    }

    private void secondWalk(CoordinateTree<D, T> node, float m) {
        node.setLocation(node.preliminaryX + m, node.level());
        if (node.hasChildren()) {
            for (var child : node.children()) {
                secondWalk(child, m + node.modifier);
            }
        }
    }

    private void executeShifts(CoordinateTree<D, T> node) {
        float shift = 0;
        float change = 0;

        List<CoordinateTree<D, T>> children = node.children();
        for (int i = 0; i < children.size(); i++) {
            CoordinateTree<D, T> child = children.get(children.size() - i - 1);
            child.preliminaryX += shift;
            child.modifier += shift;
            change += child.change;
            shift += child.shift + change;
        }
    }

    private void moveSubTree(CoordinateTree<D, T> left, CoordinateTree<D, T> right, float shift) {
        var children = left.parent().children();
        int subtrees = -1;
        for (CoordinateTree<D, T> child : children) {
            if (child == left) {
                subtrees = 0;
            }
            if (child == right) {
                break;
            }
            if (subtrees == -1) {
                continue;
            }
            subtrees++;
        }
        right.change -= shift / (float) subtrees;
        right.shift += shift;
        left.change += shift / (float) subtrees;
        right.preliminaryX += shift;
        right.modifier += shift;
    }

    private CoordinateTree<D, T> ancestor(CoordinateTree<D, T> insideLeft, CoordinateTree<D, T> currentRoot, CoordinateTree<D, T> defaultAncestor) {
        CoordinateTree<D, T> leftSibling = currentRoot.leftSibling();
        while (leftSibling != null) {
            if (leftSibling == insideLeft.ancestor) {
                return insideLeft.ancestor;
            }
            leftSibling =  leftSibling.leftSibling();
        }
        return defaultAncestor;
    }

    private CoordinateTree<D, T> nextLeft(CoordinateTree<D, T> node) {
        if (node.hasChildren()) {
            return node.getLeftChild();
        } else {
            return node.thread;
        }
    }

    private CoordinateTree<D, T> nextRight(CoordinateTree<D, T> node) {
        if (node.hasChildren()) {
            return node.getRightChild();
        } else {
            return node.thread;
        }
    }
}