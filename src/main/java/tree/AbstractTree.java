package tree;

import java.util.List;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class AbstractTree<DATA, TREE extends AbstractTree<DATA, TREE>> {

    public enum TraverseStrategy {
        DEPTH_FIRST,
        BREADTH_FIRST
    }

    protected final DATA data;

    protected TREE parent;
    protected List<TREE> children;

    public AbstractTree(DATA data, TREE parent, List<TREE> children) {
        this.data = data;
        this.parent = parent;
        this.children = children;
    }

    public abstract TREE addChild(DATA data);

    public void traverse(TraverseStrategy traverseStrategy, TreeVisitor<DATA, TREE> visitor) {
        switch (traverseStrategy) {
            case DEPTH_FIRST: {
                depthFirstTraversal(visitor);
                break;
            }
            case BREADTH_FIRST: {
                breadthFirstTraversal(visitor);
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

    public TREE getLeftChild() {
        return this.children.get(0);
    }

    public TREE getRightChild() {
        return this.children.get(this.children.size() - 1);
    }

    void depthFirstTraversal(TreeVisitor visitor) {
        Stack<TREE> visitNext = new Stack<>();
        visitNext.push((TREE) this);
        while (!visitNext.empty()) {
            TREE head = visitNext.pop();
            visitor.accept(head);

            if (head.children != null) {
                for (int i = head.children.size()-1; i >= 0; i--) {
                    visitNext.push((TREE) head.children.get(i));
                }
            }
        }
    }

    void breadthFirstTraversal(TreeVisitor visitor) {
        Queue<TREE> visitNext = new LinkedBlockingQueue<>();
        visitNext.add((TREE) this);
        while (!visitNext.isEmpty()) {
            TREE head = visitNext.remove();
            visitor.accept(head);

            if (!(head.children == null)) {
                visitNext.addAll(head.children);
            }
        }
    }
}
