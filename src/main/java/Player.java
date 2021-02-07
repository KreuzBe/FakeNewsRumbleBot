import net.dv8tion.jda.api.entities.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Player {

    public static HashMap<Long, Player> playerCache = new HashMap<>();

    private GameContainer gc;

    private User user;
    private long joinMessageId;
    private long puzzleMessageId;
    private long voteMessageId;
    private long resultMessageId;

    private String submittedHeadline = "Fake news";

    private int score = 0;

    private int voteTarget = -1;
    private ArrayList<Player> voters = new ArrayList<>();

    private HashMap<String, ArrayList<String>> possibleComponents = new HashMap<>();
    private HashMap<String, String> selectedComponents = new HashMap<>();

    // TODO HashMap for ct;
    private HashMap<Long, String> messageCT = new HashMap<>();

    public Player(User user) {
        if (playerCache.containsKey(user.getIdLong())) {
            user.openPrivateChannel().complete().sendMessage("You are already in a game... please leave the current game first!").complete();
        }
        playerCache.put(user.getIdLong(), this);
        this.user = user;
        submittedHeadline = submittedHeadline.concat(" " + user.getDiscriminator());
    }

    public void nextRound() {
        voteTarget = -1;
        selectedComponents.clear();
        possibleComponents.clear();
        voters.clear();
        messageCT.clear();
    }

    public GameContainer getGc() {
        return gc;
    }

    public void setGc(GameContainer gc) {
        this.gc = gc;
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

    public long getPuzzleMessageId() {
        return puzzleMessageId;
    }

    public void setPuzzleMessageId(long puzzleMessageId) {
        this.puzzleMessageId = puzzleMessageId;
    }

    public long getVoteMessageId() {
        return voteMessageId;
    }

    public void setVoteMessageId(long voteMessageId) {
        this.voteMessageId = voteMessageId;
    }

    public long getResultMessageId() {
        return resultMessageId;
    }

    public void setResultMessageId(long resultMessageId) {
        this.resultMessageId = resultMessageId;
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

    public int getVotedTarget() {
        return voteTarget;
    }

    public void setVotedTarget(int hasVoted) {
        this.voteTarget = hasVoted;
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

    public ArrayList<String> getPossibleComponents(String type) {
        return possibleComponents.get(type);
    }

    public void setPossibleComponents(String type, ArrayList<String> possibleComponents) {
        this.possibleComponents.put(type, possibleComponents);
    }

    public void setSelectedComponent(String type, String value) {
        this.selectedComponents.put(type, value);
    }

    public String getSelectedComponent(String type) {
        return selectedComponents.get(type);
    }

    public void setMessageComponentType(long id, String type) {
        messageCT.put(id, type);
    }

    public String getMessageComponentType(long id) {
        return messageCT.get(id);
    }

    public long getMessageComponentType(String ct) {
        for (Map.Entry<Long, String> e : messageCT.entrySet()) {
            if (e.getValue().equals(ct))
                return e.getKey();
        }
        return 0;
    }

    /**
     * Deletes this player from the player cache
     */
    public void removeFromCache() {
        playerCache.remove(user.getIdLong());
        this.setGc(null);
    }

    public static Player getById(long id) {
        return playerCache.get(id);
    }


}
