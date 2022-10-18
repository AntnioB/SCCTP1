package scc.cache;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import scc.utils.AzureProperties;

public class RedisCache {
	private static final String RedisHostname = AzureProperties.getProperties().getProperty(AzureProperties.REDIS_URL);
	private static final String RedisKey = AzureProperties.getProperties().getProperty(AzureProperties.REDIS_KEY);

	private static JedisPool instance;

	public synchronized static JedisPool getCachePool() {
		if (instance != null)
			return instance;
		final JedisPoolConfig poolConfig = new JedisPoolConfig();
		poolConfig.setMaxTotal(128);
		poolConfig.setMaxIdle(128);
		poolConfig.setMinIdle(16);
		poolConfig.setTestOnBorrow(true);
		poolConfig.setTestOnReturn(true);
		poolConfig.setTestWhileIdle(true);
		poolConfig.setNumTestsPerEvictionRun(3);
		poolConfig.setBlockWhenExhausted(true);
		instance = new JedisPool(poolConfig, RedisHostname, 6380, 1000, RedisKey, true);
		return instance;

	}
}
