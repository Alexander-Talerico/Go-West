import java.io.*;
import java.net.*;
import javax.swing.*;

public class Client {
    public static void main(String[] args) {

        try {
            Socket socket = new Socket("192.168.1.164", 5000);

            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));

            PrintWriter out = new PrintWriter(
                socket.getOutputStream(), true);

            in.readLine();

            String[] options = {"Create Room", "Join Room"};

            int choice = JOptionPane.showOptionDialog(
                null, "Create or join a room?", "Multiplayer",
                0, JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);

            if (choice == 0) {
                out.println("CREATE");

                String room = in.readLine();

                JOptionPane.showMessageDialog(
                    null, "Your room code is: " + room.substring(5));

                in.readLine();

                JOptionPane.showMessageDialog(
                    null, "Waiting for players to join...");

                String message;

                while ((message = in.readLine()) != null) {
                    JOptionPane.showMessageDialog(null, message);
                }

            } else if (choice == 1) {
                out.println("JOIN");

                in.readLine();

                String code = JOptionPane.showInputDialog(
                    null, "Enter room code:");

                if (code != null) {
                    out.println(code.trim());

                    JOptionPane.showMessageDialog(
                        null, in.readLine());
                }
            }

            socket.close();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                null, "Could not connect to server!");
        }
    }
}