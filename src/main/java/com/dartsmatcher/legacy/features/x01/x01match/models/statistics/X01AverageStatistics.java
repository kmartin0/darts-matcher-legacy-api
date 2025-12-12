package com.dartsmatcher.legacy.features.x01.x01match.models.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class X01AverageStatistics {

	private int pointsThrown;

	private int dartsThrown;

	private int average;

	private int pointsThrownFirstNine;

	private int dartsThrownFirstNine;

	private int averageFirstNine;

}
