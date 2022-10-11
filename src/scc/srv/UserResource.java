package scc.srv;

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
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String createUser(User user) {
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
        return res.getItem().toString();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
    public String updateUser(){
        CosmosItemResponse<UserDAO> res = CosmosDBLayer.getInstance().;
        int statusCode = res.getStatusCode();
        return null;
    }

}
