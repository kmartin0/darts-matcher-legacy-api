package com.dartsmatcher.legacy.utils;

import com.dartsmatcher.legacy.features.basematch.MatchPlayer;
import com.dartsmatcher.legacy.features.x01.x01match.models.X01Match;
import com.dartsmatcher.legacy.features.x01.x01match.models.leg.X01Leg;
import com.dartsmatcher.legacy.features.x01.x01match.models.leg.X01LegRound;
import com.dartsmatcher.legacy.features.x01.x01match.models.leg.X01LegRoundScore;
import com.dartsmatcher.legacy.features.x01.x01match.models.set.X01Set;

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

public class X01ThrowerUtils {

	public static void updateThrower(X01Match match) {
		X01ThrowerUtils.updateCurrentThrower(match);
		X01ThrowerUtils.updateThrowsFirst(match);
	}

	public static void updateThrowsFirst(X01Match match) {
		if (match == null || match.getTimeline() == null) return;

		match.getTimeline().forEach(x01Set -> {
			if (x01Set.getLegs() != null) {
				ArrayList<String> orderOfPlayInSet = createSetOrderOfPlay(match, x01Set);

				x01Set.getLegs().forEach(x01Leg -> {
					ArrayList<String> orderOfPlayInLeg = createLegOrderOfPlay(match.getPlayers().size(), orderOfPlayInSet, x01Leg, x01Set);

					x01Leg.setThrowsFirst(orderOfPlayInLeg.get(0));
				});
			}
		});
	}

	public static void updateCurrentThrower(X01Match match) {
		if (match == null) return;

		if (match.getTimeline() == null || match.getTimeline().isEmpty()) {
			match.setCurrentThrower(match.getPlayers().isEmpty() ? null : match.getPlayers().get(0).getPlayerId());
			return;
		}

		boolean matchHasResult = match.getX01Result() != null && match.getX01Result().stream().anyMatch(x01PlayerResult -> x01PlayerResult.getResult() != null);
		if (matchHasResult) {
			match.setCurrentThrower(null);
			return;
		}

		Optional<X01Set> currentSet = match.getTimeline().stream()
				.filter(x01Set -> {
					if (x01Set.getResult() == null) return true;
					else
						return x01Set.getResult().stream().anyMatch(x01PlayerResult -> x01PlayerResult.getResult() == null);
				})
				.findFirst();

		ArrayList<String> orderOfPlayInSet = createSetOrderOfPlay(match, currentSet.orElse(null));

		if (!currentSet.isPresent()) {
			match.setCurrentThrower(orderOfPlayInSet.get(0));
			return;
		}

		Optional<X01Leg> currentLeg = currentSet.get().getLegs().stream()
				.filter(x01Leg -> x01Leg.getWinner() == null)
				.findFirst();

		ArrayList<String> orderOfPlayInLeg = createLegOrderOfPlay(match.getPlayers().size(), orderOfPlayInSet, currentLeg.orElse(null), currentSet.get());

		match.setCurrentThrower(getCurrentThrowerInLeg(match, currentLeg.orElse(null), orderOfPlayInLeg));
	}

	public static String getCurrentThrowerInLeg(X01Match match, X01Leg leg, ArrayList<String> orderOfPlayInLeg) {
		if (leg == null) {
			return orderOfPlayInLeg.get(0);
		}

		X01LegRound x01LegRound = leg.getRounds().stream()
				.filter(_x01LegRound -> _x01LegRound.getPlayerScores().size() < match.getPlayers().size())
				.findFirst()
				.orElse(null);

		if (x01LegRound == null) {
			return orderOfPlayInLeg.get(0);
		} else {
			for (String playerId : orderOfPlayInLeg) {
				Optional<X01LegRoundScore> playerScore = x01LegRound.getPlayerScore(playerId);
				if (!playerScore.isPresent()) {
					return playerId;
				}
			}
		}

		return orderOfPlayInLeg.get(0);
	}

	public static ArrayList<String> createLegOrderOfPlay(int numOfPlayers, ArrayList<String> setOrderOfPlay, X01Leg leg, X01Set set) {
		if (leg == null) {
			int legsPlayed = (set != null && set.getLegs() != null)
					? set.getLegs().stream().mapToInt(X01Leg::getLeg).max().orElse(0)
					: 0;

			int throwsFirstInLeg = legsPlayed % numOfPlayers;

			return createOrderOfPlay(setOrderOfPlay, throwsFirstInLeg);
		}

		int throwsFirstInLeg = (leg.getLeg() - 1) % numOfPlayers;

		return createOrderOfPlay(setOrderOfPlay, throwsFirstInLeg);
	}

	public static ArrayList<String> createSetOrderOfPlay(X01Match match, X01Set set) {

		ArrayList<String> matchOrderOfPlay = match.getPlayers().stream()
				.map(MatchPlayer::getPlayerId)
				.collect(Collectors.toCollection(ArrayList::new));

		int throwsFirstInSet = 0;

		if (match.getPlayers().size() > 0) {
			if (set == null) {
				int setsPlayed = match.getTimeline().stream().mapToInt(X01Set::getSet).max().orElse(0);
				throwsFirstInSet = setsPlayed % match.getPlayers().size();
			} else {
				throwsFirstInSet = (set.getSet() - 1) % match.getPlayers().size();
			}
		}

		return createOrderOfPlay(matchOrderOfPlay, throwsFirstInSet);
	}

	public static ArrayList<String> createOrderOfPlay(ArrayList<String> initialOrder, int firstToThrow) {
		ArrayList<String> newOrderOfPlay = new ArrayList<>();
		if (initialOrder.size() == 1) return initialOrder;

		newOrderOfPlay.addAll(initialOrder.subList(firstToThrow, initialOrder.size()));
		newOrderOfPlay.addAll(initialOrder.subList(0, firstToThrow));

		return newOrderOfPlay;
	}


}
