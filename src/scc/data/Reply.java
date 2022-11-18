package scc.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Reply {
    private String reply, ownerId;

    public Reply(@JsonProperty("reply") String reply, @JsonProperty("ownerId") String ownerId) {
        this.reply = reply;
        this.ownerId = ownerId;
    }

    public String getOwnerId(){
        return ownerId;
    }

    public void setOwnerId(String ownerId){
        this.ownerId = ownerId;
    }

    public String getReply(){
        return reply;
    }

    public void setReply(String reply){
        this.reply = reply;
    }

    @Override
    public String toString(){
        return reply;
    }
}
