package org.springframework.cloud.gateway.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.api.CachingRouteReader;
import org.springframework.cloud.gateway.api.CompositeRouteReader;
import org.springframework.cloud.gateway.api.RouteReader;
import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.cloud.gateway.config.PropertiesRouteReader;
import org.springframework.cloud.gateway.discovery.DiscoveryClientRouteReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import reactor.core.publisher.Flux;

@SpringBootConfiguration
@EnableAutoConfiguration
public class GatewayTestApplication {

	/*
	TO test run `spring cloud configserver eureka`,
	then run this app with `--spring.profiles.active=discovery`
	should be able to hit http://localhost:8008/configserver/foo/default a normal configserver api
	 */
	@Configuration
	@EnableDiscoveryClient
	@Profile("discovery")
	protected static class GatewayDiscoveryConfiguration {

		@Bean
		public DiscoveryClientRouteReader discoveryClientRouteReader(DiscoveryClient discoveryClient) {
			return new DiscoveryClientRouteReader(discoveryClient);
		}

		@Bean
		public PropertiesRouteReader propertiesRouteReader(GatewayProperties properties) {
			return new PropertiesRouteReader(properties);
		}

		@Bean
		@Primary
		public RouteReader compositeRouteReader(DiscoveryClientRouteReader discoveryClientRouteReader,
												PropertiesRouteReader propertiesRouteReader) {
			final Flux<RouteReader> flux = Flux.just(discoveryClientRouteReader, propertiesRouteReader);
			final CompositeRouteReader composite = new CompositeRouteReader(flux);
			return new CachingRouteReader(composite);
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(GatewayTestApplication.class, args);
	}
}
