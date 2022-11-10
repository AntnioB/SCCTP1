package scc.auction;

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

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import scc.cache.RedisCache;
import scc.srv.MainApplication;

public class CosmosDBAuctionLayer {
	private static final String CONNECTION_URL = MainApplication.CONNECTION_URL;
	private static final String DB_KEY = MainApplication.DB_KEY;
	private static final String DB_NAME = MainApplication.DB_NAME;

	private static CosmosDBAuctionLayer instance;

	public static synchronized CosmosDBAuctionLayer getInstance() {
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
		instance = new CosmosDBAuctionLayer(client);
		return instance;

	}

	private CosmosClient client;
	private CosmosDatabase db;
	private CosmosContainer auctions;

	public CosmosDBAuctionLayer(CosmosClient client) {
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
		RedisCache.getCachePool().getResource().set(auction.getId(), auction.toAuction().toString());
		return auctions.createItem(auction);
	}

	public CosmosPagedIterable<AuctionDAO> getAuctionById(String id) {
		init();
		return auctions.queryItems("SELECT * FROM auctions WHERE auctions.id=\"" + id + "\"",
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

	public void close() {
		client.close();
	}

}
