package scc.auction;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import scc.utils.Status;
import scc.utils.UniqueId;
import scc.utils.ZonedDateTimeDeserializer;

/**
 * Represents an Auction, as returned to the clients
 */
public class Auction {
    private String id, title, description, photoId;
    private String ownerId;
	@JsonDeserialize(using = ZonedDateTimeDeserializer.class)
    private ZonedDateTime endTime;
	private double minPrice;
	private Status status;
	private String winnerBidId;

    public Auction(@JsonProperty("id") String id, @JsonProperty("title")String title,@JsonProperty("photoId") String photoId,
     @JsonProperty("description") String description, @JsonProperty("ownerId") String ownerId,
     @JsonProperty("endTime") ZonedDateTime endTime, @JsonProperty("minPrice") double minPrice){
		this.id = UniqueId.randomUUID(id);
		this.title = title;
		this.description = description;
		this.photoId = photoId;
		this.ownerId = ownerId;
        this.endTime = endTime;
		this.minPrice = minPrice;
		if(endTime.isAfter(ZonedDateTime.now()))
			status = Status.OPEN;
		else status = Status.CLOSED;
		winnerBidId = null;
    }

	public Auction(String id, String title,String photoId, String description, String ownerId,ZonedDateTime endTime, double minPrice, Status status, String winnerBidId){
		this.id = id;
		this.title = title;
		this.description = description;
		this.photoId = photoId;
		this.ownerId = ownerId;
        this.endTime = endTime;
		this.minPrice = minPrice;
		this.status = status;
		this.winnerBidId = winnerBidId;
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
	
	@Override
	public String toString() {
		return "Auction [id=" + id + ", title=" + title + ", description=" + description + ", photoId=" + photoId + ", owner="
		+ ownerId+ ", endTime="+ endTime.toString()+ ", minPrice=" + minPrice + ", status= "+status.toString()+ " winnerBid="+winnerBidId+"]";
	}
}
