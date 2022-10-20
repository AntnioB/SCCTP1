package scc.srv;

import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.WebApplicationException;

import com.azure.cosmos.models.CosmosItemResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import scc.data.Bid;
import scc.data.BidDAO;
import scc.data.CosmosDBBidLayer;

@Path("/auction/{id}")
public class BidResource {
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String createBid(Bid bid, @PathParam("id") String id) throws JsonProcessingException{

        CosmosItemResponse<BidDAO> res = CosmosDBBidLayer.getInstance().putBid(new BidDAO(bid));
        int statusCode = res.getStatusCode();

        //TODO checkar se bid maior que ultimo bid

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
            if(next.getAuction().getId().equals(id))
                res.append(ite.toString() + "\n");
        }
        return res.toString();
    }
}
