package com.anastasiaeverstova.myeduserver;

import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.jetbrains.annotations.NotNull;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.session.data.redis.config.ConfigureRedisAction;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.format.DateTimeFormatter;

@SpringBootApplication
@EnableCaching
public class MyeduserverApplication {

	@Value(value = "${frontend.root.url}")
	private String FRONTEND_URL;

	public static void main(String[] args) {
		SpringApplication.run(MyeduserverApplication.class, args);
	}

	@Bean
	public static ConfigureRedisAction configureRedisAction() {
		return ConfigureRedisAction.NO_OP;
	}

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(@NotNull CorsRegistry registry) {
				registry.addMapping("/**")
						.allowCredentials(true)
						.exposedHeaders("*")
						.maxAge(3600L)
						.allowedOriginPatterns("http://localhost:[*]", FRONTEND_URL)
						.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
			}
		};
	}

	@Bean
	public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperCustomizer() {
		return builder -> {
			DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

			builder.deserializers(new LocalDateDeserializer(df));
			builder.deserializers(new LocalDateTimeDeserializer(dtf));

			builder.serializers(new LocalDateSerializer(df));
			builder.serializers(new LocalDateTimeSerializer(dtf));
		};
	}

	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}
}
