package com.dartsmatcher.legacy.config;

import com.dartsmatcher.legacy.serializers.ObjectIdSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.bson.types.ObjectId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

@Configuration
public class JacksonConfig {

	@Bean
	public ObjectMapper jsonObjectMapper() {
		ArrayList<Module> modules = new ArrayList<>();

		//ObjectId
		SimpleModule objectIdModule = new SimpleModule();
		objectIdModule.addSerializer(ObjectId.class, new ObjectIdSerializer());
		modules.add(objectIdModule);

		// LocalDateTime
		SimpleModule localDateTimeModule = new SimpleModule();
		localDateTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
		localDateTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
		modules.add(localDateTimeModule);

		return Jackson2ObjectMapperBuilder.json()
				.modules(modules)
				.build();
	}
}
