package scc.question;

import java.util.List;

public class QuestionDAO {

    private String _rid, _ts, id, ownerId, auctionId, message;
    private List<Reply> replies;

    public QuestionDAO(String id, String ownerId, String auctionId, String message, List<Reply> replies) {
        this.id = id;
        this.ownerId = ownerId;
        this.auctionId = auctionId;
        this.message = message;
        this.replies = replies;
    }

    public QuestionDAO(Question q) {
        this(q.getId(), q.getOwnerId(), q.getAuctionId(), q.getMessage(), q.getReplies());
    }

    public String get_rid() {
        return this._rid;
    }

    public void set_rid(String _rid) {
        this._rid = _rid;
    }

    public String get_ts() {
        return this._ts;
    }

    public void set_ts(String _ts) {
        this._ts = _ts;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwnerID() {
        return this.ownerId;
    }

    public void setOwnerID(String ownerID) {
        this.ownerId = ownerID;
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

    public List<Reply> getReplies() {
        return this.replies;
    }

    public void setReplies(List<Reply> replies) {
        this.replies = replies;
    }

    public Question toQuestion() {
        return new Question(id, ownerId, auctionId, message, replies);
    }

    @Override
    public String toString() {
        return "Question [id = " + id + "\n ownerId = " + ownerId + "\n auctionId = " + auctionId + "\n message = "
                + message + "\n replies = " + replies.toString() + "]";
    }

}
