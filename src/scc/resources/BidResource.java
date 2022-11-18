package scc.resources;

import java.util.Iterator;

import jakarta.ws.rs.WebApplicationException;

import com.azure.cosmos.models.CosmosItemResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import scc.cache.RedisCache;
import scc.cosmosDBLayers.AuctionLayer;
import scc.cosmosDBLayers.BidLayer;
import scc.cosmosDBLayers.UserLayer;
import scc.data.Bid;
import scc.data.database.AuctionDAO;
import scc.data.database.BidDAO;
import scc.utils.UniqueId;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.core.Cookie;

@Path("/auction/{id}/bid")
public class BidResource {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createBid(@CookieParam("scc:session") Cookie session, Bid bid, @PathParam("id") String auctionId)
            throws JsonProcessingException {

        String id;

        if (!RedisCache.userExists(bid.getBidderId())) {
            if (!UserLayer.getInstance().getUserById(bid.getBidderId()).iterator().hasNext())
                throw new NotFoundException("User does not exist");
        }

        try {
            NewCookie cookie = RedisCache.checkCookieUser(session, bid.getBidderId());

            double minBidAmount;
            BidLayer bidDB = BidLayer.getInstance();
            Iterator<BidDAO> highestBid = bidDB.getHighestBid(auctionId).iterator();
            if (highestBid.hasNext()){
                BidDAO next = highestBid.next();
                id = UniqueId.bidId(auctionId, Integer.parseInt(next.getId().split("-")[1]) + 1);
                minBidAmount = next.getAmount();
            }
            else {
                id = UniqueId.bidId(auctionId, 1);
                if (RedisCache.auctionExists(auctionId)) {
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.registerModule(new JavaTimeModule());
                    AuctionDAO auction = mapper.readValue(RedisCache.getAuction(auctionId), AuctionDAO.class);
                    minBidAmount = auction.getMinPrice();
                } else {
                    AuctionLayer auctionDB = AuctionLayer.getInstance();
                    if (auctionDB.getAuctionById(auctionId).iterator().hasNext())
                        minBidAmount = auctionDB.getAuctionById(auctionId).iterator().next().getMinPrice();
                    else
                        throw new NotFoundException("Auction does not exist");
                }
            }
            if (bid.getAmount() <= minBidAmount)
                throw new WebApplicationException(403);

            bid.setId(id);
            CosmosItemResponse<BidDAO> res = bidDB.putBid(new BidDAO(bid));
            int statusCode = res.getStatusCode();
            if (statusCode > 300) {
                throw new WebApplicationException(statusCode);
            }
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String json = ow.writeValueAsString(res.getItem().toBid());
            return Response.ok(json,MediaType.APPLICATION_JSON).cookie(cookie).build();
        } catch (WebApplicationException e) {
            throw e;
        }
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String listBids(@PathParam("id") String auctionId) {
        BidLayer db = BidLayer.getInstance();
        StringBuilder res = new StringBuilder();
        Iterator<BidDAO> ite = db.getBidByAuctionId(auctionId).iterator();
        BidDAO next;
        while (ite.hasNext()) {
            next = ite.next();
            res.append(next.toString() + "\n\n");
        }
        return res.toString();
    }

    // TODO just for testing purposes need to delete
    @DELETE
    @Path("/delete")
    @Produces(MediaType.TEXT_PLAIN)
    public String deleteAll() {
        BidLayer db = BidLayer.getInstance();
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
        BidLayer db = BidLayer.getInstance();
        Iterator<BidDAO> ite = db.getHighestBid(auctionId).iterator();
        while (ite.hasNext()) {
            return ite.next().toString();
        }
        return "";
    }
}
