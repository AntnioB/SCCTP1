package scc.auction;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.Iterator;

import org.glassfish.jersey.client.ClientConfig;

import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import scc.cache.RedisCache;
import scc.srv.MainApplication;
import scc.user.CosmosDBLayer;
import scc.user.UserDAO;

@Path("/auction")
public class AuctionResource {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createAuction(@CookieParam("scc:session") Cookie session, Auction auction)
            throws JsonProcessingException {

        try {
            NewCookie cookie = RedisCache.checkCookieUser(session, auction.getOwnerId());
            ObjectMapper om = new ObjectMapper()
                    .registerModule(new JavaTimeModule());
            ObjectWriter ow = om.writer().withDefaultPrettyPrinter();
            if (!RedisCache.userExists(auction.getOwnerId())) {
                if (!CosmosDBLayer.getInstance().getUserById(auction.getOwnerId()).iterator().hasNext())
                    throw new WebApplicationException("User does not exist", 404);
            }
            if (auction.getEndTime().isBefore(ZonedDateTime.now()))
                throw new WebApplicationException("Prohibited Time", 403);
            CosmosItemResponse<AuctionDAO> res = CosmosDBAuctionLayer.getInstance().putAuction(new AuctionDAO(auction));
            int statusCode = res.getStatusCode();
            if (statusCode > 300)
                throw new WebApplicationException(statusCode);

            String json = ow.writeValueAsString(res.getItem().toAuction());
            RedisCache.putAuction(auction.getId(), json);
            return Response.ok(json, MediaType.APPLICATION_JSON).cookie(cookie).build();
        } catch (WebApplicationException e) {
            throw e;
        }
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteAuction(@CookieParam("scc:session") Cookie session, @PathParam("id") String id,
            String ownerId) {
        try {
            NewCookie cookie = RedisCache.checkCookieUser(session, ownerId);
            CosmosDBAuctionLayer db = CosmosDBAuctionLayer.getInstance();
            CosmosItemResponse<Object> res = db.delAuctionById(id);
            int resStatus = res.getStatusCode();
            if (resStatus > 300)
                throw new WebApplicationException(resStatus);
            RedisCache.deleteAuction(id);
            return Response.ok(String.valueOf(res.getStatusCode()), MediaType.APPLICATION_JSON).cookie(cookie).build();
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
    public Response updateAuction(@CookieParam("scc:session") Cookie session, Auction auction)
            throws JsonProcessingException {

        try {
            NewCookie cookie = RedisCache.checkCookieUser(session, auction.getOwnerId());
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
            return Response.ok(json, MediaType.APPLICATION_JSON).cookie(cookie).build();
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
            res.append(ite.next().toAuction().toString() + "\n\n");
        }
        return res.toString();
    }

    // TODO just for testing purposes need to delete
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

    private boolean auctionExists(String id, CosmosDBAuctionLayer db) {
        if (RedisCache.auctionExists(id))
            return true;
        CosmosPagedIterable<AuctionDAO> res = db.getAuctionById(id);
        return res.iterator().hasNext();
    }

    @GET
    @Path("/search")
    @Produces(MediaType.TEXT_PLAIN)
    public String search() {

        String hostname = "https://" + MainApplication.PROP_SERVICE_NAME + ".search.windows.net/";
        ClientConfig config = new ClientConfig();
        Client client = ClientBuilder.newClient(config);

        URI baseURI = UriBuilder.fromUri(hostname).build();

        WebTarget target = client.target(baseURI);

        String index = "cosmosdb-index";
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode obj = mapper.createObjectNode();
        obj.put("count", "true");
        obj.put("search", "test");

        String resultStr = target.path("indexes/" + index + "/docs").queryParam("api-version", "2020-06-30")
                .queryParam("search", "test")
                .request().header("api-key", MainApplication.PROP_QUERY_KEY)
                .accept(MediaType.APPLICATION_JSON).post(Entity.entity(obj.toString(), MediaType.APPLICATION_JSON))
                .readEntity(String.class);

        return resultStr;
    }
}
