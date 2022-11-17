package scc.srv;

import java.util.HashSet;
import java.util.Set;

import jakarta.ws.rs.core.Application;
import scc.auction.AuctionResource;
import scc.bid.BidResource;
import scc.question.QuestionResource;
import scc.user.UserResource;

public class MainApplication extends Application {
	private Set<Object> singletons = new HashSet<Object>();
	private Set<Class<?>> resources = new HashSet<Class<?>>();

	public static final String STORAGE_CONNECTION_STRING = "DefaultEndpointsProtocol=https;AccountName=sccstwesteurope58152;AccountKey=XvQ4aJby4B/j1jGlIj7nq7RRvhQbebQm+rdyz5RL0qMHYgHnbK4gIyPgAFGsWpgw68+axTuXCU7F+AStdePOLg==;EndpointSuffix=core.windows.net";
	public static final String CONNECTION_URL = "https://scc2358152.documents.azure.com:443/";
	public static final String DB_KEY = "FCPKbiuLmxa07dUQCmIHscHUH4k7M0JIrZo3zgRtOj2RitQ5LKLEkTOM7kmWP7oMjOmzxiTvbhfNACDbAf0HPw==";
	public static final String DB_NAME = "scc23db58152";
	public static final String REDIS_HOSTNAME = "rediswesteurope58152.redis.cache.windows.net";
	public static final String REDIS_KEY = "nosNMIKFD542P5PjR1DxtCNGjnXYfRIMQAzCaCdRJcg=";
	public static final String SEARCH_PROP_FILE = "search-azurekeys-westeurope.props";
	public static final String PROP_SERVICE_NAME = "scc23cs58152";
	public static final String PROP_QUERY_KEY = "DBt6oSQr98R3xkdJbqTy6AuHfpAioWWacOI0Gt7ah9AzSeAE878e";

	public MainApplication() {
		resources.add(ControlResource.class);
		resources.add(MediaResource.class);
		resources.add(UserResource.class);
		resources.add(AuctionResource.class);
		resources.add(BidResource.class);
		resources.add(QuestionResource.class);
		singletons.add(new MediaResource());
	}

	@Override
	public Set<Class<?>> getClasses() {
		return resources;
	}

	@Override
	public Set<Object> getSingletons() {
		return singletons;
	}
}
