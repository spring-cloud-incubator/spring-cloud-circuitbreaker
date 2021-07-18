/*
 * Copyright 2013-2020 the original author or authors.
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

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.bulkhead.ThreadPoolBulkhead;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.vavr.collection.HashMap;
import io.vavr.control.Try;

import org.springframework.cloud.circuitbreaker.resilience4j.common.Resilience4JBulkheadCompareAndGetter;
import org.springframework.cloud.circuitbreaker.resilience4j.common.Resilience4JThreadPoolBulkheadCompareAndGetter;
import org.springframework.cloud.client.circuitbreaker.Customizer;

/**
 * @author Andrii Bohutskyi
 */
public class Resilience4jBulkheadProvider {

	private final ThreadPoolBulkheadRegistry threadPoolBulkheadRegistry;

	private final BulkheadRegistry bulkheadRegistry;

	private final ConcurrentHashMap<String, Resilience4jBulkheadConfigurationBuilder.BulkheadConfiguration> configurations = new ConcurrentHashMap<>();

	private Function<String, Resilience4jBulkheadConfigurationBuilder.BulkheadConfiguration> defaultConfiguration;

	private final Resilience4JBulkheadCompareAndGetter resilience4JBulkheadCompareAndGetter = Resilience4JBulkheadCompareAndGetter.getInstance();

	private final Resilience4JThreadPoolBulkheadCompareAndGetter resilience4JThreadPoolBulkheadCompareAndGetter = Resilience4JThreadPoolBulkheadCompareAndGetter.getInstance();


	public Resilience4jBulkheadProvider(ThreadPoolBulkheadRegistry threadPoolBulkheadRegistry,
			BulkheadRegistry bulkheadRegistry) {
		this.bulkheadRegistry = bulkheadRegistry;
		this.threadPoolBulkheadRegistry = threadPoolBulkheadRegistry;
		defaultConfiguration = id -> new Resilience4jBulkheadConfigurationBuilder()
				.bulkheadConfig(this.bulkheadRegistry.getDefaultConfig())
				.threadPoolBulkheadConfig(this.threadPoolBulkheadRegistry.getDefaultConfig()).build();
	}

	public void configureDefault(
			Function<String, Resilience4jBulkheadConfigurationBuilder.BulkheadConfiguration> defaultConfiguration) {
		this.defaultConfiguration = defaultConfiguration;
	}

	public void configure(Consumer<Resilience4jBulkheadConfigurationBuilder> consumer, String... ids) {
		for (String id : ids) {
			Resilience4jBulkheadConfigurationBuilder builder = new Resilience4jBulkheadConfigurationBuilder();
			consumer.accept(builder);
			Resilience4jBulkheadConfigurationBuilder.BulkheadConfiguration configuration = builder.build();
			configurations.put(id, configuration);
		}
	}

	public void addBulkheadCustomizer(Customizer<Bulkhead> customizer, String... ids) {
		for (String id : ids) {
			Resilience4jBulkheadConfigurationBuilder.BulkheadConfiguration configuration = configurations
					.computeIfAbsent(id, defaultConfiguration);
			Bulkhead bulkhead = resilience4JBulkheadCompareAndGetter
				.compareAndGet(id, bulkheadRegistry, configuration.getBulkheadConfig(), HashMap.empty());
			customizer.customize(bulkhead);
		}
	}

	public void addThreadPoolBulkheadCustomizer(Customizer<ThreadPoolBulkhead> customizer, String... ids) {
		for (String id : ids) {
			Resilience4jBulkheadConfigurationBuilder.BulkheadConfiguration configuration = configurations
					.computeIfAbsent(id, defaultConfiguration);
			ThreadPoolBulkhead threadPoolBulkhead = resilience4JThreadPoolBulkheadCompareAndGetter.compareAndGet(
				id, threadPoolBulkheadRegistry, configuration.getThreadPoolBulkheadConfig(), HashMap.empty()
			);
			customizer.customize(threadPoolBulkhead);
		}
	}

	protected BulkheadRegistry getBulkheadRegistry() {
		return bulkheadRegistry;
	}

	protected ThreadPoolBulkheadRegistry getThreadPoolBulkheadRegistry() {
		return threadPoolBulkheadRegistry;
	}

	public <T> T run(String id, Supplier<T> toRun, Function<Throwable, T> fallback, CircuitBreaker circuitBreaker,
			TimeLimiter timeLimiter, io.vavr.collection.Map<String, String> tags) {
		Supplier<CompletionStage<T>> bulkheadCall = decorateBulkhead(id, tags, toRun);
		final Callable<T> timeLimiterCall = decorateTimeLimiter(bulkheadCall, timeLimiter);
		final Callable<T> circuitBreakerCall = circuitBreaker.decorateCallable(timeLimiterCall);
		return Try.of(circuitBreakerCall::call).recover(fallback).get();
	}

	private <T> Supplier<CompletionStage<T>> decorateBulkhead(final String id,
			final io.vavr.collection.Map<String, String> tags, final Supplier<T> supplier) {
		Resilience4jBulkheadConfigurationBuilder.BulkheadConfiguration configuration = configurations
				.computeIfAbsent(id, defaultConfiguration);

		if (bulkheadRegistry.find(id).isPresent() && !threadPoolBulkheadRegistry.find(id).isPresent()) {
			Bulkhead bulkhead = resilience4JBulkheadCompareAndGetter
				.compareAndGet(id, bulkheadRegistry, configuration.getBulkheadConfig(), tags);
			CompletableFuture<T> asyncCall = CompletableFuture.supplyAsync(supplier);
			return Bulkhead.decorateCompletionStage(bulkhead, () -> asyncCall);
		}
		else {
			ThreadPoolBulkhead threadPoolBulkhead = resilience4JThreadPoolBulkheadCompareAndGetter.compareAndGet(
				id, threadPoolBulkheadRegistry, configuration.getThreadPoolBulkheadConfig(), tags
			);
			return threadPoolBulkhead.decorateSupplier(supplier);
		}
	}

	private <T> Callable<T> decorateTimeLimiter(final Supplier<CompletionStage<T>> supplier, TimeLimiter timeLimiter) {
		final Supplier<Future<T>> futureSupplier = () -> supplier.get().toCompletableFuture();
		return timeLimiter.decorateFutureSupplier(futureSupplier);
	}

}
