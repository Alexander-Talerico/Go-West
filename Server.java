
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;

public class Server {

    private static final int PORT = 5000;
    private static final Map<String, Room> rooms = new HashMap<>();

    private static class Room {
        Socket host;
        PrintWriter hostOut;
        CountDownLatch started = new CountDownLatch(1);
        boolean full = false;

        Room(Socket host, PrintWriter hostOut) {
            this.host = host;
            this.hostOut = hostOut;
        }
    }

    public static void main(String[] args) {
        try (ServerSocket server = new ServerSocket(PORT)) {
            System.out.println("Server running on port " + PORT);
            System.out.println("Waiting for players...");

            while (true) {
                Socket socket = server.accept();
                System.out.println("Player connected!");

                new Thread(() -> handleClient(socket)).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket socket) {
        try {
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));

            PrintWriter out = new PrintWriter(
                socket.getOutputStream(), true);

            out.println("READY");

            String action = in.readLine();

            if (action == null) {
                return;
            }

            if (action.equals("CREATE")) {
                String code = generateRoomCode();
                Room room = new Room(socket, out);

                synchronized (rooms) {
                    rooms.put(code, room);
                }

                out.println("ROOM:" + code);

                System.out.println("Room created: " + code);

                // Wait until another player joins.
                room.started.await();

                out.println("START");

            } else if (action.equals("JOIN")) {
                out.println("CODE");

                String code = in.readLine();

                if (code == null) {
                    return;
                }

                Room room;

                synchronized (rooms) {
                    room = rooms.get(code.trim().toUpperCase());

                    if (room != null && !room.full) {
                        room.full = true;
                    } else {
                        room = null;
                    }
                }

                if (room == null) {
                    out.println("ERROR:Room not found or full.");
                    return;
                }

                // Tell both players to start.
                out.println("START");
                room.hostOut.println("START");
                room.started.countDown();

                System.out.println("Player joined room: " + code);
            }

        } catch (Exception e) {
            System.out.println("Player disconnected.");
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }

    private static String generateRoomCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        Random random = new Random();

        synchronized (rooms) {
            do {
                code.setLength(0);

                for (int i = 0; i < 6; i++) {
                    code.append(chars.charAt(
                        random.nextInt(chars.length())));
                }

            } while (rooms.containsKey(code.toString()));
        }

        return code.toString();
    }
}
