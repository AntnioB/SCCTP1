package scc.resources;

import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.Map;


import com.azure.core.credential.AzureKeyCredential;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.azure.search.documents.SearchClient;
import com.azure.search.documents.SearchClientBuilder;
import com.azure.search.documents.SearchDocument;
import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.util.SearchPagedIterable;
import com.azure.search.documents.util.SearchPagedResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import scc.cache.RedisCache;
import scc.cosmosDBLayers.AuctionLayer;
import scc.cosmosDBLayers.UserLayer;
import scc.data.Auction;
import scc.data.database.AuctionDAO;
import scc.srv.MainApplication;

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
                if (!UserLayer.getInstance().getUserById(auction.getOwnerId()).iterator().hasNext())
                    throw new WebApplicationException("User does not exist", 404);
            }
            if (auction.getEndTime().isBefore(ZonedDateTime.now()))
                throw new WebApplicationException("Prohibited Time", 403);
            CosmosItemResponse<AuctionDAO> res = AuctionLayer.getInstance().putAuction(new AuctionDAO(auction));
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
            AuctionLayer db = AuctionLayer.getInstance();
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
            AuctionLayer db = AuctionLayer.getInstance();
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
        AuctionLayer db = AuctionLayer.getInstance();
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
        AuctionLayer db = AuctionLayer.getInstance();
        Iterator<AuctionDAO> ite = db.getAuctions().iterator();
        while (ite.hasNext()) {
            db.delAuction(ite.next());
        }
        return "200";
    }

    private boolean auctionExists(String id, AuctionLayer db) {
        if (RedisCache.auctionExists(id))
            return true;
        CosmosPagedIterable<AuctionDAO> res = db.getAuctionById(id);
        return res.iterator().hasNext();
    }

    @GET
    @Path("/search")
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchRest(@DefaultValue("*") @QueryParam("query") String query) {

        SearchClient searchClient = new SearchClientBuilder()
                .credential(new AzureKeyCredential(MainApplication.PROP_QUERY_KEY))
                .endpoint("https://scc23cs58152.search.windows.net")
                .indexName("cosmosdb-index")
                .buildClient();

        SearchOptions options = new SearchOptions()
                .setIncludeTotalCount(true)
                .setSelect("id", "title", "description", "ownerId", "status", "photoId", "minPrice")
                .setSearchFields("title", "description")
                .setTop(5);

        SearchPagedIterable searchPagedIterable = searchClient.search(query, options, null);

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

        StringBuilder json = new StringBuilder();
        for (SearchPagedResponse resultResponse : searchPagedIterable.iterableByPage()) {
            resultResponse.getValue().forEach((searchResult) -> {
                
                for (Map.Entry<String, Object> res : searchResult
                        .getDocument(SearchDocument.class)
                        .entrySet()) {
                    try {
                        json.append(ow.writeValueAsString(res.getKey() +" -> "+res.getValue()));
                        json.append("\n");
                        
                    } catch (JsonProcessingException e) {
                        throw new WebApplicationException(418);
                    }

                }
                json.append("\n");
            });
        }

        return Response.ok(json.toString()).build();
    }

}
