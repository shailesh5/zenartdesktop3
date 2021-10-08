package com.jio.asp.gstr1.common.config;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

import asp.crypto.utils.Cryptograph;


@Configuration
@EnableMongoRepositories(basePackages = "com.jio.asp")
@PropertySource(value = {"${GSTR1_APP_PROP_PATH_EXT}"})
public class MongoConfig {

	static Logger log = LoggerFactory.getLogger(MongoConfig.class);

	@Autowired
	private Environment environment;

	@Bean
	public MongoDbFactory mongoDbFactory() {

		String mongoPassword = Cryptograph.decrypt(environment.getProperty("mongo_password"),"");
		ServerAddress	serverAddress = new ServerAddress(environment.getProperty("mongo_host"),
				Integer.parseInt(environment.getProperty("mongo_port")));

		MongoCredential credential = MongoCredential.createCredential(environment.getProperty("mongo_username"),
				environment.getProperty("mongo_db_name"), mongoPassword.toCharArray());

		MongoClient mongoClient = new MongoClient(serverAddress, Arrays.asList(credential));

		//		MongoClient mongoClient = new MongoClient(serverAddress);
		MongoDbFactory mongoFact = new SimpleMongoDbFactory(mongoClient, environment.getProperty("mongo_db_name"));

		return mongoFact;
	}

	@Bean
	public MongoDbFactory mongoDbFactorySaveGstn() {
		String mongoPassword = Cryptograph.decrypt(environment.getProperty("mongo_password"),"");
		ServerAddress serverAddress = new ServerAddress(environment.getProperty("mongo_host"),
				Integer.parseInt(environment.getProperty("mongo_port")));
		MongoCredential credential = MongoCredential.createCredential(environment.getProperty("mongo_username"),
				environment.getProperty("mongo_db_name"), mongoPassword.toCharArray());
		MongoClient mongoClient = new MongoClient(serverAddress, Arrays.asList(credential));
		return new SimpleMongoDbFactory(mongoClient, environment.getProperty("mongo_db_name"));
	}

	@Bean
	public MongoTemplate mongoTemplate() throws Exception{
		MongoTemplate mongoTemplate = new MongoTemplate(mongoDbFactory());
		return mongoTemplate;
	}

	@Bean
	public MongoTemplate mongoGstnTemplate(){
		MongoTemplate mongoTemplate = new MongoTemplate(mongoDbFactorySaveGstn());
		return mongoTemplate;
	}


}
