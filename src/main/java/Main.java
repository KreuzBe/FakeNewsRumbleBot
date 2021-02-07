import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

/**
 * Main is GameManager (So far)
 */
public class Main implements EventListener {

    private static final String CMD_JOIN = "!join";
    private static final String CMD_SET_MAX_PLAYER = "!setmaxplayer";
    private static final String CMD_SET_MAX_ROUNDS = "!setmaxrounds";
    private static final String CMD_HELP = "!help";
    private static final String CMD_KILL = "!kill";

    /**
     * Key: Discord ID of the bot
     * Value: Discord ID of the authorized User
     */
    private static final HashMap<Long, Long> authorizedUsers = new HashMap<>();

    static {
        authorizedUsers.put(807342641904615431L, 196693552615391232L);
        authorizedUsers.put(807303662161887243L, 317389493386870784L);
    }

    private static JDA jda;
    private int maxPlayers = 3;
    private int maxRounds = 3;

    private static GameContainer currentGame;

    public static void main(String[] args)
            throws LoginException, InterruptedException {

        if (args.length == 0) {
            System.err.println("NO ARGUMENT!!! Please use a token as argument!");
            System.exit(-1);
        }
        jda = JDABuilder.createDefault(args[0])
                .addEventListeners(new Main()).setActivity(Activity.listening("Huey Lewis and the News!"))
                .build();

        jda.awaitReady();
    }


    @Override
    public void onEvent(@NotNull GenericEvent event) {
        if (event instanceof ReadyEvent) {
            // BOT IS READY, DO SETUP
            System.out.println("API is ready!");
        } else if (event instanceof MessageReceivedEvent) {
            // MESSAGE EVENT
            Message m = ((MessageReceivedEvent) event).getMessage();
            if (!m.getAuthor().isBot()) {
                if (m.getContentRaw().equalsIgnoreCase(CMD_JOIN)) {
                    // JOIN REQUEST
                    if (Player.getById(m.getAuthor().getIdLong()) != null) {
                        // JOIN DENIED: CANT JOIN TWICE
                        m.getAuthor().openPrivateChannel().complete().sendMessage("You are already in a game...").complete();
                    } else if (currentGame != null && currentGame.getGameState() != GameContainer.GameState.LOBBY) {
                        // JOIN DENIED: Game already running
                        m.getAuthor().openPrivateChannel().complete().sendMessage("The game is already full. We are sorry...\nPlease wait.").complete();
                    } else {
                        // SUCCESSFUlLY JOINED
                        if (currentGame == null)
                            currentGame = new GameContainer(maxRounds, maxPlayers, jda);
                        currentGame.addPlayer(new Player(m.getAuthor()));
                    }
                } else if (m.getContentRaw().toLowerCase(Locale.ROOT).startsWith(CMD_SET_MAX_PLAYER)) {
                    try {
                        maxPlayers = Integer.parseInt(m.getContentRaw().split(" ")[1]);
                        m.getChannel().sendMessage("The new amout of players is " + maxPlayers).complete();
                    } catch (Exception e) {
                        m.getChannel().sendMessage("Some thing went wrong. Please use \"" + CMD_SET_MAX_PLAYER + " <integer>\"").complete();
                    }

                } else if (m.getContentRaw().toLowerCase(Locale.ROOT).startsWith(CMD_SET_MAX_ROUNDS)) {
                    try {
                        maxRounds = Integer.parseInt(m.getContentRaw().split(" ")[1]);
                        m.getChannel().sendMessage("The new amout of rounds is " + maxRounds).complete();
                    } catch (Exception e) {
                        m.getChannel().sendMessage("Some thing went wrong. Please use \"" + CMD_SET_MAX_ROUNDS + " <integer>\"").complete();
                    }
                } else if (m.getContentRaw().toLowerCase(Locale.ROOT).startsWith(CMD_HELP)) {
                    EmbedBuilder helpEBuilder = new EmbedBuilder().setColor(0x55FF66)
                            .setTitle("How to play:")
                            .addField("Make up your own headline!", "In the first phase of the game, you get a headline with a gap and a word pallet to chose from. The words can be selected by adding a reaction with the corresponding letter to the word list. The selected word is put in the gap. After a certain time, you are not able to select another word. Then the next phase begins.", false)
                            .addField("Find the correct headline!", "Now, you get multiple headlines similar to each other. These are the ones created by the other players and the \"real\" headline we got from the internet. You have to choose which headline is the real one, by adding a reaction with the corresponding letter. After a certain time, you are not able to choose. ", false)
                            .addField("Seeing the results:", "You get a overview about what points you scored (you fooled other players or guessed the right headline). You now get to see the original headline and a link to the page it appeared on. In case there are multiple rounds, the next one starts after a certain time.", false);
                } else if (m.getContentRaw().equalsIgnoreCase(CMD_KILL)) {
                    // KILL COMMANDS
                    long jdaId = jda.getSelfUser().getIdLong();
                    if (authorizedUsers.containsKey(jdaId) && authorizedUsers.get(jdaId) == m.getAuthor().getIdLong()) {
                        System.out.println("Stopping...");
                        jda.shutdown();
                        System.exit(0);
                    }
                }
            }
        }
    }

    public static void resetGame() {
        currentGame = null;
    }
}



