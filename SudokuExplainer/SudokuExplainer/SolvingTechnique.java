package SudokuExplainer;

// Suppress spelling check if need.
//@SuppressWarnings("SpellCheckingInspection")
public enum SolvingTechnique {
    HiddenSingle("Hidden Single"),
    DirectPointing("Direct Pointing"),
    DirectHiddenPair("Direct Hidden Pair"),
    NakedSingle("Naked Single"),
    DirectHiddenTriple("Direct Hidden Triple"),
    LockedCandidates("Pointing & Claiming"),
    NakedPair("Naked Pair"),
    XWing("X-Wing"),
    HiddenPair("Hidden Pair"),
    NakedTriple("Naked Triple"),
    Swordfish("Swordfish"),
    HiddenTriple("Hidden Triple"),
    TurbotFish("Turbot Fish"),
    XYWing("XY-Wing"),
    XYZWing("XYZ-Wing"),
    WWing("W-Wing"),
    AlmostLockedPair("Almost Locked Pair"), // under construction...
    XYZWingExtension("XYZ-Wing Extension"),
    WXYZWing("WXYZ-Wing"),
    WXYZWingExtension("WXYZ-Wing Extension"),
    VWXYZWing("VWXYZ-Wing"),
    UniqueLoop("Unique Rectangle / Loop"),
    UniqueRectangleExtension("Unique Rectangle Extension"), // under construction...
    AlmostLockedTriple("Almost Locked Triple"), // under construction...
    NakedQuad("Naked Quadruple"),
    Jellyfish("Jellyfish"),
    HiddenQuad("Hidden Quadruple"),
    BivalueUniversalGrave("Bivalue Universal Grave"),
    AlignedPairExclusion("Aligned Pair Exclusion"),
    ForcingChainCycle("Forcing Chains & Cycles"),
    AlignedTripleExclusion("Aligned Triple Exclusion"),
    NishioForcingChain("Nishio Forcing Chains"),
    //AlignedQuadrupleExclusion("Aligned Quadruple Exclusion"), // very slow...
    //AlignedQuintupleExclusion("Aligned Quintuple Exclusion"), // extremely slow...
    MultipleForcingChain("Multiple Forcing Chains"),
    DynamicForcingChain("Dynamic Forcing Chains"),
    DynamicForcingChainPlus("Dynamic Forcing Chains (+)"),
    NestedForcingChain("Nested Forcing Chains");

    private final String name;

    SolvingTechnique(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

}
