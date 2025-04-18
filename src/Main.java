
import java.io.*;
import java.net.Socket;

public class Main {
    public static void main(String[] args) {
        SetUpClient();
    }

    public static void SetUpClient() {
        String serverAddress = "localhost"; // change as needed
        int serverPort = 9900;

        try (
                Socket client = new Socket(serverAddress, serverPort);
                BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
                PrintWriter output = new PrintWriter(client.getOutputStream(), true);
                BufferedReader input = new BufferedReader(new InputStreamReader(client.getInputStream()));
                DataOutputStream dataOut = new DataOutputStream(client.getOutputStream());
                DataInputStream dataIn = new DataInputStream(client.getInputStream())
        ) {
            System.out.println("Connected to server.");

            while (true) {
                System.out.print("Enter command (UPLOAD <file>, DOWNLOAD <file>, bye): ");
                String command = keyboard.readLine();
                if (command == null) continue;

                if (command.startsWith("UPLOAD ")) {
                    String filename = command.substring(7).trim();
                    File file = new File(filename);
                    if (!file.exists()) {
                        System.out.println("File not found.");
                        continue;
                    }

                    output.println(command);
                    output.println(file.length());

                        try (FileInputStream fis = new FileInputStream(file)) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = fis.read(buffer)) != -1) {
                            dataOut.write(buffer, 0, bytesRead);
                        }
                        dataOut.flush();
                    }

                    String serverResponse = input.readLine();
                    System.out.println("Server: " + serverResponse);

                } else if (command.startsWith("DOWNLOAD ")) {
                    String filename = command.substring(9).trim();
                    output.println(command);

                    String serverResponse = input.readLine();
                    if (!"OK".equals(serverResponse)) {
                        System.out.println("Server: " + serverResponse);
                        continue;
                    }

                    long fileSize = Long.parseLong(input.readLine());
                    File file = new File("downloaded_" + filename);

                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        byte[] buffer = new byte[4096];
                        long totalRead = 0;

                        while (totalRead < fileSize) {
                            int bytesToRead = (int) Math.min(buffer.length, fileSize - totalRead);
                            int bytesRead = dataIn.read(buffer, 0, bytesToRead);
                            if (bytesRead == -1) {
                                System.out.println("Error: Unexpected end of stream");
                                break;
                            }
                            fos.write(buffer, 0, bytesRead);
                            totalRead += bytesRead;
                        }
                        fos.flush(); // ensure all data written to disk
                    }

                    // ✅ Show confirmation after full read
                    System.out.println("Download complete: " + file.getName());

                } else if (command.equalsIgnoreCase("bye")) {
                    output.println("bye");
                    break;
                } else {
                    output.println(command);
                    System.out.println("Server: " + input.readLine());
                }
            }

            System.out.println("Disconnected from server.");

        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}