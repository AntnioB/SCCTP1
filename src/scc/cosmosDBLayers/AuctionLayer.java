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
import scc.data.database.AuctionDAO;
import scc.srv.MainApplication;
import scc.utils.Status;

public class AuctionLayer {
	private static final String CONNECTION_URL = MainApplication.CONNECTION_URL;
	private static final String DB_KEY = MainApplication.DB_KEY;
	private static final String DB_NAME = MainApplication.DB_NAME;

	private static AuctionLayer instance;

	public static synchronized AuctionLayer getInstance() {
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
		instance = new AuctionLayer(client);
		return instance;

	}

	private CosmosClient client;
	private CosmosDatabase db;
	private CosmosContainer auctions;

	public AuctionLayer(CosmosClient client) {
		this.client = client;
	}

	private synchronized void init() {
		if (db != null)
			return;
		db = client.getDatabase(DB_NAME);
		auctions = db.getContainer("auctions");

	}

	public CosmosItemResponse<Object> delAuctionById(String id) {
		init();
		PartitionKey key = new PartitionKey(id);
		return auctions.deleteItem(id, key, new CosmosItemRequestOptions());
	}

	public CosmosItemResponse<Object> delAuction(AuctionDAO auction) {
		init();
		return auctions.deleteItem(auction, new CosmosItemRequestOptions());
	}

	public CosmosItemResponse<AuctionDAO> putAuction(AuctionDAO auction) {
		init();
		return auctions.createItem(auction);
	}

	public CosmosPagedIterable<AuctionDAO> getAuctionById(String id) throws JsonProcessingException {
		init();
		CosmosPagedIterable<AuctionDAO> cosmosPagedIterable = auctions.queryItems(
				"SELECT * FROM auctions WHERE auctions.id=\"" + id + "\"",
				new CosmosQueryRequestOptions(), AuctionDAO.class);
		if (cosmosPagedIterable.iterator().hasNext()) {
			ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
			String json = ow.writeValueAsString(cosmosPagedIterable.iterator().next().toAuction());
			RedisCache.putAuction(id, json);
		}
		return cosmosPagedIterable;
	}

	public CosmosPagedIterable<AuctionDAO> getAuctionByOwnerId(String ownerId) {
		init();
		return auctions.queryItems("SELECT * FROM auctions WHERE auctions.ownerId=\"" + ownerId + "\"",
				new CosmosQueryRequestOptions(), AuctionDAO.class);
	}

	public CosmosPagedIterable<AuctionDAO> getAuctions() {
		init();
		return auctions.queryItems("SELECT * FROM auctions ", new CosmosQueryRequestOptions(), AuctionDAO.class);
	}

	public CosmosItemResponse<AuctionDAO> updateAuction(AuctionDAO auction) {
		init();
		return auctions.upsertItem(auction);
	}

	public CosmosPagedIterable<AuctionDAO> getOpenAuctions() {
		init();
		return auctions.queryItems("SELECT * FROM auctions WHERE auctions.status= \"" + Status.OPEN + "\"",
				new CosmosQueryRequestOptions(), AuctionDAO.class);
	}

	public CosmosPagedIterable<AuctionDAO> getAuctionsAboutToClose(){
		init();
		return auctions.queryItems("SELECT TOP 10 * FROM auctions WHERE auctions.status=\"" + Status.OPEN + "\" ORDER BY auctions.endTime ASC",
				 new CosmosQueryRequestOptions(), AuctionDAO.class);
	}

	public void close() {
		client.close();
	}

}
