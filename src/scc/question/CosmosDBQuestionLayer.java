package scc.question;

import java.util.logging.Logger;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.util.CosmosPagedIterable;

import scc.utils.AzureProperties;

public class CosmosDBQuestionLayer {
    private static final String CONNECTION_URL = "https://scc2358152.documents.azure.com:443/";
	private static final String DB_KEY = "JWMuqWZDbMyaQsblMkKNYM546yh9E2pt6lbubC91xt0v83To5IMGByfZzSWmuGGNtGgYyTRx4KtByWNHdyKMWQ==";
	private static final String DB_NAME = "scc23db58152";

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
        if (AzureProperties.getProperties().isEmpty())
			Logger.getLogger("Main").warning("NO Properties");
		questions = db.getContainer("questions");
		;
	}

    public CosmosItemResponse<QuestionDAO> putQuestion(QuestionDAO question){
        init();
        return questions.createItem(question);
    }

    public CosmosPagedIterable<QuestionDAO> getQuestionById(String id) {
		init();
		return questions.queryItems("SELECT * FROM questions WHERE questions.id=\"" + id + "\"", new CosmosQueryRequestOptions(),
				QuestionDAO.class);
	}

    public CosmosPagedIterable<QuestionDAO> getQuestionsByAuctionId(String auctionId) {
		init();
		return questions.queryItems("SELECT * FROM questions WHERE questions.auctionId=\"" + auctionId + "\"", new CosmosQueryRequestOptions(),
				QuestionDAO.class);
	}

    public void close() {
		client.close();
	}

}
