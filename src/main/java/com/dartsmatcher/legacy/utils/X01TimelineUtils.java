package com.dartsmatcher.legacy.utils;

import com.dartsmatcher.legacy.features.basematch.MatchPlayer;
import com.dartsmatcher.legacy.features.x01.x01match.models.X01Match;
import com.dartsmatcher.legacy.features.x01.x01match.models.leg.X01Leg;
import com.dartsmatcher.legacy.features.x01.x01match.models.leg.X01LegRound;
import com.dartsmatcher.legacy.features.x01.x01match.models.set.X01Set;
import com.dartsmatcher.legacy.features.x01.x01match.models.set.X01SetPlayerResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;

public class X01TimelineUtils {

	public static void updateTimeline(X01Match x01Match) {
		X01TimelineUtils.updateTimelineInPlay(x01Match);
		X01TimelineUtils.updateTimelineOrder(x01Match);
	}

	private static void updateTimelineInPlay(X01Match x01Match) {
		if (x01Match.getX01Result().stream().anyMatch(x01PlayerResult -> x01PlayerResult.getResult() != null)) return;

		// When there are no players, the timeline should be empty.
		if (x01Match.getPlayers().isEmpty()) {
			x01Match.setTimeline(new ArrayList<>());

			return;
		}

		// If the timeline is empty, create one.
		if (x01Match.getTimeline() == null || x01Match.getTimeline().isEmpty()) {
			x01Match.setTimeline(new ArrayList<>(Collections.singletonList(createSet(x01Match.getPlayers(), 1))));

			return;
		}

		// If all sets are finished, create a new one.
		Optional<X01Set> setInPlay = x01Match.getTimeline().stream().filter(set -> set.getResult().stream().anyMatch(x01PlayerResult -> x01PlayerResult.getResult() == null)).findFirst();
		if (!setInPlay.isPresent()) {
			int lastSet = x01Match.getTimeline().stream().mapToInt(X01Set::getSet).max().orElse(0);
			x01Match.getTimeline().add(createSet(x01Match.getPlayers(), lastSet + 1));

			return;
		}

		// If all legs in the set in play are finished, create a new one.
		Optional<X01Leg> legInPlay = setInPlay.get().getLegs().stream().filter(x01Leg -> x01Leg.getWinner() == null).findFirst();
		if (!legInPlay.isPresent()) {
			int lastLeg = setInPlay.get().getLegs().stream().mapToInt(X01Leg::getLeg).max().orElse(0);
			setInPlay.get().getLegs().add(createLeg(lastLeg + 1));

			return;
		}

		// If all rounds in a leg are finished, create a new one.
		Optional<X01LegRound> roundInPlay = legInPlay.get().getRounds().stream().filter(x01LegRound -> x01LegRound.getPlayerScores().size() < x01Match.getPlayers().size()).findFirst();
		if (!roundInPlay.isPresent()) {
			int lastRound = legInPlay.get().getRounds().stream().mapToInt(X01LegRound::getRound).max().orElse(0);
			legInPlay.get().getRounds().add(createLegRound(lastRound + 1));
		}
	}

	private static void updateTimelineOrder(X01Match x01Match) {

		// Sort the sets.
		x01Match.getTimeline().sort(Comparator.comparing(X01Set::getSet));

		// Make sure there are no gaps in the sets.
		for (int i = 0; i < x01Match.getTimeline().size(); i++) {
			x01Match.getTimeline().get(i).setSet(i + 1);
		}

		x01Match.getTimeline().forEach(x01Set -> {
			// Sort the legs.
			x01Set.getLegs().sort(Comparator.comparing(X01Leg::getLeg));

			// Make sure there are no gaps in the legs.
			for (int i = 0; i < x01Set.getLegs().size(); i++) {
				x01Set.getLegs().get(i).setLeg(i + 1);
			}

			x01Set.getLegs().forEach(x01Leg -> {
				// Sort the rounds.
				x01Leg.getRounds().sort(Comparator.comparing(X01LegRound::getRound));

				// Make sure there are no gaps in the rounds.
				for (int i = 0; i < x01Leg.getRounds().size(); i++) {
					x01Leg.getRounds().get(i).setRound(i + 1);
				}
			});
		});
	}

	public static X01LegRound createLegRound(int roundNumber) {
		return new X01LegRound(roundNumber, new ArrayList<>());
	}

	public static X01Leg createLeg(int legNumber) {
		return new X01Leg(legNumber, null, null, new ArrayList<>(Collections.singletonList(createLegRound(1))));
	}

	public static X01Set createSet(ArrayList<MatchPlayer> players, int setNumber) {
		ArrayList<X01SetPlayerResult> playerResults = players.stream()
				.map(matchPlayer -> new X01SetPlayerResult(matchPlayer.getPlayerId(), 0, null))
				.collect(Collectors.toCollection(ArrayList::new));

		return new X01Set(setNumber, playerResults, new ArrayList<>(Collections.singletonList(createLeg(1))));
	}

}
