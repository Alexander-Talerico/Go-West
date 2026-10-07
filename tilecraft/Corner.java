// Francis Lopata 2026

package tilecraft;

public enum Corner {
    NW(0, 0),
    NE(0, 2),
    SE(2, 2),
    SW(2, 0);

    private final int row;
    private final int col;

    Corner(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public int getIndex() {
        return ordinal();
    }

    public Corner rotateCW() {
        return values()[(this.ordinal() + 1) % 4];
    }

    public Corner rotateCCW() {
        return values()[(this.ordinal() + 3) % 4];
    }

    public Edge getCounterClockwiseEdge() {
        return Edge.values()[(this.ordinal() + 3) % 4]; // NW -> WEST
    }

    public Edge getClockwiseEdge() {
        return Edge.values()[this.ordinal()]; // NW -> NORTH
    }
}