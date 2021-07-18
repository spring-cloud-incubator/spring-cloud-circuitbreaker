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

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.vavr.collection.HashMap;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author dangzhicairang
 */
public class Resilience4JBulkheadCompareAndGetterTest {

	Resilience4JBulkheadCompareAndGetter compareAndGetter = Resilience4JBulkheadCompareAndGetter.getInstance();

	@Test
	public void testGetNewOne() {
		BulkheadConfig oldConfig =
			BulkheadConfig
				.custom()
				.writableStackTraceEnabled(true)
				.fairCallHandlingStrategyEnabled(true)
				.maxWaitDuration(Duration.ZERO)
				.maxConcurrentCalls(2)
				.build();
		BulkheadRegistry registry = BulkheadRegistry.of(oldConfig);
		Bulkhead oldInstance = registry.bulkhead("test");

		BulkheadConfig newConfig =
			BulkheadConfig
				.custom()
				.writableStackTraceEnabled(true)
				.fairCallHandlingStrategyEnabled(true)
				.maxWaitDuration(Duration.ZERO)
				.maxConcurrentCalls(4)
				.build();
		Bulkhead newInstance = compareAndGetter.compareAndGet("test", registry, newConfig, HashMap.empty());

		assertThat(oldInstance == newInstance).isFalse();
	}

	@Test
	public void testGetOld() {
		BulkheadConfig oldConfig =
			BulkheadConfig
				.custom()
				.maxConcurrentCalls(2)
				.writableStackTraceEnabled(true)
				.fairCallHandlingStrategyEnabled(true)
				.maxWaitDuration(Duration.ZERO)
				.build();
		BulkheadRegistry registry = BulkheadRegistry.of(oldConfig);
		Bulkhead oldInstance = registry.bulkhead("test");

		BulkheadConfig newConfig =
			BulkheadConfig
				.custom()
				.maxConcurrentCalls(2)
				.writableStackTraceEnabled(true)
				.fairCallHandlingStrategyEnabled(true)
				.maxWaitDuration(Duration.ZERO)
				.build();
		Bulkhead newInstance = compareAndGetter.compareAndGet("test", registry, newConfig, HashMap.empty());

		assertThat(oldInstance == newInstance).isTrue();
	}
}
