package grid;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.net.URL;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import tilecraft.TerrainType;
import tilecraft.Tile;
import tilecraft.TileGenerator;

public class Grid extends JPanel {

    private static final int ROWS = 10;
    private static final int COLUMNS = 30;
    private static final int TILE_SIZE = 40;

    // Tiles currently placed on the board
    private Tile[][] board = new Tile[ROWS][COLUMNS];

    private int hoveredRow = -1;
    private int hoveredColumn = -1;

    private Clip placeSound;

    public Grid() {
        int width = COLUMNS * TILE_SIZE;
        int height = ROWS * TILE_SIZE;

        setPreferredSize(new Dimension(width, height));

        placeSound = loadSound(
            "/sounds/place-tile-sound.wav"
        );

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                int column = event.getX() / TILE_SIZE;
                int row = event.getY() / TILE_SIZE;

                if (row >= 0 && row < ROWS &&
                    column >= 0 && column < COLUMNS) {

                    // Shift and right click deletes a tile
                    if (event.getButton() == MouseEvent.BUTTON3
                            && event.isShiftDown()) {

                        if (board[row][column] != null) {
                            board[row][column] = null;
                            System.out.println("Tile deleted");
                        }
                    }

                    // Right click rotates a tile
                    else if (event.getButton()
                            == MouseEvent.BUTTON3) {

                        if (board[row][column] != null) {
                            board[row][column]
                                .rotateClockwise();

                            System.out.println(
                                "Rotated "
                                + board[row][column]
                            );
                        }
                    }

                    // Left click places a tile
                    else if (event.getButton()
                            == MouseEvent.BUTTON1) {

                        if (board[row][column] == null) {
                            board[row][column] =
                                TileGenerator.generateTile();

                            playSound(placeSound);

                            System.out.println(
                                "Placed "
                                + board[row][column]
                            );

                            TileGenerator.onTilePlaced();
                        }
                    }

                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent event) {
                hoveredRow = -1;
                hoveredColumn = -1;
                repaint();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent event) {
                int newColumn =
                    event.getX() / TILE_SIZE;

                int newRow =
                    event.getY() / TILE_SIZE;

                if (newRow != hoveredRow ||
                    newColumn != hoveredColumn) {

                    hoveredRow = newRow;
                    hoveredColumn = newColumn;

                    repaint();
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        for (int row = 0; row < ROWS; row++) {
            for (int column = 0;
                 column < COLUMNS;
                 column++) {

                int x = column * TILE_SIZE;
                int y = row * TILE_SIZE;

                if (board[row][column] == null) {
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

                if (row == hoveredRow &&
                    column == hoveredColumn &&
                    board[row][column] != null) {

                    graphics.setColor(Color.YELLOW);

                    graphics.drawRect(
                        x + 1,
                        y + 1,
                        TILE_SIZE - 2,
                        TILE_SIZE - 2
                    );

                    graphics.drawRect(
                        x + 2,
                        y + 2,
                        TILE_SIZE - 4,
                        TILE_SIZE - 4
                    );

                    graphics.drawRect(
                        x + 3,
                        y + 3,
                        TILE_SIZE - 6,
                        TILE_SIZE - 6
                    );
                }
            }
        }
    }

    private void drawTile(Graphics graphics, Tile tile, int x, int y) {
        tilecraft.Tilecraft.drawTile(graphics, tile, x, y, TILE_SIZE, false);
    }

    private Color getTerrainColor(
            TerrainType terrain) {

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

        if (terrain == TerrainType.RIVER) {
            return new Color(65, 145, 210);
        }

        return Color.MAGENTA;
    }

    private Clip loadSound(String fileName) {
        try {
            URL soundLocation =
                getClass().getResource(fileName);

            if (soundLocation == null) {
                System.out.println(
                    "Sound file not found: "
                    + fileName
                );

                return null;
            }

            AudioInputStream audio =
                AudioSystem.getAudioInputStream(
                    soundLocation
                );

            Clip clip = AudioSystem.getClip();
            clip.open(audio);
            audio.close();

            return clip;
        } catch (Exception error) {
            System.out.println(
                "Could not load sound: "
                + fileName
            );

            error.printStackTrace();
            return null;
        }
    }

    private void playSound(Clip sound) {
        if (sound != null) {
            sound.stop();
            sound.flush();
            sound.setFramePosition(0);
            sound.start();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame window =
                new JFrame("Go West Grid");

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