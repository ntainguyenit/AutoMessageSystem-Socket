import java.net.*;
import java.io.*;
import java.util.StringTokenizer;
import java.util.stream.Collectors; 

public class ServerProgram {
    private static final int PORT = 60001; 

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running and listening on port " + PORT + "...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port " + PORT);
            e.printStackTrace();
        }
    }
}

class ClientHandler extends Thread {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String clientRequest;
            
            while ((clientRequest = in.readLine()) != null) {
                System.out.println("Client sent: " + clientRequest);
                String response = processRequest(clientRequest);
                out.println(response); 

                if (clientRequest.trim().equalsIgnoreCase("/quit")) {
                    break;
                }
                if (clientRequest.trim().equalsIgnoreCase("/shutdown")) {
                     System.exit(0); 
                }
            }

        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        } finally {
            try {
                if (clientSocket != null) clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String processRequest(String request) {
        String req = request.trim().toLowerCase();

        if (req.startsWith("/+:") && request.trim().length() > 3) {
            return handleCalculationRequest(request.substring(3).trim());
        } else if (req.equals("/tm")) {
            return "Mon hoc He phan tan";
        } else if (req.equals("/ht")) {
            return "Ten may server: " + clientSocket.getLocalAddress().getHostName();
        } else if (req.equals("/xskt")) {
            return handleXsktRequest("kqsx.txt");
        } else if (req.equals("/notepad")) {
            return openApplication("notepad");
        } else if (req.equals("/excel")) {
            return openApplication("excel");
        } else if (req.startsWith("/c;")) {
            try {
                String parts = request.substring(3).trim(); 
                String[] params = parts.split(";");
                if (params.length == 2) {
                    int k = Integer.parseInt(params[0].trim());
                    int n = Integer.parseInt(params[1].trim());
                    return handleCombinationRequest(k, n);
                }
                return "Loi: Dinh dang /c;k;n khong dung.";
            } catch (NumberFormatException e) {
                return "Loi: k hoac n phai la so nguyen.";
            }
        } else if (req.equals("/quit")) {
            return "QUIT_ACK"; 
        } else if (req.equals("/shutdown")) {
            return "SERVER_SHUTDOWN";
        } else {
            return "Loi: Yeu cau khong hop le hoac chua duoc trien khai.";
        }
    }
    
    private String handleCalculationRequest(String data) {
         try {
            StringTokenizer st = new StringTokenizer(data, ";");
            if (st.countTokens() < 1) return "Loi: Du lieu dau vao khong dung dinh dang.";

            String numberString = st.nextToken();
            StringTokenizer numTokens = new StringTokenizer(numberString, ",");
            
            int count = numTokens.countTokens();
            if (count == 0) return "Loi: khong tim thay so.";
            
            long sum = 0;
            long max = Long.MIN_VALUE;
            long min = Long.MAX_VALUE;

            while (numTokens.hasMoreTokens()) {
                long num = Long.parseLong(numTokens.nextToken().trim());
                sum += num;
                if (num > max) max = num;
                if (num < min) min = num;
            }

            return "Ket qua Server gui ve Client nhu sau: \n" +
                   "- Tong: " + sum + "\n" +
                   "- So lon nhat: " + max + "\n" +
                   "- So nho nhat: " + min + "\n" +
                   "- So chu so Client gui len: " + count;

        } catch (NumberFormatException e) {
            return "Loi: Du lieu chua ky tu khong phai la so.";
        } catch (Exception e) {
            return "Loi xu ly: " + e.getMessage();
        }
    }

    private String handleXsktRequest(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            return "Loi: Khong tim thay file " + fileName + " tren Server.";
        }

        try (BufferedReader fileIn = new BufferedReader(new FileReader(file))) {
            String content = fileIn.lines().collect(Collectors.joining("\n"));
            return "Noi dung file " + fileName + ":\n" + content;
        } catch (IOException e) {
            return "Loi khi doc file: " + e.getMessage();
        }
    }

    private String openApplication(String appName) {
        String command = "";
        if (appName.equals("notepad")) {
            command = "notepad.exe";
        } else if (appName.equals("excel")) {
            command = "excel.exe";
        } else {
            return "Loi: Ung dung khong duoc ho tro.";
        }

        try {
            Runtime.getRuntime().exec(command);
            return "Server da thuc hien lenh mo " + appName + ".";
        } catch (IOException e) {
            System.err.println("Loi mo " + appName + ": " + e.getMessage());
            return "Loi: Server khong mo duoc " + appName + ". (Kiem tra duong dan/he dieu hanh)";
        }
    }
    
    private long factorial(int n) {
        if (n < 0) return 0;
        long result = 1;
        for (int i = 2; i <= n; i++) {
            if (result > Long.MAX_VALUE / i) return -1; 
            result *= i;
        }
        return result;
    }

    private String handleCombinationRequest(int k, int n) {
        if (k < 0 || k > n) {
            return "Loi: k phai nam trong khoang 0 <= k <= n.";
        }
        if (n > 20) {
            return "Loi: So n qua lon (chi ho tro n <= 20 de tranh tran so Long).";
        }
        
        if (k > n / 2) {
            k = n - k;
        }

        long result = 1;
        for (int i = 0; i < k; i++) {
            result = result * (n - i) / (i + 1);
        }

        return "Ket qua C(" + n + ", " + k + ") = " + result;
    }
}