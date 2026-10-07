// Francis Lopata 2026

package tilecraft;

import static tilecraft.TerrainType.*;

public enum TileDeck {

    // CITY TILES
    
    ALL_CITY(new TerrainType[][] {
        { SETTLEMENT, SETTLEMENT, SETTLEMENT },
        { SETTLEMENT, SETTLEMENT, SETTLEMENT },
        { SETTLEMENT, SETTLEMENT, SETTLEMENT }
    }, 1),

    CITY_3_EDGE(new TerrainType[][] {
        { SETTLEMENT, SETTLEMENT, SETTLEMENT },
        { SETTLEMENT, SETTLEMENT, SETTLEMENT },
        { SETTLEMENT, FIELD,      SETTLEMENT }
    }, 4),
    
    CITY_3_EDGE_GATE(new TerrainType[][] {
        { SETTLEMENT, SETTLEMENT, SETTLEMENT },
        { SETTLEMENT, GATE,       SETTLEMENT },
        { SETTLEMENT, ROAD,       SETTLEMENT }
    }, 3),

    CITY_OPPOSITE_CONNECTED(new TerrainType[][] {
        { SETTLEMENT, SETTLEMENT, SETTLEMENT },
        { FIELD,      SETTLEMENT, FIELD },
        { SETTLEMENT, SETTLEMENT, SETTLEMENT }
    }, 3),

    CITY_OPPOSITE_DISCONNECTED(new TerrainType[][] {
        { SETTLEMENT, SETTLEMENT, SETTLEMENT },
        { FIELD,      FIELD,      FIELD },
        { SETTLEMENT, SETTLEMENT, SETTLEMENT }
    }, 3),

    CITY_CORNER(new TerrainType[][] {
        { SETTLEMENT, SETTLEMENT, SETTLEMENT },
        { SETTLEMENT, FIELD,      FIELD },
        { SETTLEMENT, FIELD,      FIELD }
    }, 5),

    CITY_CORNER_WITH_ROAD(new TerrainType[][] {
        { SETTLEMENT, SETTLEMENT, SETTLEMENT },
        { SETTLEMENT, ROAD,       ROAD },
        { SETTLEMENT, ROAD,       FIELD }
    }, 5),

    CITY_CAP(new TerrainType[][] {
        { SETTLEMENT, SETTLEMENT, SETTLEMENT },
        { FIELD,      FIELD,      FIELD },
        { FIELD,      FIELD,      FIELD }
    }, 5),

    CITY_CAP_ROAD_STRAIGHT(new TerrainType[][] {
        { SETTLEMENT, SETTLEMENT, SETTLEMENT },
        { ROAD,       ROAD,       ROAD },
        { FIELD,      FIELD,      FIELD }
    }, 3),

    CITY_CAP_ROAD_CURVE_LEFT(new TerrainType[][] {
        { SETTLEMENT, SETTLEMENT, SETTLEMENT },
        { ROAD,       ROAD,       FIELD },
        { FIELD,      ROAD,       FIELD }
    }, 3),
    
    CITY_CAP_ROAD_CURVE_RIGHT(new TerrainType[][] {
        { SETTLEMENT, SETTLEMENT, SETTLEMENT },
        { FIELD,      ROAD,       ROAD },
        { FIELD,      ROAD,       FIELD }
    }, 3),
    
    CITY_CAP_ROAD_FORK(new TerrainType[][] {
        { SETTLEMENT, SETTLEMENT, SETTLEMENT },
        { ROAD,       ROAD,       ROAD },
        { FIELD,      ROAD,       FIELD }
    }, 3),

    // JUST PATHS
    
    ROAD_STRAIGHT(new TerrainType[][] {
        { FIELD, ROAD, FIELD },
        { FIELD, ROAD, FIELD },
        { FIELD, ROAD, FIELD }
    }, 8),

    ROAD_CURVE(new TerrainType[][] {
        { FIELD, FIELD, FIELD },
        { ROAD,  ROAD,  FIELD },
        { FIELD, ROAD,  FIELD }
    }, 9),
    
    ROAD_FORK(new TerrainType[][] {
        { FIELD, FIELD,  FIELD },
        { ROAD,  ROAD,  ROAD },
        { FIELD, ROAD, FIELD }
    }, 4),
    
    ROAD_INTERSECTION(new TerrainType[][] {
        { FIELD, ROAD, FIELD },
        { ROAD,  ROAD, ROAD },
        { FIELD, ROAD, FIELD }
    }, 4),
    
    // MAY BE REMOVED, TERRAIN GENERATION

    RIVER_STRAIGHT(new TerrainType[][] {
        { FIELD, RIVER, FIELD },
        { FIELD, RIVER, FIELD },
        { FIELD, RIVER, FIELD }
    }, 5),

    RIVER_CURVE(new TerrainType[][] {
        { FIELD, RIVER, FIELD },
        { FIELD, RIVER, RIVER },
        { FIELD, FIELD, FIELD }
    }, 5),

    PURE_FIELD(new TerrainType[][] {
        { FIELD, FIELD, FIELD },
        { FIELD, FIELD, FIELD },
        { FIELD, FIELD, FIELD }
    }, 4);

    private final TerrainType[][] template;
    private final int weight;

    TileDeck(TerrainType[][] template, int weight) {
        this.template = template;
        this.weight = weight;
    }

    public TerrainType[][] createCopy() {
        TerrainType[][] copy = new TerrainType[3][3];
        for (int r = 0; r < 3; r++) {
            System.arraycopy(template[r], 0, copy[r], 0, 3);
        }
        return copy;
    }

    public int getWeight() {
        return weight;
    }
}