package scc.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Bid {
    private Auction auction;
    private User bidder;
    private double value;

    public Bid(@JsonProperty("auction") Auction auction, @JsonProperty("bidder") User bidder,@JsonProperty("value") double value){
        this.auction = auction;
        this.bidder = bidder;
        this.value = value;
    }

    public Auction getAuction(){
        return auction;
    }

    public void setAuction(Auction auction){
        this.auction = auction;
    }

    public User getBidder(){
        return bidder;
    }

    public void setBidder(User bidder){
        this.bidder = bidder;
    }

    public double getValue(){
        return value;
    }

    public void setValue(double value){
        this.value = value;
    }

    @Override
    public String toString(){
        return "Bid [auction = "+auction.toString()+"\n bidder = "+bidder.toString()+"\n value = "+ value +"]";
    }

}
