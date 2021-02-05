import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.internal.entities.EmoteImpl;

import javax.security.auth.login.LoginException;
import java.util.Objects;

public class Main implements EventListener {
    public static void main(String[] args)
            throws LoginException, InterruptedException {
        // Note: It is important to register your ReadyListener before building
        JDA jda = JDABuilder.createDefault("ODA3MzAzNjYyMTYxODg3MjQz.YB2CIA.vd95_yJt9NcmGNN4coDCHNwbydQ")
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
