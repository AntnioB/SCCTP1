package scc.cache;

import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.core.Cookie;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import scc.utils.AzureProperties;
import redis.clients.jedis.exceptions.JedisException;

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

	public synchronized static void putCookie(String uuid, String userId) {
		try (Jedis jedis = RedisCache.getCachePool().getResource()) {
			jedis.set("cookie:" + uuid, userId);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized static String checkCookieUser(Cookie session, String id) throws NotAuthorizedException {
		if (session == null || session.getValue() == null)
			throw new NotAuthorizedException("No session initialized");
		String value;
		try (Jedis jedis = RedisCache.getCachePool().getResource()) {
			value = jedis.get(session.getValue());
		} catch (Exception e) {
			throw new JedisException("Error in get cookie!");
		}
		if (value == null ||  value.length() == 0)
			throw new NotAuthorizedException("No valid session initialized");
		if (!value.equals(id) && !value.equals("admin"))
			throw new NotAuthorizedException("Invalid user : " + value);
		return value;
	}

}
