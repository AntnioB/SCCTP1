package scc.question;

public class Reply {
    
    String ownerId, questionId, message;


    public Reply(String ownerId, String questionId, String message) {
        this.ownerId = ownerId;
        this.questionId = questionId;
        this.message = message;
    }


    public String getOwnerId() {
        return this.ownerId;
    }

    public String getQuestionId() {
        return this.questionId;
    }

    public String getMessage() {
        return this.message;
    }


    @Override
    public String toString() {
        return "{" +
            " ownerId='" + getOwnerId() + "'" +
            ", questionId='" + getQuestionId() + "'" +
            ", message='" + getMessage() + "'" +
            "}";
    }

}
