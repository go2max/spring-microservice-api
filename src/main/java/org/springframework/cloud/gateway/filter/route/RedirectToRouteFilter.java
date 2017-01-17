package org.springframework.cloud.gateway.filter.route;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.WebFilter;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.isResponseCommitted;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.parse;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.setResponseStatus;

import reactor.core.publisher.Mono;

/**
 * @author Spencer Gibb
 */
public class RedirectToRouteFilter implements RouteFilter {
	@Override
	public WebFilter apply(String statusString, String[] args) {
		validate(args, 1);

		final HttpStatus httpStatus = parse(statusString);
		final URL url;
		try {
			url = URI.create(args[0]).toURL();
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("Invalid url " + args[0], e);
		}

		//TODO: caching can happen here
		return (exchange, chain) ->
			chain.filter(exchange).then(() -> {
				if (!isResponseCommitted(exchange)) {
					setResponseStatus(exchange, httpStatus);

					final ServerHttpResponse response = exchange.getResponse();
					response.getHeaders().set(HttpHeaders.LOCATION, url.toString());
					return response.setComplete();
				}
				return Mono.empty();
			});
	}

}
