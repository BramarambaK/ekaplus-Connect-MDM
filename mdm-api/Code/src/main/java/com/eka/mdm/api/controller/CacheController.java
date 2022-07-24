package com.eka.mdm.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.eka.mdm.api.ecache.ICacheManager;

/**
 * <p>
 * <code>CacheController</code> Controller which exposes end points to do
 * operation on cahe.
 * <p>
 * <hr>
 * 
 * @author Ranjan.Jha
 * @version 1.0
 */

@RestController
@RequestMapping("/cache")
public class CacheController {

	@Autowired
	ICacheManager cacheManager;

	@RequestMapping(value = "/evict", method = RequestMethod.GET, produces = "application/json")
	public void evictCache() {

		cacheManager.evict();
	}

}
