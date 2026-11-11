package scoreboard;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;

public class Scoreboard extends JPanel {

    ArrayList<String> names = new ArrayList<String>();
    ArrayList<Integer> scores = new ArrayList<Integer>();

    JTextField nameBox;
    JPanel list;
    JPanel stuff;
    JButton minButton;

    public Scoreboard() {

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(300, 320));

        JLabel title = new JLabel("GO WEST SCOREBOARD");
        JLabel words = new JLabel("chart the points here");

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(title);
        top.add(words);

        minButton = new JButton("Minimize");

        minButton.addActionListener(e -> {
            if (stuff.isVisible()) {
                stuff.setVisible(false);
                minButton.setText("Expand");
            } else {
                stuff.setVisible(true);
                minButton.setText("Minimize");
            }
            revalidate();
        });

        stuff = new JPanel();
        stuff.setLayout(new BoxLayout(stuff, BoxLayout.Y_AXIS));

        stuff.add(new JLabel("Add player"));

        nameBox = new JTextField(15);
        JButton addButton = new JButton("Add");

        JPanel addPanel = new JPanel();
        addPanel.add(nameBox);
        addPanel.add(addButton);

        stuff.add(addPanel);
        stuff.add(new JLabel("Players list:"));

        list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        stuff.add(list);

        addButton.addActionListener(e -> addPlayer());

        JPanel middle = new JPanel(new BorderLayout());
        middle.add(minButton, BorderLayout.NORTH);
        middle.add(stuff, BorderLayout.CENTER);

        add(top, BorderLayout.NORTH);
        add(middle, BorderLayout.CENTER);

        loadPlayers();
        showPlayers();
    }

    // adds a player
    public void addPlayer() {

        String name = nameBox.getText();

        if (name.trim().equals("")) {
            JOptionPane.showMessageDialog(this, "type a name first!");
            return;
        }

        names.add(name);
        scores.add(0);

        nameBox.setText("");

        savePlayers();
        showPlayers();
    }

    // shows all the players
    public void showPlayers() {

        list.removeAll();

        for (int i = 0; i < names.size(); i++) {

            final int number = i;

            JLabel player = new JLabel(names.get(i) + " has " + scores.get(i) + " points ");

            JButton plus = new JButton("+");
            JButton minus = new JButton("-");

            plus.addActionListener(e -> {
                scores.set(number, scores.get(number) + 1);
                savePlayers();
                showPlayers();
            });

            minus.addActionListener(e -> {
                scores.set(number, scores.get(number) - 1);
                savePlayers();
                showPlayers();
            });

            JPanel row = new JPanel();
            row.add(player);
            row.add(plus);
            row.add(minus);

            list.add(row);
        }

        list.revalidate();
        list.repaint();
    }

    // saves the players to a file
    public void savePlayers() {

        try {
            PrintWriter writer = new PrintWriter("players.txt");

            for (int i = 0; i < names.size(); i++) {
                writer.println(names.get(i) + "\t" + scores.get(i));
            }

            writer.close();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Could not save players");
        }
    }

    // loads the players from the file
    public void loadPlayers() {

        try {
            BufferedReader reader = new BufferedReader(new FileReader("players.txt"));

            String line;

            while ((line = reader.readLine()) != null) {

                String[] parts = line.split("\t");

                if (parts.length == 2) {
                    names.add(parts[0]);
                    scores.add(Integer.parseInt(parts[1]));
                }
            }

            reader.close();

        } catch (FileNotFoundException e) {
            // no saved players yet
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Could not load players");
        }
    }

    // runs scoreboard by itself for testing
    public static void main(String[] args) {

        JFrame window = new JFrame("Go West Scoreboard");

        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Scoreboard board = new Scoreboard();

        window.add(board);
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }
}
