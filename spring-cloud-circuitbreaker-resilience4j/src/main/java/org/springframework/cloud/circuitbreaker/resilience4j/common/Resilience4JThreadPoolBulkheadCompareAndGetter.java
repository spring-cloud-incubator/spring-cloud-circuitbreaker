/*
 * Copyright 2013-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.circuitbreaker.resilience4j.common;

import io.github.resilience4j.bulkhead.ThreadPoolBulkhead;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadConfig;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadRegistry;
import io.vavr.collection.Map;

/**
 * implement for ThreadPoolBulkhead.
 * @author dangzhicairang
 */
public class Resilience4JThreadPoolBulkheadCompareAndGetter
	implements CompareAndGetter<ThreadPoolBulkhead, ThreadPoolBulkheadRegistry, ThreadPoolBulkheadConfig> {

	private static Resilience4JThreadPoolBulkheadCompareAndGetter instance = new Resilience4JThreadPoolBulkheadCompareAndGetter();

	public static Resilience4JThreadPoolBulkheadCompareAndGetter getInstance() {
		return instance;
	}

	/**
	 * ignore compare the contextPropagators and rejectedExecutionHandler.
	 * it mean if you modify these properties by Config Classes, it also
	 * 		not take effect
	 * @param threadPoolBulkhead instance that exist in registry.
	 * @param config the new ThreadPoolBulkheadConfig that be configured
	 *      by config file.
	 * @return
	 */
	@Override
	public boolean compare(ThreadPoolBulkhead threadPoolBulkhead, ThreadPoolBulkheadConfig config) {
		ThreadPoolBulkheadConfig oldConfig = threadPoolBulkhead.getBulkheadConfig();
		return oldConfig.isWritableStackTraceEnabled() == config.isWritableStackTraceEnabled()
			&& oldConfig.getCoreThreadPoolSize() == config.getCoreThreadPoolSize()
			&& oldConfig.getQueueCapacity() == config.getQueueCapacity()
			&& oldConfig.getKeepAliveDuration().equals(config.getKeepAliveDuration())
			&& oldConfig.getMaxThreadPoolSize() == config.getMaxThreadPoolSize();
	}

	@Override
	public ThreadPoolBulkhead get(String id, ThreadPoolBulkheadRegistry register, ThreadPoolBulkheadConfig config, Map<String, String> tags) {

		return ThreadPoolBulkhead.of(id, config, tags);
	}

	@Override
	public ThreadPoolBulkhead register(String id, ThreadPoolBulkheadRegistry register, ThreadPoolBulkheadConfig config, Map<String, String> tags) {

		return register.bulkhead(id, config, tags);
	}
}
