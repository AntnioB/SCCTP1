package scc.auction;

import java.time.ZonedDateTime;

import scc.utils.Status;

/**
 * Represents an Auction, as stored in the Database
 */
public class AuctionDAO {
    private String _rid;
	private String _ts;
	private String id;
	private String title;
	private String description;
	private String photoId;
    private String ownerId;
	private ZonedDateTime endTime;
	private double minPrice;
	private String winnerBidId;
	private Status status;

    public AuctionDAO(){
    }
    public AuctionDAO(Auction a){
        this(a.getId(),a.getTitle(),a.getDescription(),a.getPhotoId(),a.getOwnerId(),a.getEndTime(), a.getMinPrice(),a.getWinnerBidId(), a.getStatus());
    }
    public AuctionDAO(String id, String title, String description, String photoId, String ownerId, ZonedDateTime endTime, double minPrice, String winnerBidId, Status status){
        super();
		this.id = id;
		this.title = title;
		this.description = description;
		this.photoId = photoId;
		this.ownerId = ownerId;
        this.endTime = endTime;
		this.minPrice = minPrice;
		this.winnerBidId = winnerBidId;
		this.status = status;
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

    public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getPhotoId() {
		return photoId;
	}
	public void setPhotoId(String photoId) {
		this.photoId = photoId;
	}

    public String getOwnerId(){
        return ownerId;
    }

	public void setOwnerId(String ownerId){
		this.ownerId = ownerId;
	}

    public ZonedDateTime getEndTime(){
        return endTime;
    }

	public Status getStatus(){
		return status;
	}

	public void setStatus(Status status){
		this.status = status;
	}

	public String getWinnerBidId(){
		return winnerBidId;
	}

	public void setWinnerBidId(String winnerBidId){
		this.winnerBidId = winnerBidId;
	}

	public double getMinPrice(){
		return minPrice;
	}

	public void setMinPrice(double minPrice){
		this.minPrice = minPrice;
	}

    public Auction toAuction(){
        return new Auction( id,  title, photoId,  description,  ownerId, endTime, minPrice, status,  winnerBidId);
    }

    @Override
	public String toString() {
		return "AuctionDAO [_rid=" + _rid + ", _ts=" + _ts + ", id=" +  id + ", title=" + title + ", description=" + description + ", photoId=" + photoId + ", owner="
				+ ownerId+ ", endTime="+ endTime.toString()+ ", minPrice=" + minPrice + ", status= "+status.toString()+" winnerBid="+winnerBidId+"]";
	}
}
