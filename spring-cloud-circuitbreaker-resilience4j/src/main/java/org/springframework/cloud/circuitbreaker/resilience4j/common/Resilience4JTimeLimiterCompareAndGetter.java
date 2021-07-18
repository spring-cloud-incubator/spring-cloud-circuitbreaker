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

import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import io.vavr.collection.Map;

/**
 * implement for TimeLimiter.
 * @author dangzhicairang
 */
public class Resilience4JTimeLimiterCompareAndGetter
	implements CompareAndGetter<TimeLimiter, TimeLimiterRegistry, TimeLimiterConfig> {

	private static Resilience4JTimeLimiterCompareAndGetter instance = new Resilience4JTimeLimiterCompareAndGetter();

	public static Resilience4JTimeLimiterCompareAndGetter getInstance() {
		return instance;
	}

	@Override
	public boolean compare(TimeLimiter timeLimiter, TimeLimiterConfig config) {
		TimeLimiterConfig oldConfig = timeLimiter.getTimeLimiterConfig();
		return oldConfig.shouldCancelRunningFuture() == config.shouldCancelRunningFuture()
			&& oldConfig.getTimeoutDuration().equals(config.getTimeoutDuration());
	}

	@Override
	public TimeLimiter get(String id, TimeLimiterRegistry register, TimeLimiterConfig config, Map<String, String> tags) {

		return TimeLimiter.of(id, config, tags);
	}

	@Override
	public TimeLimiter register(String id, TimeLimiterRegistry register, TimeLimiterConfig config, Map<String, String> tags) {

		return register.timeLimiter(id, config, tags);
	}
}
