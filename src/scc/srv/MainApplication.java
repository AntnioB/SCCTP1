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

	public static final String STORAGE_CONNECTION_STRING = "DefaultEndpointsProtocol=https;AccountName=sccstwesteurope58152;AccountKey=WNckVW46778TW1g5ARl+XgYtg7z6KmD8IqA7lAnfJDYeVcbfSjfMyWZccIot7BhhW59S6jUFeipS+AStxwYwlQ==;EndpointSuffix=core.windows.net";
	public static final String CONNECTION_URL = "https://scc2358152.documents.azure.com:443/";
	public static final String DB_KEY = "8f7PwYs9fHHx0zu9XAaNXfVxosWNQ18CrchiX7zZA708W8Vyy2cVS4MRJTrGtDlrsCrwHY9CfGvNACDbNO2WZw==";
	public static final String DB_NAME = "scc23db58152";
	public static final String REDIS_HOSTNAME = "rediswesteurope58152.redis.cache.windows.net";
	public static final String REDIS_KEY= "KRd1DcJAf3kmFRxfcdTkk3BC5vafYFnO3AzCaC16PPc=";

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
