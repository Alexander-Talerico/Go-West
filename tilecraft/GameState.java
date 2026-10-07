package tilecraft;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameState {

    private final List<Player> players = new ArrayList<>();
    private int currentPlayerIndex = 0;
    private int turnCount = 1;
    private Tile currentTile;

    public GameState(int numberOfPlayers) {
        if (numberOfPlayers < 1) {
            throw new IllegalArgumentException("Must have at least 1 player.");
        }

        // Distinct colors for player identification
        Color[] defaultColors = {
            new Color(30, 90, 180),   // Player 1 (Blue)
            new Color(190, 40, 40),   // Player 2 (Red)
            new Color(30, 140, 40),   // Player 3 (Green)
            new Color(180, 120, 20),  // Player 4 (Gold)
            new Color(120, 40, 160)   // Player 5 (Purple)
        };

        for (int i = 0; i < numberOfPlayers; i++) {
            Color color = defaultColors[i % defaultColors.length];
            players.add(new Player(i + 1, "Player " + (i + 1), color));
        }

        // Initial draw for turn 1
        drawNewTile();
    }

    public void drawNewTile() {
        this.currentTile = TileGenerator.generateTile();
    }

    public void endTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        turnCount++;
        drawNewTile();
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public Tile getCurrentTile() {
        return currentTile;
    }

    public int getTurnCount() {
        return turnCount;
    }

    public int getPlayerCount() {
        return players.size();
    }

    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }
}