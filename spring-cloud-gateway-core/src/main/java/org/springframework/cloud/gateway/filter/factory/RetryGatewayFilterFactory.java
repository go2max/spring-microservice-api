/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.springframework.cloud.gateway.filter.factory;

import java.util.function.Predicate;

import org.springframework.http.HttpMethod;
import reactor.retry.DefaultRepeat;
import reactor.retry.Repeat;
import reactor.retry.RepeatContext;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.tuple.Tuple;
import org.springframework.web.server.ServerWebExchange;

public class RetryGatewayFilterFactory implements GatewayFilterFactory {
	@Override
	public GatewayFilter apply(Tuple args) {
		return (exchange, chain) -> {
			Predicate<? super RepeatContext<Object>> predicate = context -> {
				ServerWebExchange ex = (ServerWebExchange) context.applicationContext();
				boolean retryableStatusCode = ex.getResponse().getStatusCode().is5xxServerError();
				boolean retryableMethod = ex.getRequest().getMethod().equals(HttpMethod.GET);
				return retryableMethod && retryableStatusCode;
			};
			Repeat<Object> repeat = DefaultRepeat.create(predicate, 4)
					.withApplicationContext(exchange);
			return chain.filter(exchange).repeatWhen(repeat).next();
		};
	}
}
