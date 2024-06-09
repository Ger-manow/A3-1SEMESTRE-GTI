package germano.campominado;

import java.util.ArrayList;

public class ScoreManager {

    private final ArrayList<GameSettings> gameMatches;

    public ScoreManager() {
        gameMatches = new ArrayList<>();
    }

    public void addGame(GameSettings gameSettings) {
        gameMatches.add(gameSettings);
    }

    public void getAllGames() {

    }
    
    public void getGame(int id){
        
    }
}
