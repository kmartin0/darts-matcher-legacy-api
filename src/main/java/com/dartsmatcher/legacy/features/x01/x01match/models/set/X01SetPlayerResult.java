package com.dartsmatcher.legacy.features.x01.x01match.models.set;

import com.dartsmatcher.legacy.features.basematch.ResultType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class X01SetPlayerResult {

	private String playerId;

	private int legsWon;

	private ResultType result;

}
