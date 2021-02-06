import net.dv8tion.jda.api.entities.User;

import java.util.ArrayList;
import java.util.HashMap;

public class Player {

    public static HashMap<Long, Player> playerCache = new HashMap<>();

    private User user;
    private long joinMessageId;
    private long voteMessageId;

    private String submittedHeadline = "Fake news";

    private int score = 0;

    private int hasVoted = -1;
    private int gotVoted = 0;
    private ArrayList<Player> voters = new ArrayList<>();

    public Player(User user) {
        if (playerCache.containsKey(user.getIdLong())) {
            user.openPrivateChannel().complete().sendMessage("You are already in a game... please leave the current game first!").complete();
            System.err.println(user.getName() + " has tried to enter a second game!");
            // TODO tell the user how to leave the game
        }
        playerCache.put(user.getIdLong(), this);
        this.user = user;
        submittedHeadline = submittedHeadline.concat(" " + user.getDiscriminator());
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

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void addToScore(int value) {
        score += value;
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

    public void addVoter(Player player) {
        voters.add(player);
    }

    public void removeVoter(Player player) {
        voters.remove(player);
    }

    public ArrayList<Player> getVoters() {
        return voters;
    }

    public void clearVoter() {
        voters.clear();
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
