package SeaBattle;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

public class ClientHandler extends Thread {
    Socket clientSocket;
    DataInputStream dis;
    DataOutputStream dos;

    private GameController gameController;
    private int[][] playerGameField = null;
    private int[][] opponentGameField = null;

    public ClientHandler(Socket clientSocket, DataInputStream dis, DataOutputStream dos) {
        this.clientSocket = clientSocket;
        this.dis = dis;
        this.dos = dos;

        this.gameController = new GameController();
    }

    public void run() {
        while (true) {
            try {
                String clientMessage = dis.readUTF();
                String response = processRequest(clientMessage);
                dos.writeUTF(response);
            }
            catch (EOFException e) {
                String errorMessage = "Client is disconected";
                Server.writeToConsole(errorMessage);
                break;
            }
            catch (IOException e) {
                String errorMessage = "Cannot read client message";
                Server.writeToConsole(errorMessage);
                break;
            }
        }
    }

    public String processRequest(String request) {
        String response = null;

        try {
            String serverDebugMessage = String.format("Client message: %s", request);
            Client.writeToConsole(serverDebugMessage);
            
            String command = request.split(" ")[0];
            if (command.equals("generate_random_field")) {
                String gameFieldOwner = request.split(" ")[1];
                generateRandomField(gameFieldOwner);

                if (gameFieldOwner.equals("player"))
                    response = Arrays.deepToString(playerGameField);
                else if (gameFieldOwner.equals("opponent"))
                    response = Arrays.deepToString(opponentGameField);
            }
            else if (command.equals("player_shot")) {
                int x = Integer.parseInt(request.split(" ")[1]) - 1;
                int y = Integer.parseInt(request.split(" ")[2]) - 1;

                response = gameController.makePlayersTurn(opponentGameField, x, y);
            }
            else if (command.equals("make_turn")) {
                response = gameController.makeOpponentsTurn(playerGameField);
            }
            else if (command.equals("get_player_field")) {
                response = Arrays.deepToString(playerGameField);
            }
            else if (command.equals("get_opponent_field")) {
                response = Arrays.deepToString(opponentGameField);
            }
            else response = "unknown command";
        }
        catch (Exception e) {
            String errorMessage = "Unknown error: " + e.getMessage();
            Server.writeToConsole(errorMessage);
        }
        
        Server.writeToConsole(response);
        return response;
    }

    private void generateRandomField(String gameFieldOwner) {
       int[][] gameField = GameController.generateRandomField();
        for (int i = 0; i < gameField.length; ++i) {
            for (int j = 0; j < gameField[i].length; ++j) {
                if (gameField[i][j] != 5)
                    gameField[i][j] = 0;
                else
                    gameField[i][j] = 1;
            }
        }

        if (gameFieldOwner.equals("player"))
            this.playerGameField = gameField;
        else if (gameFieldOwner.equals("opponent"))
            this.opponentGameField = gameField;
    }
}