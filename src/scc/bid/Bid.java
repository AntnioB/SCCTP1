package scc.bid;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Bid {
    private double amount;
    private String id, auctionId, bidderId;

    public Bid(@JsonProperty("auctionId") String auctionId, @JsonProperty("bidderId") String bidderId,@JsonProperty("amount") double amount){
        this.id = UUID.randomUUID().toString();
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.amount = amount;
    }

    public Bid(String id, String auctionId, String bidderId, double amount) {
        this.id = id;
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.amount = amount;
    }

    public String getId(){
        return id;
    }

    public void setId(String id){
        this.id = id;
    }

    public String getAuctionId(){
        return auctionId;
    }

    public void setAuctionId(String auctionId){
        this.auctionId = auctionId;
    }

    public String getBidderId(){
        return bidderId;
    }

    public void setBidderId(String bidderId){
        this.bidderId = bidderId;
    }

    public double getAmount(){
        return amount;
    }

    public void setAmount(double amount){
        this.amount = amount;
    }

    @Override
    public String toString(){
        return "Bid [auction = "+auctionId+"\n bidder = "+bidderId+"\n amount = "+ amount +"]";
    }

}
