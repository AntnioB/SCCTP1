package scc.auction;

import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.Optional;

import com.azure.cosmos.models.CosmosItemResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import scc.cache.RedisCache;
import scc.user.CosmosDBLayer;
import scc.user.UserDAO;

@Path("/auction")
public class AuctionResource {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String createAuction(@CookieParam("scc:session") Cookie session, Auction auction)
            throws JsonProcessingException {

        try {
            RedisCache.checkCookieUser(session, auction.getOwnerId());
            ObjectMapper om = new ObjectMapper()
                    .registerModule(new JavaTimeModule());
            ObjectWriter ow = om.writer().withDefaultPrettyPrinter();
            Iterator<UserDAO> ite = CosmosDBLayer.getInstance().getUserById(auction.getOwnerId()).iterator();
            if (!ite.hasNext())
                throw new NotFoundException("User does not exist");
            if (auction.getEndTime().isBefore(ZonedDateTime.now()))
                return "Prohibited Time";
            CosmosItemResponse<AuctionDAO> res = CosmosDBAuctionLayer.getInstance().putAuction(new AuctionDAO(auction));
            int statusCode = res.getStatusCode();
            if (statusCode > 300)
                throw new WebApplicationException(statusCode);

            RedisCache.putAuction(auction.getId(), auction.toString());
            String json = ow.writeValueAsString(res.getItem().toAuction());
            return json;
        } catch (WebApplicationException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException(e);
        }
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String deleteAuction(@CookieParam("scc:session") Cookie session, @PathParam("id") String id,
            String ownerId) {
        try {
            RedisCache.checkCookieUser(session, ownerId);
            CosmosDBAuctionLayer db = CosmosDBAuctionLayer.getInstance();
            CosmosItemResponse<Object> res = db.delAuctionById(id);
            int resStatus = res.getStatusCode();
            if (resStatus > 300)
                throw new WebApplicationException(resStatus);
            RedisCache.deleteAuction(id);
            return String.valueOf(res.getStatusCode());
        } catch (WebApplicationException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException(e);
        }
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String updateAuction(@CookieParam("scc:session") Cookie session, Auction auction)
            throws JsonProcessingException {

        try {
            RedisCache.checkCookieUser(session, auction.getOwnerId());
            CosmosDBAuctionLayer db = CosmosDBAuctionLayer.getInstance();
            if (!auctionExists(auction.getId(), db))
                throw new WebApplicationException("Auction not found", 404);
            CosmosItemResponse<AuctionDAO> res = db.updateAuction(new AuctionDAO(auction));
            int statusCode = res.getStatusCode();
            if (statusCode > 300)
                throw new WebApplicationException(statusCode);
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String json = ow.writeValueAsString(res.getItem().toAuction());
            RedisCache.putAuction(auction.getId(), json);
            return json;
        } catch (WebApplicationException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException(e);
        }
    }

    @GET
    @Path("/list")
    @Produces(MediaType.TEXT_PLAIN)
    public String listAuctions() {
        CosmosDBAuctionLayer db = CosmosDBAuctionLayer.getInstance();
        StringBuilder res = new StringBuilder();
        Iterator<AuctionDAO> ite = db.getAuctions().iterator();
        while (ite.hasNext()) {
            res.append(ite.next().toString());
        }
        return res.toString();
    }

    private boolean auctionExists(String id, CosmosDBAuctionLayer db) {
        Optional<AuctionDAO> res = db.getAuctions().stream()
                .filter(auction -> auction.getId().equals(id)).findFirst();
        return res.isPresent();
    }


    //TODO just for testing purposes need to delete
    @DELETE
    @Path("/delete")
    @Produces(MediaType.TEXT_PLAIN)
    public String deleteAll() {
        CosmosDBAuctionLayer db = CosmosDBAuctionLayer.getInstance();
        Iterator<AuctionDAO> ite = db.getAuctions().iterator();
        while (ite.hasNext()) {
            db.delAuction(ite.next());
        }
        return "200";
    }

}
