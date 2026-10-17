
import java.io.*;
import java.net.*;
import javax.swing.*;
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

                // Create a room.
                out.println("CREATE");

                String response = in.readLine();

                if (response == null ||
                    !response.startsWith("ROOM:")) {

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

                // Join an existing room.
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

            // Open the existing game window.
            SwingUtilities.invokeLater(() -> {
                Grid.main(new String[0]);
            });

            // Keep the connection alive for now.
            // Multiplayer synchronization can be added here later.
            while (!socket.isClosed()) {
                Thread.sleep(1000);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                null,
                "Could not connect to the server.\n"
                + "Check that the server is running and the IP is correct.",
                "Connection Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
