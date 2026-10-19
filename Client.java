
import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.Desktop;
import grid.Grid;

public class Client {

    private static final String SERVER_IP = "192.168.56.1";
    private static final int PORT = 5000;

    public static void main(String[] args) {

        try (
            Socket socket = new Socket(SERVER_IP, PORT);
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(
                socket.getOutputStream(), true)
        ) {
            in.readLine(); // READY

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

            if (choice == 0) {
                out.println("CREATE");

                String response = in.readLine();

                if (response == null || !response.startsWith("ROOM:")) {
                    JOptionPane.showMessageDialog(
                        null, "Could not create room.");
                    return;
                }

                String code = response.substring(5);

                JOptionPane.showMessageDialog(
                    null,
                    "Your room code is: " + code
                    + "\n\nWaiting for another player to join."
                );

                response = in.readLine();

                if (!"START".equals(response)) {
                    JOptionPane.showMessageDialog(
                        null, "Connection lost before the game started.");
                    return;
                }

            } else if (choice == 1) {
                out.println("JOIN");

                String response = in.readLine();

                if (!"CODE".equals(response)) {
                    JOptionPane.showMessageDialog(
                        null, "Could not join a room.");
                    return;
                }

                String code = JOptionPane.showInputDialog(
                    null, "Enter room code:");

                if (code == null || code.trim().isEmpty()) {
                    return;
                }

                out.println(code.trim().toUpperCase());

                response = in.readLine();

                if (!"START".equals(response)) {
                    JOptionPane.showMessageDialog(
                        null,
                        response == null
                            ? "Connection lost."
                            : response.replace("ERROR:", "")
                    );
                    return;
                }

            } else {
                return;
            }

            // Launch Tilecraft in a separate process.
            try {
                new ProcessBuilder(
                    "java", "tilecraft.Tilecraft"
                )
                .directory(new File(System.getProperty("user.dir")))
                .start();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(
                    null,
                    "Could not launch Tilecraft:\n" + e.getMessage()
                );
            }

            // Launch Grid.
            SwingUtilities.invokeLater(() -> {
                try {
                    Grid.main(new String[0]);
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(
                        null,
                        "Could not launch Grid:\n" + e.getMessage()
                    );
                }
            });

            // Open index.html.
            try {
                File htmlFile = new File(
                    System.getProperty("user.dir"), "index.html"
                );

                if (htmlFile.exists() && Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().browse(htmlFile.toURI());
                } else {
                    JOptionPane.showMessageDialog(
                        null,
                        "Could not find index.html:\n"
                            + htmlFile.getAbsolutePath()
                    );
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                    null,
                    "Could not open index.html:\n" + e.getMessage()
                );
            }

            // Keep the multiplayer connection alive.
            while (!socket.isClosed()) {
                Thread.sleep(1000);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                null,
                "Could not connect to the server.\n"
                + "Check that the server is running and the IP is correct.\n\n"
                + e.getMessage(),
                "Connection Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
}