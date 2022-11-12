package scc.question;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class Question {

    private String id, ownerId, auctionId, message;

    @JsonDeserialize(using = )
    private List<Reply> replies;

    public Question(@JsonProperty("ownerId") String ownerId,
            @JsonProperty("auctionId") String auctionId, @JsonProperty("message") String message) {
        this.id = UUID.randomUUID().toString();
        this.auctionId = auctionId;
        this.message = message;
        this.ownerId = ownerId;
        this.replies = new ArrayList<Reply>();
    }

    public Question(String id, String ownerId, String auctionId, String message, List<Reply> replies) {
        this.id=id;
        this.auctionId = auctionId;
        this.message = message;
        this.ownerId = ownerId;
        this.replies = replies;
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

    public List<Reply> getReplies() {
        return this.replies;
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
