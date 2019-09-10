package SudokuExplainer.solver;

import java.security.*;
import java.util.*;
import SudokuExplainer.*;
import SudokuExplainer.solver.checks.*;
import SudokuExplainer.solver.rules.*;
import SudokuExplainer.solver.rules.als.*;
import SudokuExplainer.solver.rules.chaining.*;
import SudokuExplainer.solver.rules.chaining.aic.*;
import SudokuExplainer.solver.rules.directed.*;
import SudokuExplainer.solver.rules.subset.*;
import SudokuExplainer.solver.rules.unique.*;
import SudokuExplainer.solver.rules.wing.*;
import SudokuExplainer.tools.*;
import SudokuExplainer.tools.tuples.Pair;
import SudokuExplainer.tools.tuples.Quad;
import SudokuExplainer.units.*;

/**
 * The solver for Sudoku grids.
 * Used to:
 * <ul>
 * <li>Build or rebuild the potential values of empty cells of a grid
 * <li>Get all available hints, excluding those requiring chaining rules
 * <li>Get the next available hint that follows a given list of hints, in
 * increasing order of difficulty
 * <li>Solve a grid using brute-force
 * <li>Solve a grid using logical hints, and get a rating of the grid as well as a
 * list of the rules that were used.
 * <li>Check the validity of a grid
 * </ul>
 * In all cases, a validity check is automatically enforced as soon as an invalid grid
 * would cause performance loss or any other problems.
 * <p>
 * The solving techniques themselves are implemented in the various classes of the
 * packages {@link SudokuExplainer.solver.rules}, {@link SudokuExplainer.solver.rules.chaining}
 * and {@link SudokuExplainer.solver.rules.unique}. Checks for validity are
 * implemented in classes of the package {@link SudokuExplainer.solver.checks}.
 */
@SuppressWarnings("CatchMayIgnoreException")
public class Solver {

    private static final String ADVANCED_WARNING1 =
        "This Sudoku seems to require advanced techniques\n" +
        "that may take a very long computing time.\n" +
        "Do you want to continue anyway?";
    private static final String ADVANCED_WARNING2 =
        "The next solving techniques are advanced ones\n" +
        "that may take a very long computing time.\n" +
        "Do you want to continue anyway?";

    private Grid grid;
    private List<HintProducer> directHintProducers;
    private List<IndirectHintProducer> indirectHintProducers;
    private List<WarningHintProducer> validatorHintProducers;
    private List<WarningHintProducer> warningHintProducers;
    private List<IndirectHintProducer> chainingHintProducers;
    private List<IndirectHintProducer> chainingHintProducers2;
    private List<IndirectHintProducer> advancedHintProducers;
    private List<IndirectHintProducer> experimentalHintProducers;

    private boolean isUsingAdvanced = false;


    private static class DefaultHintsAccumulator implements HintsAccumulator {

        private final List<Hint> result;

        private DefaultHintsAccumulator(List<Hint> result) {
            super();
            this.result = result;
        }

        public void add(Hint hint) {
            if (!result.contains(hint))
                result.add(hint);
        }

    } // class DefaultHintsAccumulator

    private void addIfWorth(SolvingTechnique technique, Collection<HintProducer> coll, HintProducer producer) {
        if (Settings.getInstance().getTechniques().contains(technique))
            coll.add(producer);
    }

    private void addIfWorth(SolvingTechnique technique, Collection<IndirectHintProducer> coll, IndirectHintProducer producer) {
        if (Settings.getInstance().getTechniques().contains(technique))
            coll.add(producer);
    }

    private void addDirectTechniques() {
        directHintProducers = new ArrayList<>();
        addIfWorth(SolvingTechnique.HiddenSingle, directHintProducers, new HiddenSingle());
        addIfWorth(SolvingTechnique.DirectPointing, directHintProducers, new Intersection(true));
        addIfWorth(SolvingTechnique.DirectHiddenPair, directHintProducers, new HiddenSubset(2, true));
        addIfWorth(SolvingTechnique.NakedSingle, directHintProducers, new NakedSingle());
        addIfWorth(SolvingTechnique.DirectHiddenTriple, directHintProducers, new HiddenSubset(3, true));
    }

    private void addIndirectTechniques() {
        indirectHintProducers = new ArrayList<>();
        addIfWorth(SolvingTechnique.LockedCandidates, indirectHintProducers, new Intersection(false));
        addIfWorth(SolvingTechnique.NakedPair, indirectHintProducers, new NakedSubset(2));
        addIfWorth(SolvingTechnique.XWing, indirectHintProducers, new Fisherman(2));
        addIfWorth(SolvingTechnique.HiddenPair, indirectHintProducers, new HiddenSubset(2, false));
        addIfWorth(SolvingTechnique.NakedTriple, indirectHintProducers, new NakedSubset(3));
        addIfWorth(SolvingTechnique.Swordfish, indirectHintProducers, new Fisherman(3));
        addIfWorth(SolvingTechnique.HiddenTriple, indirectHintProducers, new HiddenSubset(3, false));
        addIfWorth(SolvingTechnique.TurbotFish, indirectHintProducers, new TurbotFish());
        addIfWorth(SolvingTechnique.XYWing, indirectHintProducers, new XYWing(false));
        addIfWorth(SolvingTechnique.XYZWing, indirectHintProducers, new XYWing(true));
        addIfWorth(SolvingTechnique.WWing, indirectHintProducers, new WWing());
        //addIfWorth(SolvingTechnique.AlmostLockedPair, indirectHintProducers, new AlmostLockedPair());
        addIfWorth(SolvingTechnique.XYZWingExtension, indirectHintProducers, new WXYZWing(true));
        addIfWorth(SolvingTechnique.UniqueLoop, indirectHintProducers, new UniqueLoops());
        //addIfWorth(SolvingTechnique.UniqueRectangleExtension, indirectHintProducers, new UniqueRectangleExtension());
        addIfWorth(SolvingTechnique.WXYZWing, indirectHintProducers, new WXYZWing(false));
        addIfWorth(SolvingTechnique.WXYZWingExtension, indirectHintProducers, new VWXYZWing(true));
        addIfWorth(SolvingTechnique.VWXYZWing, indirectHintProducers, new VWXYZWing(false));
        addIfWorth(SolvingTechnique.NakedQuad, indirectHintProducers, new NakedSubset(4));
        //addIfWorth(SolvingTechnique.AlmostLockedTriple, indirectHintProducers, new AlmostLockedTriple());
        addIfWorth(SolvingTechnique.Jellyfish, indirectHintProducers, new Fisherman(4));
        addIfWorth(SolvingTechnique.HiddenQuad, indirectHintProducers, new HiddenSubset(4, false));
        addIfWorth(SolvingTechnique.BivalueUniversalGrave, indirectHintProducers, new BivalueUniversalGrave());
        addIfWorth(SolvingTechnique.AlignedPairExclusion, indirectHintProducers, new AlignedPairExclusion());
    }

    private void addChainDifficultyTechniques() {
        chainingHintProducers = new ArrayList<>();
        addIfWorth(SolvingTechnique.ForcingChainCycle, chainingHintProducers, new Chaining(false, false, false, 0));
        addIfWorth(SolvingTechnique.AlignedTripleExclusion, chainingHintProducers, new AlignedExclusion(3));
        //addIfWorth(SolvingTechnique.AlignedQuadExclusion, chainingHintProducers, new AlignedExclusion(4));
        //addIfWorth(SolvingTechnique.AlignedQuintExclusion, chainingHintProducers, new AlignedExclusion(5));
        addIfWorth(SolvingTechnique.NishioForcingChain, chainingHintProducers, new Chaining(false, true, true, 0));
        addIfWorth(SolvingTechnique.MultipleForcingChain, chainingHintProducers, new Chaining(true, false, false, 0));
        addIfWorth(SolvingTechnique.DynamicForcingChain, chainingHintProducers, new Chaining(true, true, false, 0));
        chainingHintProducers2 = new ArrayList<>();
        addIfWorth(SolvingTechnique.DynamicForcingChainPlus, chainingHintProducers2, new Chaining(true, true, false, 1));
    }

    private void checkValidityAndSolvablity() {
        // These rules are not really solving techs. They check the validity of the puzzle
        validatorHintProducers = new ArrayList<>();
        validatorHintProducers.add(new NoDoubles());
        warningHintProducers = new ArrayList<>();
        warningHintProducers.add(new NumberOfFilledCells());
        warningHintProducers.add(new NumberOfValues());
        warningHintProducers.add(new BruteForceAnalysis(false));
    }

    private void addNestedChainTechniques() {
        // These are very slow. We add them only as "rescue"
        advancedHintProducers = new ArrayList<>();
        addIfWorth(SolvingTechnique.NestedForcingChain, advancedHintProducers, new Chaining(true, true, false, 2));
        addIfWorth(SolvingTechnique.NestedForcingChain, advancedHintProducers, new Chaining(true, true, false, 3));
        experimentalHintProducers = new ArrayList<>(); // Two levels of nesting !?
        addIfWorth(SolvingTechnique.NestedForcingChain, experimentalHintProducers, new Chaining(true, true, false, 4));
        addIfWorth(SolvingTechnique.NestedForcingChain, experimentalHintProducers, new Chaining(true, true, false, 5));
    }

    public Solver(Grid grid) {
        this.grid = grid;
        addDirectTechniques();
        addIndirectTechniques();
        addChainDifficultyTechniques();
        checkValidityAndSolvablity();
        addNestedChainTechniques();
    }

    /**
     * This is the basic Sudoku rule: If a cell contains a value,
     * that value can be removed from the potential values of
     * all cells in the same block, row or column.
     * @param partType the Class of the part to cancel in
     * (block, row or column)
     */
    private <T extends Grid.Region> void cancelBy(Class<T> partType) {
        Grid.Region[] parts = grid.getRegions(partType);
        for (Grid.Region part : parts) {
            for (int i = 0; i < 9; i++) {
                Cell cell = part.getCell(i);
                if (!cell.isEmpty()) {
                    int value = cell.getValue();
                    // Remove the cell value from the potential values of other cells
                    for (int j = 0; j < 9; j++)
                        part.getCell(j).removePotentialValue(value);
                }
            }
        }
    }

    /**
     * Rebuild, for each empty cell, the set of potential values.
     */
    public void rebuildPotentialValues() {
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                Cell cell = grid.getCell(x, y);
                if (cell.getValue() == 0) {
                    for (int value = 1; value <= 9; value++)
                        cell.addPotentialValue(value);
                }
            }
        }
        cancelPotentialValues();
    }

    /**
     * Remove all illegal potential values according
     * to the current values of the cells.
     * Can be invoked after a new cell gets a value.
     */
    public void cancelPotentialValues() {
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                Cell cell = grid.getCell(x, y);
                if (cell.getValue() != 0)
                    cell.clearPotentialValues();
            }
        }
        cancelBy(Grid.Block.class);
        cancelBy(Grid.Row.class);
        cancelBy(Grid.Column.class);
    }

    /**
     * Lower the current thread's priority.
     * @return the previous thread's priority
     */
    private int lowerPriority() {
        try {
            int result = Thread.currentThread().getPriority();
            Thread.currentThread().setPriority((Thread.NORM_PRIORITY + Thread.MIN_PRIORITY * 2) / 3);
            return result;
        } catch (AccessControlException ignored) {}
        return 0;
    }

    /**
     * Reset the current thread's priority to the given value.
     * Typically, the given value is the value returned by
     * {@link #lowerPriority()}.
     * @param priority the new priority
     */
    private void normalPriority(int priority) {
        try {
            Thread.currentThread().setPriority(priority);
        } catch (AccessControlException ignored) {}
    }

    /**
     * Get the first available validity warning hint.
     * This can be used to check the validity of a
     * Sudoku grid. If the sudoku is valid, <code>null</code>
     * is returned; else, a warning hint.
     * @return a warning hint if the sudoku is invalid, <code>null</code>
     * if the sudoku is valid.
     */
    public Hint checkValidity() {
        int oldPriority = lowerPriority();
        SingleHintAccumulator accu = new SingleHintAccumulator();
        try {
            for (WarningHintProducer producer : validatorHintProducers)
                producer.getHints(grid, accu);
            for (WarningHintProducer producer : warningHintProducers)
                producer.getHints(grid, accu);
        } catch (InterruptedException willProbablyHappen) {}
        normalPriority(oldPriority);
        return accu.getHint();
    }

    private void gatherProducer(List<Hint> previousHints, List<Hint> curHints,
            HintsAccumulator accu, HintProducer producer) throws InterruptedException {
        // Get last hint producer. Because the last producer may not have produced
        // all its hints, we will need to restart from scratch with it.
        HintProducer lastProducer = null;
        if (!previousHints.isEmpty())
            lastProducer = previousHints.get(previousHints.size() - 1).getRule();

        if (curHints.size() < previousHints.size() && producer != lastProducer) {
            // Reuse previously computed hints of this producer
            Hint hint = previousHints.get(curHints.size());
            while (hint.getRule() == producer) {
                accu.add(hint);
                hint = previousHints.get(curHints.size());
            }
        } else {
            // Compute now
            producer.getHints(grid, accu);
        }
    }

    public void gatherHints(List<Hint> previousHints, final List<Hint> result,
            HintsAccumulator accu, Asker asker) {

        int oldPriority = lowerPriority();
        boolean isAdvanced = false;
        try {
            for (HintProducer producer : directHintProducers)
                gatherProducer(previousHints, result, accu, producer);
            for (HintProducer producer : indirectHintProducers)
                gatherProducer(previousHints, result, accu, producer);
            for (HintProducer producer : validatorHintProducers)
                gatherProducer(previousHints, result, accu, producer);
            if (result.isEmpty()) {
                for (HintProducer producer : warningHintProducers)
                    gatherProducer(previousHints, result, accu, producer);
            }
            for (HintProducer producer : chainingHintProducers)
                gatherProducer(previousHints, result, accu, producer);
            for (HintProducer producer : chainingHintProducers2)
                gatherProducer(previousHints, result, accu, producer);
            boolean hasWarning = false;
            for (Hint hint : result) {
                if (hint instanceof WarningHint) {
                    hasWarning = true;
                    break;
                }
            }
            // We have not been interrupted yet. So no rule has been found yet
            if (!hasWarning &&
                    !(advancedHintProducers.isEmpty() && experimentalHintProducers.isEmpty()) &&
                    (isUsingAdvanced || asker.ask(ADVANCED_WARNING2))) {
                isAdvanced = true;
                isUsingAdvanced = true;
                for (HintProducer producer : advancedHintProducers)
                    gatherProducer(previousHints, result, accu, producer);
                for (HintProducer producer : experimentalHintProducers) {
                    if (result.isEmpty() && Settings.getInstance().isUsingAllTechniques())
                        gatherProducer(previousHints, result, accu, producer);
                }
            }
        } catch (InterruptedException willProbablyHappen) {}
        if (!isAdvanced)
            isUsingAdvanced = false;
        normalPriority(oldPriority);
    }

    public List<Hint> getAllHints(Asker asker) {
        int oldPriority = lowerPriority();
        List<Hint> result = new ArrayList<>();
        HintsAccumulator accu = new DefaultHintsAccumulator(result);
        try {
            for (HintProducer producer : directHintProducers)
                producer.getHints(grid, accu);
            for (IndirectHintProducer producer : indirectHintProducers)
                producer.getHints(grid, accu);
            for (WarningHintProducer producer : validatorHintProducers)
                producer.getHints(grid, accu);
            if (result.isEmpty()) {
                for (WarningHintProducer producer : warningHintProducers)
                    producer.getHints(grid, accu);
            }
            if (result.isEmpty()) {
                for (IndirectHintProducer producer : chainingHintProducers)
                    producer.getHints(grid, accu);
            }
            if (result.isEmpty()) {
                for (IndirectHintProducer producer : chainingHintProducers2)
                    producer.getHints(grid, accu);
            }
            if (result.isEmpty() &&
                    !(advancedHintProducers.isEmpty() && experimentalHintProducers.isEmpty()) &&
                    (isUsingAdvanced || asker.ask(ADVANCED_WARNING2))) {
                isUsingAdvanced = true;
                for (IndirectHintProducer producer : advancedHintProducers) {
                    if (result.isEmpty())
                        producer.getHints(grid, accu);
                }
                for (IndirectHintProducer producer : experimentalHintProducers) {
                    if (result.isEmpty() && Settings.getInstance().isUsingAllTechniques())
                        producer.getHints(grid, accu);
                }
            }
        } catch (InterruptedException cannotHappen) {}
        normalPriority(oldPriority);
        return result;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isSolved() {
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                if (grid.getCellValue(x, y) == 0) {
                    return false;
                }
            }
        }
        return true;
    }

    private static class RuleComparer implements Comparator<Rule> {
        public int compare(Rule r1, Rule r2) {
            double d1 = r1.getDifficulty();
            double d2 = r2.getDifficulty();
            if (d1 < d2) return -1;
            else if (d1 > d2) return 1;
            else return r1.getName().compareTo(r2.getName());
        }
    }

    /**
     * Solve the Sudoku passed to the constructor.
     * <p>
     * Returns a sorted map between the rules that were used and
     * their frequency. Rules are sorted by difficulty.
     * @return the map between used rules and their frequency and difficulty info
     * @throws UnsupportedOperationException if the Sudoku cannot
     * be solved without recursive guessing (brute-force).
     */
    public Pair<Map<Rule,Integer>, Quad<Double,Double,Double,Integer>> solve(Asker asker) {
        int oldPriority = lowerPriority();
        // rebuildPotentialValues();
        Map<Rule,Integer> usedRules = new TreeMap<>(new RuleComparer());
        double difficulty, pearlDifficulty, diamondDifficulty;
        difficulty = pearlDifficulty = diamondDifficulty = 0.0;
        double tempDiff = 0.0;
        boolean diamondCheck = false;
        int stepCount = 0;

        boolean isUsingAdvanced = false;
        while (!isSolved()) {
            SingleHintAccumulator accu = new SingleHintAccumulator();
            try {
                for (HintProducer producer : directHintProducers)
                    producer.getHints(grid, accu);
                for (IndirectHintProducer producer : indirectHintProducers)
                    producer.getHints(grid, accu);
                for (IndirectHintProducer producer : chainingHintProducers)
                    producer.getHints(grid, accu);
                for (IndirectHintProducer producer : chainingHintProducers2)
                    producer.getHints(grid, accu);
                if (!(advancedHintProducers.isEmpty() && experimentalHintProducers.isEmpty()) &&
                        (asker == null || isUsingAdvanced || asker.ask(ADVANCED_WARNING1))) {
                    isUsingAdvanced = true;
                    for (IndirectHintProducer producer : advancedHintProducers)
                        producer.getHints(grid, accu);
                    for (IndirectHintProducer producer : experimentalHintProducers) {
                        if (Settings.getInstance().isUsingAllTechniques())
                            producer.getHints(grid, accu);
                    }
                }
            } catch (InterruptedException willHappen) {}
            Hint hint = accu.getHint();
            if (hint == null)
                throw new UnsupportedOperationException("Failed to solve this Sudoku");
            assert hint instanceof Rule;
            Rule rule = (Rule)hint;
            double ruleDiff = rule.getDifficulty();
            if (usedRules.containsKey(rule))
                usedRules.put(rule, usedRules.get(rule) + 1);
            else
                usedRules.put(rule, 1);
            hint.apply();

            if (stepCount++ == 0) {
                // Only for step 1
                pearlDifficulty = ruleDiff;
                if (ruleDiff <= 2.5) {
                    diamondDifficulty = ruleDiff;
                    diamondCheck = true;
                    continue;
                }
            }

            if (ruleDiff >= 2.5) {
                tempDiff = ruleDiff;
            }
            else if (!diamondCheck) {
                diamondDifficulty = tempDiff;
                diamondCheck = true;
            }
        }
        normalPriority(oldPriority);
        return new Pair<>(usedRules, new Quad<>(difficulty, pearlDifficulty, diamondDifficulty, stepCount));
    }

    /**
     * Get whether the grid's difficulty is between the two
     * bounds or not. If yes, return the actual difficulty.
     * If no, return a value less than <tt>min</tt> if the
     * grid is less difficult than <tt>min</tt> and a value
     * greater than <tt>max</tt> if the grid is more
     * difficult than <tt>max</tt>.
     * @param min the minimal difficulty (inclusive)
     * @param max the maximal difficulty (inclusive)
     * @return The actual difficulty if it is between the
     * given bounds. An arbitrary out-of-bounds value else.
     */
    public Quad<Double,Double,Double,Integer> analyseDifficulty(double min, double max) {
        int oldPriority = lowerPriority();
        try {
            boolean diamondCheck = false;
            double difficulty, pearlDifficulty, diamondDifficulty;
            difficulty = pearlDifficulty = diamondDifficulty = 0.0;
            double tempDiff = 0.0;
            int stepCount = 0;

            while (!isSolved()) {
                SingleHintAccumulator accu = new SingleHintAccumulator();
                try {
                    for (HintProducer producer : directHintProducers)
                        producer.getHints(grid, accu);
                    for (IndirectHintProducer producer : indirectHintProducers)
                        producer.getHints(grid, accu);
                    for (IndirectHintProducer producer : chainingHintProducers)
                        producer.getHints(grid, accu);
                    for (IndirectHintProducer producer : chainingHintProducers2)
                        producer.getHints(grid, accu);
                    // Only used for generator. Ignore advanced/experimental techniques
                } catch (InterruptedException willHappen) {}
                Hint hint = accu.getHint();
                if (hint == null) {
                    System.err.println("Failed to solve:\n" + grid.toString());
                    return new Quad<>(20d, 0d, 0d, 0);
                }
                assert hint instanceof Rule;
                Rule rule = (Rule)hint;
                double ruleDiff = rule.getDifficulty();
                if (ruleDiff > difficulty)
                    difficulty = ruleDiff;
                if (difficulty >= min && max >= 12.0)
                    break;
                if (difficulty > max)
                    break;
                hint.apply();

                if (stepCount++ == 0) {
                    // Only for step 1
                    pearlDifficulty = ruleDiff;
                }
                if (ruleDiff >= 2.5) {
                    tempDiff = ruleDiff;
                }
                else if (!diamondCheck) {
                    diamondDifficulty = tempDiff;
                    diamondCheck = true;
                }
            }
            return new Quad<>(difficulty, pearlDifficulty, diamondDifficulty, stepCount);
        } finally {
            normalPriority(oldPriority);
        }
    }

    public Pair<Double, Double> getDifficulty() {
        Grid backup = new Grid();
        grid.copyTo(backup);
        try {
            double difficulty = Double.NEGATIVE_INFINITY;
            double total = 0d;

            while (!isSolved()) {
                SingleHintAccumulator accu = new SingleHintAccumulator();
                try {
                    for (HintProducer producer : directHintProducers)
                        producer.getHints(grid, accu);
                    for (IndirectHintProducer producer : indirectHintProducers)
                        producer.getHints(grid, accu);
                    for (IndirectHintProducer producer : chainingHintProducers)
                        producer.getHints(grid, accu);
                    for (IndirectHintProducer producer : chainingHintProducers2)
                        producer.getHints(grid, accu);
                    for (IndirectHintProducer producer : advancedHintProducers)
                        producer.getHints(grid, accu);
                    for (IndirectHintProducer producer : experimentalHintProducers)
                        producer.getHints(grid, accu);
                } catch (InterruptedException willHappen) {}
                Hint hint = accu.getHint();
                if (hint == null) {
                    System.err.println("Failed to solve:\n" + grid.toString());
                    return new Pair<>(20.0, 0d);
                }
                assert hint instanceof Rule;
                Rule rule = (Rule)hint;
                double ruleDiff = rule.getDifficulty();
                total += ruleDiff;
                if (ruleDiff > difficulty)
                    difficulty = ruleDiff;
                hint.apply();
            }
            return new Pair<>(difficulty, total);
        } finally {
            backup.copyTo(grid);
        }
    }

    public Map<String, Integer> toNamedList(Map<Rule, Integer> rules) {
        Map<String, Integer> hints = new LinkedHashMap<>();
        for (Rule rule : rules.keySet()) {
            int count = rules.get(rule);
            String name = rule.getName();
            if (hints.containsKey(name))
                hints.put(name, hints.get(name) + count);
            else
                hints.put(name, count);
        }
        return hints;
    }

    public Hint analyse(Asker asker) {
        Grid copy = new Grid();
        grid.copyTo(copy);
        try {
            SingleHintAccumulator accu = new SingleHintAccumulator();
            try {
                for (WarningHintProducer producer : validatorHintProducers)
                    producer.getHints(grid, accu);
                for (WarningHintProducer producer : warningHintProducers)
                    producer.getHints(grid, accu);
                Analyser engine = new Analyser(this, asker);
                engine.getHints(grid, accu);
            } catch (InterruptedException willProbablyHappen) {}
            return accu.getHint();
        } finally {
            copy.copyTo(grid);
        }
    }

    public Hint bruteForceSolve() {
        SingleHintAccumulator accu = new SingleHintAccumulator();
        try {
            for (WarningHintProducer producer : validatorHintProducers)
                producer.getHints(grid, accu);
            Solution engine = new Solution();
            engine.getHints(grid, accu);
        } catch (InterruptedException willProbablyHappen) {}
        return accu.getHint();
    }

}
