import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import tilecraft.Tilecraft;
import tilecraft.TileGenerator;

public class Client {
private static final int PORT = 5000;
private static final int DISCOVERY_PORT = 5001;
private static final String DISCOVERY_REQUEST = "TILECRAFT_DISCOVER";
private static final String DISCOVERY_RESPONSE = "TILECRAFT_SERVER:";

    private static Socket connectToServer() throws IOException {
    try {
        Socket socket = new Socket();
        socket.connect(
            new InetSocketAddress("127.0.0.1", PORT),
            1000
        );
        System.out.println("Connected to local server.");
        return socket;

    } catch (IOException ignored) {
    }

    System.out.println("Searching for Tilecraft servers...");

    try (DatagramSocket discoverySocket = new DatagramSocket()) {

        discoverySocket.setBroadcast(true);
        discoverySocket.setSoTimeout(4000);

        byte[] requestData = DISCOVERY_REQUEST.getBytes();
        DatagramPacket request = new DatagramPacket(
            requestData,
            requestData.length,
            InetAddress.getByName("255.255.255.255"),
            DISCOVERY_PORT
        );

        discoverySocket.send(request);

        byte[] responseBuffer = new byte[1024];

        while (true) {

            DatagramPacket response = new DatagramPacket(
                responseBuffer,
                responseBuffer.length
            );

            discoverySocket.receive(response);

            String message = new String(
                response.getData(),
                0,
                response.getLength()
            ).trim();

            if (message.startsWith(DISCOVERY_RESPONSE)) {

                int serverPort = Integer.parseInt(
                    message.substring(DISCOVERY_RESPONSE.length())
                );

                InetAddress serverAddress = response.getAddress();

                System.out.println(
                    "Found Tilecraft server at: "
                    + serverAddress.getHostAddress()
                );

                Socket socket = new Socket();

                socket.connect(
                    new InetSocketAddress(serverAddress, serverPort),
                    3000
                );

                System.out.println("Connected to Tilecraft server!");

                return socket;
            }
        }

    } catch (SocketTimeoutException e) {
        throw new IOException(
            "No Tilecraft server found on your local network.\n"
            + "Make sure someone has started the server.",
            e
        );
    }
}
private static class BackgroundPanel extends JPanel {
    private BufferedImage background;

    public BackgroundPanel(String imagePath) {
        try {
            background = ImageIO.read(new File(imagePath));
        } catch (IOException e) {
            System.out.println("Could not load background.png");
            e.printStackTrace();
        }
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (background != null) {
            g.drawImage(
                background,
                0,
                0,
                getWidth(),
                getHeight(),
                this
            );
        }
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

            final int[] selectedChoice = {-1};

        JFrame menuFrame = new JFrame("Tilecraft Multiplayer");
        BackgroundPanel backgroundPanel = new BackgroundPanel("background.png");
        backgroundPanel.setLayout(new GridBagLayout());
        JButton createButton = new JButton("Create Room");
        JButton joinButton = new JButton("Join Room");
        createButton.setFont(new Font("SansSerif", Font.BOLD, 20));
        joinButton.setFont(new Font("SansSerif", Font.BOLD, 20));
        createButton.setPreferredSize(new Dimension(200, 42));
        joinButton.setPreferredSize(new Dimension(200, 42));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(12, 0, 12, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        createButton.addActionListener(e -> {
            selectedChoice[0] = 0;
            menuFrame.dispose();
        });

        joinButton.addActionListener(e -> {
        selectedChoice[0] = 1;
        menuFrame.dispose();
        });

        gbc.gridy = 0;
        backgroundPanel.add(createButton, gbc);

        gbc.gridy = 1;
        backgroundPanel.add(joinButton, gbc);

        menuFrame.setContentPane(backgroundPanel);
        menuFrame.setSize(900, 650);
        menuFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        menuFrame.setLocationRelativeTo(null);
        menuFrame.setResizable(false);
        menuFrame.setVisible(true);

        while (menuFrame.isDisplayable() && selectedChoice[0] == -1) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }

        int choice = selectedChoice[0];

        if (choice == -1) {
            return;
        }

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

                //String code = JOptionPane.showInputDialog(null, "Enter room code:");
                final String[] enteredCode = {null};

            JFrame codeFrame = new JFrame("Join Room");
            BackgroundPanel codePanel = new BackgroundPanel("background.png");

            codePanel.setLayout(new GridBagLayout());

            JTextField codeField = new JTextField("Join a Room", 12);
            codeField.setHorizontalAlignment(JTextField.CENTER);
            codeField.setFont(new Font("SansSerif", Font.BOLD, 20));

            JButton joinRoomButton = new JButton("JOIN");

            joinRoomButton.addActionListener(e -> {
            enteredCode[0] = codeField.getText().trim().toUpperCase();
            codeFrame.dispose();
            });

            GridBagConstraints joinScreen = new GridBagConstraints();
            joinScreen.gridx = 0;
            joinScreen.insets = new Insets(10, 0, 10, 0);

            joinScreen.gridy = 1;
            codePanel.add(codeField, joinScreen);

            joinScreen.gridy = 2;
            codePanel.add(joinRoomButton, joinScreen);

            codeFrame.setContentPane(codePanel);
            codeFrame.setSize(900, 650);
            codeFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            codeFrame.setLocationRelativeTo(null);
            codeFrame.setResizable(false);
            codeFrame.setVisible(true);

while (codeFrame.isDisplayable() && enteredCode[0] == null) {
    Thread.sleep(100);
}

String code = enteredCode[0];

if (code == null || code.isEmpty()) {
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
            lobbyFrame.setSize(900, 650);
            lobbyFrame.setResizable(false);
            BackgroundPanel lobbyBackground = new BackgroundPanel("background.png");
            lobbyBackground.setLayout(new BorderLayout(10, 10));
            lobbyFrame.setContentPane(lobbyBackground);
            JLabel statusLabel = new JLabel("Players in room: " + startCount + " / 4", SwingConstants.CENTER);
            JTextArea logArea = new JTextArea(6, 28);
            JButton startButton = new JButton("Start Game");

            SwingUtilities.invokeAndWait(() -> {
                lobbyFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                lobbyFrame.setLayout(new BorderLayout(10, 10));

                JPanel topPanel = new JPanel(new GridLayout(2, 1, 8, 8));
                topPanel.setOpaque(false);
                topPanel.setBorder(BorderFactory.createEmptyBorder(315, 15, 5, 15));

                JLabel codeLabel = new JLabel("Room Code: " + finalCode, SwingConstants.CENTER);
                codeLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
                codeLabel.setForeground(Color.WHITE);
                 codeLabel.setOpaque(false);
               
                statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
                statusLabel.setForeground(Color.WHITE);
                statusLabel.setOpaque(false);
                topPanel.add(codeLabel);
                topPanel.add(statusLabel);
                lobbyFrame.add(topPanel, BorderLayout.NORTH);

                /*logArea.setEditable(false);
                logArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
                if (finalIsHost) {
                    logArea.append("You created the room (Player 1).\n");
                    logArea.append("You can start now (1-4 players) or wait for others.\n");
                } else {
                    logArea.append("Joined room as Player " + startCount + "!\n");
                    logArea.append("Waiting for the host to start the game...\n");
                }
                */
                JScrollPane scroll = new JScrollPane(logArea);
                scroll.setOpaque(false);
                scroll.getViewport().setOpaque(false);

                scroll.setBorder(null);

                logArea.setOpaque(false);
                logArea.setBackground(new Color(0, 0, 0, 0));
                logArea.setForeground(Color.WHITE);

                lobbyFrame.add(scroll, BorderLayout.CENTER);

                if (finalIsHost) {
                    JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                    bottomPanel.setOpaque(false);
                    bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 12, 15));
                    startButton.setFont(new Font("SansSerif", Font.BOLD, 14));
                    startButton.addActionListener(e -> {
                        startButton.setEnabled(false);
                        out.println("HOST_START");
                    });
                    bottomPanel.add(startButton);
                    lobbyFrame.add(bottomPanel, BorderLayout.SOUTH);
                }
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
