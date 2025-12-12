package com.dartsmatcher.legacy.features.x01.x01Dartbot;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class X01DartBotThrow {

	private String dartBotId;

	private ObjectId matchId;

	private int set;

	private int leg;

	private int round;

}
