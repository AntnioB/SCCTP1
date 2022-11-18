package scc.resources;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.azure.cosmos.models.CosmosItemResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import scc.cache.RedisCache;
import scc.cosmosDBLayers.AuctionLayer;
import scc.cosmosDBLayers.QuestionLayer;
import scc.data.Question;
import scc.data.Reply;
import scc.data.database.AuctionDAO;
import scc.data.database.QuestionDAO;

@Path("/auction/{auctionId}/question")
public class QuestionResource {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createQuestion(@CookieParam("scc:session") Cookie session, Question question,
            @PathParam("auctionId") String auctionId) throws JsonProcessingException {

        try {
            NewCookie cookie = RedisCache.checkCookieUser(session, question.getOwnerId());

            QuestionLayer db = QuestionLayer.getInstance();
            CosmosItemResponse<QuestionDAO> res = db.putQuestion(new QuestionDAO(question));
            int statusCode = res.getStatusCode();
            if (statusCode > 300)
                throw new WebApplicationException(statusCode);
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String json = ow.writeValueAsString(res.getItem().toQuestion());
            return Response.ok(json,MediaType.APPLICATION_JSON).cookie(cookie).build();
        } catch (WebApplicationException e) {
            throw e;
        }
    }

    @POST
    @Path("/{questionId}/reply")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createReply(@CookieParam("scc:session") Cookie session, Reply reply,
            @PathParam("auctionId") String auctionId,
            @PathParam("questionId") String questionId)
            throws JsonProcessingException {
        try {
            NewCookie cookie = RedisCache.checkCookieUser(session, reply.getOwnerId());
            AuctionDAO auction;
            if (RedisCache.auctionExists(auctionId)) {
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());
                auction = mapper.readValue(RedisCache.getAuction(auctionId), AuctionDAO.class);
            } else {
                AuctionLayer dba = AuctionLayer.getInstance();
                auction = dba.getAuctionById(auctionId).iterator().next();
            }
            if (!auction.getOwnerId().equals(reply.getOwnerId())) {
                throw new WebApplicationException("User and Auction Owner do not match", 403);
            }
            QuestionLayer dbq = QuestionLayer.getInstance();
            Iterator<QuestionDAO> ite = dbq.getQuestionById(questionId).iterator();
            if (!ite.hasNext())
                throw new NotFoundException("Question does not exist");
            QuestionDAO question = ite.next();
            question.setReply(reply.toString());
            CosmosItemResponse<QuestionDAO> res = dbq.updateQuestion(question);
            int statusCode = res.getStatusCode();
            if (statusCode > 300)
                throw new WebApplicationException(statusCode);
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String json = ow.writeValueAsString(res.getItem().toQuestion());
            return Response.ok(json,MediaType.APPLICATION_JSON).cookie(cookie).build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (NoSuchElementException e) {
            throw new NotFoundException("Auction does not exist");
        }

    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String listQuestions(@PathParam("auctionId") String auctionId) throws JsonProcessingException {
        QuestionLayer db = QuestionLayer.getInstance();
        StringBuilder res = new StringBuilder();
        Iterator<QuestionDAO> ite = db.getQuestionsByAuctionId(auctionId).iterator();
        QuestionDAO next;
        while (ite.hasNext()) {
            next = ite.next();
            res.append(next.toQuestion().toString() + "\n\n");
        }
        return res.toString();
    }
}