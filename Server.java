import java.io.*;
import java.net.*;
import java.util.*;

public class Server {

    private static final int PORT = 5000;
    private static final int MAX_PLAYERS = 4;
    private static final Map<String, Room> rooms = new HashMap<>();

    private static class Room {
        final List<PrintWriter> clients = new ArrayList<>();
        boolean started = false;

        synchronized int addClient(PrintWriter out) {
            clients.add(out);
            return clients.size();
        }

        synchronized void broadcastToAll(String message) {
            for (PrintWriter client : clients) {
                client.println(message);
            }
        }

        synchronized void broadcastExcept(String message, PrintWriter sender) {
            for (PrintWriter client : clients) {
                if (client != sender) {
                    client.println(message);
                }
            }
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
        Room currentRoom = null;
        PrintWriter out = null;

        try {
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));

            out = new PrintWriter(
                socket.getOutputStream(), true);

            out.println("READY");

            String action = in.readLine();
            if (action == null) {
                return;
            }

            if (action.equals("CREATE")) {
                String code = generateRoomCode();
                currentRoom = new Room();
                currentRoom.addClient(out);

                synchronized (rooms) {
                    rooms.put(code, currentRoom);
                }

                out.println("ROOM:" + code);
                System.out.println("Room created: " + code + " (1/" + MAX_PLAYERS + " players)");

            } else if (action.equals("JOIN")) {
                out.println("CODE");

                String code = in.readLine();
                if (code == null) {
                    return;
                }

                int newCount = 0;
                synchronized (rooms) {
                    Room room = rooms.get(code.trim().toUpperCase());
                    if (room != null && !room.started && room.clients.size() < MAX_PLAYERS) {
                        currentRoom = room;
                        newCount = currentRoom.addClient(out);
                    }
                }

                if (currentRoom == null) {
                    out.println("ERROR:Room not found, already started, or full (max 4 players).");
                    return;
                }

                out.println("JOINED:" + newCount);
                currentRoom.broadcastToAll("PLAYER_JOINED:" + newCount);

                System.out.println("Player " + newCount + " joined room: " + code);
            } else {
                return;
            }

            String line;
            while ((line = in.readLine()) != null) {
                if (line.equals("HOST_START")) {
                    synchronized (currentRoom) {
                        if (!currentRoom.started) {
                            currentRoom.started = true;
                            int totalPlayers = currentRoom.clients.size();
                            currentRoom.broadcastToAll("START:" + totalPlayers);
                            System.out.println("Game started with " + totalPlayers + " player(s)!");
                        }
                    }
                } else {
                    currentRoom.broadcastExcept(line, out);
                }
            }

        } catch (Exception e) {
            System.out.println("Player disconnected.");
        } finally {
            if (currentRoom != null && out != null) {
                synchronized (currentRoom) {
                    currentRoom.clients.remove(out);
                }
            }
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