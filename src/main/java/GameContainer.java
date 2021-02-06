import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class GameContainer implements EventListener {
    private static final int MAX_PLAYERS = 2;

    public enum GameState {LOBBY, PUZZLE, VOTE, REVEAL}

    private JDA jda;
    private ArrayList<Player> playersInGame = new ArrayList<>();
    private String playerListString = "";

    private int maxRounds;

    private GameState gameState = GameState.LOBBY;

    private String correctAnswer = ":)";
    private int correctVotes = 0;
    private Player[] voteResult;

    public GameContainer(int maxRounds, JDA jda) {
        this.maxRounds = maxRounds;
        this.jda = jda;
        jda.addEventListener(this);
    }

    private void startGame() {
        System.out.println("Starting the game");
        for (int i = 0; i < maxRounds; i++) {
            System.out.println("Round: " + i);
            gameState = GameState.PUZZLE;
            puzzle();
        }
        System.out.println("Ending Game... " + gameState);
    }

    private void puzzle() {
        // Get headline
        // Puzzle
        gameState = GameState.VOTE;
        vote();
    }

    private void vote() {
        voteResult = new Player[playersInGame.size() + 1];

        EmbedBuilder voteEBuilder = new EmbedBuilder();
        voteEBuilder.setColor(0x0050F0);
        voteEBuilder.setTitle("Vote the real headline:");
        for (Player pig : playersInGame) {
            int i;
            do {
                i = (int) (Math.random() * (playersInGame.size() + 1));
            } while (voteResult[i] != null);
            voteResult[i] = pig;
            pig.setVoteResultIndex(i);
        }
        for (int i = 0; i < voteResult.length; i++) {
            if (voteResult[i] == null) {
                voteEBuilder.addField(":regional_indicator_" + (char) (i + 'a') + ":", correctAnswer, false);
            } else {
                voteEBuilder.addField(":regional_indicator_" + (char) (i + 'a') + ":", voteResult[i].getSubmittedHeadline(), false);
            }
        }
        MessageEmbed voteEmbed = voteEBuilder.build();
        for (Player pig : playersInGame) {
            Message voteMessage = pig.getUser().openPrivateChannel().complete().sendMessage(voteEmbed).complete();
            for (int i = 1; i <= voteResult.length; i++) {
                voteMessage.addReaction("U+1f1e" + (5 + i)).queue();
            }
            pig.setVoteMessageId(voteMessage.getIdLong());
        }


        gameState = GameState.REVEAL;
        reveal();
    }

    private void reveal() {

    }

    @Override
    public void onEvent(@NotNull GenericEvent event) {
        if (gameState == GameState.VOTE) {
            if (event instanceof MessageReactionAddEvent) {
                System.out.println(event);
                MessageReactionAddEvent mrae = (MessageReactionAddEvent) event;
                if (mrae.getUser() != null && Player.getById(mrae.getUser().getIdLong()) != null) {
                    Player player = Player.getById(mrae.getUser().getIdLong());
                    if (mrae.getMessageIdLong() != player.getVoteMessageId()) {
                        return;
                    }
                    String emoji = mrae.getReaction().getReactionEmote().getAsCodepoints();
                    System.out.println(emoji);
                    if (emoji.startsWith("U+1f1e")) {
                        int vote = (emoji.charAt(emoji.length() - 1) - '6');
                        if (vote < 0 || vote >= voteResult.length) {
                            mrae.getUser().openPrivateChannel().complete().sendMessage("Please vote one of the options.").complete();
                        } else if (player.getHasVoted() != -1) {
                            if (voteResult[player.getHasVoted()] == null)
                                correctVotes--;
                            else
                                voteResult[player.getHasVoted()].subtractGotVoted();

                            player.setHasVoted(vote);
                            if (voteResult[vote] == null)
                                correctVotes++;
                            else
                                voteResult[vote].addGotVoted();
                        } else {
                            player.setHasVoted(vote);
                            if (voteResult[vote] == null)
                                correctVotes++;
                            else
                                voteResult[vote].addGotVoted();
                        }
                    }
                    updateVoteMessages();
                }
            } else if (event instanceof MessageReactionRemoveEvent) {
                MessageReactionRemoveEvent mrre = (MessageReactionRemoveEvent) event;
                if (mrre.getUser() != null && Player.getById(mrre.getUser().getIdLong()) != null) {
                    Player player = Player.getById(mrre.getUser().getIdLong());
                    if (mrre.getMessageIdLong() != player.getVoteMessageId())
                        return;
                    String emoji = mrre.getReaction().getReactionEmote().getAsCodepoints();
                    if (emoji.startsWith("U+1f1e")) {
                        int vote = (emoji.charAt(emoji.length() - 1) - '6');
                        if (vote == player.getHasVoted()) {
                            if (voteResult[vote] == null)
                                correctVotes--;
                            else
                                voteResult[vote].subtractGotVoted();
                            player.setHasVoted(-1);
                            updateVoteMessages();
                        }
                    }

                }
            }
        }

    }

    public void addPlayer(Player player) {
        playersInGame.add(player);
        long joinMsgID = player.getUser().openPrivateChannel().complete().sendMessage(new EmbedBuilder().addField("Players joined:", playerListString, true).build()).complete().getIdLong();
        player.setJoinMessageId(joinMsgID);
        updatePlayerString();
        for (Player pig : playersInGame) {
            pig.getUser().openPrivateChannel().complete().editMessageById(pig.getJoinMessageId(), new EmbedBuilder().setColor(0xFFFF00).addField("Players joined:", "> " + playerListString, true).build()).queue();
        }
        if (playersInGame.size() == MAX_PLAYERS) {
            for (Player p : playersInGame) {
                p.getUser().openPrivateChannel().complete().editMessageById(p.getJoinMessageId(), new EmbedBuilder().setColor(0x00FF00).addField("Players joined:", playerListString, true).build()).queue();
                p.getUser().openPrivateChannel().complete().sendMessage("Enough players joined. Starting the game...").queue();
            }
            startGame();
        }
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    private void updatePlayerString() {
        playerListString = "";
        for (Player pig : playersInGame) {
            playerListString = playerListString.concat(pig.getUser().getName() + " ");
        }
    }

    private void updateVoteMessages() {
        EmbedBuilder voteEBuilder = new EmbedBuilder();
        voteEBuilder.setColor(0x0050F0);
        voteEBuilder.setTitle("Vote the real headline:");
        for (int i = 0; i < voteResult.length; i++) {
            if (voteResult[i] == null) {
                voteEBuilder.addField(":regional_indicator_" + (char) (i + 'a') + ": (" + correctVotes + ")", correctAnswer, false);
            } else {
                voteEBuilder.addField(":regional_indicator_" + (char) (i + 'a') + ": (" + voteResult[i].getGotVoted() + ")", voteResult[i].getSubmittedHeadline(), false);
            }
        }

        for (Player pig : playersInGame) {

            pig.getUser().openPrivateChannel().complete().editMessageById(pig.getVoteMessageId(), voteEBuilder.build()).queue();
        }
    }
}
