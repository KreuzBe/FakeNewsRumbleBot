import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;

public class GameContainer implements EventListener {
    private static final int MAX_PLAYERS = 2;

    public enum GameState {LOBBY, PUZZLE, VOTE, REVEAL}

    private JDA jda;
    private ArrayList<Player> playersInGame = new ArrayList<>();
    private String playerListString = "";

    private int maxRounds;

    private GameState gameState = GameState.LOBBY;

    private String correctAnswer = "I am the real headline!";
    private int correctVotes = 0;
    private ArrayList<Player> correctVoters = new ArrayList<>();
    private Player[] voteResult;

    public GameContainer(int maxRounds, JDA jda) {
        this.maxRounds = maxRounds;
        this.jda = jda;
        jda.addEventListener(this);
    }

    private void startGame() {
        System.out.println("Starting the game");
        Thread waitingThread = new Thread(this::puzzle);
        waitingThread.start();
    }

    private void puzzle() {

        //FIXME RESET GAME!!!

        // Get headline
        // Puzzle

        //XXX if timer thead necessary, reuse waiting Thread from vote!!!
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
        updateVoteMessages();

        for (int i = 10; i >= 0; i--) {
            System.out.println(i);
            for (Player pig : playersInGame) {
                pig.getUser().openPrivateChannel().complete().editMessageById(pig.getVoteMessageId(), "Time left: " + i).complete();
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        for (Player pig : playersInGame) {
            pig.getUser().openPrivateChannel().complete().sendMessage("Voting time is over!!!").complete();
        }
        gameState = GameState.REVEAL;
        reveal();

//        gameState = GameState.REVEAL;
//        reveal();
    }

    private void reveal() {


        EmbedBuilder revealEBuilder;
        int score;
        for (Player pig : playersInGame) {
            score = 0;
            if (voteResult[pig.getHasVoted()] == null) {
                revealEBuilder = new EmbedBuilder().setColor(0x00AA00).addField("You were right: ", "+1", false);
                score += 1;
            } else if (voteResult[pig.getHasVoted()].getUser().getIdLong() == pig.getUser().getIdLong()) {
                revealEBuilder = new EmbedBuilder().setColor(0xAA0000).addField("You voted for yourself... :disappointed:", "-3", false).addField("But the original headline was: ", "\"" + correctAnswer + "\"", false);
                score -= 3;
            } else {
                revealEBuilder = new EmbedBuilder().setColor(0xAA0000).addField("You voted wrong: ", "+0", false);
            }
            revealEBuilder.setTitle("RESULTS:");
            score += 2 * pig.getGotVoted();
            if (pig.getGotVoted() > 0)
                revealEBuilder.addField(pig.getGotVoted() + " people believed your lie:", "+" + 2 * pig.getGotVoted(), false);
            pig.addToScore(score);
            revealEBuilder.addBlankField(false).addField("Score:", (score < 0 ? "-" : "+") + Math.abs(score), false);
            revealEBuilder.setFooter("Your have now " + pig.getScore() + " points");
            revealEBuilder.setDescription("Original headline:```" + correctAnswer + "```").appendDescription("https://github.com/KreuzBe/FakeNewsRumbleBot");
            pig.getUser().openPrivateChannel().complete().sendMessage(revealEBuilder.build()).complete();
        }

        for (int i = 10; i >= 0; i--) {
            System.out.println(i);

            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        gameState = GameState.PUZZLE;
        puzzle();

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
                            if (voteResult[player.getHasVoted()] == null) {
                                correctVotes--;
                                correctVoters.remove(player);
                            } else {
                                voteResult[player.getHasVoted()].subtractGotVoted();
                                voteResult[player.getHasVoted()].removeVoter(player);
                            }
                            player.setHasVoted(vote);
                            if (voteResult[vote] == null) {
                                correctVotes++;
                                correctVoters.add(player);
                            } else {
                                voteResult[vote].addGotVoted();
                                voteResult[vote].addVoter(player);
                            }
                        } else {
                            player.setHasVoted(vote);
                            if (voteResult[vote] == null) {
                                correctVotes++;
                                correctVoters.add(player);
                            } else {
                                voteResult[vote].addGotVoted();
                                voteResult[vote].addVoter(player);
                            }
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
                            if (voteResult[vote] == null) {
                                correctVotes--;
                                correctVoters.remove(player);
                            } else {
                                voteResult[vote].subtractGotVoted();
                                voteResult[vote].removeVoter(player);
                            }
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
        long joinMsgID = player.getUser().openPrivateChannel().complete().sendMessage(new EmbedBuilder().addField("Players joined (" + playersInGame.size() + "/" + MAX_PLAYERS + "): ", playerListString, true).build()).complete().getIdLong();
        player.setJoinMessageId(joinMsgID);
        updatePlayerString();
        for (Player pig : playersInGame) {
            pig.getUser().openPrivateChannel().complete().editMessageById(pig.getJoinMessageId(), new EmbedBuilder().setColor(0xFFFF00).addField("Players joined(" + MAX_PLAYERS + "/" + playersInGame.size() + "):", "> " + playerListString, true).build()).queue();
        }
        if (playersInGame.size() == MAX_PLAYERS) {
            for (Player p : playersInGame) {
                p.getUser().openPrivateChannel().complete().editMessageById(p.getJoinMessageId(), new EmbedBuilder().setColor(0x00FF00).addField("Players joined(" + MAX_PLAYERS + "/" + playersInGame.size() + "):", playerListString, true).build()).queue();
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
            String voterString = "";
            for (Player pig : (voteResult[i] == null ? correctVoters : voteResult[i].getVoters())) {
                voterString += pig.getUser().getName() + "\n";
            }
            //if (voterString.length() > 1)
            //  voterString = voterString.substring(0, voterString.length() - 2);

            if (voteResult[i] == null) {
                voteEBuilder.addField(":regional_indicator_" + (char) (i + 'a') + ": (" + correctVotes + ")", correctAnswer, true);
            } else {
                voteEBuilder.addField(":regional_indicator_" + (char) (i + 'a') + ": (" + voteResult[i].getGotVoted() + ")", voteResult[i].getSubmittedHeadline(), true);
            }
            voteEBuilder.addBlankField(true);
            voteEBuilder.addField("Voters:", voterString, true);
            voteEBuilder.addBlankField(false);
        }

        for (Player pig : playersInGame) {
            if (pig.getHasVoted() != -1)
                voteEBuilder.setDescription("You voted: :regional_indicator_" + (char) (pig.getHasVoted() + 'a') + ":");
            else
                voteEBuilder.setDescription("");
            pig.getUser().openPrivateChannel().complete().editMessageById(pig.getVoteMessageId(), voteEBuilder.build()).queue();
        }
    }
}
