package tree;

import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Supplier;

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

    public abstract TREE addChild(DATA data);

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

    public int level() {
        return this.level;
    }

    public TREE getLeftChild() {
        return this.children.get(0);
    }

    public TREE getRightChild() {
        return this.children.get(this.children.size() - 1);
    }

    public Optional<TREE> getLeftmost(int targetLevel) {
        return getLeftOrRightMost(targetLevel, this::getLeftChild);
    }

    public Optional<TREE> getRightmost(int targetLevel) {
        return getLeftOrRightMost(targetLevel, this::getRightChild);
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
        Stack<TREE> visitNext = new Stack<>();
        Stack<TREE> visitStack = new Stack<>();
        visitNext.push((TREE) this);
        visitStack.push((TREE) this);
        while (!visitNext.empty()) {
            TREE head = visitNext.pop();
            if (head.children != null) {
                for (int i = head.children.size()-1; i >= 0; i--) {
                    TREE child = head.children.get(i);
                    visitNext.push(child);
                    visitStack.push(child);
                }
            }
        }
        while (!visitStack.isEmpty()) {
            if (!visitor.accept(visitStack.pop())) return;
        }
    }

    private Optional<TREE> getLeftOrRightMost(int targetLevel, Supplier<TREE> supplier) {
        if (targetLevel <= this.level && targetLevel != -1) {
            throw new IllegalArgumentException("Parameter targetLevel must be greater than the current level");
        }
        if (this.children == null) {
            if (targetLevel == -1) {
                return Optional.of((TREE) this);
            }
            return Optional.empty();
        }
        if(this.level == targetLevel - 1) {
            return Optional.of(supplier.get());
        }

        return supplier.get().getLeftmost(targetLevel);
    }
}
