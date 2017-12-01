package org.springframework.cloud.gateway.sample

import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.cloud.gateway.route.builder.filters
import org.springframework.cloud.gateway.route.builder.routes
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AdditionalRoutes {

	@Bean
	fun additionalRouteLocator(builder: RouteLocatorBuilder): RouteLocator = builder.routes {
		route(id = "test-kotlin") {
			host("kotlin.abc.org") and path("/image/png")
			filters {
				addResponseHeader("X-TestHeader", "foobar")
			}
			uri("http://httpbin.org:80")
		}
	}

}