package scc.user;

import java.util.Iterator;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.ws.rs.NotFoundException;
import scc.utils.Hash;

public class Login {
    
    private String id, password;
    public Login(@JsonProperty("id") String id, @JsonProperty("password") String password){
        this.id=id;
        this.password=password;
    }

    public String getId(){
        return id;
    }

    public boolean authenticate(){
        Iterator<UserDAO> ite = CosmosDBLayer.getInstance().getUserById(id).iterator();
        if(!ite.hasNext())
            throw new NotFoundException("User does not exist");
        UserDAO user = ite.next();
        
        if(Hash.of(password).equals(user.getPwd())){
            return true;
        }
        else
            return false;
    }
}
