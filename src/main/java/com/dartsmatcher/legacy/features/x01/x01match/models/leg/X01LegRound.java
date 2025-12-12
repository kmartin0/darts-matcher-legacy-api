package com.dartsmatcher.legacy.features.x01.x01match.models.leg;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class X01LegRound {

	private int round;

	private ArrayList<X01LegRoundScore> playerScores;

	@JsonIgnore
	public Optional<X01LegRoundScore> getPlayerScore(String playerId) {
		return getPlayerScores().stream()
				.filter(legRoundScore -> Objects.equals(legRoundScore.getPlayerId(), playerId))
				.findFirst();
	}

}
