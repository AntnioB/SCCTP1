package scc.question;

import java.util.Iterator;

import com.azure.cosmos.models.CosmosItemResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

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
import scc.auction.AuctionDAO;
import scc.auction.CosmosDBAuctionLayer;
import scc.cache.RedisCache;

@Path("/auction/{auctionId}/question")
public class QuestionResource {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String createQuestion(@CookieParam("scc:session") Cookie session, Question question,
            @PathParam("auctionId") String auctionId) throws JsonProcessingException {


        try {
            RedisCache.checkCookieUser(session, question.getOwnerId());

            CosmosDBQuestionLayer db = CosmosDBQuestionLayer.getInstance();
            CosmosItemResponse<QuestionDAO> res = db.putQuestion(new QuestionDAO(question));
            int statusCode = res.getStatusCode();
            if (statusCode > 300)
                throw new WebApplicationException(statusCode);
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String json = ow.writeValueAsString(res.getItem().toQuestion());
            return json;
        } catch (WebApplicationException e) {
            throw e;
        } 
    }

    @POST
    @Path("/{questionId}/reply")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String createReply(@CookieParam("scc:session") Cookie session, Reply reply, @PathParam("auctionId") String auctionId,
            @PathParam("questionId") String questionId)
            throws JsonProcessingException {
        try {
            RedisCache.checkCookieUser(session, reply.getOwnerId());
            CosmosDBAuctionLayer dba = CosmosDBAuctionLayer.getInstance();
            Iterator<AuctionDAO> itea = dba.getAuctionById(auctionId).iterator();
            if(!itea.hasNext()){
                throw new NotFoundException("Auction does not exist");
            }
            AuctionDAO auction = itea.next();
            if(!auction.getOwnerId().equals(reply.getOwnerId())){
                throw new WebApplicationException("User and Auction Owner do not match",403);
            }
            CosmosDBQuestionLayer dbq = CosmosDBQuestionLayer.getInstance();
            Iterator<QuestionDAO> ite = dbq.getQuestionById(questionId).iterator();
            if (!ite.hasNext())
                throw new NotFoundException("Question does not exist");
            Question question = ite.next().toQuestion();
            question.setReply(reply.toString());
            CosmosItemResponse<QuestionDAO> res = dbq.updateQuestion(question);
            int statusCode = res.getStatusCode();
            if (statusCode > 300)
                throw new WebApplicationException(statusCode);
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String json = ow.writeValueAsString(res.getItem().toQuestion());
            return json;
        } catch (WebApplicationException e) {
            throw e;
        } 

    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String listQuestions(@PathParam("auctionId") String auctionId) throws JsonProcessingException {
        CosmosDBQuestionLayer db = CosmosDBQuestionLayer.getInstance();
        StringBuilder res = new StringBuilder();
        Iterator<QuestionDAO> ite = db.getQuestionsByAuctionId(auctionId).iterator();
        QuestionDAO next;
        while (ite.hasNext()) {
            next = ite.next();
            res.append(next.toQuestion().toString() + "\n");
        }
        return res.toString();
    }
}