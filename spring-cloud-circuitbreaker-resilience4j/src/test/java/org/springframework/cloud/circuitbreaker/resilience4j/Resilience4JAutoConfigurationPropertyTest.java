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

import java.time.Duration;
import java.util.Optional;

import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * @author Andrii Bohutskyi
 * @author 荒
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Resilience4JAutoConfigurationPropertyTest.class)
@EnableAutoConfiguration
@ActiveProfiles(profiles = "test-properties")
public class Resilience4JAutoConfigurationPropertyTest {

	@Autowired
	Resilience4JCircuitBreakerFactory factory;

	@Test
	public void testCircuitBreakerPropertiesPopulated() {
		CircuitBreakerRegistry circuitBreakerRegistry = factory.getCircuitBreakerRegistry();
		assertThat(circuitBreakerRegistry).isNotNull();
		assertThat(circuitBreakerRegistry.find("test_circuit")).isPresent();
		assertThat(
				circuitBreakerRegistry.find("test_circuit").get().getCircuitBreakerConfig().getMinimumNumberOfCalls())
						.isEqualTo(5);
	}

	@Test
	public void testTimeLimiterPropertiesPopulated() {
		TimeLimiterRegistry timeLimiterRegistry = factory.getTimeLimiterRegistry();
		assertThat(timeLimiterRegistry).isNotNull();
		assertThat(timeLimiterRegistry.find("test_circuit")).isPresent();
		assertThat(timeLimiterRegistry.find("test_circuit").get().getTimeLimiterConfig().getTimeoutDuration())
				.isEqualTo(Duration.ofSeconds(18));
	}

	@Test
	public void testThreadPoolBulkheadPropertiesPopulated() {
		ThreadPoolBulkheadRegistry threadPoolBulkheadRegistry = factory.getBulkheadProvider()
				.getThreadPoolBulkheadRegistry();
		assertThat(threadPoolBulkheadRegistry).isNotNull();
		assertThat(threadPoolBulkheadRegistry.find("test_circuit")).isPresent();
		assertThat(threadPoolBulkheadRegistry.find("test_circuit").get().getBulkheadConfig().getMaxThreadPoolSize())
				.isEqualTo(100);
	}

	@Test
	public void testBulkheadPropertiesPopulated() {
		BulkheadRegistry bulkheadRegistry = factory.getBulkheadProvider().getBulkheadRegistry();
		assertThat(bulkheadRegistry).isNotNull();
		assertThat(bulkheadRegistry.find("test_circuit")).isPresent();
		assertThat(bulkheadRegistry.find("test_circuit").get().getBulkheadConfig().getMaxConcurrentCalls())
				.isEqualTo(50);
	}

	@Test
	public void testDefaultCircuitBreakerPropertiesPopulated() {
		factory.create("default_circuitBreaker").run(() -> "result");
		CircuitBreakerRegistry circuitBreakerRegistry = factory.getCircuitBreakerRegistry();
		assertThat(circuitBreakerRegistry).isNotNull();
		assertThat(circuitBreakerRegistry.find("default_circuitBreaker")).isPresent();
		assertThat(circuitBreakerRegistry.find("default_circuitBreaker").get().getCircuitBreakerConfig()
				.getMinimumNumberOfCalls()).isEqualTo(20);
	}

	@Test
	public void testDefaultTimeLimiterPropertiesPopulated() {
		factory.create("default_circuitBreaker").run(() -> "result");
		TimeLimiterRegistry timeLimiterRegistry = factory.getTimeLimiterRegistry();
		assertThat(timeLimiterRegistry).isNotNull();
		assertThat(timeLimiterRegistry.find("default_circuitBreaker")).isPresent();
		assertThat(timeLimiterRegistry.find("default_circuitBreaker").get().getTimeLimiterConfig().getTimeoutDuration())
				.isEqualTo(Duration.ofMillis(150));
	}

	@Test
	public void testDefaultThreadPoolBulkheadPropertiesPopulated() {
		factory.create("default_circuitBreaker").run(() -> "result");
		ThreadPoolBulkheadRegistry threadPoolBulkheadRegistry = factory.getBulkheadProvider()
				.getThreadPoolBulkheadRegistry();
		assertThat(threadPoolBulkheadRegistry).isNotNull();
		assertThat(threadPoolBulkheadRegistry.find("default_circuitBreaker")).isPresent();
		assertThat(threadPoolBulkheadRegistry.find("default_circuitBreaker").get().getBulkheadConfig()
				.getMaxThreadPoolSize()).isEqualTo(50);
	}

	@Test
	public void testTestGroupCircuitBreakerPropertiesPopulated() {
		factory.create("a_in_test_group", "test_group").run(() -> "result");
		CircuitBreakerRegistry circuitBreakerRegistry = factory.getCircuitBreakerRegistry();
		Optional<CircuitBreaker> circuitBreaker = circuitBreakerRegistry.find("a_in_test_group");
		assertThat(circuitBreaker).isPresent();
		assertThat(circuitBreaker.get().getCircuitBreakerConfig().getMinimumNumberOfCalls())
				.isEqualTo(30);
	}

	@Test
	public void testTestGroupTimeLimiterPropertiesPopulated() {
		factory.create("a_in_test_group", "test_group").run(() -> "result");
		TimeLimiterRegistry timeLimiterRegistry = factory.getTimeLimiterRegistry();
		Optional<TimeLimiter> timeLimiter = timeLimiterRegistry.find("a_in_test_group");
		assertThat(timeLimiter).isPresent();
		assertThat(timeLimiter.get().getTimeLimiterConfig().getTimeoutDuration())
				.isEqualTo(Duration.ofMillis(500));
	}

	@Test
	public void testTestCircuitTimeLimiterPropertiesPopulated() {
		factory.create("test_circuit", "test_group").run(() -> "result");
		TimeLimiterRegistry timeLimiterRegistry = factory.getTimeLimiterRegistry();
		Optional<TimeLimiter> timeLimiter = timeLimiterRegistry.find("test_circuit");
		assertThat(timeLimiter).isPresent();
		assertThat(timeLimiter.get().getTimeLimiterConfig().getTimeoutDuration())
				.isEqualTo(Duration.ofSeconds(18));
	}


	@Test
	public void testTestCircuitCircuitBreakerPropertiesPopulated() {
		factory.create("test_circuit", "test_group").run(() -> "result");
		CircuitBreakerRegistry circuitBreakerRegistry = factory.getCircuitBreakerRegistry();
		Optional<CircuitBreaker> circuitBreaker = circuitBreakerRegistry.find("test_circuit");
		assertThat(circuitBreaker).isPresent();
		assertThat(circuitBreaker.get().getCircuitBreakerConfig().getMinimumNumberOfCalls())
				.isEqualTo(5);
	}


	@Test
	public void testTestGroupInstanceTimeLimiterPropertiesPopulated() {
		factory.create("a_in_test_group_instance", "test_group_instance").run(() -> "result");
		TimeLimiterRegistry timeLimiterRegistry = factory.getTimeLimiterRegistry();
		assertThat(timeLimiterRegistry.find("a_in_test_group_instance")).isNotPresent();
		Optional<TimeLimiter> timeLimiter = timeLimiterRegistry.find("test_group_instance");
		assertThat(timeLimiter).isPresent();
		assertThat(timeLimiter.get().getTimeLimiterConfig().getTimeoutDuration())
				.isEqualTo(Duration.ofMillis(600));
	}
}
