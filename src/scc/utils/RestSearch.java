package scc.utils;

import java.net.URI;
import java.util.Iterator;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.glassfish.jersey.client.ClientConfig;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;
import scc.srv.MainApplication;

public class RestSearch {
    {

        try {
            String serviceName = MainApplication.PROP_SERVICE_NAME;
            String queryKey = MainApplication.PROP_QUERY_KEY;

            String hostname = "https://" + serviceName + ".search.windows.net/";
            ClientConfig config = new ClientConfig();
            Client client = ClientBuilder.newClient(config);

            URI baseURI = UriBuilder.fromUri(hostname).build();

            WebTarget target = client.target(baseURI);

            String index = "cosmosdb-index";

            // SIMPLE QUERY
            // Check parameters at:
            // https://docs.microsoft.com/en-us/rest/api/searchservice/search-documents
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode obj = mapper.createObjectNode();
            obj.put("count", "true");
            obj.put("search", "laboriosam");

            String resultStr = target.path("indexes/" + index + "/docs/search").queryParam("api-version", "2020-06-30")
                    .request().header("api-key", queryKey)
                    .accept(MediaType.APPLICATION_JSON).post(Entity.entity(obj.toString(), MediaType.APPLICATION_JSON))
                    .readEntity(String.class);

            JsonNode resultObj = mapper.readTree(resultStr);

            System.out.println("Number of results : " + resultObj.get("@odata.count").asInt());
            Iterator<JsonNode> it = resultObj.withArray("value").elements();
            while (it.hasNext()) {
                JsonNode el = it.next();
                System.out.println();
                Iterator<Entry<String, JsonNode>> fields = el.fields();
                while (fields.hasNext()) {
                    Entry<String, JsonNode> val = fields.next();
                    System.out.println(val.getKey() + "->" + val.getValue());
                }
            }

            System.out.println();
            System.out.println("=============== Second query ======================");
            obj = mapper.createObjectNode();
            obj.put("count", "true");
            obj.put("search", "laboriosam");
            obj.put("searchFields", "title");
            obj.put("select", "id,owner,title,decription");
            obj.put("filter", "owner eq 'Gardner.Labadie'");

            resultStr = target.path("indexes/" + index + "/docs/search").queryParam("api-version", "2019-05-06")
                    .request().header("api-key", queryKey)
                    .accept(MediaType.APPLICATION_JSON).post(Entity.entity(obj.toString(), MediaType.APPLICATION_JSON))
                    .readEntity(String.class);

            System.out.println("Number of results : " + resultObj.get("@odata.count").asInt());
            it = resultObj.withArray("value").elements();
            while (it.hasNext()) {
                JsonNode el = it.next();
                System.out.println();
                Iterator<Entry<String, JsonNode>> fields = el.fields();
                while (fields.hasNext()) {
                    Entry<String, JsonNode> val = fields.next();
                    System.out.println(val.getKey() + "->" + val.getValue());
                }
            }

            System.out.println();
            System.out.println("=============== Third query ======================");
            obj = mapper.createObjectNode();
            obj.put("count", "true");
            obj.put("search", "Gardner");
            obj.put("searchFields", "title,description");
            obj.put("searchMode", "all");
            obj.put("queryType", "full");
            obj.put("select", "id,owner,title,decription");

            resultStr = target.path("indexes/" + index + "/docs/search").queryParam("api-version", "2019-05-06")
                    .request().header("api-key", queryKey)
                    .accept(MediaType.APPLICATION_JSON).post(Entity.entity(obj.toString(), MediaType.APPLICATION_JSON))
                    .readEntity(String.class);

            System.out.println("Number of results : " + resultObj.get("@odata.count").asInt());
            it = resultObj.withArray("value").elements();
            while (it.hasNext()) {
                JsonNode el = it.next();
                System.out.println();
                Iterator<Entry<String, JsonNode>> fields = el.fields();
                while (fields.hasNext()) {
                    Entry<String, JsonNode> val = fields.next();
                    System.out.println(val.getKey() + "->" + val.getValue());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
