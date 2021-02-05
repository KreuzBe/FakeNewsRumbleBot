import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class GameContainer implements EventListener {
    private static final int MAX_PLAYERS = 5;

    private enum GameState {LOBBY, PUZZLE, VOTE, REVEAL}

    private JDA jda;
    private ArrayList<Player> player;

    private int maxRounds;

    private String correctAnswer;
    private GameState gameState = GameState.LOBBY;


    public GameContainer(int maxRounds, JDA jda) {
        maxRounds = this.maxRounds;
        this.jda = jda;

        for (int i = 0; i < maxRounds; i++) {
            gameState = GameState.PUZZLE;
            puzzle();
            gameState = GameState.VOTE;
            vote();
            gameState = GameState.REVEAL;
            reveal();
        }
    }

    private void puzzle() {

    }

    private void vote() {

    }


    private void reveal() {

    }

    @Override
    public void onEvent(@NotNull GenericEvent event) {

    }


}
