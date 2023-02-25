package SeaBattle;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class SeaBattleSceneController {
    Client client;

    private boolean isInGame;
    private boolean isGameFinished;
    private boolean isPlayersTurn;

    @FXML private Pane playerField;
    @FXML private Pane opponentField;
    
    // 0 - water
    // 1 - ship
    // 2 - damaged ship
    // 3 - missed shot
    int[][] playerGameField;
    int[][] opponentGameField;

    @FXML private void initialize() {
        this.client = new Client();

        this.isInGame = false;
        this.isGameFinished = true;

        initializeGameField(playerField, 11, "player");
        initializeGameField(opponentField, 11, "opponent");

        randomizePositions();
    }

    private void initializeGameField(Pane gameField, int tableSize, String gameFieldOwner) {
        NumberBinding minSide = Bindings
            .min(gameField.heightProperty(), gameField.widthProperty())
            .divide(tableSize);
            gameField.getChildren().clear();

        for(int y = 0; y < 11; ++y) {
            for(int x = 0; x < 11; ++x) {
                Rectangle rectangle = new Rectangle(0, 0, Color.LIGHTGRAY);
                
                rectangle.xProperty().bind(minSide.multiply(x));
                rectangle.yProperty().bind(minSide.multiply(y));
                rectangle.heightProperty().bind(minSide.subtract(2));
                rectangle.widthProperty().bind(rectangle.heightProperty());

                final int cellX = x, cellY = y;
                EventHandler<MouseEvent> onClick = new EventHandler<MouseEvent>() {
                    @Override 
                    public void handle(MouseEvent e) {
                        processMove(cellX, cellY, gameFieldOwner);
                    }
                };

                rectangle.addEventFilter(MouseEvent.MOUSE_CLICKED, onClick);

                if ((y != 0 && x != 0) || (y == 0 && x == 0)) {
                    gameField.getChildren().add(rectangle);
                }
                else {
                    Text text = new Text("");
                    
                    if (x == 0)
                        text = new Text(Integer.toString(y));
                    
                    if (y == 0)
                        text = new Text(Integer.toString(x));
                        
                    StackPane stack = new StackPane();
                    stack.layoutXProperty().bind(minSide.multiply(x));
                    stack.layoutYProperty().bind(minSide.multiply(y));

                    stack.getChildren().addAll(rectangle, text);
                    gameField.getChildren().add(stack);
                }
            }
        }
    }

    private void processMove(int x, int y, String gameFieldOwner) {
        if (this.isInGame && !this.isGameFinished) {
            System.out.println(String.format("Clicked at (%d, %d) by %s", x, y, gameFieldOwner));

            if (1 <= x && x <= 10 && 1 <= y && y <= 10) {
                if (this.isPlayersTurn && gameFieldOwner == "opponent") {
                    switchActivePlayer();
                    sendMove(x, y);
                }
            }
        }
    }

    private void sendMove(int x, int y) {
        String playerShotResponse = client.makeRequest(String.format("player_shot %d %d", x, y));
        String opponentFieldAfterShot = client.makeRequest("get_opponent_field");
        this.opponentGameField = convertFromStringToMatrix(opponentFieldAfterShot);
        displayGameFields();

        switch (playerShotResponse) {
            case "all opponent ships are destroyed":
                endTheGame("Вы выиграли!", "Вы уничтожили все корабли противника");
                break;
            case "missed shot":
                waitForOppenentsTurn();
                break;
            case "damaged ship":
                switchActivePlayer();
                break;
            case "wrong shot location":
                switchActivePlayer();
                break;
        }
    }

    private void endTheGame(String messageHeader, String messageText) {
        this.isGameFinished = true;

        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Игра окончена");
        alert.setHeaderText(messageHeader);
        alert.setContentText(messageText);

        alert.showAndWait();
    }

    private void waitForOppenentsTurn() {
        String opponentShotResponse = client.makeRequest("make_turn");
        String playerFieldAfterShot = client.makeRequest("get_player_field");
        this.playerGameField = convertFromStringToMatrix(playerFieldAfterShot);
        displayGameFields();

        if (opponentShotResponse.equals("all your ships are destroyed"))
            endTheGame("Вы проиграли.", "Противник уничтожил все ваши корабли");
        else
            Client.writeToConsole(opponentShotResponse);

        switchActivePlayer();
    }

    @FXML private Button randomizePositionsButton;

    @FXML private void onRandomizePositionsButtonClick(ActionEvent event) {
        randomizePositions();
    }

    private void randomizePositions() {
        if (!this.isInGame) {
            this.playerGameField = generateRandomField("player");
            this.opponentGameField = generateRandomField("opponent");
            displayGameFields();
        }
    }

    private int[][] generateRandomField(String gameFieldOwner) {
        String responseString = client.makeRequest("generate_random_field " + gameFieldOwner);
        return convertFromStringToMatrix(responseString);
    }

    private int[][] convertFromStringToMatrix(String matrixString) {
        String[] rows = matrixString.split("\\], \\[");

        int[][] matrix = new int[rows.length][];
        for (int i = 0; i < rows.length; ++i) {
            String row = rows[i].replaceAll("\\[|\\]", "");

            String[] elements = row.split(", ");
            
            matrix[i] = new int[elements.length];
            for (int j = 0; j < elements.length; j++) {
                matrix[i][j] = Integer.parseInt(elements[j]);
            }
        }

        return matrix;
    }

    private void displayGameFields() {
        // player field
        for (Node cell : playerField.getChildren()) {
            if (cell instanceof Rectangle)
                ((Rectangle)cell).setFill(Color.LIGHTGRAY);
        }

        assert playerGameField.length == 10;
        for (int i = 0; i < playerGameField.length; ++i) {
            assert playerGameField[i].length == 10;

            for (int j = 0; j < playerGameField[i].length; ++j) {
                int cellIndex = (i + 1) * 11 + j + 1;
                Node cell = playerField.getChildren().get(cellIndex);

                if (playerGameField[i][j] == 1) {    
                    ((Rectangle)cell).setFill(Color.DARKSLATEBLUE);
                }

                if (playerGameField[i][j] == 2) {
                    ((Rectangle)cell).setFill(Color.ORANGERED);
                }

                if (playerGameField[i][j] == 3) {
                    ((Rectangle)cell).setFill(Color.ROYALBLUE);
                }
            }
        }

        // opponent field
        for (Node cell : opponentField.getChildren()) {
            if (cell instanceof Rectangle)
                ((Rectangle)cell).setFill(Color.LIGHTGRAY);
        }

        assert opponentGameField.length == 10;
        for (int i = 0; i < opponentGameField.length; ++i) {
            assert opponentGameField[i].length == 10;

            for (int j = 0; j < opponentGameField[i].length; ++j) {
                int cellIndex = (i + 1) * 11 + j + 1;
                Node cell = opponentField.getChildren().get(cellIndex);

                if (opponentGameField[i][j] == 1) {    
                    ((Rectangle)cell).setFill(Color.LIGHTGRAY);
                }

                if (opponentGameField[i][j] == 2) {
                    ((Rectangle)cell).setFill(Color.ORANGERED);
                }

                if (opponentGameField[i][j] == 3) {
                    ((Rectangle)cell).setFill(Color.ROYALBLUE);
                }
            }
        }
    }

    @FXML private Button playButton;

    @FXML private void onPlayButtonClick(ActionEvent event) {
        this.isInGame = !this.isInGame;

        if (this.isInGame) {
            this.isGameFinished = false;
            setActivePlayer("player");
            this.randomizePositionsButton.setStyle("-fx-background-color: #808080");
            this.playButton.setText("Сыграть заново");
        }
        else {
            this.playerGameField = generateRandomField("player");
            this.opponentGameField = generateRandomField("opponent");
            displayGameFields();

            this.randomizePositionsButton.setStyle("-fx-background-color: #42aaff");
            this.playButton.setText("Начать игру");
        }
    }

    @FXML private Label playerLabel;
    @FXML private Label opponentLabel;

    private void setActivePlayer(String player) {
        if (player == "player") {
            this.isPlayersTurn = true;
            this.playerLabel.setStyle("-fx-text-fill: #A67C00");
            this.opponentLabel.setStyle("-fx-text-fill: #000");
        }
        
        if (player == "opponent") {
            this.isPlayersTurn = false;
            this.opponentLabel.setStyle("-fx-text-fill: #A67C00");
            this.playerLabel.setStyle("-fx-text-fill: #000");
        }
    }

    private void switchActivePlayer() {
        if (this.isPlayersTurn)
            setActivePlayer("opponent");
        else
            setActivePlayer("player");
    }
}
