package com.eka.mdm.api.ecache;

import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eka.mdm.api.util.ContextProvider;
import com.eka.mdm.dataobject.ApplicationProps;
import com.eka.mdm.dataobject.RequestContext;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Service
public class RedisCacheManager implements ICacheManager {

	@Autowired
	private ContextProvider contextProvider;

	private JedisPool pool = null;

	final static Logger logger = ESAPI.getLogger(RedisCacheManager.class);
	
	public Jedis getJedis() {

		// configure our pool connection
		// Set timeout to 30 Min

		ApplicationProps applicationProps = contextProvider.getCurrentContext().getApplicationProps();

		JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();

		logger.debug(Logger.EVENT_SUCCESS,ESAPI.encoder().encodeForHTML("Creating instance of redis at host: " + applicationProps.getEka_redis_host() + " and port: "
				+ applicationProps.getEka_redis_port()));
		
		pool = new JedisPool(jedisPoolConfig, applicationProps.getEka_redis_host(),
				Integer.parseInt(applicationProps.getEka_redis_port()), 1800000,applicationProps.getEka_redis_password());

		Jedis jedis = pool.getResource();
		return jedis;
	}

	public void closeRedisResource(Jedis jedis) {

		jedis.close();
		pool.close();

	}

	@Override
	public void evict() {
		Jedis jedis = getJedis();
		jedis.flushAll();

		closeRedisResource(jedis);
	}

}
