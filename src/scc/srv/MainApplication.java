package scc.srv;

import java.util.HashSet;
import java.util.Set;

import jakarta.ws.rs.core.Application;
import scc.auction.AuctionResource;
import scc.bid.BidResource;
import scc.question.QuestionResource;
import scc.user.UserResource;
import scc.utils.GenericExceptionMapper;

public class MainApplication extends Application
{
	private Set<Object> singletons = new HashSet<Object>();
	private Set<Class<?>> resources = new HashSet<Class<?>>();

	public static final String STORAGE_CONNECTION_STRING = "DefaultEndpointsProtocol=https;AccountName=sccstwesteurope58152;AccountKey=X+JDdrYlQHiqwHb3chKgz7nAbRX6QG/lnM+Dv5QkCH8NgvC8bZ2GgD4nDU3nb82C4J05SWTQkEnv+AStOaowTw==;EndpointSuffix=core.windows.net";
	public static final String CONNECTION_URL = "https://scc2358152.documents.azure.com:443/";
	public static final String DB_KEY = "TNOXOA9PWk9mhXlgbUi6nxDlIWzuqjWi8zA60TtzmBPQJaVeKDXZJTkJ0AKe5ZkkdiyEKgCxY5ifACDbeRntSg==";
	public static final String DB_NAME = "scc23db58152";
	public static final String REDIS_HOSTNAME = "rediswesteurope58152.redis.cache.windows.net";
	public static final String REDIS_KEY= "B56BM8FbZ364CLodhDIgmfu5xD9Eg9KSLAzCaA0ajFc=";

	public MainApplication() {
		resources.add(ControlResource.class);
		resources.add(MediaResource.class);
		resources.add(UserResource.class);
		resources.add(AuctionResource.class);
		resources.add(BidResource.class);
		resources.add(QuestionResource.class);
		resources.add(GenericExceptionMapper.class);
		singletons.add( new MediaResource());	
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
