package scc.srv;

import java.util.HashSet;
import java.util.Set;

import jakarta.ws.rs.core.Application;
import scc.auction.AuctionResource;
import scc.bid.BidResource;
import scc.user.UserResource;

public class MainApplication extends Application
{
	private Set<Object> singletons = new HashSet<Object>();
	private Set<Class<?>> resources = new HashSet<Class<?>>();

	public static final String STORAGE_CONNECTION_STRING = "DefaultEndpointsProtocol=https;AccountName=sccstwesteurope58152;AccountKey=3B39TIrMFqGnY4g/aNfFNut/L994E2kzvX01s5O7wiWkOSJN/Xd5GASK/DGxmgg52hZMRE5x6vHS+AStYQXm0A==;EndpointSuffix=core.windows.net";
	public static final String CONNECTION_URL = "https://scc2358152.documents.azure.com:443/";
	public static final String DB_KEY = "jCVntk2e4yJxDq5WbrVfD1VFAwKCerRHWFA1GpSKCRmSAbiHDKSQ9rHxvJnmySWW2uWIHTjH6densNdxqCm9yQ==";
	public static final String DB_NAME = "scc23db58152";
	public static final String REDIS_HOSTNAME = "rediswesteurope58152.redis.cache.windows.net";
	public static final String REDIS_KEY= "pKOMktKAqiC9qWKttRTgrPoyRYKjVbwOSAzCaCzp6HM=";

	public MainApplication() {
		resources.add(ControlResource.class);
		resources.add(MediaResource.class);
		resources.add(UserResource.class);
		resources.add(AuctionResource.class);
		resources.add(BidResource.class);
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
