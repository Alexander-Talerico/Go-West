package tilecraft;

public record Position(int x, int y) {

    public Position getAdjacent(Edge edge) {
        return switch (edge) {
            case NORTH -> new Position(x, y - 1);
            case EAST  -> new Position(x + 1, y);
            case SOUTH -> new Position(x, y + 1);
            case WEST  -> new Position(x - 1, y);
        };
    }
}