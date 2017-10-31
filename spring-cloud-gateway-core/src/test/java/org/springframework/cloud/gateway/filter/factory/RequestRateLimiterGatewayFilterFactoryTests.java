package org.springframework.cloud.gateway.filter.factory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter.Response;
import org.springframework.cloud.gateway.test.BaseWebClientTests;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.tuple.Tuple;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter.BURST_CAPACITY_KEY;
import static org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter.REPLENISH_RATE_KEY;
import static org.springframework.tuple.TupleBuilder.tuple;

import reactor.core.publisher.Mono;

/**
 * see https://gist.github.com/ptarjan/e38f45f2dfe601419ca3af937fff574d#file-1-check_request_rate_limiter-rb-L36-L62
 * @author Spencer Gibb
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@DirtiesContext
public class RequestRateLimiterGatewayFilterFactoryTests extends BaseWebClientTests {

	@Autowired
	private RequestRateLimiterGatewayFilterFactory filterFactory;

	@MockBean
	private RateLimiter rateLimiter;

	@MockBean
	private GatewayFilterChain filterChain;

	@Autowired
	@Qualifier("resolver1")
	KeyResolver resolver1;

	@Autowired
	@Qualifier("resolver2")
	KeyResolver resolver2;

	@Test
	public void allowedWorks() throws Exception {
		assertFilterFactory(resolver1, "allowedkey", true, HttpStatus.OK);
	}

	@Test
	public void notAllowedWorks() throws Exception {
		assertFilterFactory(resolver2, "notallowedkey", false, HttpStatus.TOO_MANY_REQUESTS);
	}

	private void assertFilterFactory(KeyResolver keyResolver, String key, boolean allowed, HttpStatus expectedStatus) {
		int replenishRate = 10;
		int burstCapacity = 2 * replenishRate;

		Tuple args = tuple().of(REPLENISH_RATE_KEY, replenishRate, BURST_CAPACITY_KEY, burstCapacity);
		when(rateLimiter.isAllowed(key, args))
				.thenReturn(Mono.just(new Response(allowed, 1)));


		MockServerHttpRequest request = MockServerHttpRequest.get("/").build();
		MockServerWebExchange exchange = MockServerWebExchange.from(request);
		exchange.getResponse().setStatusCode(HttpStatus.OK);

		when(this.filterChain.filter(exchange)).thenReturn(Mono.empty());

		Mono<Void> response = filterFactory.apply(args).filter(exchange, this.filterChain);
		response.subscribe(aVoid -> assertThat(exchange.getResponse().getStatusCode())
				.isEqualTo(expectedStatus));

	}

	@EnableAutoConfiguration
	@SpringBootConfiguration
	@Import(BaseWebClientTests.DefaultTestConfig.class)
	public static class TestConfig {
		@Bean
		KeyResolver resolver1() {
			return exchange -> Mono.just("allowedkey");
		}

		@Bean
		KeyResolver resolver2() {
			return exchange -> Mono.just("notallowedkey");
		}
	}
}
