package tilecraft;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

public class Tilecraft extends JFrame {

    public static final int DEFAULT_PLAYER_COUNT = 4;

    private final GameState gameState;
    private final TileCanvas canvas = new TileCanvas();
    private final JLabel headerLabel = new JLabel("", SwingConstants.CENTER);

    public Tilecraft() {
        this(DEFAULT_PLAYER_COUNT);
    }

    public Tilecraft(int playerCount) {
        this.gameState = new GameState(playerCount);

        setTitle("Tilecraft - Turn-Based Inspector");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(650, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Header Panel showing turn and current player
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(230, 230, 230));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 8, 10));

        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        topPanel.add(headerLabel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        add(canvas, BorderLayout.CENTER);

        // Control buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 6));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        JButton btnRotateCCW = new JButton("Rotate CCW");
        JButton btnEndTurn = new JButton("End Turn");
        JButton btnRotateCW = new JButton("Rotate CW");

        btnEndTurn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnEndTurn.setBackground(new Color(220, 240, 220));

        buttonPanel.add(btnRotateCCW);
        buttonPanel.add(btnEndTurn);
        buttonPanel.add(btnRotateCW);
        add(buttonPanel, BorderLayout.SOUTH);

        btnRotateCCW.addActionListener(e -> {
            Tile tile = gameState.getCurrentTile();
            if (tile != null) {
                tile.rotateCounterClockwise();
                updateUIState();
            }
        });

        btnRotateCW.addActionListener(e -> {
            Tile tile = gameState.getCurrentTile();
            if (tile != null) {
                tile.rotateClockwise();
                updateUIState();
            }
        });

        btnEndTurn.addActionListener(e -> {
            gameState.endTurn();
            updateUIState();
        });

        updateUIState();
    }

    private void updateUIState() {
        Player current = gameState.getCurrentPlayer();
        headerLabel.setText(String.format("Turn %d: %s's Turn", gameState.getTurnCount(), current.getName()));
        headerLabel.setForeground(current.getBannerColor());

        canvas.setTile(gameState.getCurrentTile());
        canvas.repaint();
    }

    private static class TileCanvas extends JPanel {

        private static final Color COLOR_FIELD = new Color(225, 245, 225);
        private static final Color COLOR_CITY = new Color(250, 210, 210);

        private static final Color LINE_RIVER = new Color(30, 144, 255);
        private static final Color LINE_ROAD = new Color(80, 80, 80);
        private static final Color LINE_WALL = new Color(160, 30, 30);
        private static final Color LINE_GATE = new Color(180, 120, 20);
        private static final Color LINE_GRID = new Color(110, 110, 110);

        private Tile currentTile;

        public TileCanvas() {
            setBackground(new Color(240, 240, 240));
        }

        public void setTile(Tile tile) {
            this.currentTile = tile;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (currentTile == null) return;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int panelW = getWidth();
            int panelH = getHeight();
            int size = Math.min(panelW, panelH) - 40;
            int startX = (panelW - size) / 2;
            int startY = Math.max(22, (panelH - size) / 2 - 2);
            int cell = size / 3;

            // 1. Draw Cell Background Colors
            drawCellBackgrounds(g2, startX, startY, cell);

            // 2. Draw Paths
            drawPaths(g2, startX, startY, cell);

            // 3. Draw City Walls
            drawWalls(g2, startX, startY, cell);

            // 4. Draw Gate structure if present
            drawGate(g2, startX, startY, cell);

            // 5. 3x3 Grid Outlines
            drawGrid(g2, startX, startY, cell);

            // 6. Labels on top
            drawLabels(g2, startX, startY, cell);

            // 7. Tile Subtitle
            g2.setColor(Color.DARK_GRAY);
            g2.setFont(new Font("SansSerif", Font.BOLD, 13));
            String title = currentTile.getId() + " [" + currentTile.getArchetype().name() + "] (Facing: " + currentTile.getFacing() + ")";
            int tw = g2.getFontMetrics().stringWidth(title);
            g2.drawString(title, (panelW - tw) / 2, startY - 8);

            g2.dispose();
        }

        private void drawCellBackgrounds(Graphics2D g2, int sx, int sy, int cell) {
            g2.setColor(COLOR_FIELD);
            g2.fillRect(sx, sy, cell * 3, cell * 3);

            // Center
            if (isCityTerrain(currentTile.getCenter())) {
                g2.setColor(COLOR_CITY);
                g2.fillRect(sx + cell, sy + cell, cell, cell);
            }

            // Edges
            g2.setColor(COLOR_CITY);
            for (Edge e : Edge.values()) {
                if (isCityTerrain(currentTile.getEffectiveEdge(e))) {
                    g2.fillRect(sx + e.getCol() * cell, sy + e.getRow() * cell, cell, cell);
                }
            }

            // Corners
            fillCorner(g2, Corner.NW, sx, sy, cell);
            fillCorner(g2, Corner.NE, sx, sy, cell);
            fillCorner(g2, Corner.SE, sx, sy, cell);
            fillCorner(g2, Corner.SW, sx, sy, cell);
        }

        private void fillCorner(Graphics2D g2, Corner corner, int sx, int sy, int cell) {
            if (!isCityTerrain(currentTile.getEffectiveCorner(corner))) return;

            int cx = sx + corner.getCol() * cell;
            int cy = sy + corner.getRow() * cell;

            boolean nCity = isCityTerrain(currentTile.getEffectiveEdge(Edge.NORTH));
            boolean eCity = isCityTerrain(currentTile.getEffectiveEdge(Edge.EAST));
            boolean sCity = isCityTerrain(currentTile.getEffectiveEdge(Edge.SOUTH));
            boolean wCity = isCityTerrain(currentTile.getEffectiveEdge(Edge.WEST));

            g2.setColor(COLOR_CITY);

            switch (corner) {
                case NW -> {
                    if (nCity && wCity) {
                        g2.fillRect(cx, cy, cell, cell);
                    } else if (nCity) {
                        Polygon p = new Polygon();
                        p.addPoint(cx, cy);
                        p.addPoint(cx + cell, cy);
                        p.addPoint(cx + cell, cy + cell);
                        g2.fillPolygon(p);
                    } else if (wCity) {
                        Polygon p = new Polygon();
                        p.addPoint(cx, cy);
                        p.addPoint(cx, cy + cell);
                        p.addPoint(cx + cell, cy + cell);
                        g2.fillPolygon(p);
                    }
                }
                case NE -> {
                    if (nCity && eCity) {
                        g2.fillRect(cx, cy, cell, cell);
                    } else if (nCity) {
                        Polygon p = new Polygon();
                        p.addPoint(cx + cell, cy);
                        p.addPoint(cx, cy);
                        p.addPoint(cx, cy + cell);
                        g2.fillPolygon(p);
                    } else if (eCity) {
                        Polygon p = new Polygon();
                        p.addPoint(cx + cell, cy);
                        p.addPoint(cx + cell, cy + cell);
                        p.addPoint(cx, cy + cell);
                        g2.fillPolygon(p);
                    }
                }
                case SE -> {
                    if (sCity && eCity) {
                        g2.fillRect(cx, cy, cell, cell);
                    } else if (sCity) {
                        Polygon p = new Polygon();
                        p.addPoint(cx + cell, cy + cell);
                        p.addPoint(cx, cy + cell);
                        p.addPoint(cx, cy);
                        g2.fillPolygon(p);
                    } else if (eCity) {
                        Polygon p = new Polygon();
                        p.addPoint(cx + cell, cy + cell);
                        p.addPoint(cx + cell, cy);
                        p.addPoint(cx, cy);
                        g2.fillPolygon(p);
                    }
                }
                case SW -> {
                    if (sCity && wCity) {
                        g2.fillRect(cx, cy, cell, cell);
                    } else if (sCity) {
                        Polygon p = new Polygon();
                        p.addPoint(cx, cy + cell);
                        p.addPoint(cx + cell, cy + cell);
                        p.addPoint(cx + cell, cy);
                        g2.fillPolygon(p);
                    } else if (wCity) {
                        Polygon p = new Polygon();
                        p.addPoint(cx, cy + cell);
                        p.addPoint(cx, cy);
                        p.addPoint(cx + cell, cy);
                        g2.fillPolygon(p);
                    }
                }
            }
        }

        private void drawPaths(Graphics2D g2, int sx, int sy, int cell) {
            TerrainType center = currentTile.getCenter();
            Point centerPt = new Point(sx + (int) (1.5 * cell), sy + (int) (1.5 * cell));

            drawPathNetwork(g2, sx, sy, cell, TerrainType.RIVER, LINE_RIVER, 8.0f, centerPt);

            if (center == TerrainType.GATE) {
                g2.setColor(LINE_ROAD);
                g2.setStroke(new BasicStroke(6.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                for (Edge e : Edge.values()) {
                    if (currentTile.getEffectiveEdge(e) == TerrainType.ROAD) {
                        Point outer = getEdgeOuterPoint(e, sx, sy, cell);
                        Point gateEdgePoint = getCenterEntry(e, sx, sy, cell);
                        g2.drawLine(outer.x, outer.y, gateEdgePoint.x, gateEdgePoint.y);
                    }
                }
            } else {
                drawPathNetwork(g2, sx, sy, cell, TerrainType.ROAD, LINE_ROAD, 6.0f, centerPt);
            }
        }

        private void drawPathNetwork(Graphics2D g2, int sx, int sy, int cell, TerrainType pathType, Color color, float strokeWidth, Point centerPt) {
            List<Edge> activeEdges = new ArrayList<>();
            for (Edge e : Edge.values()) {
                if (currentTile.getEffectiveEdge(e) == pathType) {
                    activeEdges.add(e);
                }
            }

            if (activeEdges.isEmpty()) return;

            g2.setColor(color);
            g2.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            if (activeEdges.size() == 2 && Math.abs(activeEdges.get(0).getIndex() - activeEdges.get(1).getIndex()) == 2) {
                Point p1 = getEdgeOuterPoint(activeEdges.get(0), sx, sy, cell);
                Point p2 = getEdgeOuterPoint(activeEdges.get(1), sx, sy, cell);
                g2.drawLine(p1.x, p1.y, p2.x, p2.y);
                return;
            }

            for (Edge e : activeEdges) {
                Point ep = getEdgeOuterPoint(e, sx, sy, cell);
                Point cp = getCenterEntry(e, sx, sy, cell);

                Path2D path = new Path2D.Float();
                path.moveTo(ep.x, ep.y);
                path.lineTo(cp.x, cp.y);
                path.lineTo(centerPt.x, centerPt.y);
                g2.draw(path);
            }
        }

        private void drawWalls(Graphics2D g2, int sx, int sy, int cell) {
            boolean allCity = true;
            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < 3; c++) {
                    if (!isCityTerrain(currentTile.getCell(r, c))) {
                        allCity = false;
                        break;
                    }
                }
            }
            if (allCity) return;

            g2.setColor(LINE_WALL);
            g2.setStroke(new BasicStroke(4.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            // 1. Diagonal Walls for corner transitions
            if (isDiagonalCorner(Corner.NW)) g2.drawLine(sx, sy, sx + cell, sy + cell);
            if (isDiagonalCorner(Corner.NE)) g2.drawLine(sx + 3 * cell, sy, sx + 2 * cell, sy + cell);
            if (isDiagonalCorner(Corner.SE)) g2.drawLine(sx + 3 * cell, sy + 3 * cell, sx + 2 * cell, sy + 2 * cell);
            if (isDiagonalCorner(Corner.SW)) g2.drawLine(sx, sy + 3 * cell, sx + cell, sy + 2 * cell);

            // 2. Horizontal Internal Cell Boundaries
            for (int r = 0; r < 2; r++) {
                for (int c = 0; c < 3; c++) {
                    boolean top = isCityTerrain(currentTile.getCell(r, c));
                    boolean bottom = isCityTerrain(currentTile.getCell(r + 1, c));

                    if (top ^ bottom) {
                        // Suppress straight wall if the cell is bounded diagonally
                        if (r == 0 && c == 0 && isDiagonalCorner(Corner.NW)) continue;
                        if (r == 0 && c == 2 && isDiagonalCorner(Corner.NE)) continue;
                        if (r == 1 && c == 0 && isDiagonalCorner(Corner.SW)) continue;
                        if (r == 1 && c == 2 && isDiagonalCorner(Corner.SE)) continue;

                        int y = sy + (r + 1) * cell;
                        int x1 = sx + c * cell;
                        int x2 = x1 + cell;
                        g2.drawLine(x1, y, x2, y);
                    }
                }
            }

            // 3. Vertical Internal Cell Boundaries
            for (int c = 0; c < 2; c++) {
                for (int r = 0; r < 3; r++) {
                    boolean left = isCityTerrain(currentTile.getCell(r, c));
                    boolean right = isCityTerrain(currentTile.getCell(r, c + 1));

                    if (left ^ right) {
                        // Suppress straight wall if the cell is bounded diagonally
                        if (c == 0 && r == 0 && isDiagonalCorner(Corner.NW)) continue;
                        if (c == 1 && r == 0 && isDiagonalCorner(Corner.NE)) continue;
                        if (c == 0 && r == 2 && isDiagonalCorner(Corner.SW)) continue;
                        if (c == 1 && r == 2 && isDiagonalCorner(Corner.SE)) continue;

                        int x = sx + (c + 1) * cell;
                        int y1 = sy + r * cell;
                        int y2 = y1 + cell;
                        g2.drawLine(x, y1, x, y2);
                    }
                }
            }
        }

        private boolean isDiagonalCorner(Corner corner) {
            if (!isCityTerrain(currentTile.getEffectiveCorner(corner))) return false;

            boolean nCity = isCityTerrain(currentTile.getEffectiveEdge(Edge.NORTH));
            boolean eCity = isCityTerrain(currentTile.getEffectiveEdge(Edge.EAST));
            boolean sCity = isCityTerrain(currentTile.getEffectiveEdge(Edge.SOUTH));
            boolean wCity = isCityTerrain(currentTile.getEffectiveEdge(Edge.WEST));

            return switch (corner) {
                case NW -> nCity ^ wCity;
                case NE -> nCity ^ eCity;
                case SE -> sCity ^ eCity;
                case SW -> sCity ^ wCity;
            };
        }

        private boolean isCityTerrain(TerrainType type) {
            return type == TerrainType.SETTLEMENT || type == TerrainType.GATE;
        }

        private void drawGate(Graphics2D g2, int sx, int sy, int cell) {
            if (currentTile.getCenter() != TerrainType.GATE) return;

            Edge roadEdge = null;
            for (Edge e : Edge.values()) {
                if (currentTile.getEffectiveEdge(e) == TerrainType.ROAD) {
                    roadEdge = e;
                    break;
                }
            }
            if (roadEdge == null) return;

            Point gatePt = getCenterEntry(roadEdge, sx, sy, cell);

            g2.setColor(LINE_GATE);
            g2.setStroke(new BasicStroke(5.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawOval(gatePt.x - 10, gatePt.y - 10, 20, 20);

            g2.setColor(new Color(60, 40, 20));
            g2.fillOval(gatePt.x - 6, gatePt.y - 6, 12, 12);
        }

        private void drawGrid(Graphics2D g2, int sx, int sy, int cell) {
            g2.setColor(LINE_GRID);
            g2.setStroke(new BasicStroke(2.0f));

            g2.drawRect(sx, sy, 3 * cell, 3 * cell);
            g2.drawLine(sx + cell, sy, sx + cell, sy + 3 * cell);
            g2.drawLine(sx + 2 * cell, sy, sx + 2 * cell, sy + 3 * cell);
            g2.drawLine(sx, sy + cell, sx + 3 * cell, sy + cell);
            g2.drawLine(sx, sy + 2 * cell, sx + 3 * cell, sy + 2 * cell);
        }

        private void drawLabels(Graphics2D g2, int sx, int sy, int cell) {
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));

            // Row 0 (Top)
            drawCenteredText(g2, "NW", currentTile.getEffectiveCorner(Corner.NW).name(), sx + cell / 2, sy + cell / 2);
            drawCenteredText(g2, "NORTH", currentTile.getEffectiveEdge(Edge.NORTH).name(), sx + cell + cell / 2, sy + cell / 2);
            drawCenteredText(g2, "NE", currentTile.getEffectiveCorner(Corner.NE).name(), sx + 2 * cell + cell / 2, sy + cell / 2);

            // Row 1 (Middle)
            drawCenteredText(g2, "WEST", currentTile.getEffectiveEdge(Edge.WEST).name(), sx + cell / 2, sy + cell + cell / 2);
            drawCenteredText(g2, currentTile.getId(), currentTile.getCenter().name(), sx + cell + cell / 2, sy + cell + cell / 2);
            drawCenteredText(g2, "EAST", currentTile.getEffectiveEdge(Edge.EAST).name(), sx + 2 * cell + cell / 2, sy + cell + cell / 2);

            // Row 2 (Bottom)
            drawCenteredText(g2, "SW", currentTile.getEffectiveCorner(Corner.SW).name(), sx + cell / 2, sy + 2 * cell + cell / 2);
            drawCenteredText(g2, "SOUTH", currentTile.getEffectiveEdge(Edge.SOUTH).name(), sx + cell + cell / 2, sy + 2 * cell + cell / 2);
            drawCenteredText(g2, "SE", currentTile.getEffectiveCorner(Corner.SE).name(), sx + 2 * cell + cell / 2, sy + 2 * cell + cell / 2);
        }

        private void drawCenteredText(Graphics2D g2, String title, String subtitle, int cx, int cy) {
            FontMetrics fm = g2.getFontMetrics();
            int h = fm.getHeight();

            int tw1 = fm.stringWidth(title);
            int tw2 = fm.stringWidth(subtitle);

            int maxW = Math.max(tw1, tw2) + 8;
            g2.setColor(new Color(255, 255, 255, 185));
            g2.fillRect(cx - maxW / 2, cy - h, maxW, h * 2 + 2);

            g2.setColor(Color.BLACK);
            g2.drawString(title, cx - tw1 / 2, cy - 2);
            g2.drawString(subtitle, cx - tw2 / 2, cy + h - 2);
        }

        private Point getEdgeOuterPoint(Edge edge, int sx, int sy, int cell) {
            return switch (edge) {
                case NORTH -> new Point(sx + cell + cell / 2, sy);
                case EAST  -> new Point(sx + 3 * cell, sy + cell + cell / 2);
                case SOUTH -> new Point(sx + cell + cell / 2, sy + 3 * cell);
                case WEST  -> new Point(sx, sy + cell + cell / 2);
            };
        }

        private Point getCenterEntry(Edge edge, int sx, int sy, int cell) {
            return switch (edge) {
                case NORTH -> new Point(sx + cell + cell / 2, sy + cell);
                case EAST  -> new Point(sx + 2 * cell, sy + cell + cell / 2);
                case SOUTH -> new Point(sx + cell + cell / 2, sy + 2 * cell);
                case WEST  -> new Point(sx + cell, sy + cell + cell / 2);
            };
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Tilecraft().setVisible(true));
    }
}