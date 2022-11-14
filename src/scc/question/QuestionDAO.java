package scc.question;

public class QuestionDAO {

    private String _rid, _ts, id, ownerId, auctionId, message, reply;

    public QuestionDAO(String id, String ownerId, String auctionId, String message, String reply) {
        this.id = id;
        this.ownerId = ownerId;
        this.auctionId = auctionId;
        this.message = message;
        this.reply = reply;
    }

    public QuestionDAO(Question q) {
        this(q.getId(), q.getOwnerId(), q.getAuctionId(), q.getMessage(), q.getReply());
    }

    public QuestionDAO(){}

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

    public String getReply() {
        return this.reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public Question toQuestion() {
        return new Question(id, ownerId, auctionId, message, reply);
    }

    @Override
    public String toString() {
        return "Question [id = " + id + "\n ownerId = " + ownerId + "\n auctionId = " + auctionId + "\n message = "
                + message + "\n replies = " + reply + "]";
    }

}
