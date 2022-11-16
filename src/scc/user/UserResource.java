package scc.user;

import java.util.Iterator;
import java.util.Optional;
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
        CosmosItemResponse<UserDAO> res = CosmosDBLayer.getInstance().putUser(tmp);
        int statusCode = res.getStatusCode();
        if (statusCode > 300)
            throw new WebApplicationException(statusCode);
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

        // TODO kinda trash
        User aux = res.getItem().toUser();
        aux.setPwd(user.getPwd());
        String json = ow.writeValueAsString(aux);
        RedisCache.putUser(user.getId(), json);
        return json;
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String deleteUser(@CookieParam("scc:session") Cookie session, @PathParam("id") String id) {
        try {

            RedisCache.checkCookieUser(session, id);

            CosmosDBLayer db = CosmosDBLayer.getInstance();
            CosmosItemResponse<Object> res = db.delUserById(id);
            int resStatus = res.getStatusCode();
            if (resStatus > 300)
                throw new WebApplicationException(resStatus);
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

            CosmosDBLayer db = CosmosDBLayer.getInstance();
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
        CosmosDBLayer db = CosmosDBLayer.getInstance();
        StringBuilder res = new StringBuilder();
        Iterator<UserDAO> ite = db.getUsers().iterator();
        while (ite.hasNext()) {
            res.append(ite.next().getId() + "\n\n");
        }
        return res.toString();
    }

    // TODO just for testing purposes need to delete
    @DELETE
    @Path("/delete")
    @Produces(MediaType.TEXT_PLAIN)
    public String deleteAll() {
        CosmosDBLayer db = CosmosDBLayer.getInstance();
        Iterator<UserDAO> ite = db.getUsers().iterator();
        while (ite.hasNext()) {
            db.delUser(ite.next());
        }
        return "200";
    }

    private boolean userExists(String id, CosmosDBLayer db) {
        if (RedisCache.userExists(id))
            return true;
        CosmosPagedIterable<UserDAO> res = db.getUserById(id);
        return res.iterator().hasNext();
    }

}
