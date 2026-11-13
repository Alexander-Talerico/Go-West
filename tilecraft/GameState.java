package tilecraft;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameState {

    public static final int MAX_TURNS = 100;

    private final List<Player> players = new ArrayList<>();
    private int currentPlayerIndex = 0;
    private int turnCount = 1;
    private Tile currentTile;

    private boolean tilePlacedThisTurn = false;
    private boolean claimedThisTurn = false;
    private boolean gameOver = false;

    public GameState(int numberOfPlayers) {
        if (numberOfPlayers < 1) {
            throw new IllegalArgumentException("Must have at least 1 player.");
        }

        Color[] defaultColors = {
            new Color(30, 90, 180),   // Player 1 (Blue)
            new Color(190, 40, 40),   // Player 2 (Red)
            new Color(30, 140, 40),   // Player 3 (Green)
            new Color(180, 120, 20),  // Player 4 (Gold)
        };

        for (int i = 0; i < numberOfPlayers; i++) {
            Color color = defaultColors[i % defaultColors.length];
            players.add(new Player(i + 1, "Player " + (i + 1), color));
        }

        drawNewTile();
    }

    public void drawNewTile() {
        if (!gameOver) {
            this.currentTile = TileGenerator.drawForTurn();
        } else {
            this.currentTile = null;
        }
    }

    public void markTilePlaced() {
        this.tilePlacedThisTurn = true;
        this.currentTile = null;
    }

    public boolean isTilePlacedThisTurn() {
        return tilePlacedThisTurn;
    }

    public boolean hasClaimedThisTurn() {
        return claimedThisTurn;
    }

    public void markClaimedThisTurn() {
        this.claimedThisTurn = true;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void endTurn() {
        if (gameOver) return;

        if (turnCount >= MAX_TURNS) {
            gameOver = true;
            currentTile = null;
            return;
        }

        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        turnCount++;
        tilePlacedThisTurn = false;
        claimedThisTurn = false;
        drawNewTile();
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
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