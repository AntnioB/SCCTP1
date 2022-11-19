package scc.resources;

import java.util.Iterator;
import java.util.UUID;

import jakarta.ws.rs.WebApplicationException;

import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.Response;
import scc.cache.RedisCache;
import scc.cosmosDBLayers.AuctionLayer;
import scc.cosmosDBLayers.BidLayer;
import scc.cosmosDBLayers.QuestionLayer;
import scc.cosmosDBLayers.UserLayer;
import scc.data.User;
import scc.data.database.AuctionDAO;
import scc.data.database.BidDAO;
import scc.data.database.QuestionDAO;
import scc.data.database.UserDAO;
import scc.srv.Login;
import scc.utils.Hash;
import scc.utils.UniqueId;

@Path("/user")
public class UserResource {

    @POST
    @Path("/auth")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response auth(Login login) throws JsonMappingException, JsonProcessingException {

        boolean pwdOk = login.authenticate();

        if (pwdOk == true) {
            String uid = UUID.randomUUID().toString();
            NewCookie cookie = new NewCookie.Builder("scc:session")
                    .value(uid)
                    .path("/")
                    .comment("sessionid")
                    .maxAge(300)
                    .secure(false)
                    .httpOnly(true)
                    .build();
            RedisCache.putCookie(uid, login.getId());
            return Response.ok().cookie(cookie).build();
        } else
            throw new NotAuthorizedException("Incorrect login");
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String createUser(User user) throws JsonProcessingException {
        user.setId(UniqueId.randomUUID(user.getId()));
        UserDAO tmp = new UserDAO(user);
        tmp.setPwd(Hash.of(user.getPwd()));
        CosmosItemResponse<UserDAO> res = UserLayer.getInstance().putUser(tmp);
        int statusCode = res.getStatusCode();
        if (statusCode > 300)
            throw new WebApplicationException(statusCode);
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

        // TODO Imprimir json om pass hashed
        User aux = res.getItem().toUser();
        String jsonPrime = ow.writeValueAsString(aux);
        aux.setPwd(user.getPwd());
        String json = ow.writeValueAsString(aux);
        RedisCache.putUser(user.getId(), jsonPrime);
        return json;
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String deleteUser(@CookieParam("scc:session") Cookie session, @PathParam("id") String id) {
        try {

            RedisCache.checkCookieUser(session, id);

            UserLayer db = UserLayer.getInstance();
            CosmosItemResponse<Object> res = db.delUserById(id);
            int resStatus = res.getStatusCode();
            if (resStatus > 300)
                throw new WebApplicationException(resStatus);
            AuctionLayer dbauctions = AuctionLayer.getInstance();
            CosmosPagedIterable<AuctionDAO> auctions = dbauctions.getAuctionByOwnerId(id);
            for (AuctionDAO auctionDAO : auctions) {
                auctionDAO.setOwnerId("Deleted User");
                dbauctions.updateAuction(auctionDAO);
            }
            BidLayer dbbids = BidLayer.getInstance();
            CosmosPagedIterable<BidDAO> bids = dbbids.getBidByBidderId(id);
            for (BidDAO bidDAO : bids) {
                bidDAO.setBidderId("Deleted User");
                dbbids.updateBid(bidDAO);
            }
            QuestionLayer dbquestions = QuestionLayer.getInstance();
            CosmosPagedIterable<QuestionDAO> questions = dbquestions.getQuestionsByOwnerId(id);
            for (QuestionDAO questionDAO : questions) {
                questionDAO.setOwnerID("Deleted User");
                dbquestions.updateQuestion(questionDAO);
            }
            RedisCache.deleteUser(id);
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
    public String updateUser(@CookieParam("scc:session") Cookie session, User user) throws JsonProcessingException {

        try {
            RedisCache.checkCookieUser(session, user.getId());

            UserLayer db = UserLayer.getInstance();
            if (!userExists(user.getId(), db))
                throw new WebApplicationException(409);
            CosmosItemResponse<UserDAO> res = db.updateUser(new UserDAO(user));
            int statusCode = res.getStatusCode();
            if (statusCode > 300)
                throw new WebApplicationException(statusCode);
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String json = ow.writeValueAsString(res.getItem().toUser());
            RedisCache.putUser(user.getId(), json);
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
    public String listUsers() {
        UserLayer db = UserLayer.getInstance();
        StringBuilder res = new StringBuilder();
        Iterator<UserDAO> ite = db.getUsers().iterator();
        while (ite.hasNext()) {
            res.append(ite.next().getId() + "\n\n");
        }
        return res.toString();
    }
    
    @GET
    @Path("/{id}/auctions")
    @Produces(MediaType.TEXT_PLAIN)
    public String getUserAuctions(@PathParam("id") String id){
        //did not see the need to for a user to be authenticated to make this request

        AuctionLayer db = AuctionLayer.getInstance();
        Iterator<AuctionDAO> ite = db.getAuctionByOwnerId(id).iterator();
        StringBuilder res = new StringBuilder();
        while(ite.hasNext()){
            res.append(ite.next().toAuction() + "\n\n");
        }
        return res.toString();
    }

    // TODO just for testing purposes need to delete
    @DELETE
    @Path("/delete")
    @Produces(MediaType.TEXT_PLAIN)
    public String deleteAll() {
        UserLayer db = UserLayer.getInstance();
        Iterator<UserDAO> ite = db.getUsers().iterator();
        while (ite.hasNext()) {
            db.delUser(ite.next());
        }
        return "200";
    }


    private boolean userExists(String id, UserLayer db) throws JsonProcessingException {
        if (RedisCache.userExists(id))
            return true;
        CosmosPagedIterable<UserDAO> res = db.getUserById(id);
        UserDAO user = res.iterator().next();
        if (user != null) {
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String json = ow.writeValueAsString(user.toUser());
            RedisCache.putAuction(id, json);
        }
        return user != null;
    }

    

}
