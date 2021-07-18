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

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.vavr.collection.Map;

/**
 * implement for Bulkhead.
 * @author dangzhicairang
 */
public class Resilience4JBulkheadCompareAndGetter
	implements CompareAndGetter<Bulkhead, BulkheadRegistry, BulkheadConfig> {

	private static Resilience4JBulkheadCompareAndGetter instance = new Resilience4JBulkheadCompareAndGetter();

	public static Resilience4JBulkheadCompareAndGetter getInstance() {
		return instance;
	}

	@Override
	public boolean compare(Bulkhead bulkhead, BulkheadConfig config) {
		BulkheadConfig oldConfig = bulkhead.getBulkheadConfig();
		return oldConfig.isWritableStackTraceEnabled() == config.isWritableStackTraceEnabled()
			&& oldConfig.isFairCallHandlingEnabled() == config.isFairCallHandlingEnabled()
			&& oldConfig.getMaxWaitDuration().equals(config.getMaxWaitDuration())
			&& oldConfig.getMaxConcurrentCalls() == config.getMaxConcurrentCalls();
	}

	@Override
	public Bulkhead get(String id, BulkheadRegistry register, BulkheadConfig config, Map<String, String> tags) {

		return Bulkhead.of(id, config, tags);
	}

	@Override
	public Bulkhead register(String id, BulkheadRegistry register, BulkheadConfig config, Map<String, String> tags) {

		return register.bulkhead(id, config, tags);
	}
}
