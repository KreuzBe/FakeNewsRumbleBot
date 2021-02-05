import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;
import java.util.HashMap;

/**
 * Main is GameManager (So far)
 */
public class Main implements EventListener {

    private static final String CMD_JOIN = "!join";
    private static final String CMD_KILL = "!kill";

    /**
     * Key: Discord ID of the bot
     * Value: Discord ID of the authorized User
     */
    private static HashMap<Long, Long> authorizedUsers = new HashMap<>();

    static {
        authorizedUsers.put(807342641904615431L, 196693552615391232L);
        authorizedUsers.put(807303662161887243L, 317389493386870784L);
    }

    private static JDA jda;

    private GameContainer currentGame;

    public static void main(String[] args)
            throws LoginException, InterruptedException {
        if (args.length == 0) {
            System.err.println("NO ARGUMENT!!! Please use a token as argument!");
            System.exit(-1);
        }
        jda = JDABuilder.createDefault(args[0])
                .addEventListeners(new Main())
                .build();

        jda.awaitReady();
    }


    @Override
    public void onEvent(@NotNull GenericEvent event) {
        if (event instanceof ReadyEvent) {
            // BOT IS READY, DO SETUP
            System.out.println("API is ready!");
            currentGame = new GameContainer(5, jda);
        } else if (event instanceof MessageReceivedEvent) {
            // MESSAGE EVENTS
            Message m = ((MessageReceivedEvent) event).getMessage();
            if (!m.getAuthor().isBot()) {
                if (m.getContentRaw().equalsIgnoreCase(CMD_JOIN))
                    // JOIN REQUEST
                    if (Player.getById(m.getAuthor().getIdLong()) != null) {
                        // JOIN DENIED: CANT JOIN TWICE
                        m.getAuthor().openPrivateChannel().complete().sendMessage("You are already in a game...").complete();
                    } else if (currentGame.getGameState() != GameContainer.GameState.LOBBY) {
                        // JOIN DENIED: Game already running
                        m.getAuthor().openPrivateChannel().complete().sendMessage("The game is already full. We are sorry...\nPlease wait.").complete();
                    } else {
                        // SUCCESSFUlLY JOINED
                        currentGame.addPlayer(new Player(m.getAuthor()));
                    }
            } else if (m.getContentRaw().equalsIgnoreCase(CMD_KILL)) {
                // KILL COMMANDS
                long jdaId = jda.getSelfUser().getIdLong();
                if (authorizedUsers.containsKey(jdaId) && authorizedUsers.get(jdaId) == m.getAuthor().getIdLong()) {
                    System.out.println("Stopping...");
                    System.exit(0);
                }
            }
        }
    }
}



