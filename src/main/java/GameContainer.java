import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class GameContainer implements EventListener {
    private static final int MAX_PLAYERS = 1;

    public enum GameState {LOBBY, PUZZLE, VOTE, REVEAL}

    private JDA jda;
    private ArrayList<Player> playersInGame = new ArrayList<>();
    private String playerListString = "";

    private int maxRounds;

    private String correctAnswer;
    private GameState gameState = GameState.LOBBY;


    public GameContainer(int maxRounds, JDA jda) {
        this.maxRounds = maxRounds;
        this.jda = jda;
    }

    private void startGame() {
        System.out.println("Starting the game");
        for (int i = 0; i < maxRounds; i++) {
            System.out.println("Round: " + i);
            gameState = GameState.PUZZLE;
            puzzle();
            gameState = GameState.VOTE;
            vote();
            gameState = GameState.REVEAL;
            reveal();
        }
        System.out.println("Ending Game... " + gameState);
    }

    private void puzzle() {

    }

    private void vote() {

    }

    private void reveal() {

    }

    @Override
    public void onEvent(@NotNull GenericEvent event) {
        if (event instanceof MessageReactionAddEvent) {

        }
    }

    public void addPlayer(Player player) {
        playersInGame.add(player);
        long joinMsgID = player.getUser().openPrivateChannel().complete().sendMessage(new EmbedBuilder().addField("Players joined:", playerListString, true).build()).complete().getIdLong();
        player.setJoinMessageId(joinMsgID);
        updatePlayerString();
        for (Player pig : playersInGame) {
            pig.getUser().openPrivateChannel().complete().editMessageById(pig.getJoinMessageId(), new EmbedBuilder().setColor(0xFFFF00).addField("Players joined:", "> " + playerListString, true).build()).complete();
        }
        if (playersInGame.size() == MAX_PLAYERS) {
            for (Player p : playersInGame) {
                p.getUser().openPrivateChannel().complete().editMessageById(p.getJoinMessageId(), new EmbedBuilder().setColor(0x00FF00).addField("Players joined:", playerListString, true).build()).complete();
                p.getUser().openPrivateChannel().complete().sendMessage("Enough players joined. Starting the game...").complete();
            }
            startGame();
        }
    }

    private void updatePlayerString() {
        playerListString = "";
        for (Player pig : playersInGame) {
            playerListString = playerListString.concat(pig.getUser().getName() + " ");
        }
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }
}
