package grid;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import tilecraft.TerrainType;
import tilecraft.Tile;
import tilecraft.TileGenerator;

public class Grid extends JPanel {

    private static final int ROWS = 5;
    private static final int COLUMNS = 12;
    private static final int TILE_SIZE = 80;

    // The tiles currently placed on the board
    private Tile[][] board = new Tile[ROWS][COLUMNS];

    public Grid() {
        int width = COLUMNS * TILE_SIZE;
        int height = ROWS * TILE_SIZE;

        setPreferredSize(new Dimension(width, height));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                int column = event.getX() / TILE_SIZE;
                int row = event.getY() / TILE_SIZE;

                // Makes sure the clicked square is inside the grid
                if (row >= 0 && row < ROWS &&
                    column >= 0 && column < COLUMNS) {

                    // Right click rotates tile
                    if (event.getButton() == MouseEvent.BUTTON3) {
                        if (board[row][column] != null) {
                            board[row][column].rotateClockwise();
                            System.out.println(
                                "Rotated " + board[row][column]
                            );
                        }
                    }

                    // Left click places a new tile
                    else if (event.getButton() == MouseEvent.BUTTON1) {
                        if (board[row][column] == null) {
                            board[row][column] =
                                TileGenerator.generateTile();

                            System.out.println(
                                "Placed " + board[row][column]
                            );
                        }
                    }

                    repaint();
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLUMNS; column++) {

                int x = column * TILE_SIZE;
                int y = row * TILE_SIZE;

                if (board[row][column] == null) {
                    // Draw a grid square
                    if ((row + column) % 2 == 0) {
                        graphics.setColor(
                            new Color(194, 178, 128)
                        );
                    } else {
                        graphics.setColor(
                            new Color(170, 155, 110)
                        );
                    }

                    graphics.fillRect(
                        x,
                        y,
                        TILE_SIZE,
                        TILE_SIZE
                    );
                } else {
                    drawTile(
                        graphics,
                        board[row][column],
                        x,
                        y
                    );
                }

                graphics.setColor(Color.BLACK);
                graphics.drawRect(
                    x,
                    y,
                    TILE_SIZE,
                    TILE_SIZE
                );
            }
        }
    }

    private void drawTile(
            Graphics graphics,
            Tile tile,
            int x,
            int y) {

        // Every tile contains a smaller 3 by 3 grid
        for (int tileRow = 0; tileRow < 3; tileRow++) {
            for (int tileColumn = 0;
                 tileColumn < 3;
                 tileColumn++) {

                int smallSize = TILE_SIZE / 3;

                int smallX =
                    x + tileColumn * smallSize;

                int smallY =
                    y + tileRow * smallSize;

                TerrainType terrain =
                    tile.getCell(tileRow, tileColumn);

                graphics.setColor(getTerrainColor(terrain));

                graphics.fillRect(
                    smallX,
                    smallY,
                    smallSize,
                    smallSize
                );

                graphics.setColor(Color.DARK_GRAY);

                graphics.drawRect(
                    smallX,
                    smallY,
                    smallSize,
                    smallSize
                );
            }
        }
    }

    private Color getTerrainColor(TerrainType terrain) {
        if (terrain == TerrainType.FIELD) {
            return new Color(150, 190, 105);
        }

        if (terrain == TerrainType.SETTLEMENT) {
            return new Color(185, 95, 75);
        }

        if (terrain == TerrainType.ROAD) {
            return new Color(105, 90, 75);
        }

        if (terrain == TerrainType.GATE) {
            return new Color(215, 160, 55);
        }

        if (terrain == TerrainType.RAILROAD) {
            return new Color(70, 70, 70);
        }

        if (terrain == TerrainType.MOUNTAIN) {
            return new Color(125, 125, 115);
        }

        if (terrain == TerrainType.RIVER) {
            return new Color(65, 145, 210);
        }

        // Used for unknown terrains
        return Color.MAGENTA;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame window = new JFrame("Go West Grid");

            window.setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
            );

            window.add(new Grid());
            window.pack();
            window.setLocationRelativeTo(null);
            window.setResizable(false);
            window.setVisible(true);
        });
    }
}