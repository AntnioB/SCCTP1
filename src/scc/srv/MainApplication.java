package scc.srv;

import java.util.HashSet;
import java.util.Set;

import jakarta.ws.rs.core.Application;
import scc.auction.AuctionResource;
import scc.bid.BidResource;
import scc.media.MediaResource;
import scc.question.QuestionResource;
import scc.user.UserResource;
import scc.utils.GenericExceptionMapper;

public class MainApplication extends Application {
	private Set<Object> singletons = new HashSet<Object>();
	private Set<Class<?>> resources = new HashSet<Class<?>>();

	public static final String STORAGE_CONNECTION_STRING = "DefaultEndpointsProtocol=https;AccountName=sccstwesteurope58152;AccountKey=R7HRsgWLnonRzrhdmaMgPsJiK2+IN66AO5OgWR341Fcj0hW/flx5pd6HSivuKgAwwCSNNk4fZ8pE+ASt+nsBQw==;EndpointSuffix=core.windows.net";
	public static final String CONNECTION_URL = "https://scc2358152.documents.azure.com:443/";
	public static final String DB_KEY = "Lz27NP1VmRREZJOOKAnYIbtD2AGbpwxpTnXwxnYyGRiEwxeUbjsAw4OOfDDkFSUpDGqdW0wYQc6bACDb0N4DTQ==";
	public static final String DB_NAME = "scc23db58152";
	public static final String REDIS_HOSTNAME = "rediswesteurope58152.redis.cache.windows.net";
	public static final String REDIS_KEY = "jJm1HZsPQgqChx53LXKNZI2sAwTyu3xVgAzCaOkipTs=";
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
