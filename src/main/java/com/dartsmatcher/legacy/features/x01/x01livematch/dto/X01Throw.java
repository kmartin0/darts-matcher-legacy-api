package com.dartsmatcher.legacy.features.x01.x01livematch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class X01Throw {

	private ObjectId matchId;

	@NotEmpty
	private String playerId;

	@Min(0)
	private int leg;

	@Min(0)
	private int set;

	@Min(0)
	private int round;

	@Min(0)
	@Max(180)
	private int score;

	@Min(0)
	@Max(3)
	private int dartsUsed;

	@Min(0)
	private int doublesMissed;

}
