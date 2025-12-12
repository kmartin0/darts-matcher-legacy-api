package com.dartsmatcher.legacy.features.x01.x01match.models.leg;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class X01LegRoundScore {

	private String playerId;

	private int doublesMissed;

	@Max(3)
	@Min(1)
	private int dartsUsed;

	@Min(0)
	@Max(180)
	private int score;

}
