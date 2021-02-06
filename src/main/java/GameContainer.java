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


    public enum GameState {LOBBY, PUZZLE, VOTE, REVEAL}


    private JDA jda; // JDA required by discord bot
    private ArrayList<Player> playersInGame = new ArrayList<>(); // all participants in current game

    private int maxRounds = 3;
    private int currentRound = 0;
    private int maxPlayers = 2;

    private GameState gameState = GameState.LOBBY;

    private String correctAnswer = "I am the real headline!";
    private ArrayList<Player> correctVoters = new ArrayList<>(); // those who voted for the correct headline
    private Player[] voteResult; // the players how they appear in the vote list

    public GameContainer(int maxRounds, int maxPlayers, JDA jda) {
        this.maxRounds = maxRounds;
        this.maxPlayers = maxPlayers;
        this.jda = jda;
        jda.addEventListener(this);
    }

    private void startGame() {
        System.out.println("Starting the game");
        Thread waitingThread = new Thread(this::puzzle);
        waitingThread.start();
    }

    private void puzzle() {
        if (currentRound == maxRounds) {
            //TODO END GAME
            return;
        }
        currentRound++;
        correctAnswer += ".";
        correctVoters.clear();
        for (Player pig : playersInGame) {
            pig.setVotedTarget(-1);
            pig.clearVoter();
        }


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
            if (voteResult[pig.getVotedTarget()] == null) {
                revealEBuilder = new EmbedBuilder().setColor(0x00AA00).addField("You were right: ", "+1", false);
                score += 1;
            } else if (voteResult[pig.getVotedTarget()].getUser().getIdLong() == pig.getUser().getIdLong()) {
                revealEBuilder = new EmbedBuilder().setColor(0xAA0000).addField("You voted for yourself... :disappointed:", "-3", false).addField("But the original headline was: ", "\"" + correctAnswer + "\"", false);
                score -= 3;
            } else {
                revealEBuilder = new EmbedBuilder().setColor(0xAA0000).addField("You voted wrong: ", "+0", false);
            }
            revealEBuilder.setTitle("RESULTS:");
            score += 2 * pig.getVoters().size();
            if (pig.getVoters().size() > 0)
                revealEBuilder.addField(pig.getVoters().size() + " people believed your lie:", "+" + 2 * pig.getVoters().size(), false);
            pig.addToScore(score);
            revealEBuilder.addBlankField(false).addField("Score:", (score < 0 ? "-" : "+") + Math.abs(score), false);
            revealEBuilder.setFooter("Your have now " + pig.getScore() + " points");
            revealEBuilder.setDescription("Original headline:```" + correctAnswer + "```").appendDescription("https://github.com/KreuzBe/FakeNewsRumbleBot");
            pig.setResultMessageId(pig.getUser().openPrivateChannel().complete().sendMessage(revealEBuilder.build()).complete().getIdLong());
        }

        for (int i = 5; i >= 0; i--) {
            for (Player pig : playersInGame) {
                pig.getUser().openPrivateChannel().complete().editMessageById(pig.getResultMessageId(), "Time left: " + i).complete();
            }
            try {
                Thread.sleep(1000);
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
                        } else if (player.getVotedTarget() != -1) {
                            if (voteResult[player.getVotedTarget()] == null) {
                                correctVoters.remove(player);
                            } else {
                                voteResult[player.getVotedTarget()].removeVoter(player);
                            }
                            player.setVotedTarget(vote);
                            if (voteResult[vote] == null) {
                                correctVoters.add(player);
                            } else {
                                voteResult[vote].addVoter(player);
                            }
                        } else {
                            player.setVotedTarget(vote);
                            if (voteResult[vote] == null) {
                                correctVoters.add(player);
                            } else {
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
                        if (vote == player.getVotedTarget()) {
                            if (voteResult[vote] == null) {
                                correctVoters.remove(player);
                            } else {
                                voteResult[vote].removeVoter(player);
                            }
                            player.setVotedTarget(-1);
                            updateVoteMessages();
                        }
                    }

                }
            }
        }

    }

    public void addPlayer(Player player) {
        playersInGame.add(player);

        String playerListString = "";
        for (Player pig : playersInGame) {
            playerListString = playerListString.concat(pig.getUser().getName() + " ");
        }

        long joinMsgID = player.getUser().openPrivateChannel().complete().sendMessage(new EmbedBuilder().addField("Players joined (" + playersInGame.size() + "/" + maxPlayers + "): ", playerListString, true).build()).complete().getIdLong();
        player.setJoinMessageId(joinMsgID);

        for (Player pig : playersInGame) {
            pig.getUser().openPrivateChannel().complete().editMessageById(pig.getJoinMessageId(), new EmbedBuilder().setColor(0xFFFF00).addField("Players joined(" + maxPlayers + "/" + playersInGame.size() + "):", "> " + playerListString, true).build()).queue();
        }
        if (playersInGame.size() == maxPlayers) {
            for (Player p : playersInGame) {
                p.getUser().openPrivateChannel().complete().editMessageById(p.getJoinMessageId(), new EmbedBuilder().setColor(0x00FF00).addField("Players joined(" + maxPlayers + "/" + playersInGame.size() + "):", playerListString, true).build()).queue();
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


    private void updateVoteMessages() {
        EmbedBuilder voteEBuilder = new EmbedBuilder();
        voteEBuilder.setColor(0x0050F0);
        voteEBuilder.setTitle("Vote the real headline:");
        for (int i = 0; i < voteResult.length; i++) {
            String voterString = "";
            for (Player pig : (voteResult[i] == null ? correctVoters : voteResult[i].getVoters())) {
                voterString += "> " + pig.getUser().getName() + "\n";
            }
            if (voteResult[i] == null) {
                voteEBuilder.addField(":regional_indicator_" + (char) (i + 'a') + ": (" + correctVoters.size() + ")", correctAnswer, true);
            } else {
                voteEBuilder.addField(":regional_indicator_" + (char) (i + 'a') + ": (" + voteResult[i].getVoters().size() + ")", voteResult[i].getSubmittedHeadline(), true);
            }
            voteEBuilder.addBlankField(true);
            voteEBuilder.addField("Voters:", voterString, true);
            voteEBuilder.addBlankField(false);
        }

        for (Player pig : playersInGame) {
            if (pig.getVotedTarget() != -1)
                voteEBuilder.setDescription("You voted: :regional_indicator_" + (char) (pig.getVotedTarget() + 'a') + ":");
            else
                voteEBuilder.setDescription("");
            pig.getUser().openPrivateChannel().complete().editMessageById(pig.getVoteMessageId(), voteEBuilder.build()).queue();
        }
    }
}
