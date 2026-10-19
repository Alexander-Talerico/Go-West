package tilecraft;

import java.util.Arrays;
import java.util.Random;
import java.util.function.Consumer;

public class TileGenerator {

    private static final Random RNG = new Random();
    private static int counter = 1;

    private static Tile stagedTile = null;
    private static Runnable placementListener = null;
    private static Consumer<String> networkSender = null;

    public static void setSeed(long seed) {
        RNG.setSeed(seed);
        counter = 1;
        stagedTile = null;
    }

    public static void setNetworkSender(Consumer<String> sender) {
        networkSender = sender;
    }

    public static void sendNetworkMessage(String message) {
        if (networkSender != null) {
            networkSender.accept(message);
        }
    }

    public static Tile drawForTurn() {
        stagedTile = createRandomTile();
        return stagedTile;
    }

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