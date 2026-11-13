package tilecraft;

import java.util.Arrays;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.BooleanSupplier;
import grid.Grid;

public class TileGenerator {

    private static final Random RNG = new Random();
    private static int counter = 1;

    private static Tile stagedTile = null;
    private static Runnable placementListener = null;
    private static BooleanSupplier canPlaceCheck = null;
    private static Consumer<String> networkSender = null;
    private static Grid activeGrid = null;

    public static void setSeed(long seed) {
        RNG.setSeed(seed);
        counter = 1;
        stagedTile = null;
    }

    public static void setGrid(Grid grid) {
        activeGrid = grid;
    }

    public static void setCanPlaceCheck(BooleanSupplier check) {
        canPlaceCheck = check;
    }

    public static boolean canPlaceTileNow() {
        return canPlaceCheck == null || canPlaceCheck.getAsBoolean();
    }

    public static void setNetworkSender(Consumer<String> sender) {
        networkSender = sender;
    }

    public static void sendNetworkMessage(String message) {
        if (networkSender != null) {
            networkSender.accept(message);
        }
    }

    public static Tile getCurrentTile() {
        return stagedTile;
    }

    public static Tile drawForTurn() {
        stagedTile = createValidRandomTile();
        return stagedTile;
    }

    public static Tile generateTile() {
        if (stagedTile != null) {
            Tile placed = stagedTile;
            stagedTile = null;
            return placed;
        }
        return createValidRandomTile();
    }

    public static void setPlacementListener(Runnable listener) {
        placementListener = listener;
    }

    public static void onTilePlaced() {
        if (placementListener != null) {
            placementListener.run();
        }
    }

    private static Tile createValidRandomTile() {
        Tile[][] board = (activeGrid != null) ? activeGrid.getBoard() : null;

        if (board == null || isBoardEmpty(board)) {
            for (int attempts = 0; attempts < 1000; attempts++) {
                Tile candidate = createRandomTile();
                if (hasRoad(candidate)) {
                    return candidate;
                }
            }
            return createRandomTile();
        }

        for (int attempts = 0; attempts < 1000; attempts++) {
            Tile candidate = createRandomTile();
            if (BoardValidator.hasValidPlacement(board, candidate)) {
                return candidate;
            }
        }

        return createRandomTile();
    }

    private static boolean isBoardEmpty(Tile[][] board) {
        for (Tile[] row : board) {
            for (Tile cell : row) {
                if (cell != null) return false;
            }
        }
        return true;
    }

    private static boolean hasRoad(Tile tile) {
        if (tile.getCenter() == TerrainType.ROAD || tile.getCenter() == TerrainType.GATE) {
            return true;
        }
        for (Edge e : Edge.values()) {
            if (tile.getEffectiveEdge(e) == TerrainType.ROAD) {
                return true;
            }
        }
        return false;
    }

    private static Tile createRandomTile() {
        int totalWeight = Arrays.stream(TileDeck.values())
                .mapToInt(TileDeck::getWeight)
                .sum();

        int roll = RNG.nextInt(totalWeight);
        int runningTotal = 0;

        for (TileDeck card : TileDeck.values()) {
            runningTotal += card.getWeight();
            if (roll < runningTotal) {
                return new Tile("T-" + (counter++), card);
            }
        }

        return new Tile("T-" + (counter++), TileDeck.PURE_FIELD);
    }
}