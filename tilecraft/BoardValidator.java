package tilecraft;

public class BoardValidator {

    private static final int COLUMNS = 25;
    private static final int[] EAST_ROAD_ROWS = {1, 3, 5, 7, 9};

    public static boolean canPlace(Tile[][] board, Tile tile, int row, int col) {
        int rows = board.length;
        int cols = board[0].length;

        if (row < 0 || row >= rows || col < 0 || col >= cols || board[row][col] != null) {
            return false;
        }

        boolean isBoardEmpty = true;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (board[r][c] != null) {
                    isBoardEmpty = false;
                    break;
                }
            }
        }

        // If the board is empty, allow placement
        /*if (isBoardEmpty) {
            if (col == cols - 1 && isEastRoadRow(row)) {
                return tile.getEffectiveEdge(Edge.EAST) == TerrainType.ROAD;
            }
            return true;
        }*/

        boolean hasAdjacent = false;

        // Check North Neighbor
        if (row > 0 && board[row - 1][col] != null) {
            hasAdjacent = true;
            if (board[row - 1][col].getEffectiveEdge(Edge.SOUTH) != tile.getEffectiveEdge(Edge.NORTH)) {
                return false;
            }
        }

        // Check South Neighbor
        if (row < rows - 1 && board[row + 1][col] != null) {
            hasAdjacent = true;
            if (board[row + 1][col].getEffectiveEdge(Edge.NORTH) != tile.getEffectiveEdge(Edge.SOUTH)) {
                return false;
            }
        }

        // Check West Neighbor
        if (col > 0 && board[row][col - 1] != null) {
            hasAdjacent = true;
            if (board[row][col - 1].getEffectiveEdge(Edge.EAST) != tile.getEffectiveEdge(Edge.WEST)) {
                return false;
            }
        }

        // Check East Neighbor
        if (col < cols - 1) {
            if (board[row][col + 1] != null) {
                hasAdjacent = true;
                if (board[row][col + 1].getEffectiveEdge(Edge.WEST) != tile.getEffectiveEdge(Edge.EAST)) {
                    return false;
                }
            }
        } else if (col == cols - 1) {
            if (isEastRoadRow(row)) {
                hasAdjacent = true;
                if (tile.getEffectiveEdge(Edge.EAST) != TerrainType.ROAD) {
                    return false;
                }
            }
        }

        return hasAdjacent;
    }

    private static boolean isEastRoadRow(int row) {
        for (int r : EAST_ROAD_ROWS) {
            if (r == row) return true;
        }
        return false;
    }

    public static boolean hasValidPlacement(Tile[][] board, Tile tile) {
        int rows = board.length;
        int cols = board[0].length;

        for (int rot = 0; rot < 4; rot++) {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    if (canPlace(board, tile, r, c)) {
                        return true;
                    }
                }
            }
            tile.rotateClockwise();
        }
        return false;
    }
}