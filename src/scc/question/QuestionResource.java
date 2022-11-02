package scc.question;

import java.util.Iterator;
import java.util.UUID;

import com.azure.cosmos.models.CosmosItemResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import scc.user.CosmosDBLayer;
import scc.user.UserDAO;

@Path("/auction/{id}/question")
public class QuestionResource {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String createQuestion(Question question, @PathParam("id") String id) throws JsonProcessingException {
        question.setId(UUID.randomUUID().toString());
        CosmosDBQuestionLayer db = CosmosDBQuestionLayer.getInstance();
        CosmosItemResponse<QuestionDAO> res = db.putQuestion(new QuestionDAO(question));
        int statusCode = res.getStatusCode();
        if (statusCode > 300)
            throw new WebApplicationException(statusCode);
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(res.getItem().toQuestion());
        return json;
    }

    @POST
    @Path("/{questionId}/reply")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String createReply(Reply reply, @PathParam("id") String id, @PathParam("questionId") String questionId)
            throws JsonProcessingException {
        CosmosDBQuestionLayer db = CosmosDBQuestionLayer.getInstance();
        Iterator<QuestionDAO> ite = db.getQuestionById(questionId).iterator();
        if (!ite.hasNext())
            throw new NotFoundException("Question does not exist");
        Question question = ite.next().toQuestion();
        question.addReply(reply);
        CosmosItemResponse<QuestionDAO> res = db.updateQuestion(question);
        int statusCode = res.getStatusCode();
        if (statusCode > 300)
            throw new WebApplicationException(statusCode);
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(res.getItem().toQuestion());
        return json;
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String listQuestions(@PathParam("id") String id) throws JsonProcessingException {
        CosmosDBQuestionLayer db = CosmosDBQuestionLayer.getInstance();
        StringBuilder res = new StringBuilder();
        Iterator<QuestionDAO> ite = db.getQuestionsByAuctionId(id).iterator();
        QuestionDAO next;
        while (ite.hasNext()) {
            next = ite.next();
            res.append(next.toQuestion().toString() + "\n");
        }
        return res.toString();
    }
}