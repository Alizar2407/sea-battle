package SeaBattle;

import java.io.*;
import java.net.Socket;

public class Client {
    Socket clienSocket;
    DataInputStream dis;
    DataOutputStream dos;

    public Client() {
        int port = 4400;
        
        try {    
            this.clienSocket = new Socket("localhost", port);
            this.start();

        } catch (IOException e) {
            String errorMessage = String.format("Cannot connect to port #%d", port);
            Client.writeToConsole(errorMessage);
        }
    }

    public void start() {
        try {
            this.dis = new DataInputStream(this.clienSocket.getInputStream()); 
            this.dos  = new DataOutputStream(this.clienSocket.getOutputStream());        

        } catch (IOException e) {
            String errorMessage = "Cannot start client: " + e.getMessage();
            Client.writeToConsole(errorMessage);
        }
    }

    public String makeRequest(String requestString){
        try {
            Client.writeToConsole(requestString);
            dos.writeUTF(requestString);

            String serverMessage = dis.readUTF();      
            return serverMessage;

        } catch (IOException e) {
            return null;
        }
    }

    public void stop() {
        try {
            if (dos != null)
                dos.close();
            
            if (dis != null)
                dis.close();
                
            if (clienSocket != null)
                this.clienSocket.close();

        } catch (Exception e) {
            String errorMessage = "Error while stopping client: " + e.getMessage();
            Client.writeToConsole(errorMessage);
        }
    }

    public static void writeToConsole(String message) {
        String coloredMessage = "\033[33m" + message + "\033[0m";
        System.out.println(coloredMessage);
    }
}
