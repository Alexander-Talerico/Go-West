import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    static Map<String, Socket> rooms = new HashMap<>();
    public static void main(String[] args) {
        int port = 5000;

        try (ServerSocket serverSocket = new ServerSocket(port)) {

            System.out.println("Server started on port " + port);
            System.out.println("Waiting for players...");

            while (true) {
                Socket clientSocket = serverSocket.accept();

                System.out.println("A player connected!");

                new Thread(() -> handleClient(clientSocket)).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
static void handleClient(Socket socket) {
        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
            );

            PrintWriter out = new PrintWriter(
                    socket.getOutputStream(), true
            );

            
            out.println("Do you want to CREATE or JOIN a room?");

            String action = in.readLine();

            if (action.equalsIgnoreCase("CREATE")) {

                
                String roomCode = generateRoomCode();

                rooms.put(roomCode, socket);

                out.println("ROOM:" + roomCode);
                out.println("Waiting for someone to join...");

                System.out.println("Created room: " + roomCode);

                
                while (true) {
                    Thread.sleep(1000);
                }

            } else if (action.equalsIgnoreCase("JOIN")) {

                out.println("Enter room code:");

                String roomCode = in.readLine();

                if (rooms.containsKey(roomCode)) {

                    Socket hostSocket = rooms.get(roomCode);

                    
                    out.println("You have joined the room!");

                    
                    PrintWriter hostOut = new PrintWriter(
                            hostSocket.getOutputStream(), true
                    );

                    hostOut.println("Someone has joined your room!");

                    System.out.println("Player joined room: " + roomCode);

                } else {
                    out.println("Room does not exist.");
                }
            }

        } catch (Exception e) {
            System.out.println("Player disconnected.");
        }
    }
static String generateRoomCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();

        Random random = new Random();

        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }

        return code.toString();
    }


}
