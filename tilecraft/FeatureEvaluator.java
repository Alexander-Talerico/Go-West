package tilecraft;

import java.awt.Point;
import java.util.*;

public class FeatureEvaluator {

    private static final int COLUMNS = 25;
    private static final int[] EAST_ROAD_ROWS = {1, 3, 5, 7, 9};

    public static class EvalResult {
        public final boolean validAndComplete;
        public final double points;
        public final String featureType;
        public final Set<String> featureNodes;

        public EvalResult(boolean validAndComplete, double points, String featureType, Set<String> featureNodes) {
            this.validAndComplete = validAndComplete;
            this.points = points;
            this.featureType = featureType;
            this.featureNodes = featureNodes;
        }
    }

    public static EvalResult evaluateFeature(
            Tile[][] board,
            int row,
            int col,
            int subR,
            int subC,
            Set<String> alreadyClaimedNodes) {

        if (row < 0 || row >= board.length || col < 0 || col >= board[0].length) {
            return new EvalResult(false, 0.0, "", Collections.emptySet());
        }

        Tile startTile = board[row][col];
        if (startTile == null) {
            return new EvalResult(false, 0.0, "", Collections.emptySet());
        }

        TerrainType clickedTerrain = startTile.getCell(subR, subC);

        if (clickedTerrain == TerrainType.SETTLEMENT || clickedTerrain == TerrainType.GATE) {
            Edge startEdge = findStartingCityEdge(startTile, subR, subC);
            if (startEdge != null) {
                return evaluateCity(board, row, col, startEdge, alreadyClaimedNodes);
            } else if (startTile.getArchetype() == TileDeck.ALL_CITY) {
                return evaluateCity(board, row, col, Edge.NORTH, alreadyClaimedNodes);
            }
        }

        if (clickedTerrain == TerrainType.ROAD) {
            Edge startEdge = findStartingRoadEdge(startTile, subR, subC);
            if (startEdge != null) {
                return evaluateRoad(board, row, col, startEdge, alreadyClaimedNodes);
            }
        }

        for (Edge e : Edge.values()) {
            if (startTile.getEffectiveEdge(e) == TerrainType.SETTLEMENT) {
                EvalResult cityRes = evaluateCity(board, row, col, e, alreadyClaimedNodes);
                if (cityRes.validAndComplete) return cityRes;
            }
        }
        for (Edge e : Edge.values()) {
            if (startTile.getEffectiveEdge(e) == TerrainType.ROAD) {
                EvalResult roadRes = evaluateRoad(board, row, col, e, alreadyClaimedNodes);
                if (roadRes.validAndComplete) return roadRes;
            }
        }

        return new EvalResult(false, 0.0, "", Collections.emptySet());
    }

    private static Edge findStartingCityEdge(Tile tile, int subR, int subC) {
        for (Edge e : Edge.values()) {
            if (e.getRow() == subR && e.getCol() == subC && tile.getEffectiveEdge(e) == TerrainType.SETTLEMENT) {
                return e;
            }
        }
        if (subR == 1 && subC == 1 && isCityTerrain(tile.getCenter())) {
            for (Edge e : Edge.values()) {
                if (tile.getEffectiveEdge(e) == TerrainType.SETTLEMENT) return e;
            }
        }
        if (subR == 0 && subC == 0) {
            if (tile.getEffectiveEdge(Edge.NORTH) == TerrainType.SETTLEMENT) return Edge.NORTH;
            if (tile.getEffectiveEdge(Edge.WEST) == TerrainType.SETTLEMENT) return Edge.WEST;
        } else if (subR == 0 && subC == 2) {
            if (tile.getEffectiveEdge(Edge.NORTH) == TerrainType.SETTLEMENT) return Edge.NORTH;
            if (tile.getEffectiveEdge(Edge.EAST) == TerrainType.SETTLEMENT) return Edge.EAST;
        } else if (subR == 2 && subC == 2) {
            if (tile.getEffectiveEdge(Edge.SOUTH) == TerrainType.SETTLEMENT) return Edge.SOUTH;
            if (tile.getEffectiveEdge(Edge.EAST) == TerrainType.SETTLEMENT) return Edge.EAST;
        } else if (subR == 2 && subC == 0) {
            if (tile.getEffectiveEdge(Edge.SOUTH) == TerrainType.SETTLEMENT) return Edge.SOUTH;
            if (tile.getEffectiveEdge(Edge.WEST) == TerrainType.SETTLEMENT) return Edge.WEST;
        }
        return null;
    }

    private static Edge findStartingRoadEdge(Tile tile, int subR, int subC) {
        for (Edge e : Edge.values()) {
            if (e.getRow() == subR && e.getCol() == subC && tile.getEffectiveEdge(e) == TerrainType.ROAD) {
                return e;
            }
        }
        for (Edge e : Edge.values()) {
            if (tile.getEffectiveEdge(e) == TerrainType.ROAD) {
                return e;
            }
        }
        return null;
    }

    private static EvalResult evaluateCity(
            Tile[][] board,
            int startR,
            int startC,
            Edge startEdge,
            Set<String> alreadyClaimedNodes) {

        int rows = board.length;
        int cols = board[0].length;

        Set<String> visitedStates = new HashSet<>();
        Set<Point> uniqueTiles = new HashSet<>();
        Queue<int[]> queue = new ArrayDeque<>();

        String initKey = "CITY:" + startR + ":" + startC + ":" + startEdge.ordinal();
        visitedStates.add(initKey);
        queue.add(new int[]{startR, startC, startEdge.ordinal()});

        boolean complete = true;

        while (!queue.isEmpty()) {
            int[] curr = queue.poll();
            int r = curr[0];
            int c = curr[1];
            Edge edge = Edge.values()[curr[2]];

            if (alreadyClaimedNodes.contains("CITY:" + r + ":" + c + ":" + edge.ordinal())) {
                return new EvalResult(false, 0.0, "CITY", Collections.emptySet());
            }

            Tile tile = board[r][c];
            uniqueTiles.add(new Point(r, c));

            for (Edge other : getInternallyConnectedCityEdges(tile, edge)) {
                String k = "CITY:" + r + ":" + c + ":" + other.ordinal();
                if (visitedStates.add(k)) {
                    queue.add(new int[]{r, c, other.ordinal()});
                }
            }

            int nr = r + (edge == Edge.SOUTH ? 1 : edge == Edge.NORTH ? -1 : 0);
            int nc = c + (edge == Edge.EAST ? 1 : edge == Edge.WEST ? -1 : 0);
            Edge opp = getOppositeEdge(edge);

            if (nr < 0 || nr >= rows || nc < 0 || nc >= cols) {
                complete = false;
            } else {
                Tile neighbor = board[nr][nc];
                if (neighbor == null) {
                    complete = false;
                } else if (neighbor.getEffectiveEdge(opp) == TerrainType.SETTLEMENT) {
                    String nk = "CITY:" + nr + ":" + nc + ":" + opp.ordinal();
                    if (visitedStates.add(nk)) {
                        queue.add(new int[]{nr, nc, opp.ordinal()});
                    }
                } else {
                    complete = false;
                }
            }
        }

        if (!complete || uniqueTiles.isEmpty()) {
            return new EvalResult(false, 0.0, "CITY", Collections.emptySet());
        }

        double totalPoints = 0.0;
        for (Point pt : uniqueTiles) {
            int tileCol = pt.y;
            double multiplier = 1.0 + (tileCol * 0.05);
            totalPoints += 2.0 * multiplier;
        }

        totalPoints = Math.round(totalPoints * 100.0) / 100.0;
        return new EvalResult(true, totalPoints, "CITY", visitedStates);
    }

    private static EvalResult evaluateRoad(
            Tile[][] board,
            int startR,
            int startC,
            Edge startEdge,
            Set<String> alreadyClaimedNodes) {

        int rows = board.length;
        int cols = board[0].length;

        Set<String> visitedStates = new HashSet<>();
        Set<Point> uniqueTiles = new HashSet<>();
        Queue<int[]> queue = new ArrayDeque<>();

        String initKey = "ROAD:" + startR + ":" + startC + ":" + startEdge.ordinal();
        visitedStates.add(initKey);
        queue.add(new int[]{startR, startC, startEdge.ordinal()});

        boolean complete = true;

        while (!queue.isEmpty()) {
            int[] curr = queue.poll();
            int r = curr[0];
            int c = curr[1];
            Edge edge = Edge.values()[curr[2]];

            if (alreadyClaimedNodes.contains("ROAD:" + r + ":" + c + ":" + edge.ordinal())) {
                return new EvalResult(false, 0.0, "ROAD", Collections.emptySet());
            }

            Tile tile = board[r][c];
            uniqueTiles.add(new Point(r, c));

            if (!isRoadTerminatorTile(tile)) {
                for (Edge other : Edge.values()) {
                    if (other != edge && tile.getEffectiveEdge(other) == TerrainType.ROAD) {
                        String k = "ROAD:" + r + ":" + c + ":" + other.ordinal();
                        if (visitedStates.add(k)) {
                            queue.add(new int[]{r, c, other.ordinal()});
                        }
                    }
                }
            }

            int nr = r + (edge == Edge.SOUTH ? 1 : edge == Edge.NORTH ? -1 : 0);
            int nc = c + (edge == Edge.EAST ? 1 : edge == Edge.WEST ? -1 : 0);
            Edge opp = getOppositeEdge(edge);

            if (edge == Edge.EAST && c == cols - 1) {
                if (!isEastRoadRow(r)) {
                    complete = false;
                }
            } else if (nr < 0 || nr >= rows || nc < 0 || nc >= cols) {
                complete = false;
            } else {
                Tile neighbor = board[nr][nc];
                if (neighbor == null) {
                    complete = false;
                } else if (neighbor.getEffectiveEdge(opp) == TerrainType.ROAD) {
                    String nk = "ROAD:" + nr + ":" + nc + ":" + opp.ordinal();
                    if (visitedStates.add(nk)) {
                        queue.add(new int[]{nr, nc, opp.ordinal()});
                    }
                } else {
                    complete = false;
                }
            }
        }

        if (!complete || uniqueTiles.isEmpty()) {
            return new EvalResult(false, 0.0, "ROAD", Collections.emptySet());
        }

        double totalPoints = 0.0;
        for (Point pt : uniqueTiles) {
            int tileCol = pt.y;
            double multiplier = 1.0 + (tileCol * 0.05);
            totalPoints += 1.0 * multiplier;
        }

        totalPoints = Math.round(totalPoints * 100.0) / 100.0;
        return new EvalResult(true, totalPoints, "ROAD", visitedStates);
    }

    private static List<Edge> getInternallyConnectedCityEdges(Tile tile, Edge fromEdge) {
        List<Edge> result = new ArrayList<>();
        if (tile.getArchetype() == TileDeck.CITY_OPPOSITE_DISCONNECTED) {
            result.add(fromEdge);
            return result;
        }
        for (Edge e : Edge.values()) {
            if (tile.getEffectiveEdge(e) == TerrainType.SETTLEMENT) {
                result.add(e);
            }
        }
        return result;
    }

    private static boolean isRoadTerminatorTile(Tile tile) {
        if (tile.getCenter() == TerrainType.GATE) {
            return true;
        }
        int roadEdges = 0;
        for (Edge e : Edge.values()) {
            if (tile.getEffectiveEdge(e) == TerrainType.ROAD) {
                roadEdges++;
            }
        }
        return roadEdges >= 3;
    }

    private static boolean isEastRoadRow(int row) {
        for (int r : EAST_ROAD_ROWS) {
            if (r == row) return true;
        }
        return false;
    }

    private static boolean isCityTerrain(TerrainType t) {
        return t == TerrainType.SETTLEMENT || t == TerrainType.GATE;
    }

    private static Edge getOppositeEdge(Edge edge) {
        return switch (edge) {
            case NORTH -> Edge.SOUTH;
            case SOUTH -> Edge.NORTH;
            case EAST  -> Edge.WEST;
            case WEST  -> Edge.EAST;
        };
    }
}