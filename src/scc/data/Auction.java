package scc.data;

import java.util.Calendar;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents an Auction, as returned to the clients
 */
public class Auction {
    private String id, title, description, photoId;
    private User owner;
    private Calendar endTime;

    public Auction(@JsonProperty("id") String id, @JsonProperty("title")String title,@JsonProperty("photoId") String photoId,
     @JsonProperty("description") String description, @JsonProperty("owner") User owner,
     @JsonProperty("endTime") Calendar endTime){
        super();
		this.id = id;
		this.title = title;
		this.description = description;
		this.photoId = photoId;
		this.owner = owner;
        this.endTime = endTime;
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
	
	@Override
	public String toString() {
		return "Auction [id=" + id + ", title=" + title + ", description=" + description + ", photoId=" + photoId + ", owner="
				+ owner.getId()+ ", endTime="+ endTime.getTime().toString()+"]";
	}
}
