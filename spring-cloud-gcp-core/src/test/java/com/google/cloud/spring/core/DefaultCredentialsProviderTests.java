/*
 * Copyright 2017-2019 the original author or authors.
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

package com.google.cloud.spring.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the {@link DefaultCredentialsProvider}.
 *
 * @author João André Martins
 * @author Mike Eltsufin
 * @author Chengyuan Zhao
 */
class DefaultCredentialsProviderTests {

	@Test
	void testResolveScopesDefaultScopes() {
		List<String> scopes = DefaultCredentialsProvider.resolveScopes(null);
		assertThat(scopes.size()).isGreaterThan(1);
		assertThat(scopes).contains(GcpScope.PUBSUB.getUrl());
	}

	@Test
	void testResolveScopesOverrideScopes() {
		List<String> scopes = DefaultCredentialsProvider.resolveScopes(Collections.singletonList("myscope"));
		assertThat(scopes)
				.hasSize(1)
				.contains("myscope");
	}

	@Test
	void testResolveScopesStarterScopesPlaceholder() {
		List<String> scopes = DefaultCredentialsProvider.resolveScopes(Arrays.asList("DEFAULT_SCOPES", "myscope"));
		assertThat(scopes)
				.hasSize(GcpScope.values().length + 1)
				.contains(GcpScope.PUBSUB.getUrl())
				.contains("myscope");
	}

}
