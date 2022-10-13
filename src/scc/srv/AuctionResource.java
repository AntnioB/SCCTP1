package scc.srv;

import java.util.Calendar;
import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.WebApplicationException;

import com.azure.cosmos.models.CosmosItemResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers.CalendarDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
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
        AuctionDAO tmp = new AuctionDAO(auction);
        tmp.setId(UUID.randomUUID().toString());
        CosmosItemResponse<AuctionDAO> res = CosmosDBAuctionLayer.getInstance().putAuction(tmp);
        int statusCode = res.getStatusCode();
        if (statusCode > 300)
            throw new WebApplicationException(statusCode);
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Calendar.class, new CalendarDeserializer());
        mapper.registerModule(module);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();

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

    private boolean auctionExists(String id, CosmosDBAuctionLayer db) {
        Optional<AuctionDAO> res = db.getAuctions().stream()
                .filter(auction -> auction.getId().equals(id)).findFirst();
        return res.isPresent();
    }

}
