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

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.vavr.collection.HashMap;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author dangzhicairang
 */
public class Resilience4JCircuitBreakerCompareAndGetterTest {

	Resilience4JCircuitBreakerCompareAndGetter compareAndGetter =
		Resilience4JCircuitBreakerCompareAndGetter.getInstance();

	@Test
	public void testGetNewOne() {
		CircuitBreakerConfig oldConfig =
			CircuitBreakerConfig
				.custom()
				.failureRateThreshold(50)
				.automaticTransitionFromOpenToHalfOpenEnabled(true)
				.maxWaitDurationInHalfOpenState(Duration.ofSeconds(3))
				.permittedNumberOfCallsInHalfOpenState(20)
				.slidingWindowSize(20)
				.slidingWindowType(CircuitBreakerConfig.SlidingWindowType.TIME_BASED)
				.writableStackTraceEnabled(false)
				.waitDurationInOpenState(Duration.ofSeconds(3))
				.slowCallRateThreshold(100)
				.minimumNumberOfCalls(4)
				.build();
		CircuitBreakerRegistry circuitBreakerRegistry =
			CircuitBreakerRegistry.of(oldConfig);
		CircuitBreaker oldInstance = circuitBreakerRegistry.circuitBreaker("test");

		CircuitBreakerConfig newConfig =
			CircuitBreakerConfig
				.custom()
				.failureRateThreshold(50)
				.automaticTransitionFromOpenToHalfOpenEnabled(true)
				.maxWaitDurationInHalfOpenState(Duration.ofSeconds(3))
				.permittedNumberOfCallsInHalfOpenState(20)
				.slidingWindowSize(20)
				.slidingWindowType(CircuitBreakerConfig.SlidingWindowType.TIME_BASED)
				.writableStackTraceEnabled(false)
				.waitDurationInOpenState(Duration.ofSeconds(3))
				.slowCallRateThreshold(100)
				.minimumNumberOfCalls(6)
				.build();
		CircuitBreaker newInstance = compareAndGetter.compareAndGet(
			"test", circuitBreakerRegistry, newConfig, HashMap.empty()
		);

		assertThat(oldInstance == newInstance).isFalse();
	}

	@Test
	public void testGetOld() {
		CircuitBreakerConfig oldConfig =
			CircuitBreakerConfig
				.custom()
				.minimumNumberOfCalls(4)
				.failureRateThreshold(50)
				.automaticTransitionFromOpenToHalfOpenEnabled(true)
				.maxWaitDurationInHalfOpenState(Duration.ofSeconds(3))
				.permittedNumberOfCallsInHalfOpenState(20)
				.slidingWindowSize(20)
				.slidingWindowType(CircuitBreakerConfig.SlidingWindowType.TIME_BASED)
				.writableStackTraceEnabled(false)
				.waitDurationInOpenState(Duration.ofSeconds(3))
				.slowCallRateThreshold(100)
				.build();
		CircuitBreakerRegistry circuitBreakerRegistry =
			CircuitBreakerRegistry.of(oldConfig);
		CircuitBreaker oldInstance = circuitBreakerRegistry.circuitBreaker("test");

		CircuitBreakerConfig newConfig =
			CircuitBreakerConfig
				.custom()
				.minimumNumberOfCalls(4)
				.failureRateThreshold(50)
				.automaticTransitionFromOpenToHalfOpenEnabled(true)
				.maxWaitDurationInHalfOpenState(Duration.ofSeconds(3))
				.permittedNumberOfCallsInHalfOpenState(20)
				.slidingWindowSize(20)
				.slidingWindowType(CircuitBreakerConfig.SlidingWindowType.TIME_BASED)
				.writableStackTraceEnabled(false)
				.waitDurationInOpenState(Duration.ofSeconds(3))
				.slowCallRateThreshold(100)
				.build();
		CircuitBreaker newInstance = compareAndGetter.compareAndGet(
			"test", circuitBreakerRegistry, newConfig, HashMap.empty()
		);

		assertThat(oldInstance == newInstance).isTrue();
	}
}
