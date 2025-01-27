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

package com.example;

import com.google.cloud.spanner.DatabaseId;
import com.google.cloud.spanner.SpannerOptions;
import com.google.cloud.spring.data.spanner.core.admin.DatabaseIdProvider;
import com.google.cloud.spring.data.spanner.core.admin.SpannerDatabaseAdminTemplate;
import com.google.cloud.spring.data.spanner.core.admin.SpannerSchemaUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assume.assumeThat;

/**
 * Tests for the Spanner repository example using multiple databases.
 *
 * @author Chengyuan Zhao
 */
@RunWith(SpringRunner.class)
@TestPropertySource("classpath:application-test.properties")
@EnableAutoConfiguration
public class SpannerRepositoryMultiDatabaseIntegrationTests {
	@Autowired
	private TraderRepository traderRepository;

	@Autowired
	private SpannerSchemaUtils spannerSchemaUtils;

	@Autowired
	private SpannerDatabaseAdminTemplate spannerDatabaseAdminTemplate;

	@BeforeClass
	public static void checkToRun() {
		assumeThat(
				"Spanner integration tests are disabled. "
						+ "Please use '-Dit.spanner=true' to enable them. ",
				System.getProperty("it.spanner"), is("true"));
	}

	@Before
	@After
	public void setUp() {
		createTable();
		Config.flipDatabase();
		createTable();
		this.traderRepository.deleteAll();
	}

	private void createTable() {
		if (!this.spannerDatabaseAdminTemplate.tableExists("traders_repository")) {
			this.spannerDatabaseAdminTemplate.executeDdlStrings(
					this.spannerSchemaUtils.getCreateTableDdlStringsForInterleavedHierarchy(Trader.class),
					true);
		}
	}

	@Test
	public void testLoadsCorrectData() {
		assertThat(this.traderRepository.count()).isZero();
		Config.flipDatabase();
		assertThat(this.traderRepository.count()).isZero();

		this.traderRepository.save(new Trader("1", "a", "al"));
		Config.flipDatabase();
		this.traderRepository.save(new Trader("2", "a", "al"));
		Config.flipDatabase();
		this.traderRepository.save(new Trader("3", "a", "al"));

		assertThat(this.traderRepository.count()).isEqualTo(2);
		Config.flipDatabase();
		assertThat(this.traderRepository.count()).isEqualTo(1);
	}

	/**
	 * Configuring custom multiple database connections.
	 *
	 * @author Chengyuan Zhao
	 */
	@Configuration
	static class Config {

		static boolean databaseFlipper;

		@Value("${spring.cloud.gcp.spanner.instance-id}")
		String instanceId;

		/**
		 * Flips the database connection that all repositories and templates use.
		 */
		static void flipDatabase() {
			databaseFlipper = !databaseFlipper;
		}

		@Bean
		public DatabaseIdProvider databaseIdProvider(SpannerOptions spannerOptions) {
			return () -> DatabaseId.of(spannerOptions.getProjectId(), this.instanceId,
					databaseFlipper ? "db1" : "db2");
		}
	}

}
