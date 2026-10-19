import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.Desktop;
import tilecraft.Tilecraft;
import tilecraft.TileGenerator;

public class Client {

    private static final String LOCAL_IP = "127.0.0.1";
    private static final String LAN_IP = "192.168.1.9";
    private static final int PORT = 5000;

    private static Socket connectToServer() throws IOException {
        try {
            Socket s = new Socket();
            s.connect(new InetSocketAddress(LOCAL_IP, PORT), 1500);
            return s;
        } catch (IOException e) {
            Socket s = new Socket();
            s.connect(new InetSocketAddress(LAN_IP, PORT), 3000);
            return s;
        }
    }

    public static void main(String[] args) {

        try (
            Socket socket = connectToServer();
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(
                socket.getOutputStream(), true)
        ) {
            in.readLine();

            String[] options = {"Create Room", "Join Room"};

            int choice = JOptionPane.showOptionDialog(
                null,
                "Create or join a room?",
                "Tilecraft Multiplayer",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
            );

            String roomCode = "";
            boolean isHost = false;
            int initialCount = 1;

            if (choice == 0) {
                isHost = true;
                out.println("CREATE");

                String response = in.readLine();
                if (response == null || !response.startsWith("ROOM:")) {
                    JOptionPane.showMessageDialog(null, "Could not create room.");
                    return;
                }

                roomCode = response.substring(5).trim().toUpperCase();

            } else if (choice == 1) {
                out.println("JOIN");

                String response = in.readLine();
                if (!"CODE".equals(response)) {
                    JOptionPane.showMessageDialog(null, "Could not join a room.");
                    return;
                }

                String code = JOptionPane.showInputDialog(null, "Enter room code:");
                if (code == null || code.trim().isEmpty()) {
                    return;
                }

                roomCode = code.trim().toUpperCase();
                out.println(roomCode);

                response = in.readLine();
                if (response == null || !response.startsWith("JOINED:")) {
                    JOptionPane.showMessageDialog(
                        null,
                        response == null ? "Connection lost." : response.replace("ERROR:", "")
                    );
                    return;
                }

                initialCount = Integer.parseInt(response.substring(7).trim());
            } else {
                return;
            }

            final String finalCode = roomCode;
            final boolean finalIsHost = isHost;
            final int startCount = initialCount;

            JFrame lobbyFrame = new JFrame(isHost ? "Host Lobby - Room " + finalCode : "Lobby - Room " + finalCode);
            JLabel statusLabel = new JLabel("Players in room: " + startCount + " / 4", SwingConstants.CENTER);
            JTextArea logArea = new JTextArea(6, 28);
            JButton startButton = new JButton("Start Game");

            SwingUtilities.invokeAndWait(() -> {
                lobbyFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                lobbyFrame.setLayout(new BorderLayout(10, 10));

                JPanel topPanel = new JPanel(new GridLayout(2, 1, 4, 4));
                topPanel.setBorder(BorderFactory.createEmptyBorder(12, 15, 5, 15));

                JLabel codeLabel = new JLabel("Room Code: " + finalCode, SwingConstants.CENTER);
                codeLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
                statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));

                topPanel.add(codeLabel);
                topPanel.add(statusLabel);
                lobbyFrame.add(topPanel, BorderLayout.NORTH);

                logArea.setEditable(false);
                logArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
                if (finalIsHost) {
                    logArea.append("You created the room (Player 1).\n");
                    logArea.append("You can start now (1-4 players) or wait for others.\n");
                } else {
                    logArea.append("Joined room as Player " + startCount + "!\n");
                    logArea.append("Waiting for the host to start the game...\n");
                }

                JScrollPane scroll = new JScrollPane(logArea);
                scroll.setBorder(BorderFactory.createEmptyBorder(0, 15, 5, 15));
                lobbyFrame.add(scroll, BorderLayout.CENTER);

                if (finalIsHost) {
                    JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                    bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 12, 15));
                    startButton.setFont(new Font("SansSerif", Font.BOLD, 14));
                    startButton.addActionListener(e -> {
                        startButton.setEnabled(false);
                        out.println("HOST_START");
                    });
                    bottomPanel.add(startButton);
                    lobbyFrame.add(bottomPanel, BorderLayout.SOUTH);
                }

                lobbyFrame.pack();
                lobbyFrame.setLocationRelativeTo(null);
                lobbyFrame.setVisible(true);
            });

            int finalPlayerCount = startCount;
            String line;
            while ((line = in.readLine()) != null) {
                if (line.startsWith("PLAYER_JOINED:")) {
                    int count = Integer.parseInt(line.substring(14).trim());
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("Players in room: " + count + " / 4");
                        logArea.append("Player " + count + " joined the server! (" + count + "/4)\n");
                    });
                } else if (line.startsWith("START:")) {
                    finalPlayerCount = Integer.parseInt(line.substring(6).trim());
                    break;
                }
            }

            SwingUtilities.invokeLater(lobbyFrame::dispose);

            TileGenerator.setSeed(roomCode.hashCode());
            TileGenerator.setNetworkSender(out::println);

            final int gamePlayers = finalPlayerCount;
            final Tilecraft[] gameWindow = new Tilecraft[1];
            SwingUtilities.invokeAndWait(() -> {
                gameWindow[0] = new Tilecraft(gamePlayers);
                gameWindow[0].setVisible(true);
            });

            try {
                File htmlFile = new File(
                    System.getProperty("user.dir"), "index.html"
                );

                if (htmlFile.exists() && Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().browse(htmlFile.toURI());
                }
            } catch (Exception ignored) {
            }

            String message;
            while ((message = in.readLine()) != null) {
                final String msg = message;
                SwingUtilities.invokeLater(() -> {
                    if (gameWindow[0] != null) {
                        gameWindow[0].handleNetworkMessage(msg);
                    }
                });
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                null,
                "Could not connect to the server.\n"
                + "Make sure Server is running first (`java -cp out Server`).\n\n"
                + e.getMessage(),
                "Connection Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
}