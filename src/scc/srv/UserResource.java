package scc.srv;

import java.util.Iterator;

import javax.ws.rs.WebApplicationException;

import com.azure.cosmos.models.CosmosItemResponse;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import scc.data.CosmosDBLayer;
import scc.data.User;
import scc.data.UserDAO;

@Path("/user")
public class UserResource {

    @POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String createUser(User user) {
        if(CosmosDBLayer.getInstance().getUsers().stream().anyMatch(user2 -> user2.getId().equals(user.getId())))
            throw new WebApplicationException(418);
        CosmosItemResponse<UserDAO> res = CosmosDBLayer.getInstance().putUser(new UserDAO(user));
        int statusCode = res.getStatusCode();
		if(statusCode>300)
            throw new WebApplicationException(statusCode);
        return res.getItem().toString();
	}

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String deleteUser(@PathParam("id") String id){
        CosmosDBLayer db = CosmosDBLayer.getInstance();
        CosmosItemResponse<Object> res = db.delUserById(id);
        int resStatus = res.getStatusCode();
        if(resStatus>300)
            throw new WebApplicationException(resStatus);
        return String.valueOf(res.getStatusCode());
    }

    @PUT
    @Path("/{id}")
    public String updateUser(){
        return null;
    }

    @GET
    @Path("/list")
    @Produces(MediaType.TEXT_PLAIN)
    public String listUsers(){
        CosmosDBLayer db = CosmosDBLayer.getInstance();
        StringBuilder res = new StringBuilder();
        Iterator<UserDAO> ite = db.getUsers().iterator();
        while(ite.hasNext()){
            res.append(ite.next().getId()+"\n");
        }
        return res.toString();
    }

}
