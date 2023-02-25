package SeaBattle;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    protected ServerSocket serverSocket;

    public Server() {
        int port = 4400;

        try {
            this.serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            String errorMessage = String.format("Cannot use port %d.", port);
            Client.writeToConsole(errorMessage);
        }
    }

    public void start() {
        while (true) {
            Socket clientSocket = null;
            
            try {
                clientSocket = serverSocket.accept();
                this.serveClient(clientSocket);
                
            } catch (Exception e){
                String errorMessage = "Error: " + e;
                Server.writeToConsole(errorMessage);
            }
        }
    }
    
    public void serveClient(Socket clientSocket) throws IOException {
        String clientConnectMessage = "A new client is connected: " + clientSocket;
        Server.writeToConsole(clientConnectMessage);

        DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
        DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());

        Thread clientHandlerThread = new ClientHandler(clientSocket, dis, dos);
        clientHandlerThread.start();
    }

    public static void writeToConsole(String message) {
        String coloredMessage = "\033[34m" + message + "\033[0m";
        System.out.println(coloredMessage);
    }
}
