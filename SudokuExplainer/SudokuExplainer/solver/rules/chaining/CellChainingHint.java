package SudokuExplainer.solver.rules.chaining;

import java.util.*;
import SudokuExplainer.units.*;
import SudokuExplainer.units.Grid.*;
import SudokuExplainer.solver.*;
import SudokuExplainer.tools.*;

/**
 * Cell Forcing Chain hint.
 */
public class CellChainingHint extends ChainingHint {

    private final Cell srcCell;
    private final LinkedHashMap<Integer, Potential> chains; // cell value -> outcome


    public CellChainingHint(IndirectHintProducer rule,
            Map<Cell, BitSet> removablePotentials, Cell srcCell,
            LinkedHashMap<Integer, Potential> chains) {
        super(rule, removablePotentials, true, true);
        this.srcCell = srcCell;
        this.chains = chains;
    }

    private int getValue(int index) {
        Iterator<Integer> iterator = chains.keySet().iterator();
        while (index > 0) {
            iterator.next();
            index--;
        }
        return iterator.next();
    }

    private Potential getTargetPotential(int viewNum) {
        int value = getValue(viewNum);
        return chains.get(value);
    }

    @Override
    public int getFlatViewCount() {
        return chains.size();
    }

    @Override
    public Cell[] getSelectedCells() {
        Cell dstCell = chains.values().iterator().next().cell;
        return new Cell[] {srcCell, dstCell};
    }

    @Override
    public Map<Cell, BitSet> getGreenPotentials(int viewNum) {
        if (viewNum >= getFlatViewCount())
            return super.getNestedGreenPotentials(viewNum);
        Potential target = getTargetPotential(viewNum);
        return super.getColorPotentials(target, true, true);
    }

    @Override
    public Map<Cell, BitSet> getRedPotentials(int viewNum) {
        if (viewNum >= getFlatViewCount())
            return super.getNestedRedPotentials(viewNum);
        Potential target = getTargetPotential(viewNum);
        return super.getColorPotentials(target, false, false);
    }

    @Override
    public Collection<Link> getLinks(int viewNum) {
        if (viewNum >= getFlatViewCount())
            return super.getNestedLinks(viewNum);
        Potential target = getTargetPotential(viewNum);
        return super.getLinks(target);
    }

    @Override
    protected Collection<Potential> getChainsTargets() {
        return Collections.unmodifiableCollection(chains.values());
    }

    @Override
    protected Potential getChainTarget(int viewNum) {
        return getTargetPotential(viewNum);
    }

    @Override
    public int getFlatComplexity() {
        int result = 0;
        for (Potential target : chains.values())
            result += super.getAncestorCount(target);
        return result;
    }

    @Override
    public int getSortKey() {
        return 5;
    }

    @Override
    public Region[] getRegions() {
        return null;
    }

    public double getDifficulty() {
        return getChainingRule().getDifficulty() + getLengthDifficulty();
    }

    public String getName() {
        String name = getChainingRule().getCommonName(this);
        if (name != null)
            return name;
        return super.getNamePrefix() + "Cell Forcing" + super.getNameSuffix();
    }

    @Override
    public Potential getResult() {
        return chains.values().iterator().next();
    }

    public String getClueHtml(boolean isBig) {
        if (isBig) {
            return "Look for a " + getName() +
                    " on the cell <b>" + srcCell.toString() + "</b>";
        } else {
            return "Look for a " + getName();
        }
    }

    @Override
    public String toString() {
        String prefix = getChainingRule().getCommonName(this);
        if (prefix == null)
            prefix = "Cell Forcing Chains";
        Potential dstPotential = chains.values().iterator().next();
        return prefix + ": " + srcCell.toString() + " => "
                + dstPotential.toString() + (dstPotential.isOn ? " on" : " off");
    }

    @Override
    public String toHtml() {
        String result;
        if (getChainingRule().isDynamic())
            result = HtmlLoader.loadHtml(this, "DynamicCellReductionHint.html");
        else
            result = HtmlLoader.loadHtml(this, "StaticCellReductionHint.html");
        StringBuilder assertions = new StringBuilder();
        for (Potential curTarget : chains.values()) {
            Potential curSource = getSrcPotential(curTarget);
            assertions.append("<li>If ")
                    .append(curSource.toWeakString())
                    .append(", then ")
                    .append(curTarget.toStrongString());
        }
        String cellName = srcCell.toString();
        Potential target = chains.values().iterator().next();
        String resultName = target.toStrongString();
        StringBuilder htmlChains = getChainsDetails();
        result = HtmlLoader.format(result, assertions.toString(), cellName, resultName, htmlChains);
        return super.appendNestedChainsDetails(result);
    }

    private StringBuilder getChainsDetails() {
        StringBuilder htmlChains = new StringBuilder();
        int index = 1;
        for (Potential curTarget : chains.values()) {
            Potential curSource = getSrcPotential(curTarget);
            htmlChains.append("Chain ")
                    .append(index)
                    .append(": <b>If ")
                    .append(curSource.toWeakString())
                    .append(", then ")
                    .append(curTarget.toStrongString())
                    .append("</b>")
                    .append(" (View ")
                    .append(index)
                    .append("):<br>\n");
            String curChain = getHtmlChain(curTarget);
            htmlChains.append(curChain);
            htmlChains.append("<br>\n");
            index++;
        }
        return htmlChains;
    }

}
