import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.EventListener;

import javax.security.auth.login.LoginException;

public class Main implements EventListener {
    public static void main(String[] args)
            throws LoginException, InterruptedException {
        // Note: It is important to register your ReadyListener before building
        if (args[0].isBlank()) {
            System.err.println("NO ARGUMENT!!! Please use a token as argument!");
            System.exit(-1);
        }
        JDA jda = JDABuilder.createDefault(args[0])
                .addEventListeners(new Main())
                .build();

        // optionally block until JDA is ready
        jda.awaitReady();
    }


    @Override
    public void onEvent(GenericEvent event) {
        if (event instanceof ReadyEvent) {
            System.out.println("API is ready!");
        }
    }


}
