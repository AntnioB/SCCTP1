package scc.data;

import com.fasterxml.jackson.annotation.JsonProperty;

import scc.utils.UniqueId;

public class Question {

    private String id, ownerId, auctionId, message, reply;

    public Question(@JsonProperty("ownerId") String ownerId,
            @JsonProperty("auctionId") String auctionId, @JsonProperty("message") String message) {
        this.id = UniqueId.questionId();
        this.auctionId = auctionId;
        this.message = message;
        this.ownerId = ownerId;
        reply = null;
    }

    public Question(String id, String ownerId, String auctionId, String message, String reply) {
        this.id=id;
        this.auctionId = auctionId;
        this.message = message;
        this.ownerId = ownerId;
        this.reply = reply;
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

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }


    @Override
    public String toString() {
        return "Question [id = " + id + "\n ownerId = " + ownerId + "\n auctionId = " + auctionId + "\n message = "
                + message + "\n reply = " + reply + "]";
    }
}
