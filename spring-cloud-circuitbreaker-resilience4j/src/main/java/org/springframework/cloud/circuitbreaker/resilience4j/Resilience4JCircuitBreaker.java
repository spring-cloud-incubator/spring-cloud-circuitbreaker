/*
 * Copyright 2013-2018 the original author or authors.
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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.Supplier;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.vavr.control.Try;

import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.Customizer;

/**
 * @author Ryan Baxter
 * @author Andrii Bohutskyi
 */
public class Resilience4JCircuitBreaker implements CircuitBreaker {

	private final String id;

	private Resilience4jBulkheadProvider bulkheadProvider;

	private final io.github.resilience4j.circuitbreaker.CircuitBreakerConfig circuitBreakerConfig;

	private final CircuitBreakerRegistry registry;

	private final TimeLimiterRegistry timeLimiterRegistry;

	private final TimeLimiterConfig timeLimiterConfig;

	private final ExecutorService executorService;

	private final Optional<Customizer<io.github.resilience4j.circuitbreaker.CircuitBreaker>> circuitBreakerCustomizer;

	@Deprecated
	public Resilience4JCircuitBreaker(String id,
			io.github.resilience4j.circuitbreaker.CircuitBreakerConfig circuitBreakerConfig,
			TimeLimiterConfig timeLimiterConfig, CircuitBreakerRegistry circuitBreakerRegistry,
			ExecutorService executorService,
			Optional<Customizer<io.github.resilience4j.circuitbreaker.CircuitBreaker>> circuitBreakerCustomizer) {
		this.id = id;
		this.circuitBreakerConfig = circuitBreakerConfig;
		this.registry = circuitBreakerRegistry;
		this.timeLimiterRegistry = TimeLimiterRegistry.ofDefaults();
		this.timeLimiterConfig = timeLimiterConfig;
		this.executorService = executorService;
		this.circuitBreakerCustomizer = circuitBreakerCustomizer;
	}

	public Resilience4JCircuitBreaker(String id,
			io.github.resilience4j.circuitbreaker.CircuitBreakerConfig circuitBreakerConfig,
			TimeLimiterConfig timeLimiterConfig, CircuitBreakerRegistry circuitBreakerRegistry,
			TimeLimiterRegistry timeLimiterRegistry, ExecutorService executorService,
			Optional<Customizer<io.github.resilience4j.circuitbreaker.CircuitBreaker>> circuitBreakerCustomizer) {
		this.id = id;
		this.circuitBreakerConfig = circuitBreakerConfig;
		this.registry = circuitBreakerRegistry;
		this.timeLimiterRegistry = timeLimiterRegistry;
		this.timeLimiterConfig = timeLimiterConfig;
		this.executorService = executorService;
		this.circuitBreakerCustomizer = circuitBreakerCustomizer;
	}

	@Override
	public <T> T run(Supplier<T> toRun, Function<Throwable, T> fallback) {
		TimeLimiter timeLimiter = timeLimiterRegistry.timeLimiter(id, timeLimiterConfig);
		Supplier<Future<T>> futureSupplier = () -> executorService.submit(toRun::get);
		Callable restrictedCall = TimeLimiter.decorateFutureSupplier(timeLimiter, futureSupplier);

		io.github.resilience4j.circuitbreaker.CircuitBreaker defaultCircuitBreaker = registry.circuitBreaker(id,
				circuitBreakerConfig);
		circuitBreakerCustomizer.ifPresent(customizer -> customizer.customize(defaultCircuitBreaker));

		if (bulkheadProvider != null) {
			return bulkheadProvider.run(id, toRun, fallback, defaultCircuitBreaker, timeLimiter);
		} else {
			Callable<T> callable = io.github.resilience4j.circuitbreaker.CircuitBreaker
					.decorateCallable(defaultCircuitBreaker, restrictedCall);
			return Try.of(callable::call).recover(fallback).get();
		}
	}

}
