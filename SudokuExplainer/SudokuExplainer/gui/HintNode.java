package SudokuExplainer.gui;

import javax.swing.tree.*;
import SudokuExplainer.solver.*;

/**
 * A tree node representing a hint in the hints tree
 * of the user interface.
 */
public class HintNode extends DefaultMutableTreeNode {

    private static final long serialVersionUID = 7857073221166387482L;

    private final Hint hint;
    private String name;

    public HintNode(Hint hint) {
        super();
        this.hint = hint;
        this.name = hint.toString();
    }

    HintNode(String name) {
        super();
        this.hint = null;
        this.name = name;
    }

    public Hint getHint() {
        return this.hint;
    }

    public String getName() {
        return this.name;
    }

    public boolean isHintNode() {
        return this.hint != null;
    }

    @Override
    public boolean getAllowsChildren() {
        return !isHintNode();
    }

    private int getCountHints() {
        if (isHintNode())
            return 1;
        else {
            int result = 0;
            for (int i = 0; i < super.getChildCount(); i++) {
                HintNode child = (HintNode)super.getChildAt(i);
                result += child.getCountHints();
            }
            return result;
        }
    }

    public HintNode getNodeFor(Hint hint) {
        if (hint == null)
            return null;
        if (hint.equals(this.hint))
            return this;
        for (int i = 0; i < getChildCount(); i++) {
            HintNode child = (HintNode)getChildAt(i);
            HintNode result = child.getNodeFor(hint);
            if (result != null)
                return result;
        }
        return null;
    }

    void appendCountChildrenToName() {
        int count = getCountHints();
        this.name += " (" + count + " hint" + (count <= 1 ? ")" : "s)");
    }

    @Override
    public String toString() {
        return this.name;
    }

}
