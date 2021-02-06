import net.dv8tion.jda.api.entities.User;

import java.util.HashMap;

public class Player {

    public static HashMap<Long, Player> playerCache = new HashMap<>();

    private User user;
    private long joinMessageId;
    private long voteMessageId;

    private String submittedHeadline = "No Headline";
    private int voteResultIndex = 0;

    private int hasVoted = -1;
    private int gotVoted = 0;

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

    public long getVoteMessageId() {
        return voteMessageId;
    }

    public void setVoteMessageId(long voteMessageId) {
        this.voteMessageId = voteMessageId;
    }

    public String getSubmittedHeadline() {
        return submittedHeadline;
    }

    public void submitHeadline(String submittedHeadline) {
        this.submittedHeadline = submittedHeadline;
    }

    public int getVoteResultIndex() {
        return voteResultIndex;
    }

    public void setVoteResultIndex(int voteResultIndex) {
        this.voteResultIndex = voteResultIndex;
    }

    public int getHasVoted() {
        return hasVoted;
    }

    public void setHasVoted(int hasVoted) {
        this.hasVoted = hasVoted;
    }

    public int getGotVoted() {
        return gotVoted;
    }

    public void setGotVoted(int gotVoted) {
        this.gotVoted = gotVoted;
    }

    public void addGotVoted() {
        this.gotVoted++;
    }

    public void subtractGotVoted() {
        this.gotVoted--;
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
