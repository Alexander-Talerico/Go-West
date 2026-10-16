package tilecraft;

import java.util.Arrays;
import java.util.Random;

public class TileGenerator {

    private static final Random RNG = new Random();
    private static int counter = 1;

    private static Tile stagedTile = null;
    private static Runnable placementListener = null;

    // Used by GameState to pre-draw the player's tile for their turn
    public static Tile drawForTurn() {
        stagedTile = createRandomTile();
        return stagedTile;
    }

    // Called by Grid.java when the player clicks the board to place the tile
    public static Tile generateTile() {
        if (stagedTile != null) {
            Tile placed = stagedTile;
            stagedTile = null;
            return placed;
        }
        return createRandomTile();
    }

    public static void setPlacementListener(Runnable listener) {
        placementListener = listener;
    }

    public static void onTilePlaced() {
        if (placementListener != null) {
            placementListener.run();
        }
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