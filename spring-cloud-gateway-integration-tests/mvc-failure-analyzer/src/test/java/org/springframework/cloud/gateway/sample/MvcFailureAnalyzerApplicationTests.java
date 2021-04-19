/*
 * Copyright 2013-2019 the original author or authors.
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

package org.springframework.cloud.gateway.sample;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.cloud.gateway.support.MvcFoundOnClasspathException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Spencer Gibb
 */
@ExtendWith(OutputCaptureExtension.class)
public class MvcFailureAnalyzerApplicationTests {

	@Test
	public void contextLoads(CapturedOutput output) {
		assertThatThrownBy(
				() -> new SpringApplication(MvcFailureAnalyzerApplication.class)
						.run("--server.port=0")).hasRootCauseInstanceOf(
								MvcFoundOnClasspathException.class);
		assertThat(output).contains("Spring MVC found on classpath",
				"Please remove spring-boot-starter-web dependency");
	}

}
