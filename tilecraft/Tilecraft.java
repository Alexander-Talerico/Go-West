package tilecraft;

import grid.Grid;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

public class Tilecraft extends JFrame {

    public static final int DEFAULT_PLAYER_COUNT = 4;
    private static final int FLAGS_PER_PLAYER = 7;
    private static final int FLAG_RADIUS = 12;

    private final GameState gameState;
    private final Grid boardGrid;
    private final TileCanvas previewCanvas = new TileCanvas();
    private final JLabel headerLabel = new JLabel("", SwingConstants.CENTER);
    private final JPanel flagBoxPanel = new JPanel();
    private final FlagOverlay flagOverlay;

    public Tilecraft() {
        this(DEFAULT_PLAYER_COUNT);
    }

    public Tilecraft(int playerCount) {
        this.gameState = new GameState(playerCount);
        this.boardGrid = new Grid();

        setTitle("Go West");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(230, 230, 230));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 8, 10));

        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        topPanel.add(headerLabel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        JPanel gridWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        gridWrapper.setBackground(new Color(210, 210, 210));
        gridWrapper.add(boardGrid);
        add(gridWrapper, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 8));
        bottomPanel.setBackground(new Color(240, 240, 240));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(4, 10, 10, 10));

        JButton btnRotateCCW = new JButton("Rotate CCW");
        JButton btnRotateCW = new JButton("Rotate CW");
        JButton btnEndTurn = new JButton("Skip / End Turn");

        previewCanvas.setPreferredSize(new Dimension(220, 220));

        flagBoxPanel.setPreferredSize(new Dimension(playerCount * 45 + 20, 90));
        flagBoxPanel.setBackground(new Color(250, 250, 250));
        flagBoxPanel.setBorder(BorderFactory.createLineBorder(new Color(160, 160, 160), 2));

        bottomPanel.add(btnRotateCCW);
        bottomPanel.add(previewCanvas);
        bottomPanel.add(btnRotateCW);
        bottomPanel.add(btnEndTurn);
        bottomPanel.add(flagBoxPanel);
        add(bottomPanel, BorderLayout.SOUTH);

        this.flagOverlay = new FlagOverlay(gameState.getPlayers());
        setGlassPane(flagOverlay);
        flagOverlay.setVisible(true);

        btnRotateCCW.addActionListener(e -> {
            Tile tile = gameState.getCurrentTile();
            if (tile != null) {
                tile.rotateCounterClockwise();
                updateUIState();
                TileGenerator.sendNetworkMessage("ROTATE_CCW");
            }
        });

        btnRotateCW.addActionListener(e -> {
            Tile tile = gameState.getCurrentTile();
            if (tile != null) {
                tile.rotateClockwise();
                updateUIState();
                TileGenerator.sendNetworkMessage("ROTATE_CW");
            }
        });

        btnEndTurn.addActionListener(e -> {
            gameState.endTurn();
            updateUIState();
            TileGenerator.sendNetworkMessage("END_TURN");
        });

        TileGenerator.setPlacementListener(() -> {
            gameState.endTurn();
            updateUIState();
        });

        updateUIState();
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
    }

    public void handleNetworkMessage(String msg) {
        if (msg == null) return;

        if (msg.equals("ROTATE_CCW")) {
            Tile tile = gameState.getCurrentTile();
            if (tile != null) {
                tile.rotateCounterClockwise();
                updateUIState();
            }
        } else if (msg.equals("ROTATE_CW")) {
            Tile tile = gameState.getCurrentTile();
            if (tile != null) {
                tile.rotateClockwise();
                updateUIState();
            }
        } else if (msg.equals("END_TURN")) {
            gameState.endTurn();
            updateUIState();
        } else if (msg.startsWith("GRID:")) {
            String[] parts = msg.split(":");
            if (parts.length == 5) {
                int row = Integer.parseInt(parts[1]);
                int col = Integer.parseInt(parts[2]);
                int button = Integer.parseInt(parts[3]);
                boolean shift = Boolean.parseBoolean(parts[4]);
                boardGrid.handleCellAction(row, col, button, shift, true);
            }
        } else if (msg.startsWith("FLAG:")) {
            String[] parts = msg.split(":");
            if (parts.length == 4) {
                int id = Integer.parseInt(parts[1]);
                int x = Integer.parseInt(parts[2]);
                int y = Integer.parseInt(parts[3]);
                flagOverlay.moveMarkerFromNetwork(id, x, y);
            }
        }
    }

    private void updateUIState() {
        Player current = gameState.getCurrentPlayer();
        headerLabel.setText(String.format("Turn %d: %s's Turn",
                gameState.getTurnCount(), current.getName()));
        headerLabel.setForeground(current.getBannerColor());

        previewCanvas.setTile(gameState.getCurrentTile());
        previewCanvas.repaint();
        boardGrid.repaint();
        if (flagOverlay != null) {
            flagOverlay.repaint();
        }
    }

    private static class FlagMarker {
        final int id;
        final Color color;
        final int pileIndex;
        final int stackIndex;
        int x;
        int y;
        boolean draggedFromPile = false;

        FlagMarker(int id, Color color, int pileIndex, int stackIndex) {
            this.id = id;
            this.color = color;
            this.pileIndex = pileIndex;
            this.stackIndex = stackIndex;
        }
    }

    private class FlagOverlay extends JComponent {
        private final List<FlagMarker> markers = new ArrayList<>();
        private FlagMarker activeMarker = null;
        private int offsetX = 0;
        private int offsetY = 0;

        public FlagOverlay(List<Player> players) {
            setOpaque(false);

            int nextId = 0;
            for (int p = 0; p < players.size(); p++) {
                Color color = players.get(p).getBannerColor();
                for (int i = 0; i < FLAGS_PER_PLAYER; i++) {
                    markers.add(new FlagMarker(nextId++, color, p, i));
                }
            }

            MouseAdapter adapter = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    updatePilePositions();
                    FlagMarker hit = findTopMarkerAt(e.getX(), e.getY());
                    if (hit != null) {
                        activeMarker = hit;
                        activeMarker.draggedFromPile = true;
                        offsetX = e.getX() - activeMarker.x;
                        offsetY = e.getY() - activeMarker.y;
                        markers.remove(activeMarker);
                        markers.add(activeMarker);
                        repaint();
                        TileGenerator.sendNetworkMessage(
                            "FLAG:" + activeMarker.id + ":" + activeMarker.x + ":" + activeMarker.y
                        );
                    }
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    if (activeMarker != null) {
                        activeMarker.x = e.getX() - offsetX;
                        activeMarker.y = e.getY() - offsetY;
                        repaint();
                        TileGenerator.sendNetworkMessage(
                            "FLAG:" + activeMarker.id + ":" + activeMarker.x + ":" + activeMarker.y
                        );
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (activeMarker != null) {
                        TileGenerator.sendNetworkMessage(
                            "FLAG:" + activeMarker.id + ":" + activeMarker.x + ":" + activeMarker.y
                        );
                        activeMarker = null;
                        repaint();
                    }
                }
            };

            addMouseListener(adapter);
            addMouseMotionListener(adapter);
        }

        public void moveMarkerFromNetwork(int id, int x, int y) {
            updatePilePositions();
            FlagMarker target = null;
            for (FlagMarker m : markers) {
                if (m.id == id) {
                    target = m;
                    break;
                }
            }
            if (target != null) {
                target.draggedFromPile = true;
                target.x = x;
                target.y = y;
                markers.remove(target);
                markers.add(target);
                repaint();
            }
        }

        private void updatePilePositions() {
            if (!flagBoxPanel.isShowing()) return;
            Point boxOrigin = SwingUtilities.convertPoint(flagBoxPanel, 0, 0, this);
            int numPlayers = gameState.getPlayerCount();
            int slotWidth = flagBoxPanel.getWidth() / numPlayers;
            int baseCenterY = boxOrigin.y + flagBoxPanel.getHeight() / 2 + 12;

            for (FlagMarker m : markers) {
                if (!m.draggedFromPile) {
                    m.x = boxOrigin.x + slotWidth * m.pileIndex + slotWidth / 2;
                    m.y = baseCenterY - (m.stackIndex * 4);
                }
            }
        }

        private FlagMarker findTopMarkerAt(int mx, int my) {
            for (int i = markers.size() - 1; i >= 0; i--) {
                FlagMarker m = markers.get(i);
                int dx = mx - m.x;
                int dy = my - m.y;
                if (dx * dx + dy * dy <= FLAG_RADIUS * FLAG_RADIUS) {
                    return m;
                }
            }
            return null;
        }

        @Override
        public boolean contains(int x, int y) {
            if (activeMarker != null) return true;
            updatePilePositions();
            return findTopMarkerAt(x, y) != null;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            updatePilePositions();

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int d = FLAG_RADIUS * 2;
            for (FlagMarker m : markers) {
                g2.setColor(m.color);
                g2.fillOval(m.x - FLAG_RADIUS, m.y - FLAG_RADIUS, d, d);

                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2.0f));
                g2.drawOval(m.x - FLAG_RADIUS, m.y - FLAG_RADIUS, d, d);

                g2.setColor(Color.DARK_GRAY);
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawOval(m.x - FLAG_RADIUS, m.y - FLAG_RADIUS, d, d);
            }

            g2.dispose();
        }
    }

    private static final Color COLOR_FIELD = new Color(225, 245, 225);
    private static final Color COLOR_CITY = new Color(250, 210, 210);
    private static final Color LINE_RIVER = new Color(30, 144, 255);
    private static final Color LINE_ROAD = new Color(80, 80, 80);
    private static final Color LINE_WALL = new Color(160, 30, 30);
    private static final Color LINE_GATE = new Color(180, 120, 20);
    private static final Color LINE_GRID = new Color(110, 110, 110);

    public static void drawTile(Graphics g, Tile tile, int sx, int sy, int size, boolean showLabels) {
        if (tile == null) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int cell = size / 3;
        float scale = size / 200.0f;

        drawCellBackgrounds(g2, tile, sx, sy, cell);
        drawPaths(g2, tile, sx, sy, cell, scale);
        drawWalls(g2, tile, sx, sy, cell, scale);
        drawGate(g2, tile, sx, sy, cell, scale);
        drawGrid(g2, sx, sy, cell, scale);

        if (showLabels) {
            drawLabels(g2, tile, sx, sy, cell);
        }

        g2.dispose();
    }

    private static void drawCellBackgrounds(Graphics2D g2, Tile currentTile, int sx, int sy, int cell) {
        g2.setColor(COLOR_FIELD);
        g2.fillRect(sx, sy, cell * 3, cell * 3);

        if (isCityTerrain(currentTile.getCenter())) {
            g2.setColor(COLOR_CITY);
            g2.fillRect(sx + cell, sy + cell, cell, cell);
        }

        g2.setColor(COLOR_CITY);
        for (Edge e : Edge.values()) {
            if (isCityTerrain(currentTile.getEffectiveEdge(e))) {
                g2.fillRect(sx + e.getCol() * cell, sy + e.getRow() * cell, cell, cell);
            }
        }

        fillCorner(g2, currentTile, Corner.NW, sx, sy, cell);
        fillCorner(g2, currentTile, Corner.NE, sx, sy, cell);
        fillCorner(g2, currentTile, Corner.SE, sx, sy, cell);
        fillCorner(g2, currentTile, Corner.SW, sx, sy, cell);
    }

    private static void fillCorner(Graphics2D g2, Tile currentTile, Corner corner, int sx, int sy, int cell) {
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

    private static void drawPaths(Graphics2D g2, Tile currentTile, int sx, int sy, int cell, float scale) {
        TerrainType center = currentTile.getCenter();
        Point centerPt = new Point(sx + (int) (1.5 * cell), sy + (int) (1.5 * cell));

        drawPathNetwork(g2, currentTile, sx, sy, cell, TerrainType.RIVER, LINE_RIVER, Math.max(2.5f, 8.0f * scale), centerPt);

        if (center == TerrainType.GATE) {
            g2.setColor(LINE_ROAD);
            g2.setStroke(new BasicStroke(Math.max(2.0f, 6.0f * scale), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            for (Edge e : Edge.values()) {
                if (currentTile.getEffectiveEdge(e) == TerrainType.ROAD) {
                    Point outer = getEdgeOuterPoint(e, sx, sy, cell);
                    Point gateEdgePoint = getCenterEntry(e, sx, sy, cell);
                    g2.drawLine(outer.x, outer.y, gateEdgePoint.x, gateEdgePoint.y);
                }
            }
        } else {
            drawPathNetwork(g2, currentTile, sx, sy, cell, TerrainType.ROAD, LINE_ROAD, Math.max(2.0f, 6.0f * scale), centerPt);
        }
    }

    private static void drawPathNetwork(Graphics2D g2, Tile currentTile, int sx, int sy, int cell, TerrainType pathType, Color color, float strokeWidth, Point centerPt) {
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

    private static void drawWalls(Graphics2D g2, Tile currentTile, int sx, int sy, int cell, float scale) {
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
        g2.setStroke(new BasicStroke(Math.max(1.8f, 4.5f * scale), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        if (isDiagonalCorner(currentTile, Corner.NW)) g2.drawLine(sx, sy, sx + cell, sy + cell);
        if (isDiagonalCorner(currentTile, Corner.NE)) g2.drawLine(sx + 3 * cell, sy, sx + 2 * cell, sy + cell);
        if (isDiagonalCorner(currentTile, Corner.SE)) g2.drawLine(sx + 3 * cell, sy + 3 * cell, sx + 2 * cell, sy + 2 * cell);
        if (isDiagonalCorner(currentTile, Corner.SW)) g2.drawLine(sx, sy + 3 * cell, sx + cell, sy + 2 * cell);

        for (int r = 0; r < 2; r++) {
            for (int c = 0; c < 3; c++) {
                boolean top = isCityTerrain(currentTile.getCell(r, c));
                boolean bottom = isCityTerrain(currentTile.getCell(r + 1, c));

                if (top ^ bottom) {
                    if (r == 0 && c == 0 && isDiagonalCorner(currentTile, Corner.NW)) continue;
                    if (r == 0 && c == 2 && isDiagonalCorner(currentTile, Corner.NE)) continue;
                    if (r == 1 && c == 0 && isDiagonalCorner(currentTile, Corner.SW)) continue;
                    if (r == 1 && c == 2 && isDiagonalCorner(currentTile, Corner.SE)) continue;

                    int y = sy + (r + 1) * cell;
                    int x1 = sx + c * cell;
                    int x2 = x1 + cell;
                    g2.drawLine(x1, y, x2, y);
                }
            }
        }

        for (int c = 0; c < 2; c++) {
            for (int r = 0; r < 3; r++) {
                boolean left = isCityTerrain(currentTile.getCell(r, c));
                boolean right = isCityTerrain(currentTile.getCell(r, c + 1));

                if (left ^ right) {
                    if (c == 0 && r == 0 && isDiagonalCorner(currentTile, Corner.NW)) continue;
                    if (c == 1 && r == 0 && isDiagonalCorner(currentTile, Corner.NE)) continue;
                    if (c == 0 && r == 2 && isDiagonalCorner(currentTile, Corner.SW)) continue;
                    if (c == 1 && r == 2 && isDiagonalCorner(currentTile, Corner.SE)) continue;

                    int x = sx + (c + 1) * cell;
                    int y1 = sy + r * cell;
                    int y2 = y1 + cell;
                    g2.drawLine(x, y1, x, y2);
                }
            }
        }
    }

    private static boolean isDiagonalCorner(Tile currentTile, Corner corner) {
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

    private static boolean isCityTerrain(TerrainType type) {
        return type == TerrainType.SETTLEMENT || type == TerrainType.GATE;
    }

    private static void drawGate(Graphics2D g2, Tile currentTile, int sx, int sy, int cell, float scale) {
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
        int rOuter = Math.max(4, (int) (10 * scale));
        int rInner = Math.max(2, (int) (6 * scale));

        g2.setColor(LINE_GATE);
        g2.setStroke(new BasicStroke(Math.max(1.5f, 5.0f * scale), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawOval(gatePt.x - rOuter, gatePt.y - rOuter, rOuter * 2, rOuter * 2);

        g2.setColor(new Color(60, 40, 20));
        g2.fillOval(gatePt.x - rInner, gatePt.y - rInner, rInner * 2, rInner * 2);
    }

    private static void drawGrid(Graphics2D g2, int sx, int sy, int cell, float scale) {
        g2.setColor(LINE_GRID);
        g2.setStroke(new BasicStroke(Math.max(0.6f, 2.0f * scale)));
        g2.drawRect(sx, sy, 3 * cell, 3 * cell);
    }

    private static void drawLabels(Graphics2D g2, Tile currentTile, int sx, int sy, int cell) {
    }

    private static Point getEdgeOuterPoint(Edge edge, int sx, int sy, int cell) {
        return switch (edge) {
            case NORTH -> new Point(sx + cell + cell / 2, sy);
            case EAST  -> new Point(sx + 3 * cell, sy + cell + cell / 2);
            case SOUTH -> new Point(sx + cell + cell / 2, sy + 3 * cell);
            case WEST  -> new Point(sx, sy + cell + cell / 2);
        };
    }

    private static Point getCenterEntry(Edge edge, int sx, int sy, int cell) {
        return switch (edge) {
            case NORTH -> new Point(sx + cell + cell / 2, sy + cell);
            case EAST  -> new Point(sx + 2 * cell, sy + cell + cell / 2);
            case SOUTH -> new Point(sx + cell + cell / 2, sy + 2 * cell);
            case WEST  -> new Point(sx + cell, sy + cell + cell / 2);
        };
    }

    private static class TileCanvas extends JPanel {
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

            int panelW = getWidth();
            int panelH = getHeight();
            int size = Math.min(panelW, panelH) - 16;
            int startX = (panelW - size) / 2;
            int startY = (panelH - size) / 2;

            drawTile(g, currentTile, startX, startY, size, false);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Tilecraft().setVisible(true));
    }
}