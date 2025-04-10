package com.sqlengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import javax.sql.DataSource;

@EnableMongoRepositories
@SpringBootApplication(
		exclude = {
				DataSourceAutoConfiguration.class,
				org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration.class
		}
)
public class SqlQueryEngineApplication {

	public static void main(String[] args) {
		SpringApplication.run(SqlQueryEngineApplication.class, args);
	}

}
