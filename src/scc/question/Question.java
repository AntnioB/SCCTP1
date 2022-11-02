package scc.question;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Question {

    private String id, ownerId, auctionId, message;
    private List<Reply> replies;

    public Question(@JsonProperty("id") String id, @JsonProperty("ownerId") String ownerId,
            @JsonProperty("auctionId") String auctionId, @JsonProperty("message") String message,
            @JsonProperty("replies") Reply[] replies) {
        this.id = id;
        this.auctionId = auctionId;
        this.message = message;
        this.ownerId = ownerId;
        this.replies = Arrays.asList(replies);
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuctionId() {
        return this.auctionId;
    }

    public void setAuctionId(String auctionId) {
        this.auctionId = auctionId;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getOwnerId() {
        return this.ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public Reply[] getReplies() {
        return this.replies.toArray(new Reply[10]);
    }

    public void addReply(Reply reply) {
        this.replies.add(reply);
    }

    @Override
    public String toString() {
        return "Question [id = " + id + "\n ownerId = " + ownerId + "\n auctionId = " + auctionId + "\n message = "
                + message + "\n replies = " + replies.toString() + "]";
    }
}
