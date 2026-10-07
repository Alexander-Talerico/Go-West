// Francis Lopata 2026

package tilecraft;

import java.awt.Color;
import java.util.Objects;

public class Player {

    private final int id;
    private final String name;
    private final Color bannerColor;

    public Player(int id, String name, Color bannerColor) {
        this.id = id;
        this.name = Objects.requireNonNull(name, "Player name cannot be null");
        this.bannerColor = bannerColor != null ? bannerColor : new Color(40, 40, 40);
    }

    public Player(int id, String name) {
        this(id, name, null);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Color getBannerColor() {
        return bannerColor;
    }

    @Override
    public String toString() {
        return "%s (P%d)".formatted(name, id);
    }
}