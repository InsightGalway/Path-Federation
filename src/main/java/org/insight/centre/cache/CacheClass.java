package org.insight.centre.cache;

import java.io.IOException;
import java.util.List;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;

public class CacheClass {

	public static Cache<String, List<PathCache>> infinispan() throws IOException{
		  ConfigurationBuilder builder = new ConfigurationBuilder();
		   builder.persistence()
		         .passivation(false)
		         .addSingleFileStore()
		            .preload(true)
		            .shared(false)
		            .fetchPersistentState(true)
		            .ignoreModifications(false)
		            .purgeOnStartup(false)
		            .location("data/cacheDB")
		            .async()
		               .enabled(true)
		               .threadPoolSize(5)
		            .singleton()
		          ;
		Configuration configuration = builder.build();
		
		DefaultCacheManager manager = new DefaultCacheManager(configuration);   
		Cache<String, List<PathCache>> cache = manager.getCache("CacheStore");
		return cache;
		
	}

}
