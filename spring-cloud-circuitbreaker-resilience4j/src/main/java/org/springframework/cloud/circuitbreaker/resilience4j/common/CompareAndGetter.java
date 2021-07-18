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
import io.github.resilience4j.core.Registry;
import io.vavr.collection.Map;

import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;

/**
 * when using the config files to configure components like
 * 		{@link CircuitBreaker} etc. and then use the class
 * 		{@link Resilience4JCircuitBreakerFactory} to configure
 * 		these components will not take effect, because of the
 * 		logic to get components from registry depends on
 * 		[computeIfAbsent]
 *
 * this interface provide method compareAndGet:
 * 		1] find component from registry
 * 	    2] compare it's config with config that be configured by
 * 	    		{@link Resilience4JCircuitBreakerFactory}
 * 	    3] if not match, replace and return new one
 * 	    4] return if match
 * 	    5] register new one if absent
 *
 * @param <E> the element like {@link CircuitBreaker}, etc.
 * @param <R> the Registry for E
 * @param <C> the Config for E
 * @author dangzhicairang
 */
public interface CompareAndGetter<E, R extends Registry<E, C>, C> {

	default E compareAndGet(String id, R register, C config, io.vavr.collection.Map<String, String> tags) {
		return register.find(id)
			.map(e -> {
				if (compare(e, config)) {
					return e;
				}
				return replaceAndGet(id, register, config, tags);
			})
			.orElse(register(id, register, config, tags));
	}

	boolean compare(E e, C config);

	default E replaceAndGet(String id, R register, C config, Map<String, String> tags) {

		E newOne = get(id, register, config, tags);
		register.replace(id, newOne);
		return newOne;
	}

	E get(String id, R register, C config, io.vavr.collection.Map<String, String> tags);

	E register(String id, R register, C config, io.vavr.collection.Map<String, String> tags);
}
