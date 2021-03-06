package tree;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractTree<DATA, TREE extends AbstractTree<DATA, TREE>> {

    public enum TraverseStrategy {
        DEPTH_FIRST,
        DEPTH_FIRST_REVERSE,
        BREADTH_FIRST,
        BREADTH_FIRST_REVERSE,
        POST_ORDER
    }

    protected final DATA data;

    protected TREE parent;
    protected List<TREE> children;
    protected TREE leftSibling;

    protected final int level;

    public AbstractTree(DATA data, TREE parent, List<TREE> children) {
        this.data = data;
        this.parent = parent;
        this.children = children;

        if(parent == null) {
            this.level = 0;
        } else {
            this.level = parent.level + 1;
        }
    }

    public TREE addChild(DATA data) {
        return addChild(createTreeNode(data));
    }

    public TREE addChild(TREE child) {
        if (this.children == null) {
            this.children = new ArrayList<>();
        } else {
            child.leftSibling = this.children.get(this.children.size() - 1);
        }
        child.parent = (TREE) this;
        this.children().add(child);
        return child;
    }

    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

    protected abstract TREE createTreeNode(DATA data);

    public void traverse(TraverseStrategy traverseStrategy, TreeVisitor<DATA, TREE> visitor) {
        switch (traverseStrategy) {
            case DEPTH_FIRST: {
                depthFirstTraversal(visitor, false);
                break;
            }
            case DEPTH_FIRST_REVERSE: {
                depthFirstTraversal(visitor, true);
                break;
            }
            case BREADTH_FIRST: {
                breadthFirstTraversal(visitor, false);
                break;
            }
            case BREADTH_FIRST_REVERSE: {
                breadthFirstTraversal(visitor, true);
                break;
            }
            case POST_ORDER: {
                postOrderTraversal(visitor);
                break;
            }
        }
    }

    public DATA data() {
        return this.data;
    }

    public TREE parent() {
        return this.parent;
    }

    public List<TREE> children() {
        return this.children;
    }

    public TREE leftSibling() {
        return this.leftSibling;
    }

    public TREE leftmostSibling() {
        TREE sibling = (TREE) this;
        while (sibling.leftSibling != null) {
            sibling = sibling.leftSibling;
        }
        return sibling;
    }

    public Optional<TREE> getLeftNeighbor(TREE node) {
        AtomicBoolean nodeSeen = new AtomicBoolean(false);
        List<TREE> maybeResult = new ArrayList<>(1);
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

        TREE leftNeighbor = maybeResult.get(0);
        return leftNeighbor == null
                ? Optional.empty()
                : Optional.of(leftNeighbor);
    }

    public int level() {
        return this.level;
    }

    public int maxDepth() {
        TREE tree = (TREE) this;
        if (tree.children() == null) return 1;

        return tree.children().stream().mapToInt(AbstractTree::maxDepth).max().getAsInt() + 1;
    }

    public TREE getLeftChild() {
        return this.children.get(0);
    }

    public TREE getRightChild() {
        return this.children.get(this.children.size() - 1);
    }

    public Optional<TREE> getLeftmost(int targetLevel) {
        return getLeftOrRightMost(targetLevel, true);
    }

    public Optional<TREE> getRightmost(int targetLevel) {
        return getLeftOrRightMost(targetLevel, false);
    }

    void depthFirstTraversal(TreeVisitor visitor, boolean reverseChildren) {
        Stack<TREE> visitNext = new Stack<>();
        visitNext.push((TREE) this);
        while (!visitNext.empty()) {
            TREE head = visitNext.pop();
            if (!visitor.accept(head)) return;

            if (head.children != null) {
                for (int i = 0; i < head.children.size(); i++) {
                    int index = reverseChildren
                            ? i
                            : head.children.size() - i - 1;
                    visitNext.push(head.children.get(index));
                }
            }
        }
    }

    void breadthFirstTraversal(TreeVisitor visitor, boolean reverseChildren) {
        Queue<TREE> visitNext = new LinkedBlockingQueue<>();
        visitNext.add((TREE) this);
        while (!visitNext.isEmpty()) {
            TREE head = visitNext.remove();
            if (!visitor.accept(head)) return;

            if (!(head.children == null)) {
                for (int i = 0; i < head.children.size(); i++) {
                    int index = reverseChildren
                            ? head.children.size() - i - 1
                            : i;
                    visitNext.offer(head.children.get(index));
                }
            }
        }
    }

    void postOrderTraversal(TreeVisitor visitor) {
        TREE tree = (TREE) this;
        if (tree.children() != null) {
            for (int i = 0; i < tree.children().size(); i++) {
                tree.children().get(i).postOrderTraversal(visitor);
            }
        }
        visitor.accept(tree);
    }

    private Optional<TREE> getLeftOrRightMost(int targetLevel, boolean isLeftMost) {
        targetLevel = targetLevel == -1 ? maxDepth() - 1 : targetLevel;
        if (targetLevel <= this.level && targetLevel != -1) {
            throw new IllegalArgumentException("Parameter targetLevel must be greater than the current level");
        }

        Stack<TREE> visitNext = new Stack<>();
        visitNext.push((TREE) this);
        while (!visitNext.empty()) {
            TREE head = visitNext.pop();
            if (head.level() == targetLevel) {
                return Optional.of(head);
            }
            if (head.children != null) {
                for (int i = 0; i < head.children.size(); i++) {
                    int index = isLeftMost
                            ? head.children.size() - i - 1
                            : i;
                    TREE child = head.children.get(index);
                    visitNext.push(child);
                }
            }
        }
        return Optional.empty();
    }

    public static <D, TREE_IN extends AbstractTree<D, TREE_IN>, TREE_OUT extends AbstractTree<D, TREE_OUT>> TREE_OUT from(TREE_IN tree, TREE_OUT root) {
        Map<TREE_IN, TREE_OUT> treeMapping = new HashMap<>();
        treeMapping.put(tree, root);

        tree.traverse(TraverseStrategy.BREADTH_FIRST, (t) -> {
            if (t.children() != null) {
                t.children().forEach((child) -> {
                    TREE_OUT copyParent = treeMapping.get(child.parent());
                    TREE_OUT copyChild = copyParent.addChild(child.data());
                    treeMapping.put(child, copyChild);
                });
            }
            return true;
        });
        return root;
    }
}
