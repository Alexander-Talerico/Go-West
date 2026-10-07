package tilecraft;

import java.util.Objects;

public class Tile {

    private final String id;
    private final TileDeck archetype;
    private TerrainType[][] grid = new TerrainType[3][3];
    private Edge facing = Edge.NORTH;

    public Tile(String id, TileDeck archetype) {
        this.id = Objects.requireNonNull(id);
        this.archetype = Objects.requireNonNull(archetype);
        this.grid = archetype.createCopy();
    }

    public void rotateClockwise() {
        TerrainType[][] rotated = new TerrainType[3][3];
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                rotated[c][2 - r] = this.grid[r][c];
            }
        }
        this.grid = rotated;
        this.facing = this.facing.rotateCW();
    }

    public void rotateCounterClockwise() {
        TerrainType[][] rotated = new TerrainType[3][3];
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                rotated[2 - c][r] = this.grid[r][c];
            }
        }
        this.grid = rotated;
        this.facing = this.facing.rotateCCW();
    }

    public TerrainType getCell(int row, int col) {
        return grid[row][col];
    }

    public TerrainType getEffectiveEdge(Edge edge) {
        return grid[edge.getRow()][edge.getCol()];
    }

    public TerrainType getEffectiveCorner(Corner corner) {
        return grid[corner.getRow()][corner.getCol()];
    }

    public TerrainType getCenter() {
        return grid[1][1];
    }

    public TileDeck getArchetype() {
        return archetype;
    }

    public String getId() {
        return id;
    }

    public Edge getFacing() {
        return facing;
    }

    @Override
    public String toString() {
        return "%s [%s] (Facing: %s)".formatted(id, archetype.name(), facing);
    }
}