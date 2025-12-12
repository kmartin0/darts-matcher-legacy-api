package com.dartsmatcher.legacy.features.x01.x01match.models.leg;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Optional;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class X01Leg {

	@Min(1)
	private int leg;

	private String winner;

	private String throwsFirst;

	@Valid
	@NotNull
	private ArrayList<X01LegRound> rounds;

	@JsonIgnore
	public Optional<X01LegRound> getRound(int roundNumber) {
		if (getRounds() == null) return Optional.empty();

		return getRounds().stream()
				.filter(round -> round.getRound() == roundNumber)
				.findFirst();
	}

	public int getScored(String playerId) {
		if (getRounds() == null) return 0;

		return getRounds().stream()
				.flatMap(x01LegRound -> x01LegRound.getPlayerScores().stream())
				.filter(x01LegRoundScore -> x01LegRoundScore.getPlayerId().equals(playerId))
				.mapToInt(X01LegRoundScore::getScore).sum();
	}

	public int getDartsUsed(String playerId) {
		if (getRounds() == null) return 0;

		return getRounds().stream()
				.flatMap(x01LegRound -> x01LegRound.getPlayerScores().stream())
				.filter(x01LegRoundScore -> x01LegRoundScore.getPlayerId().equals(playerId))
				.mapToInt(X01LegRoundScore::getDartsUsed).sum();
	}

}
