package scc.srv;

import java.util.HashSet;
import java.util.Set;

import jakarta.ws.rs.core.Application;
import scc.resources.AuctionResource;
import scc.resources.BidResource;
import scc.resources.ControlResource;
import scc.resources.MediaResource;
import scc.resources.QuestionResource;
import scc.resources.UserResource;
import scc.utils.GenericExceptionMapper;

public class MainApplication extends Application {
	private Set<Object> singletons = new HashSet<Object>();
	private Set<Class<?>> resources = new HashSet<Class<?>>();

	public static final String STORAGE_CONNECTION_STRING = "DefaultEndpointsProtocol=https;AccountName=sccstwesteurope58152;AccountKey=lyrEMvZSTlOQ02P1RHd4nXw48pIbiQukRKKeOEP6Pb5H2hEuB2pQJTrLQqhyM7u/3dLZJNb4yM5/+AStAE7ODg==;EndpointSuffix=core.windows.net";
	public static final String CONNECTION_URL = "https://scc2358152.documents.azure.com:443/";
	public static final String DB_KEY = "pDGK9sE9PUXsg0nLhEcfu0YQLxgIiHJVbwLPnEdTxZqc9w7jLFAA34JVDW5cO3j50CjiZxtvtr1UACDbnIrVTg==";
	public static final String DB_NAME = "scc23db58152";
	public static final String REDIS_HOSTNAME = "rediswesteurope58152.redis.cache.windows.net";
	public static final String REDIS_KEY = "2WP061eo5XbykzjX9Vf7Zal5SdFDK95DYAzCaFqVyPI=";
	public static final String PROP_SERVICE_NAME = "scccs2358152";
	public static final String PROP_QUERY_KEY = "h83LhYZqKCwRKy5neyEoPN6mfplvmJ6Fe2B6WuI7PiAzSeAVbvAH";

	public MainApplication() {
		resources.add(ControlResource.class);
		resources.add(MediaResource.class);
		resources.add(UserResource.class);
		resources.add(AuctionResource.class);
		resources.add(BidResource.class);
		resources.add(QuestionResource.class);
		resources.add(GenericExceptionMapper.class);
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
