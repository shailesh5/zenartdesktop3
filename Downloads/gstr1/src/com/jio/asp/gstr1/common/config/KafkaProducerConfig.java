package com.jio.asp.gstr1.common.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;


@Configuration
@EnableKafka
@PropertySource(value = {"${GSTR1_APP_PROP_PATH_EXT}"})
public class KafkaProducerConfig {
	
	@Autowired
	private Environment environment;
	
	@Bean
	public ProducerFactory<String, String> producerFactory() {
		return new DefaultKafkaProducerFactory<>(producerConfigs());
	}

	@Bean
	public Map<String, Object> producerConfigs() {
		Map<String, Object> props = new HashMap<>();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, environment.getProperty("kafka.server")+":"+environment.getProperty("kafka.port"));
		props.put(ProducerConfig.RETRIES_CONFIG, Integer.parseInt(environment.getProperty("kafka.retry")));
		props.put(ProducerConfig.BATCH_SIZE_CONFIG, Integer.parseInt(environment.getProperty("kafka.batch.size")));
		props.put(ProducerConfig.LINGER_MS_CONFIG, Integer.parseInt(environment.getProperty("kafka.linger.ms.config")));
		props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, Integer.parseInt(environment.getProperty("kafka.buffer.memory")));
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		return props;
	}

	@Bean
	public KafkaTemplate<String, String> kafkaTemplate() {
		return new KafkaTemplate<String, String>(producerFactory());
	}
}
