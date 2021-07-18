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

import java.time.Duration;

import io.github.resilience4j.bulkhead.ThreadPoolBulkhead;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadConfig;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadRegistry;
import io.vavr.collection.HashMap;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author dangzhicairang
 */
public class Resilience4JThreadPoolBulkheadCompareAndGetterTest {

	Resilience4JThreadPoolBulkheadCompareAndGetter compareAndGetter = Resilience4JThreadPoolBulkheadCompareAndGetter.getInstance();

	@Test
	public void testGetNewOne() {
		ThreadPoolBulkheadConfig oldConfig =
			ThreadPoolBulkheadConfig
				.custom()
				.queueCapacity(2)
				.coreThreadPoolSize(2)
				.writableStackTraceEnabled(false)
				.keepAliveDuration(Duration.ZERO)
				.maxThreadPoolSize(4)
				.build();
		ThreadPoolBulkheadRegistry registry =
			ThreadPoolBulkheadRegistry.of(oldConfig);
		ThreadPoolBulkhead oldInstacne = registry.bulkhead("test");

		ThreadPoolBulkheadConfig newConfig =
			ThreadPoolBulkheadConfig
				.custom()
				.queueCapacity(2)
				.coreThreadPoolSize(2)
				.writableStackTraceEnabled(false)
				.keepAliveDuration(Duration.ZERO)
				.maxThreadPoolSize(2)
				.build();
		ThreadPoolBulkhead newIstance =
			compareAndGetter.compareAndGet("test", registry, newConfig, HashMap.empty());

		assertThat(oldInstacne == newIstance).isFalse();
	}

	@Test
	public void testGetOld() {
		ThreadPoolBulkheadConfig oldConfig =
			ThreadPoolBulkheadConfig
				.custom()
				.queueCapacity(2)
				.coreThreadPoolSize(2)
				.maxThreadPoolSize(4)
				.writableStackTraceEnabled(false)
				.keepAliveDuration(Duration.ZERO)
				.build();
		ThreadPoolBulkheadRegistry registry =
			ThreadPoolBulkheadRegistry.of(oldConfig);
		ThreadPoolBulkhead oldInstacne = registry.bulkhead("test");

		ThreadPoolBulkheadConfig newConfig =
			ThreadPoolBulkheadConfig
				.custom()
				.queueCapacity(2)
				.coreThreadPoolSize(2)
				.maxThreadPoolSize(4)
				.writableStackTraceEnabled(false)
				.keepAliveDuration(Duration.ZERO)
				.build();
		ThreadPoolBulkhead newIstance =
			compareAndGetter.compareAndGet("test", registry, newConfig, HashMap.empty());

		assertThat(oldInstacne == newIstance).isTrue();
	}
}
