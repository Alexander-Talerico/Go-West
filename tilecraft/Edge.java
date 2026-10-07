// Francis Lopata 2026

package tilecraft;

public enum Edge {
    NORTH(0, 1),
    EAST(1, 2),
    SOUTH(2, 1),
    WEST(1, 0);

    private final int row;
    private final int col;

    Edge(int row, int col) {
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

    public Edge rotateCW() {
        return values()[(this.ordinal() + 1) % 4];
    }

    public Edge rotateCCW() {
        return values()[(this.ordinal() + 3) % 4];
    }

    public Corner getCounterClockwiseCorner() {
        return Corner.values()[this.ordinal()]; // NORTH -> NW
    }

    public Corner getClockwiseCorner() {
        return Corner.values()[(this.ordinal() + 1) % 4]; // NORTH -> NE
    }
}