package com.dartsmatcher.legacy.features.x01.x01match.models.statistics;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class X01PlayerStatistics {

	@NotNull
	private String playerId;

	private X01AverageStatistics averageStats;

	private X01CheckoutStatistics checkoutStats;

	private X01ScoresStatistics scoresStats;

}
