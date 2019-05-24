package org.insight.centre.cache;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.cache.StorageType;
import org.infinispan.eviction.EvictionStrategy;
import org.infinispan.eviction.EvictionType;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.multimap.api.embedded.EmbeddedMultimapCacheManagerFactory;
import org.infinispan.multimap.api.embedded.MultimapCache;
import org.infinispan.multimap.api.embedded.MultimapCacheManager;
import org.insight.centre.federation.SourceSelection;

import insight.dev.flushablemap.PathCache;


public class CacheClass implements Serializable{


	public static MultimapCache<Integer, PathCache> infinispan() throws IOException{
		  ConfigurationBuilder builder = new ConfigurationBuilder();
		   builder
		        .memory().storageType(StorageType.OBJECT)
		        .evictionType(EvictionType.COUNT).size(1_00)
		   		.persistence()
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
		               .threadPoolSize(100)
		            .singleton().simpleCache()
		            
		          ;
		Configuration configuration = builder.build();
		
		//DefaultCacheManager manager = new DefaultCacheManager(configuration);   
		//Cache<String, List<PathCache>> cache = manager.getCache("CacheStore");
		
		DefaultCacheManager manager = new DefaultCacheManager(configuration);
		//Cache<String, List<SourceSelection.PathCache>> cache = manager.getCache("CacheStore");
		
		// Obtain a multimap cache manager from the regular cache manager
	      MultimapCacheManager multimapCacheManager = EmbeddedMultimapCacheManagerFactory.from(manager);
	      MultimapCache<Integer, PathCache> multimap = multimapCacheManager.get("CacheStore");
		return multimap;
		
	}

}
