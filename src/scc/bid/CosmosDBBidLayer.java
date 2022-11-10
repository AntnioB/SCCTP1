package scc.bid;


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

import scc.srv.MainApplication;

public class CosmosDBBidLayer {
	private static final String CONNECTION_URL = MainApplication.CONNECTION_URL;
	private static final String DB_KEY = MainApplication.DB_KEY;
	private static final String DB_NAME = MainApplication.DB_NAME;
	private static CosmosDBBidLayer instance;

	public static synchronized CosmosDBBidLayer getInstance() {
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
		instance = new CosmosDBBidLayer(client);
		return instance;

	}

	private CosmosClient client;
	private CosmosDatabase db;
	private CosmosContainer bids;

	public CosmosDBBidLayer(CosmosClient client) {
		this.client = client;
	}

	private synchronized void init() {
		if (db != null)
			return;
		db = client.getDatabase(DB_NAME);
		bids = db.getContainer("bids");

	}

    public CosmosItemResponse<Object> delBidById(String id) {
		init();
		PartitionKey key = new PartitionKey(id);
		return bids.deleteItem(id, key, new CosmosItemRequestOptions());
	}

	public CosmosItemResponse<Object> delBid(BidDAO bid) {
		init();
		return bids.deleteItem(bid, new CosmosItemRequestOptions());
	}

	public CosmosItemResponse<BidDAO> putBid(BidDAO bid) {
		init();
		return bids.createItem(bid);
	}

	public CosmosPagedIterable<BidDAO> getBidById(String id) {
		init();
		return bids.queryItems("SELECT * FROM bids WHERE bids.id=\"" + id + "\"",
				new CosmosQueryRequestOptions(), BidDAO.class);
	}

	public CosmosPagedIterable<BidDAO> getBidByAuctionId(String id){
		init();
		return bids.queryItems("SELECT * FROM bids WHERE bids.auctionId=\"" + id + "\"",
				new CosmosQueryRequestOptions(), BidDAO.class);
	}

	public CosmosPagedIterable<BidDAO> getHighestBid(String id){
		init();
		return bids.queryItems("SELECT TOP 1 * FROM bids WHERE bids.auctionId=\"" + id + "\" ORDER BY bids.amount DESC",
				new CosmosQueryRequestOptions(),BidDAO.class);
	}

	public CosmosPagedIterable<BidDAO> getBids() {
		init();
		return bids.queryItems("SELECT * FROM bids ", new CosmosQueryRequestOptions(), BidDAO.class);
	}

	public CosmosItemResponse<BidDAO> updateBid(BidDAO bid) {
		init();
		return bids.upsertItem(bid);
	}

	public void close() {
		client.close();
	}
}
