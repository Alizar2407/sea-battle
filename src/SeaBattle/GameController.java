package SeaBattle;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

public class GameController {
    private static HashMap<Integer, Integer> ships = new HashMap<>();
    private final int maxMissesInARow = 3;

    static {
        ships.put(1, 4);
        ships.put(2, 3);
        ships.put(3, 2);
        ships.put(4, 1);
    }

    private static int randomNumber(int min, int max) { 
        Random r = new Random(); 
        return r.nextInt((max - min) + 1) + min;
    }

    private static void addShipToField(int[][] gameField, int x, int y, int kx, int ky, int shipSize) {
        if (kx == 1) {
            for (int i = x - 1; i < x + shipSize + 1; ++i) {
                if (0 <= i && i < 10) {
                    if (0 <= y - 1)
                        gameField[y - 1][i] += 1;
                    
                    if (y + 1 < 10)
                        gameField[y + 1][i] += 1;
                    
                    if (i == x - 1 || i == x + shipSize)
                        gameField[y][i] += 1;
                }
            }

            for (int i = x; i < x + shipSize; ++i)
                gameField[y][i] += 5;
        }
        
        if (ky == 1) {
            for (int i = y - 1; i < y + shipSize + 1; ++i) {
                if (0 <= i && i < 10) {
                    if (0 <= x - 1)
                        gameField[i][x-1] += 1;
                    
                    if (x + 1 < 10)
                        gameField[i][x+1] += 1;
                    
                    if (i == y - 1 || i == y + shipSize)
                        gameField[i][x] += 1;
                }
            }

            for (int i = y; i < y + shipSize; ++i) {
                gameField[i][x] += 5;
            }
        }
    }

    private static boolean checkShipLocation(int[][] gameField){
        return Arrays.stream(gameField).
            allMatch(arr -> Arrays.stream(arr).
                allMatch(cell -> cell <= 5));
    }

    public static int[][] generateRandomField() {
        int[][] gameField;

        while (true) {
            gameField = new int[10][10];    

            for (Integer shipSize : ships.keySet()) {
                int numberOfShips = ships.get(shipSize);

                for(int shipIndex = 0; shipIndex < numberOfShips; ++shipIndex) {
                    int kx = randomNumber(0, 1);
                    int ky = (kx == 0) ? 1 : 0;

                    int x, y;

                    if (kx == 0) {
                        x = randomNumber(0, 9);
                        y = randomNumber(0, 10 - shipSize);
                    } else {
                        x = randomNumber(0, 10 - shipSize);
                        y = randomNumber(0, 9);
                    }

                    addShipToField(gameField, x, y, kx, ky, shipSize);
                }
            }

            if (checkShipLocation(gameField))
                break;
        }

        return gameField;
    }

    public String makePlayersTurn(int[][] gameField, int x, int y) {
        if (gameField[y][x] == 0) {
            gameField[y][x] = 3;
            return "missed shot";
        }

        if (gameField[y][x] == 1) {
            gameField[y][x] = 2;

            boolean gameEndFlag = Arrays.stream(gameField).
                                    allMatch(arr -> Arrays.stream(arr).
                                        allMatch(cell -> cell != 1));

            if (gameEndFlag)
                return "all opponent ships are destroyed";

            return "damaged ship";
        }

        return "wrong shot location";
    }

    public String makeOpponentsTurn(int[][] gameField) {
        return nestedMakeOpponentsTurn(gameField, 0);
    }

    private int missesInARow = 0;
    private String nestedMakeOpponentsTurn(int[][] gameField, int damagedDecks) {
        int x, y;

        do {
            x = randomNumber(0, 9);
            y = randomNumber(0, 9);

            if (missesInARow > maxMissesInARow) {
                if (gameField[y][x] == 1)
                    break;
            }
            else {
                if (gameField[y][x] == 0 || gameField[y][x] == 1)
                    break;
            }
        } while (true);

        if (gameField[y][x] == 0) {
            this.missesInARow += 1;
            gameField[y][x] = 3;
            return String.format("opponent has damaged %d deck(s)", damagedDecks);
        }

        if (gameField[y][x] == 1) {
            this.missesInARow = 0;
            gameField[y][x] = 2;

            boolean gameEndFlag = Arrays.stream(gameField).
                                    allMatch(arr -> Arrays.stream(arr).
                                        allMatch(cell -> cell != 1));

            if (gameEndFlag)
                return "all your ships are destroyed";

            return nestedMakeOpponentsTurn(gameField, damagedDecks + 1);
        }

        return "error in opponent shot method";
    }
}
