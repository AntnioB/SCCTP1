package scc.srv;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.WebApplicationException;

import com.azure.cosmos.models.CosmosItemResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import scc.data.Auction;
import scc.data.AuctionDAO;
import scc.data.CosmosDBAuctionLayer;

@Path("/auction")
public class AuctionResource {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String createAuction(Auction auction) throws JsonProcessingException {
        /**
         * Iterator<UserDAO> ite =
         * CosmosDBLayer.getInstance().getUserById(user.getId()).iterator();
         * while(ite.hasNext()){
         * if(ite.next().getId().equals(user.getId()))
         * throw new WebApplicationException(403);
         * }
         */

        ObjectMapper om = new ObjectMapper()
                .registerModule(new JavaTimeModule());
        ObjectWriter ow = om.writer().withDefaultPrettyPrinter();
        auction.setId(UUID.randomUUID().toString());
        if(auction.getEndTime().isBefore(ZonedDateTime.now()))
            return "Prohibited Time";
        CosmosItemResponse<AuctionDAO> res = CosmosDBAuctionLayer.getInstance().putAuction(new AuctionDAO(auction));
        int statusCode = res.getStatusCode();
        if (statusCode > 300)
            throw new WebApplicationException(statusCode);

        String json = ow.writeValueAsString(res.getItem().toAuction());
        return json;
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String deleteAuction(@PathParam("id") String id) {
        CosmosDBAuctionLayer db = CosmosDBAuctionLayer.getInstance();
        CosmosItemResponse<Object> res = db.delAuctionById(id);
        int resStatus = res.getStatusCode();
        if (resStatus > 300)
            throw new WebApplicationException(resStatus);
        return String.valueOf(res.getStatusCode());
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String updateAuction(Auction auction) throws JsonProcessingException {
        CosmosDBAuctionLayer db = CosmosDBAuctionLayer.getInstance();
        if (!auctionExists(auction.getId(), db))
            throw new WebApplicationException(409);
        CosmosItemResponse<AuctionDAO> res = db.updateAuction(new AuctionDAO(auction));
        int statusCode = res.getStatusCode();
        if (statusCode > 300)
            throw new WebApplicationException(statusCode);
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(res.getItem().toAuction());
        return json;
    }

    @GET
    @Path("/list")
    @Produces(MediaType.TEXT_PLAIN)
    public String listAuctions() {
        CosmosDBAuctionLayer db = CosmosDBAuctionLayer.getInstance();
        StringBuilder res = new StringBuilder();
        Iterator<AuctionDAO> ite = db.getAuctions().iterator();
        while (ite.hasNext()) {
            res.append(ite.next().getEndTime().format(DateTimeFormatter.ofPattern("MM/dd/yyyy - HH:mm:ss Z")) + "\n");
        }
        return res.toString();
    }

    private boolean auctionExists(String id, CosmosDBAuctionLayer db) {
        Optional<AuctionDAO> res = db.getAuctions().stream()
                .filter(auction -> auction.getId().equals(id)).findFirst();
        return res.isPresent();
    }

}
