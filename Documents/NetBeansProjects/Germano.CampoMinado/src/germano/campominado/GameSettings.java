package germano.campominado;

import java.util.Timer;
import java.util.TimerTask;

public class GameSettings {

    private Timer timer;
    TimerTask task = new TimerTask() {
        @Override
        public void run() {
        }

    };
    public int nRows;
    public int nColumns;
    private int nMines;

    public int getnMines() {
        return nMines;
    }
    
    private int nTiles;
    public Difficulty chosenDifficulty;

    public enum Difficulty {
        EASY, MEDIUM, HARD;
    };

    public GameSettings(int nRows, int nColumns, Difficulty chosenDificulty) {
        this.nRows = nRows;
        this.nColumns = nColumns;
        this.nTiles = nColumns * nRows;
        switch (chosenDificulty) {
            case EASY -> {
                nMines = (int) (nTiles * 0.1);
            }
            case HARD -> {
                nMines = (int) (nTiles * 0.3);
            }
            case MEDIUM -> {
                nMines = (int) (nTiles * 0.2);
            }
            default ->
                throw new AssertionError();
        }
    }

}
