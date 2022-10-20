package scc.data;

public class BidDAO {
    private String _rid;
	private String _ts;
    private String auctionId;
    private String bidderId;
    private double value;
    private String id;

    public BidDAO(){}
    public BidDAO(Bid b){
        this(b.getId(),b.getAuctionId(), b.getBidderId(), b.getValue());
    }
    public BidDAO(String id,String auctionId, String bidderId, double value){
        super();
        this.id = id;
        this.auctionId = auctionId;
        this.bidderId = bidderId;
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
    public double getValue(){
        return value;
    }

    public void setValue(double value){
        this.value = value;
    }

    public Bid toBid(){
        return new Bid(id, auctionId, bidderId, value);
    }

    @Override
    public String toString(){
        return "Bid [auction = "+auctionId+"\n bidder = "+bidderId+"\n value = "+ value +"]";
    }
}
