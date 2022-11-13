package scc.srv;

import java.util.HashSet;
import java.util.Set;

import jakarta.ws.rs.core.Application;
import scc.auction.AuctionResource;
import scc.bid.BidResource;
import scc.question.QuestionResource;
import scc.user.UserResource;

public class MainApplication extends Application
{
	private Set<Object> singletons = new HashSet<Object>();
	private Set<Class<?>> resources = new HashSet<Class<?>>();

	public static final String STORAGE_CONNECTION_STRING = "DefaultEndpointsProtocol=https;AccountName=sccstwesteurope58152;AccountKey=ioxDzZVfXVYa5pk2OAsio7KJ/GHH1q/INqqRqgJkiIIgTbK+4rg9yuU3lcmkNDh9mgxQ1A/JGBPf+ASt1WqYzQ==;EndpointSuffix=core.windows.net";
	public static final String CONNECTION_URL = "https://scc2358152.documents.azure.com:443/";
	public static final String DB_KEY = "pk7wKpid6zxMHoE5P49anzAwZvjtw3hh3REcHauRf3ZL8R2hxsqZl6TyZei12JTo5b3AbU1RrYu3ACDbNhkXOg==";
	public static final String DB_NAME = "scc23db58152";
	public static final String REDIS_HOSTNAME = "rediswesteurope58152.redis.cache.windows.net";
	public static final String REDIS_KEY= "S0fHXPd4t4cbhtBZdGIJxKXErxP1lWIWYAzCaBAftGs=";

	public MainApplication() {
		resources.add(ControlResource.class);
		resources.add(MediaResource.class);
		resources.add(UserResource.class);
		resources.add(AuctionResource.class);
		resources.add(BidResource.class);
		resources.add(QuestionResource.class);
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
