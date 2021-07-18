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

package org.springframework.cloud.circuitbreaker.resilience4j;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import io.vavr.collection.HashMap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.cloud.circuitbreaker.resilience4j.common.Resilience4JCircuitBreakerCompareAndGetter;
import org.springframework.cloud.circuitbreaker.resilience4j.common.Resilience4JTimeLimiterCompareAndGetter;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;

/**
 * @author Ryan Baxter
 * @author Thomas Vitale
 */
public class ReactiveResilience4JCircuitBreaker implements ReactiveCircuitBreaker {

	private final String id;

	private final io.github.resilience4j.circuitbreaker.CircuitBreakerConfig circuitBreakerConfig;

	private final CircuitBreakerRegistry circuitBreakerRegistry;

	private final TimeLimiterConfig timeLimiterConfig;

	private final TimeLimiterRegistry timeLimiterRegistry;

	private final Optional<Customizer<CircuitBreaker>> circuitBreakerCustomizer;

	private final Resilience4JTimeLimiterCompareAndGetter resilience4JTimeLimiterCompareAndGetter = Resilience4JTimeLimiterCompareAndGetter.getInstance();

	private final Resilience4JCircuitBreakerCompareAndGetter resilience4JCircuitBreakerCompareAndGetter = Resilience4JCircuitBreakerCompareAndGetter.getInstance();

	@Deprecated
	public ReactiveResilience4JCircuitBreaker(String id,
			Resilience4JConfigBuilder.Resilience4JCircuitBreakerConfiguration config,
			CircuitBreakerRegistry circuitBreakerRegistry,
			Optional<Customizer<CircuitBreaker>> circuitBreakerCustomizer) {
		this.id = id;
		this.circuitBreakerConfig = config.getCircuitBreakerConfig();
		this.circuitBreakerRegistry = circuitBreakerRegistry;
		this.circuitBreakerCustomizer = circuitBreakerCustomizer;
		this.timeLimiterConfig = config.getTimeLimiterConfig();
		this.timeLimiterRegistry = TimeLimiterRegistry.ofDefaults();
	}

	public ReactiveResilience4JCircuitBreaker(String id,
			Resilience4JConfigBuilder.Resilience4JCircuitBreakerConfiguration config,
			CircuitBreakerRegistry circuitBreakerRegistry, TimeLimiterRegistry timeLimiterRegistry,
			Optional<Customizer<CircuitBreaker>> circuitBreakerCustomizer) {
		this.id = id;
		this.circuitBreakerConfig = config.getCircuitBreakerConfig();
		this.circuitBreakerRegistry = circuitBreakerRegistry;
		this.circuitBreakerCustomizer = circuitBreakerCustomizer;
		this.timeLimiterConfig = config.getTimeLimiterConfig();
		this.timeLimiterRegistry = timeLimiterRegistry;
	}

	@Override
	public <T> Mono<T> run(Mono<T> toRun, Function<Throwable, Mono<T>> fallback) {
		io.github.resilience4j.circuitbreaker.CircuitBreaker defaultCircuitBreaker = resilience4JCircuitBreakerCompareAndGetter
			.compareAndGet(id, circuitBreakerRegistry, circuitBreakerConfig, HashMap.empty());
		circuitBreakerCustomizer.ifPresent(customizer -> customizer.customize(defaultCircuitBreaker));
		TimeLimiter timeLimiter = resilience4JTimeLimiterCompareAndGetter.compareAndGet(
			id, timeLimiterRegistry, timeLimiterConfig, HashMap.empty()
		);
		Mono<T> toReturn = toRun.transform(CircuitBreakerOperator.of(defaultCircuitBreaker))
				.timeout(timeLimiter.getTimeLimiterConfig().getTimeoutDuration())
				// Since we are using the Mono timeout we need to tell the circuit breaker
				// about the error
				.doOnError(TimeoutException.class,
						t -> defaultCircuitBreaker.onError(
								timeLimiter.getTimeLimiterConfig().getTimeoutDuration().toMillis(),
								TimeUnit.MILLISECONDS, t));
		if (fallback != null) {
			toReturn = toReturn.onErrorResume(fallback);
		}
		return toReturn;
	}

	public <T> Flux<T> run(Flux<T> toRun, Function<Throwable, Flux<T>> fallback) {
		io.github.resilience4j.circuitbreaker.CircuitBreaker defaultCircuitBreaker = resilience4JCircuitBreakerCompareAndGetter
			.compareAndGet(id, circuitBreakerRegistry, circuitBreakerConfig, HashMap.empty());
		circuitBreakerCustomizer.ifPresent(customizer -> customizer.customize(defaultCircuitBreaker));
		TimeLimiter timeLimiter = resilience4JTimeLimiterCompareAndGetter.compareAndGet(
			id, timeLimiterRegistry, timeLimiterConfig, HashMap.empty()
		);
		Flux<T> toReturn = toRun.transform(CircuitBreakerOperator.of(defaultCircuitBreaker))
				.timeout(timeLimiter.getTimeLimiterConfig().getTimeoutDuration())
				// Since we are using the Flux timeout we need to tell the circuit breaker
				// about the error
				.doOnError(TimeoutException.class,
						t -> defaultCircuitBreaker.onError(
								timeLimiter.getTimeLimiterConfig().getTimeoutDuration().toMillis(),
								TimeUnit.MILLISECONDS, t));
		if (fallback != null) {
			toReturn = toReturn.onErrorResume(fallback);
		}
		return toReturn;
	}

}
