package scoreboard;

import tilecraft.Player;
import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.List;

public class Scoreboard extends JPanel {

    private List<Player> players = Collections.emptyList();
    private final JPanel list;
    private final JPanel stuff;
    private final JButton minButton;

    public Scoreboard() {
        this(Collections.emptyList());
    }

    public Scoreboard(List<Player> initialPlayers) {
        this.players = initialPlayers != null ? initialPlayers : Collections.emptyList();

        setLayout(new BorderLayout(0, 8));
        setPreferredSize(new Dimension(240, 320));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(160, 160, 160), 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        setBackground(new Color(248, 248, 248));

        JLabel title = new JLabel("GO WEST SCOREBOARD");
        title.setFont(new Font("SansSerif", Font.BOLD, 15));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("City = 2 pts/tile | Road = 1 pt/tile");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 11));
        subtitle.setForeground(Color.DARK_GRAY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(title);
        top.add(Box.createVerticalStrut(4));
        top.add(subtitle);

        minButton = new JButton("Minimize");
        minButton.setFocusPainted(false);

        stuff = new JPanel(new BorderLayout());
        stuff.setOpaque(false);

        list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        stuff.add(list, BorderLayout.NORTH);

        minButton.addActionListener(e -> {
            if (stuff.isVisible()) {
                stuff.setVisible(false);
                minButton.setText("Expand");
            } else {
                stuff.setVisible(true);
                minButton.setText("Minimize");
            }
            revalidate();
            repaint();
        });

        JPanel middle = new JPanel(new BorderLayout(0, 8));
        middle.setOpaque(false);
        middle.add(minButton, BorderLayout.NORTH);
        middle.add(stuff, BorderLayout.CENTER);

        add(top, BorderLayout.NORTH);
        add(middle, BorderLayout.CENTER);

        refreshScores();
    }

    public void setPlayers(List<Player> players) {
        this.players = players != null ? players : Collections.emptyList();
        refreshScores();
    }

    public void refreshScores() {
        list.removeAll();

        for (Player p : players) {
            JPanel row = new JPanel(new BorderLayout());
            row.setBackground(Color.WHITE);
            row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 210, 210), 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
            ));

            JLabel nameLabel = new JLabel(p.getName());
            nameLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
            nameLabel.setForeground(p.getBannerColor());

            JLabel scoreLabel = new JLabel(p.getScore() + " pts");
            scoreLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
            scoreLabel.setForeground(Color.DARK_GRAY);

            row.add(nameLabel, BorderLayout.WEST);
            row.add(scoreLabel, BorderLayout.EAST);

            list.add(row);
            list.add(Box.createVerticalStrut(6));
        }

        list.revalidate();
        list.repaint();
    }
}