package scc.data;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import scc.utils.Status;
import scc.utils.ZonedDateTimeDeserializer;

/**
 * Represents an Auction, as returned to the clients
 */
public class Auction {
    private String id, title, description, photoId;
    private User owner;
	@JsonDeserialize(using = ZonedDateTimeDeserializer.class)
    private ZonedDateTime endTime;
	private double minPrice;
	private Status status;
	private Bid winnerBid;

    public Auction(@JsonProperty("id") String id, @JsonProperty("title")String title,@JsonProperty("photoId") String photoId,
     @JsonProperty("description") String description, @JsonProperty("owner") User owner,
     @JsonProperty("endTime") ZonedDateTime endTime, @JsonProperty("minPrice") double minPrice){
        super();
		this.id = id;
		this.title = title;
		this.description = description;
		this.photoId = photoId;
		this.owner = owner;
        this.endTime = endTime;
		this.minPrice = minPrice;
		if(endTime.isAfter(ZonedDateTime.now()))
			status = Status.OPEN;
		else status = Status.CLOSED;
		winnerBid = null;
		
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

    public ZonedDateTime getEndTime(){
        return endTime;
    }

	public Status getStatus(){
		return status;
	}

	public void setStatus(Status status){
		this.status = status;
	}

	public double getMinPrice(){
		return minPrice;
	}

	public Bid getWinnerBid(){
		return winnerBid;
	}
	
	@Override
	public String toString() {
		return "Auction [id=" + id + ", title=" + title + ", description=" + description + ", photoId=" + photoId + ", owner="
				+ owner.getId()+ ", endTime="+ endTime.toString()+"]";
	}
}
