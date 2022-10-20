package scc.data;

public class BidDAO {
    private String _rid;
	private String _ts;
    private Auction auction;
    private User bidder;
    private double value;

    public BidDAO(){}
    public BidDAO(Bid b){
        this(b.getAuction(), b.getBidder(), b.getValue());
    }
    public BidDAO(Auction auction, User bidder, double value){
        super();
        this.auction = auction;
        this.bidder = bidder;
        this.value = value;
    }

    public String get_rid() {
		return _rid;
	}
	public void set_rid(String _rid) {
		this._rid = _rid;
	}
	public String get_ts() {
		return _ts;
	}
	public void set_ts(String _ts) {
		this._ts = _ts;
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

    public Bid toBid(){
        return new Bid(auction, bidder, value);
    }

    @Override
    public String toString(){
        return "Bid [auction = "+auction.toString()+"\n bidder = "+bidder.toString()+"\n value = "+ value +"]";
    }
}
