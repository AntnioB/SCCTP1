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
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import scc.auction.CosmosDBAuctionLayer;
import scc.cache.RedisCache;
import scc.user.CosmosDBLayer;
import scc.user.UserDAO;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.core.Cookie;

@Path("/auction/{id}/bid")
public class BidResource {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String createBid(@CookieParam("scc:session") Cookie session, Bid bid, @PathParam("id") String auctionId)
            throws JsonProcessingException {
           
        bid.setId(UUID.randomUUID().toString());
       
        Iterator<UserDAO> ite = CosmosDBLayer.getInstance().getUserById(bid.getBidderId()).iterator();
       
        
        
        if (!ite.hasNext())
            throw new NotFoundException("User does not exist");

        try {
            RedisCache.checkCookieUser(session, bid.getBidderId());

            double minBidAmount;
            CosmosDBBidLayer bidDB = CosmosDBBidLayer.getInstance();
            Iterator<BidDAO> highestBid = bidDB.getHighestBid(auctionId).iterator();
            if(highestBid.hasNext())
                minBidAmount = highestBid.next().getAmount();
            else {
                CosmosDBAuctionLayer auctionDB = CosmosDBAuctionLayer.getInstance();
                minBidAmount = auctionDB.getAuctionById(auctionId).iterator().next().getMinPrice();
            }
            if(bid.getAmount() <= minBidAmount)
                throw new WebApplicationException(403);
            
            //here
            CosmosItemResponse<BidDAO> res = bidDB.putBid(new BidDAO(bid));
            int statusCode = res.getStatusCode();
            //here
            if (statusCode > 300){
                //here
                throw new WebApplicationException(statusCode);
            }
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            //here
            String json = ow.writeValueAsString(res.getItem().toBid());
            return json;
        } catch (WebApplicationException e) {
            throw e;
        } 
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String listBids(@PathParam("id") String auctionId) {
        CosmosDBBidLayer db = CosmosDBBidLayer.getInstance();
        StringBuilder res = new StringBuilder();
        Iterator<BidDAO> ite = db.getBids().iterator();
        BidDAO next;
        while (ite.hasNext()) {
            next = ite.next();
            if (next.getAuctionId().equals(auctionId))
                res.append(next.toString() + "\n");
        }
        return res.toString();
    }

    //TODO just for testing purposes need to delete
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
    public String getHighestBid(@PathParam("id") String auctionId) {
        CosmosDBBidLayer db = CosmosDBBidLayer.getInstance();
        Iterator<BidDAO> ite = db.getHighestBid(auctionId).iterator();
        while (ite.hasNext()) {
            return ite.next().toString();
        }
        return "";
    }
}
