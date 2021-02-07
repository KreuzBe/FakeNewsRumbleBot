import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.ArrayList;

public class GameContainer implements EventListener {
    private final static int UNICODE_A = 0x1f1e6; // A is the first letter of the alphabet.

    private static HeadlineData headlineData;

    public enum GameState {LOBBY, PUZZLE, VOTE, REVEAL}

    private JDA jda; // JDA required by discord bot
    private ArrayList<Player> playersInGame = new ArrayList<>(); // all participants in current game

    private int maxRounds = 3;
    private int currentRound = 0;
    private int maxPlayers = 2;

    int comPerGap = 6;


    //TODO clear last time

    private GameState gameState = GameState.LOBBY;

    private RealHeadline correctHeadline;
    private String correctAnswer = "I am the real headline!";
    private ArrayList<Player> correctVoters = new ArrayList<>(); // those who voted for the correct headline
    private Player[] voteResult; // the players how they appear in the vote list

    private ArrayList<String> components = new ArrayList<>();
    private ArrayList<String> componentTypes = new ArrayList<>();

    public GameContainer(int maxRounds, int maxPlayers, JDA jda) {
        this.maxRounds = maxRounds;
        this.maxPlayers = maxPlayers;
        this.jda = jda;
        try {
            headlineData = new HeadlineData();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        jda.addEventListener(this);
    }

    private void startGame() {
        System.out.println("Starting the game");
        Thread waitingThread = new Thread(this::puzzle);
        waitingThread.start();
    }

    private void puzzle() {
        currentRound++;
        correctAnswer += ".";
        correctVoters.clear();
        for (Player pig : playersInGame) {
            pig.nextRound();
        }
// Start Puzzle
        gameState = GameState.PUZZLE;

        correctHeadline = headlineData.GetRandomRealHeadline();
        correctAnswer = correctHeadline.GetText();
        components.clear();
        componentTypes.clear();
        correctHeadline.GetComponents().forEach((a, b) -> {
            components.add(b);
            componentTypes.add(a);
        });

        // Missing Component
        components.set((int) (Math.random() * components.size()), null);

        EmbedBuilder puzzleEBuilder;

        for (int j = 0; j < components.size(); j++) {
            String s = components.get(j);
            if (s == null) {
                String ct = componentTypes.get(j);
                for (Player pig : playersInGame) {
                    puzzleEBuilder = new EmbedBuilder().setColor(0xa0a0a0);
                    pig.setPossibleComponents(ct, new ArrayList<>());

                    for (int i = 0; i < comPerGap; i++) {
                        String comp = "";
                        boolean isNewComp = true;
                        do {
                            isNewComp = true;
                            comp = headlineData.GetRandomHeadlineComponent(ct);
                            if (comp.equals(correctHeadline.GetComponents().get(ct))) {
                                isNewComp = false;
                                continue;
                            }
                            for (String pComp : pig.getPossibleComponents(ct)) {
                                if (pComp.equals(comp)) {
                                    isNewComp = false;
                                }
                            }
                        } while (!isNewComp);
                        pig.getPossibleComponents(ct).add(comp);
                    }
                    for (int i = 0; i < comPerGap; i++) {
                        puzzleEBuilder.addField(":regional_indicator_" + (char) (i + 'a') + ":", pig.getPossibleComponents(ct).get(i), true);
                    }
                    for (int i = 0; i < 3 - (comPerGap % 3); i++) { // fill the empty spaces (embeds can only by three inline)
                        puzzleEBuilder.addBlankField(true);
                    }
                    puzzleEBuilder.addBlankField(false);

                    Message wordsMessage = pig.getUser().openPrivateChannel().complete().sendMessage(puzzleEBuilder.build()).complete();
                    for (int i = 0; i < comPerGap; i++) {
                        wordsMessage.addReaction("U+" + Integer.toHexString(UNICODE_A + i)).queue();
                    }
                    pig.setMessageComponentType(wordsMessage.getIdLong(), componentTypes.get(j));
                }
            }
        }
        EmbedBuilder solutionEBuilder;
        for (Player pig : playersInGame) {
            solutionEBuilder = new EmbedBuilder();
            solutionEBuilder.setColor(0xFFFF00);
            solutionEBuilder.addField("Your current solution:", "```" + getPlayerHeadline(pig) + "```", false);
            pig.setPuzzleMessageId(pig.getUser().openPrivateChannel().complete().sendMessage(solutionEBuilder.build()).complete().getIdLong());
        }

        for (int i = 24; i >= 0; i--) { //TODO time depending on gaps
            int clock = (int) ((i / 24f) * 12);
            for (Player pig : playersInGame) {
                pig.getUser().openPrivateChannel().complete().editMessageById(pig.getPuzzleMessageId(), "Time left: :clock" + (clock == 0 ? 12 : clock) + ": (" + i + ")").complete();
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        for (Player pig : playersInGame) {
            pig.getUser().openPrivateChannel().complete().editMessageById(pig.getPuzzleMessageId(), "-").complete();
        }
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
                voteEBuilder.addField(":regional_indicator_" + (char) (i + 'a') + ":", getPlayerHeadline(voteResult[i]), false);

            }
        }
        MessageEmbed voteEmbed = voteEBuilder.build();
        for (Player pig : playersInGame) {
            Message voteMessage = pig.getUser().openPrivateChannel().complete().sendMessage(voteEmbed).complete();
            for (int i = 0; i < voteResult.length; i++) {
                voteMessage.addReaction("U+" + Integer.toHexString(UNICODE_A + i)).queue();
            }
            pig.setVoteMessageId(voteMessage.getIdLong());
        }
        updateVoteMessages();

        for (int i = 12; i >= 0; i--) {
            int clock = (int) ((i / 12f) * 12);
            for (Player pig : playersInGame) {
                pig.getUser().openPrivateChannel().complete().editMessageById(pig.getVoteMessageId(), "Time left: :clock" + (clock == 0 ? 12 : clock) + ": (" + i + ")").complete();
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        for (Player pig : playersInGame) {
            pig.getUser().openPrivateChannel().complete().editMessageById(pig.getVoteMessageId(), "-").complete();
        }

        for (Player pig : playersInGame) {
            pig.getUser().openPrivateChannel().complete().sendMessage("Voting time is over!!!").complete();
        }
        gameState = GameState.REVEAL;
        reveal();
    }

    private void reveal() {
        EmbedBuilder revealEBuilder;
        int score;
        for (Player pig : playersInGame) {
            score = 0;
            if (pig.getVotedTarget() == -1) {
                revealEBuilder = new EmbedBuilder().setColor(0xAA0000).addField("You did not vote... :question:", "-1", false);
                score -= 1;
            } else if (voteResult[pig.getVotedTarget()] == null) {
                revealEBuilder = new EmbedBuilder().setColor(0x00AA00).addField("You were right: ", "+1", false);
                score += 1;
            } else if (voteResult[pig.getVotedTarget()].getUser().getIdLong() == pig.getUser().getIdLong()) {
                revealEBuilder = new EmbedBuilder().setColor(0xAA0000).addField("You voted for yourself... :disappointed:", "-3", false);
                score -= 3;
            } else {
                revealEBuilder = new EmbedBuilder().setColor(0xAA0000).addField("You voted wrong: ", "+0", false);
            }
            revealEBuilder.setTitle("RESULTS:");
            score += 2 * pig.getVoters().size();
            if (pig.getVoters().size() > 0)
                revealEBuilder.addField(pig.getVoters().size() + (pig.getVoters().size() == 1 ? " person" : "people") + " believed your lie:", "+" + 2 * pig.getVoters().size(), false);
            pig.addToScore(score);
            revealEBuilder.addField("Score:", (score < 0 ? "-" : "+") + Math.abs(score), false);
            revealEBuilder.setFooter("You have now " + pig.getScore() + " points");
            revealEBuilder.setDescription("Original headline:```" + correctAnswer + "```").appendDescription(correctHeadline.GetLink());
            pig.setResultMessageId(pig.getUser().openPrivateChannel().complete().sendMessage(revealEBuilder.build()).complete().getIdLong());
        }
        if (currentRound == maxRounds) {
            //TODO END GAME
            for (Player pig : playersInGame) {
                pig.getUser().openPrivateChannel().complete().sendMessage("The game is now over\nYou have " + pig.getScore() + " points!").complete();
                pig.removeFromCache();
            }
            Main.resetGame();
            return;
        }
        for (int i = 6; i >= 0; i--) {
            int clock = (int) ((i / 6f) * 12);
            for (Player pig : playersInGame) {
                pig.getUser().openPrivateChannel().complete().editMessageById(pig.getResultMessageId(), "Time left: :clock" + (clock == 0 ? 12 : clock) + ": (" + i + ")").complete();
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        for (Player pig : playersInGame) {
            pig.getUser().openPrivateChannel().complete().editMessageById(pig.getResultMessageId(), "-").complete();
        }
        gameState = GameState.PUZZLE;
        puzzle();

    }

    @Override
    public void onEvent(@NotNull GenericEvent event) {
        if (gameState == GameState.PUZZLE) {
            if (event instanceof MessageReactionAddEvent) {
                MessageReactionAddEvent mrae = (MessageReactionAddEvent) event;
                if (mrae.getUser() != null && Player.getById(mrae.getUser().getIdLong()) != null) {
                    Player player = Player.getById(mrae.getUser().getIdLong());

                    if (player.getGc() == this) {
                        if (mrae.getReactionEmote().isEmoji()) {
                            String emoji = mrae.getReaction().getReactionEmote().getAsCodepoints();
                            int selectedOption = Integer.parseInt(emoji.substring(2), 16) - UNICODE_A;

                            if (selectedOption < 0 || selectedOption > (player.getPossibleComponents(player.getMessageComponentType(mrae.getMessageIdLong())).size())) { // TODO assert selectedOption is in right range
                                mrae.getUser().openPrivateChannel().complete().sendMessage("Please vote one of the options.").complete();
                                return;
                            }
                            player.setSelectedComponent(player.getMessageComponentType(mrae.getMessageIdLong()), player.getPossibleComponents(player.getMessageComponentType(mrae.getMessageIdLong())).get(selectedOption));
                            updatePuzzleMessage(player, mrae.getMessageIdLong());
                        }
                    }
                }
            }
        } else if (gameState == GameState.VOTE) {
            if (event instanceof MessageReactionAddEvent) {
                MessageReactionAddEvent mrae = (MessageReactionAddEvent) event;
                if (mrae.getUser() != null && Player.getById(mrae.getUser().getIdLong()) != null) {
                    Player player = Player.getById(mrae.getUser().getIdLong());
                    if (mrae.getMessageIdLong() != player.getVoteMessageId() || player.getGc() != this) {
                        return;
                    }
                    if (mrae.getReactionEmote().isEmoji()) {
                        String emoji = mrae.getReaction().getReactionEmote().getAsCodepoints();
                        int vote = Integer.parseInt(emoji.substring(2), 16) - UNICODE_A;
                        System.out.println(vote);
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
                    if (mrre.getMessageIdLong() != player.getVoteMessageId() || player.getGc() != this)
                        return;
                    String emoji = mrre.getReaction().getReactionEmote().getAsCodepoints();
                    if (emoji.startsWith("U+")) {
                        int vote = Integer.parseInt(emoji.substring(2), 16) - UNICODE_A;
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
        player.setGc(this);
        String playerListString = "";
        for (Player pig : playersInGame) {
            playerListString = playerListString.concat("> " + pig.getUser().getName() + "\n");
        }

        long joinMsgID = player.getUser().openPrivateChannel().complete().sendMessage(new EmbedBuilder().addField("Players joined (" + playersInGame.size() + "/" + maxPlayers + "): ", playerListString, true).build()).complete().getIdLong();
        player.setJoinMessageId(joinMsgID);

        for (Player pig : playersInGame) {
            pig.getUser().openPrivateChannel().complete().editMessageById(pig.getJoinMessageId(), new EmbedBuilder().setColor(0xFFFF00).addField("Players joined(" + playersInGame.size() + "/" + maxPlayers + "):", "> " + playerListString, true).build()).queue();
        }
        if (playersInGame.size() == maxPlayers) {
            for (Player p : playersInGame) {
                p.getUser().openPrivateChannel().complete().editMessageById(p.getJoinMessageId(), new EmbedBuilder().setColor(0x00FF00).addField("Players joined(" + playersInGame.size() + "/" + maxPlayers + "):", playerListString, true).build()).queue();
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

    private void updatePuzzleMessage(Player p, long messageId) {
        for (int j = 0; j < components.size(); j++) {
            String s = components.get(j);
            if (s == null) {
                String ct = componentTypes.get(j);
                EmbedBuilder puzzleEBuilder = new EmbedBuilder().setColor(0xa0a0a0);

                for (int i = 0; i < comPerGap; i++) {
                    if (p.getSelectedComponent(ct).equals(p.getPossibleComponents(ct).get(i)))
                        puzzleEBuilder.addField(":regional_indicator_" + (char) (i + 'a') + ": [selected]", p.getPossibleComponents(ct).get(i), true);
                    else
                        puzzleEBuilder.addField(":regional_indicator_" + (char) (i + 'a') + ":", p.getPossibleComponents(ct).get(i), true);
                }
                for (int i = 0; i < 3 - (comPerGap % 3); i++) { // fill the empty spaces (embeds can only by three inline)
                    puzzleEBuilder.addBlankField(true);
                }
//                puzzleEBuilder.addBlankField(false);

                p.getUser().openPrivateChannel().complete().editMessageById(messageId, puzzleEBuilder.build()).complete();
            }

        }
        EmbedBuilder solutionEBuilder = new EmbedBuilder();

        solutionEBuilder.setColor(0xFFFF00);
        solutionEBuilder.addField("Your current solution:", "```" + getPlayerHeadline(p) + "```", false);
        p.getUser().openPrivateChannel().complete().editMessageById(p.getPuzzleMessageId(), solutionEBuilder.build()).complete();


    }

    private void updateVoteMessages() {
        for (Player pig : playersInGame) {
            EmbedBuilder voteEBuilder = new EmbedBuilder();
            for (int i = 0; i < voteResult.length; i++) {
                if (voteResult[i] == null) {
                    voteEBuilder.addField(":regional_indicator_" + (char) (i + 'a') + ":" + (pig.getVotedTarget() == i ? " [selected]" : ""), correctAnswer, true);
                } else {
                    voteEBuilder.addField(":regional_indicator_" + (char) (i + 'a') + ":" + (pig.getVotedTarget() == i ? " [selected]" : ""), getPlayerHeadline(voteResult[i]), true);
                }

                String voterString = "";
                for (Player player : (voteResult[i] == null ? correctVoters : voteResult[i].getVoters())) {
                    voterString += ("> " + player.getUser().getName() + "\n");
                }

                voteEBuilder.addField(" ", voterString, true);
                voteEBuilder.addBlankField(false);
            }

            pig.getUser().openPrivateChannel().complete().editMessageById(pig.getVoteMessageId(), voteEBuilder.build()).complete();
        }
    }

    private String getPlayerHeadline(Player p) {
        String pigHeadline = "";
        for (int i = 0; i < components.size(); i++) {
            if (components.get(i) == null) {
                if (p.getSelectedComponent(componentTypes.get(i)) == null) {
                    pigHeadline += "==?== ";
                } else {
                    pigHeadline += p.getSelectedComponent(componentTypes.get(i)) + " ";
                }
            } else {
                pigHeadline += components.get(i) + " ";
            }
        }
        return pigHeadline;
    }
}
