package com.dartsmatcher.dartsmatcherapi.utils;


import com.dartsmatcher.legacy.config.JacksonConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration
@Import({JacksonConfig.class})
public class ObjectIdSerializerTests {

	@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void testSerializeObjectId_returnsObjectIdHexStringRepresentation() throws JsonProcessingException {
		String oid = "602a487d06f4948729fe4269";
		ObjectId objectId = new ObjectId(oid);

		Assertions.assertEquals("\"" + oid + "\"", objectMapper.writeValueAsString(objectId));
	}
}
