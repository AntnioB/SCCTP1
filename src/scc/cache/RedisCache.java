package scc.cache;

import java.util.Map;

import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.NewCookie;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisException;

import scc.srv.MainApplication;

public class RedisCache {

	private static JedisPool instance;
	private static final String USERS = "users";
	private static final String AUCTIONS = "auctions";

	//TO TURN OF REDIS CHANGE THIS BOOLEAN TO FALSE
	private static boolean useRedis = true;

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
		instance = new JedisPool(poolConfig, MainApplication.REDIS_HOSTNAME, 6380, 1000, MainApplication.REDIS_KEY,
				true);
		return instance;

	}

	public synchronized static void putCookie(String uuid, String userId) {
		try (Jedis jedis = RedisCache.getCachePool().getResource()) {
			jedis.set(uuid, userId);
			jedis.expire(uuid, 360);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized static NewCookie checkCookieUser(Cookie session, String id) throws NotAuthorizedException {
		if (session == null || session.getValue() == null)
			throw new NotAuthorizedException("No session initialized 1");
		String value;
		try (Jedis jedis = RedisCache.getCachePool().getResource()) {
			value = jedis.get(session.getValue());
			if (value == null || value.length() == 0)
				throw new NotAuthorizedException("No valid session initialized 2");
			if (!value.equals(id) && !value.equals("admin"))
				throw new NotAuthorizedException("Invalid user : " + value);
			NewCookie cookie = new NewCookie.Builder("scc:session")
					.value(session.getValue())
					.path("/")
					.comment("sessionid")
					.maxAge(300)
					.secure(false)
					.httpOnly(true)
					.build();
			putCookie(cookie.getValue(), id);
			return cookie;
		} catch (Exception e) {
			throw e;
		}
	}

	public synchronized static String getUser(String id) {
		try (Jedis jedis = RedisCache.getCachePool().getResource()) {
			jedis.expire(id, 1800);
			return jedis.hget(USERS, id);
		} catch (Exception e) {
			throw new JedisException("Error when getting key!");
		}
	}

	public synchronized static String getAuction(String id) {
		try (Jedis jedis = RedisCache.getCachePool().getResource()) {
			jedis.expire(id, 1800);
			return jedis.hget(AUCTIONS, id);
		} catch (Exception e) {
			throw new JedisException("Error when getting key!");
		}
	}

	public synchronized static String deleteUser(String key) {
		if(!useRedis) return "the use of cache is off";
		try (Jedis jedis = RedisCache.getCachePool().getResource()) {
			String value = jedis.hget(USERS, key);
			jedis.hdel(USERS, key);
			return value;
		} catch (Exception e) {
			throw new JedisException("Error when deleting key!");
		}
	}

	public synchronized static String deleteAuction(String key) {
		if(!useRedis) return "the use of cache is off";
		try (Jedis jedis = RedisCache.getCachePool().getResource()) {
			String value = jedis.hget(AUCTIONS, key);
			jedis.hdel(AUCTIONS, key);
			return value;
		} catch (Exception e) {
			throw new JedisException("Error when deleting key!");
		}
	}

	public synchronized static String putUser(String key, String value) {
		if(!useRedis) return "the use of cache is off";
		try (Jedis jedis = RedisCache.getCachePool().getResource()) {
			jedis.hset(USERS, Map.of(key, value));
			jedis.expire(key, 1800);
			return value;
		} catch (Exception e) {
			throw new JedisException("Error when setting key and value!");
		}
	}

	public synchronized static String putAuction(String key, String value) {
		if(!useRedis) return "the use of cache is off";
		try (Jedis jedis = RedisCache.getCachePool().getResource()) {
			jedis.hset(AUCTIONS, Map.of(key, value));
			jedis.expire(key, 1800);
			return value;
		} catch (Exception e) {
			throw new JedisException("Error when setting key and value!");
		}
	}

	public synchronized static boolean userExists(String key) {
		if(!useRedis) return false;
		try (Jedis jedis = RedisCache.getCachePool().getResource()) {
			return jedis.hexists(USERS, key);
		} catch (Exception e) {
			throw new JedisException("Error when checking key!");
		}
	}

	public synchronized static boolean auctionExists(String key) {
		if(!useRedis) return false;
		try (Jedis jedis = RedisCache.getCachePool().getResource()) {
			return jedis.hexists(AUCTIONS, key);
		} catch (Exception e) {
			throw new JedisException("Error when checking key!");
		}
	}

}
