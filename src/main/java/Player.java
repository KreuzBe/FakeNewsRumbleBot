import net.dv8tion.jda.api.entities.User;

import java.util.HashMap;

public class Player {

    public static HashMap<Long, Player> playerCache = new HashMap<>();

    private User user;
    private long joinMessageId;

    public Player(User user) {
        if (playerCache.containsKey(user.getIdLong())) {
            user.openPrivateChannel().complete().sendMessage("You are already in a game... please leave the current game first!").complete();
            System.err.println(user.getName() + " has tried to enter a second game!");
            // TODO tell the user how to leave the game
        }
        playerCache.put(user.getIdLong(), this);
        this.user = user;
    }


    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public long getJoinMessageId() {
        return joinMessageId;
    }

    public void setJoinMessageId(long joinMessageId) {
        this.joinMessageId = joinMessageId;
    }

    /**
     * Deletes this player from the player cache
     */
    public void removeFromCache() {
        playerCache.remove(user.getIdLong());
    }

    public static Player getById(long id) {
        return playerCache.get(id);
    }

}
