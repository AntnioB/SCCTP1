package scc.srv;

import java.util.Iterator;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.NotFoundException;
import scc.cache.RedisCache;
import scc.cosmosDBLayers.UserLayer;
import scc.data.database.UserDAO;
import scc.utils.Hash;

public class Login {

    private String id, password;

    public Login(@JsonProperty("id") String id, @JsonProperty("pwd") String password) {
        this.id = id;
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public boolean authenticate() throws JsonMappingException, JsonProcessingException {
        UserDAO user;
        if (RedisCache.userExists(id)) {
            ObjectMapper mapper = new ObjectMapper();
            user = mapper.readValue(RedisCache.getUser(id), UserDAO.class);
        } else {
            Iterator<UserDAO> ite = UserLayer.getInstance().getUserById(id).iterator();
            if (!ite.hasNext())
                throw new NotFoundException("User does not exist");
            user = ite.next();
        }
        if (Hash.of(password).equals(user.getPwd())) {
            return true;
        } else
            return false;
    }
}
