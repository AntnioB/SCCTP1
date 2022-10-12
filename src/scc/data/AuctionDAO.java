package scc.data;

import java.util.Calendar;

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
    private User owner;
	private Calendar endTime;

    public AuctionDAO(){
    }
    public AuctionDAO(Auction a){
        this(a.getId(),a.getTitle(),a.getDescription(),a.getPhotoId(),a.getOwner(),a.getEndTime());
    }
    public AuctionDAO(String id, String title, String description, String photoId, User owner, Calendar endTime){
        super();
		this.id = id;
		this.title = title;
		this.description = description;
		this.photoId = photoId;
		this.owner = owner;
        this.endTime = endTime;
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

    public User getOwner(){
        return owner;
    }

    public Calendar getEndTime(){
        return endTime;
    }

    public Auction toAuction(){
        return new Auction(id, title, photoId, description, owner, endTime);
    }

    @Override
	public String toString() {
		return "UserDAO [_rid=" + _rid + ", _ts=" + _ts + ", id=" +  id + ", title=" + title + ", description=" + description + ", photoId=" + photoId + ", owner="
				+ owner.getId()+ ", endTime="+ endTime.getTime().toString()+"]";
	}
}
