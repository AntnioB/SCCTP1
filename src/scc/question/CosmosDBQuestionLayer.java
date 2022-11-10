package scc.question;


import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.util.CosmosPagedIterable;

import scc.srv.MainApplication;


public class CosmosDBQuestionLayer {

	private static final String CONNECTION_URL = MainApplication.CONNECTION_URL;
	private static final String DB_KEY = MainApplication.DB_KEY;
	private static final String DB_NAME = MainApplication.DB_NAME;
	
	private static CosmosDBQuestionLayer instance;

	public static synchronized CosmosDBQuestionLayer getInstance() {
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
		instance = new CosmosDBQuestionLayer(client);
		return instance;

	}

	private CosmosClient client;
	private CosmosDatabase db;
	private CosmosContainer questions;

	public CosmosDBQuestionLayer(CosmosClient client) {
		this.client = client;
	}

	private synchronized void init() {
		if (db != null)
			return;
		db = client.getDatabase(DB_NAME);
		questions = db.getContainer("questions");
	}

	public CosmosItemResponse<QuestionDAO> putQuestion(QuestionDAO question) {
		init();
		return questions.createItem(question);
	}

	public CosmosPagedIterable<QuestionDAO> getQuestionById(String id) {
		init();
		return questions.queryItems("SELECT * FROM questions WHERE questions.id=\"" + id + "\"",
				new CosmosQueryRequestOptions(),
				QuestionDAO.class);
	}

	public CosmosPagedIterable<QuestionDAO> getQuestionsByAuctionId(String auctionId) {
		init();
		return questions.queryItems("SELECT * FROM questions WHERE questions.auctionId=\"" + auctionId + "\"",
				new CosmosQueryRequestOptions(),
				QuestionDAO.class);
	}

	public CosmosItemResponse<QuestionDAO> updateQuestion(Question question) {
		init();
		return questions.upsertItem(new QuestionDAO(question));
	}

	public void close() {
		client.close();
	}

}
