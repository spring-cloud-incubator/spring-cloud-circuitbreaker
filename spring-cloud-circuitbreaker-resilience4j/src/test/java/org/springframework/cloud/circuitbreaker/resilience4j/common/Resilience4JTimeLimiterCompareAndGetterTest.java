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

import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import io.vavr.collection.HashMap;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author dangzhicairang
 */
public class Resilience4JTimeLimiterCompareAndGetterTest {

	Resilience4JTimeLimiterCompareAndGetter compareAndGetter = Resilience4JTimeLimiterCompareAndGetter.getInstance();

	@Test
	public void testGetNewOne() {
		TimeLimiterConfig oldConfig =
			TimeLimiterConfig
				.custom()
				.cancelRunningFuture(false)
				.timeoutDuration(Duration.ofSeconds(2))
				.build();
		TimeLimiterRegistry registry = TimeLimiterRegistry.of(oldConfig);
		TimeLimiter oldInstance = registry.timeLimiter("test");

		TimeLimiterConfig newConfig =
			TimeLimiterConfig
				.custom()
				.cancelRunningFuture(false)
				.timeoutDuration(Duration.ofSeconds(3))
				.build();
		TimeLimiter newInstance =
			compareAndGetter.compareAndGet("test", registry, newConfig, HashMap.empty());

		assertThat(oldInstance == newInstance).isFalse();
	}

	@Test
	public void testGetOld() {
		TimeLimiterConfig oldConfig =
			TimeLimiterConfig
				.custom()
				.cancelRunningFuture(false)
				.timeoutDuration(Duration.ofSeconds(2))
				.build();
		TimeLimiterRegistry registry = TimeLimiterRegistry.of(oldConfig);
		TimeLimiter oldInstance = registry.timeLimiter("test");

		TimeLimiterConfig newConfig =
			TimeLimiterConfig
				.custom()
				.cancelRunningFuture(false)
				.timeoutDuration(Duration.ofSeconds(2))
				.build();
		TimeLimiter newInstance =
			compareAndGetter.compareAndGet("test", registry, newConfig, HashMap.empty());

		assertThat(oldInstance == newInstance).isTrue();
	}
}
