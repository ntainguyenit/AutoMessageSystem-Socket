import java.net.*;
import java.io.*;
import java.util.Scanner;

public class ClientProgram {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 60001; 

    public static void main(String[] args) {
        try (
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Scanner scanner = new Scanner(System.in)
        ) {
            System.out.println("Connected to Server at " + SERVER_ADDRESS + ":" + SERVER_PORT);
            System.out.println("Nhap yeu cau (vi du: /tm, /+:123,7;10, /xskt, /c;5;10, /quit):");

            String userInput;
            String serverResponse;

            while (true) {
                userInput = scanner.nextLine();

                if (userInput.trim().isEmpty()) continue;
                
                out.println(userInput);

                System.out.println("Server Response:");
                while (in.ready()) {
                    System.out.println(in.readLine());
                }
                
                serverResponse = in.readLine();
                
                if (serverResponse == null) {
                    System.out.println("Ket noi Server bi dong.");
                    break;
                }

                System.out.println(serverResponse);

                if (userInput.trim().equalsIgnoreCase("/quit") && serverResponse.equals("QUIT_ACK")) {
                    System.out.println("Client tu dong chuong trinh.");
                    break;
                }
                 if (userInput.trim().equalsIgnoreCase("/shutdown") && serverResponse.equals("SERVER_SHUTDOWN")) {
                    System.out.println("Server da tat. Client tu dong chuong trinh.");
                    break;
                }
            }

        } catch (ConnectException e) {
            System.err.println("Khong the ket noi den Server. Dam bao Server dang chay.");
        } catch (IOException e) {
            System.err.println("Loi I/O: " + e.getMessage());
        }
    }
}