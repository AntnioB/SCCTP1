package scc.bid;

import java.util.Iterator;
import java.util.UUID;

import jakarta.ws.rs.WebApplicationException;

import com.azure.cosmos.models.CosmosItemResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import scc.user.CosmosDBLayer;
import scc.user.UserDAO;

@Path("/auction/{id}/bid")
public class BidResource {
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String createBid(Bid bid, @PathParam("id") String id) throws JsonProcessingException{
        bid.setId(UUID.randomUUID().toString());

        Iterator<UserDAO> ite = CosmosDBLayer.getInstance().getUserById(bid.getBidderId()).iterator();
        if(!ite.hasNext())
            throw new NotFoundException("User does not exist");

        CosmosDBBidLayer db = CosmosDBBidLayer.getInstance();
        BidDAO highestBid = db.getHighestBid(id).iterator().next();
        if(highestBid != null && bid.getAmount() <= highestBid.getAmount())
            throw new WebApplicationException(403);
        CosmosItemResponse<BidDAO> res = db.putBid(new BidDAO(bid));
        int statusCode = res.getStatusCode();
        if (statusCode > 300)
            throw new WebApplicationException(statusCode);

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(res.getItem().toBid());
        return json; 
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String listBids(@PathParam("id") String id){
        CosmosDBBidLayer db = CosmosDBBidLayer.getInstance();
        StringBuilder res = new StringBuilder();
        Iterator<BidDAO> ite = db.getBids().iterator();
        BidDAO next;
        while (ite.hasNext()) {
            next=ite.next();
            if(next.getAuctionId().equals(id))
                res.append(next.toString()+ "\n");
        }
        return res.toString();
    }

    @DELETE
    @Path("/delete")
    @Produces(MediaType.TEXT_PLAIN)
    public String deleteAll() {
        CosmosDBBidLayer db = CosmosDBBidLayer.getInstance();
        Iterator<BidDAO> ite = db.getBids().iterator();
        while (ite.hasNext()) {
            db.delBid(ite.next());
        }
        return "200";
    }

    @GET
    @Path("/highest")
    @Produces(MediaType.TEXT_PLAIN)
    public String getHighestBid(@PathParam("id") String id){
        CosmosDBBidLayer db = CosmosDBBidLayer.getInstance();
        Iterator<BidDAO> ite = db.getHighestBid(id).iterator();
        while (ite.hasNext()) {
            return ite.next().toString();
        }
        return "";
    }
}
