package SudokuExplainer.generator;

/**
 * Enumeration of Sudoku grid's symmetries.
 */
public enum Symmetry {
    Vertical {
        @Override
        public Point[] getPoints(int x, int y) {
            return new Point[] {
                    new Point(x, y), 
                    new Point(8 - x, y)};
        }

        @Override
        public String getDescription() {
            return "Mirror symmetry around the vertical axis";
        }
    },
    Horizontal {
        @Override
        public Point[] getPoints(int x, int y) {
            return new Point[] {
                    new Point(x, y),
                    new Point(x, 8 - y)};
        }    

        @Override
        public String getDescription() {
            return "Mirror symmetry around the horizontal axis";
        }
    },
    Diagonal {
        @Override
        public Point[] getPoints(int x, int y) {
            return new Point[] {
                    new Point(x, y),
                    new Point(8 - y, 8 - x)};
        }    

        @Override
        public String getDescription() {
            return "Mirror symmetry around the raising diagonal";
        }
    },
    AntiDiagonal {
        @Override
        public Point[] getPoints(int x, int y) {
            return new Point[] {
                    new Point(x, y),
                    new Point(y, x)};
        }

        @Override
        public String toString() {
            return "Anti-diagonal";
        }

        @Override
        public String getDescription() {
            return "Mirror symmetry around the falling diagonal";
        }
    },
    BiDiagonal {
        @Override
        public Point[] getPoints(int x, int y) {
            return new Point[] {
                    new Point(x, y),
                    new Point(y, x),
                    new Point(8 - y, 8 - x),
                    new Point(8 - x, 8 - y)};
        }

        @Override
        public String toString() {
            return "Bi-diagonal";
        }

        @Override
        public String getDescription() {
            return "Mirror symmetries around both diagonals";
        }
    },
    Orthogonal {
        @Override
        public Point[] getPoints(int x, int y) {
            return new Point[] {
                    new Point(x, y),
                    new Point(8 - x, y),
                    new Point(x, 8 - y),
                    new Point(8 - x, 8 - y)};
        }    

        @Override
        public String getDescription() {
            return "Mirror symmetries around the horizontal and vertical axes";
        }
    },
    Rotational180 {
        @Override
        public Point[] getPoints(int x, int y) {
            return new Point[] {
                    new Point(x, y),
                    new Point(8 - x, 8 - y)};
        }

        @Override
        public String toString() {
            return "180-degree rotational";
        }

        @Override
        public String getDescription() {
            return "Symmetric under a 180-degree rotation (central symmetry)";
        }
    },
    Rotational90 {
        @Override
        public Point[] getPoints(int x, int y) {
            return new Point[] {
                    new Point(x, y),
                    new Point(8 - x, 8 - y),
                    new Point(y, 8 - x),
                    new Point(8 - y, x)};
        }

        @Override
        public String toString() {
            return "90-degree rotational";
        }

        @Override
        public String getDescription() {
            return "Symmetric under a 90-degree rotation";
        }
    },
    None {
        @Override
        public Point[] getPoints(int x, int y) {
            return new Point[] {
                    new Point(x, y)};
        }

        @Override
        public String getDescription() {
            return "No symmetry";
        }
    },
    Full {
        @Override
        public Point[] getPoints(int x, int y) {
            return new Point[] {
                    new Point(x, y),
                    new Point(8 - x, y),
                    new Point(x, 8 - y),
                    new Point(8 - x, 8 - y),
                    new Point(y, x),
                    new Point(8 - y, x),
                    new Point(y, 8 - x),
                    new Point(8 - y, 8 - x)};
        }    

        @Override
        public String getDescription() {
            return "All symmetries (around the 8 axes and under a 90-degree rotation)";
        }
    };

    public abstract Point[] getPoints(int x, int y);

    public abstract String getDescription();
}
