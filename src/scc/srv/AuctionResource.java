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
import scc.data.CosmosDBLayer;
import scc.data.User;
import scc.data.UserDAO;
import scc.utils.Hash;

@Path("/auction")
public class AuctionResource {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String createAuction(User user) throws JsonProcessingException {
        /**
         * Iterator<UserDAO> ite =
         * CosmosDBLayer.getInstance().getUserById(user.getId()).iterator();
         * while(ite.hasNext()){
         * if(ite.next().getId().equals(user.getId()))
         * throw new WebApplicationException(403);
         * }
         */
        UserDAO tmp = new UserDAO(user);
        tmp.setId(UUID.randomUUID().toString());
        tmp.setPwd(Hash.of(user.getPwd()));
        CosmosItemResponse<UserDAO> res = CosmosDBLayer.getInstance().putUser(tmp);
        int statusCode = res.getStatusCode();
        if (statusCode > 300)
            throw new WebApplicationException(statusCode);
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(res.getItem().toUser());
        return json;
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String deleteAuction(@PathParam("id") String id) {
        CosmosDBLayer db = CosmosDBLayer.getInstance();
        CosmosItemResponse<Object> res = db.delUserById(id);
        int resStatus = res.getStatusCode();
        if (resStatus > 300)
            throw new WebApplicationException(resStatus);
        return String.valueOf(res.getStatusCode());
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String updateAuction(User user) throws JsonProcessingException {
        CosmosDBLayer db = CosmosDBLayer.getInstance();
        if (!userExists(user.getId(), db))
            throw new WebApplicationException(409);
        CosmosItemResponse<UserDAO> res = db.updateUser(new UserDAO(user));
        int statusCode = res.getStatusCode();
        if (statusCode > 300)
            throw new WebApplicationException(statusCode);
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(res.getItem().toUser());
        return json;
    }

    @GET
    @Path("/list")
    @Produces(MediaType.TEXT_PLAIN)
    public String listUsers() {
        CosmosDBLayer db = CosmosDBLayer.getInstance();
        StringBuilder res = new StringBuilder();
        Iterator<UserDAO> ite = db.getUsers().iterator();
        while (ite.hasNext()) {
            res.append(ite.next().getId() + "\n");
        }
        return res.toString();
    }

    private boolean userExists(String id, CosmosDBLayer db) {
        Optional<UserDAO> res = db.getUsers().stream()
                .filter(user -> user.getId().equals(id)).findFirst();
        return res.isPresent();
    }

}
