package tilecraft;

import java.util.Arrays;
import java.util.Random;

public class TileGenerator {

    private static final Random RNG = new Random();
    private static int counter = 1;

    public static Tile generateTile() {
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