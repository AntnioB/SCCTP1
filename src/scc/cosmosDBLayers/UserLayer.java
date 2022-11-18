package scc.cosmosDBLayers;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import scc.cache.RedisCache;
import scc.data.database.UserDAO;
import scc.srv.MainApplication;

public class UserLayer {
	private static final String CONNECTION_URL = MainApplication.CONNECTION_URL;
	private static final String DB_KEY = MainApplication.DB_KEY;
	private static final String DB_NAME = MainApplication.DB_NAME;

	private static UserLayer instance;

	public static synchronized UserLayer getInstance() {
		if (instance != null)
			return instance;

		CosmosClient client = new CosmosClientBuilder()
				.endpoint(CONNECTION_URL)
				.key(DB_KEY)
				// .directMode()
				.gatewayMode()
				// replace by .directMode() for better performance
				.consistencyLevel(ConsistencyLevel.SESSION)
				.connectionSharingAcrossClientsEnabled(true)
				.contentResponseOnWriteEnabled(true)
				.buildClient();
		instance = new UserLayer(client);
		return instance;

	}

	private CosmosClient client;
	private CosmosDatabase db;
	private CosmosContainer users;

	public UserLayer(CosmosClient client) {
		this.client = client;
	}

	private synchronized void init() {
		if (db != null)
			return;
		db = client.getDatabase(DB_NAME);
		users = db.getContainer("users");
	}

	public CosmosItemResponse<Object> delUserById(String id) {
		init();
		PartitionKey key = new PartitionKey(id);
		return users.deleteItem(id, key, new CosmosItemRequestOptions());
	}

	public CosmosItemResponse<Object> delUser(UserDAO user) {
		init();
		return users.deleteItem(user, new CosmosItemRequestOptions());
	}

	public CosmosItemResponse<UserDAO> putUser(UserDAO user) {
		init();
		return users.createItem(user);
	}

	public CosmosPagedIterable<UserDAO> getUserById(String id) throws JsonProcessingException {
		init();
		CosmosPagedIterable<UserDAO> cosmosPagedIterable = users.queryItems(
				"SELECT * FROM users WHERE users.id=\"" + id + "\"", new CosmosQueryRequestOptions(),
				UserDAO.class);
		if (cosmosPagedIterable.iterator().hasNext()) {
			ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
			String json = ow.writeValueAsString(cosmosPagedIterable.iterator().next().toUser());
			RedisCache.putUser(id, json);
		}
		return cosmosPagedIterable;
	}

	public CosmosPagedIterable<UserDAO> getUsers() {
		init();
		return users.queryItems("SELECT * FROM users ", new CosmosQueryRequestOptions(), UserDAO.class);
	}

	public CosmosItemResponse<UserDAO> updateUser(UserDAO user) {
		init();
		return users.upsertItem(user);
	}

	public void close() {
		client.close();
	}

}
