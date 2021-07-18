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

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.vavr.collection.Map;

/**
 * implement for CircuitBreaker.
 * @author dangzhicairang
 */
public class Resilience4JCircuitBreakerCompareAndGetter
	implements CompareAndGetter<CircuitBreaker, CircuitBreakerRegistry, CircuitBreakerConfig> {

	private static Resilience4JCircuitBreakerCompareAndGetter instance = new Resilience4JCircuitBreakerCompareAndGetter();

	public static Resilience4JCircuitBreakerCompareAndGetter getInstance() {
		return instance;
	}

	/**
	 * ignore the compare if that property is a Function like
	 * 		recordResultPredicate ...
	 * it mean if you modify these properties by Config
	 * 		Classes, it also not take effect
	 * @param circuitBreaker instance that exist in registry.
	 * @param config the new CircuitBreakerConfig that be configured
	 *      by config file.
	 * @return
	 */
	@Override
	public boolean compare(CircuitBreaker circuitBreaker, CircuitBreakerConfig config) {
		CircuitBreakerConfig oldConfig = circuitBreaker.getCircuitBreakerConfig();
		return oldConfig.getFailureRateThreshold() == config.getFailureRateThreshold()
			&& oldConfig.getMaxWaitDurationInHalfOpenState().equals(config.getMaxWaitDurationInHalfOpenState())
			&& oldConfig.getPermittedNumberOfCallsInHalfOpenState() == config.getPermittedNumberOfCallsInHalfOpenState()
			&& oldConfig.isAutomaticTransitionFromOpenToHalfOpenEnabled() == config.isAutomaticTransitionFromOpenToHalfOpenEnabled()
			&& oldConfig.isWritableStackTraceEnabled() == config.isWritableStackTraceEnabled()
			&& oldConfig.getSlidingWindowSize() == config.getSlidingWindowSize()
			&& oldConfig.getSlidingWindowType() == config.getSlidingWindowType()
			&& oldConfig.getSlowCallDurationThreshold().equals(config.getSlowCallDurationThreshold())
			&& oldConfig.getSlowCallRateThreshold() == config.getSlowCallRateThreshold()
			&& oldConfig.getMinimumNumberOfCalls() == config.getMinimumNumberOfCalls();
	}

	@Override
	public CircuitBreaker get(String id, CircuitBreakerRegistry register, CircuitBreakerConfig config, Map<String, String> tags) {

		return CircuitBreaker.of(id, config, tags);
	}

	@Override
	public CircuitBreaker register(String id, CircuitBreakerRegistry register, CircuitBreakerConfig config, Map<String, String> tags) {

		return register.circuitBreaker(id, config, tags);
	}
}
