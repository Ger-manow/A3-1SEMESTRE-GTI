package germano.campominado;

import java.util.UUID;

public class GameSettings {

    public int nRows;
    public int nColumns;
    boolean gameOver;
    private int nMines;
    private int timePassed = 0;
    private UUID gameUuid;
    private String playerName;
    private int score = 0;
    private int nTiles;
    public Difficulty chosenDifficulty;

    public enum Difficulty {
        EASY, MEDIUM, HARD;
    };

    public GameSettings(int nRows, int nColumns, Difficulty chosenDificulty) {
        this.nRows = nRows;
        this.nColumns = nColumns;
        this.nTiles = nColumns * nRows;
        this.gameUuid = UUID.randomUUID();
        switch (chosenDificulty) {
            case EASY -> {
                nMines = (int) (nTiles * 0.1);
            }
            case HARD -> {
                nMines = (int) (nTiles * 0.2);
            }
            case MEDIUM -> {
                nMines = (int) (nTiles * 0.15);
            }
            default ->
                throw new AssertionError();
        }
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver() {
        this.gameOver = true;
    }

    public void setTimePassed(int timePassed) {
        this.timePassed = timePassed;
    }

    public int getTimePassed() {
        return timePassed;
    }

    public int getnMines() {
        return nMines;
    }

    public UUID getGameUuid() {
        return gameUuid;
    }

    public void setScore() {
        this.score += 1;
    }

    public int getScore() {
        return score;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
}
