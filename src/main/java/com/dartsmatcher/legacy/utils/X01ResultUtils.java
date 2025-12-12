package com.dartsmatcher.legacy.utils;

import com.dartsmatcher.legacy.features.basematch.ResultType;
import com.dartsmatcher.legacy.features.x01.x01match.models.X01Match;
import com.dartsmatcher.legacy.features.x01.x01match.models.leg.X01LegRound;
import com.dartsmatcher.legacy.features.x01.x01match.models.leg.X01LegRoundScore;
import com.dartsmatcher.legacy.features.x01.x01match.models.playerresult.X01PlayerResult;
import com.dartsmatcher.legacy.features.basematch.MatchPlayer;
import com.dartsmatcher.legacy.features.x01.x01match.models.leg.X01Leg;
import com.dartsmatcher.legacy.features.x01.x01match.models.set.X01Set;
import com.dartsmatcher.legacy.features.x01.x01match.models.set.X01SetPlayerResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class X01ResultUtils {

	public static void updateMatchResult(X01Match match) {
		if (match.getTimeline() != null) {
			match.getTimeline().forEach(set -> {
				X01ResultUtils.updateLegPlayerResults(match, set);
				X01ResultUtils.updateSetPlayerResults(match, set);
			});
		}

		X01ResultUtils.updateMatchPlayerResults(match);
	}

	public static void updateMatchResult(X01Match match, X01Set set) {
		X01ResultUtils.updateLegPlayerResults(match, set);
		X01ResultUtils.updateSetPlayerResults(match, set);
		X01ResultUtils.updateMatchPlayerResults(match);
	}

	/**
	 * Updates all leg results for a given set.
	 *
	 * @param match X01Match for which to update the leg results.
	 * @param set   X01Set set for which to update the leg results.
	 */
	public static void updateLegPlayerResults(X01Match match, X01Set set) {
		if (match == null || set == null) return;

		Map<Integer, String> legsWon = X01ResultUtils.getLegsWon(match, set);
		set.getLegs().forEach(x01Leg -> x01Leg.setWinner(legsWon.getOrDefault(x01Leg.getLeg(), null)));
	}

	/**
	 * Determines which players have won legs in a given set.
	 *
	 * @param match X01Match for which to determine which player won legs in.
	 * @param set   X01Set for which to determine which player won legs in.
	 * @return Map<Integer String> Containing the leg number as key and the player that won the leg as value.
	 */
	public static Map<Integer, String> getLegsWon(X01Match match, X01Set set) {
		Map<Integer, String> legsWon = new HashMap<>();

		if (set != null && match != null) {
			for (X01Leg leg : set.getLegs()) { // Iterate through the legs and find the players with no score remaining.
				HashMap<String, Integer> playersRemaining = new HashMap<>();
				for (X01LegRound legRound : leg.getRounds()) {
					for (X01LegRoundScore playerScore : legRound.getPlayerScores()) {
						int previousRemaining = playersRemaining.getOrDefault(playerScore.getPlayerId(), match.getX01MatchSettings().getX01());

						playersRemaining.put(playerScore.getPlayerId(), previousRemaining - playerScore.getScore());
					}
				}
				playersRemaining.forEach((playerId, remaining) -> {
					if (remaining <= 0) legsWon.put(leg.getLeg(), playerId);
				});

			}
		}

		return legsWon;
	}

	/**
	 * Updates result for a given set.
	 *
	 * @param match X01Match which the set is a part of.
	 * @param set   X01Set to update the result of.
	 */
	public static void updateSetPlayerResults(X01Match match, X01Set set) {
		if (match == null || set == null) return;

		// Find how many legs each player has won. Store playerId as key, number of legs won as value.
		Map<String, Integer> numOfLegsWon = match.getPlayers().stream()
				.collect(Collectors.toMap(MatchPlayer::getPlayerId, player -> 0));

		// Number of legs with a result.
		AtomicInteger legsPlayed = new AtomicInteger();

		// Iterate through the legs to fill numOfLegsWon and legsPlayed.
		set.getLegs().forEach(x01Leg -> {
			String legWinner = x01Leg.getWinner();
			if (legWinner != null && !legWinner.isEmpty()) {
				numOfLegsWon.put(x01Leg.getWinner(), numOfLegsWon.getOrDefault(x01Leg.getWinner(), 0) + 1);
				legsPlayed.getAndIncrement();
			}
		});

		// Number of legs still to play.
		int legsToGo = match.getX01MatchSettings().getBestOf().getLegs() - legsPlayed.get();

		// Construct a list of players that have won (in case of multiple winners it's a draw).
		ArrayList<String> setWinners = X01ResultUtils.getWinners(numOfLegsWon, legsToGo);

		// Construct and add the player results to the set.
		set.setResult(X01ResultUtils.createSetPlayerResults(numOfLegsWon, setWinners));
	}

	/**
	 * Updates result for a given match.
	 *
	 * @param match X01Match which the result has to be updated of.
	 */
	public static void updateMatchPlayerResults(X01Match match) {
		if (match == null) return;

		// Map of all match players and their total number of sets and legs won.
		Map<String, Integer> setsWon = match.getPlayers().stream()
				.collect(Collectors.toMap(MatchPlayer::getPlayerId, player -> 0));

		Map<String, Integer> legsWon = match.getPlayers().stream()
				.collect(Collectors.toMap(MatchPlayer::getPlayerId, player -> 0));

		// Reference to hold the match winners
		ArrayList<String> matchWinners = new ArrayList<>();

		// Number of sets with a result.
		AtomicInteger numPlayed = new AtomicInteger();

		if (match.getTimeline() != null) {
			// Iterate through the legs to fill numWon and setsPlayed.
			match.getTimeline().forEach(x01Set -> {
				boolean setHasWinner = false;

				for (X01SetPlayerResult setPlayerResult : x01Set.getResult()) {
					legsWon.put(setPlayerResult.getPlayerId(), legsWon.getOrDefault(setPlayerResult.getPlayerId(), 0) + setPlayerResult.getLegsWon());

					if (setPlayerResult.getResult() == ResultType.WIN || setPlayerResult.getResult() == ResultType.DRAW) {
						setsWon.put(setPlayerResult.getPlayerId(), setsWon.getOrDefault(setPlayerResult.getPlayerId(), 0) + 1);
						setHasWinner = true;
					}
				}
				if (setHasWinner) numPlayed.getAndIncrement();
			});

			// Number of sets still to play.
			int toGo = match.getX01MatchSettings().getBestOf().getSets() - numPlayed.get();

			// Construct a list of players that have won (in case of multiple winners it's a draw).
			matchWinners.addAll(X01ResultUtils.getWinners(setsWon, toGo));
		}

		// Construct and add the player results to the match.
		match.setX01Result(X01ResultUtils.createPlayerResults(setsWon, matchWinners, legsWon));
	}

	/**
	 * @param playerScores Map<String, Integer> Containing the playerId as key and their score as value.
	 * @param toGo         int The number of sets or legs that can still to be played.
	 * @return ArrayList<String> Containing the players that have won (multiple winners means a draw between these players).
	 */
	public static ArrayList<String> getWinners(Map<String, Integer> playerScores, int toGo) {
		ArrayList<String> winners = new ArrayList<>();
		if (playerScores == null || playerScores.isEmpty()) return winners;

		// The highest score from the playerScores.
		int mostWon = Collections.max(playerScores.entrySet(), Map.Entry.comparingByValue()).getValue();

		playerScores.forEach((player, score) -> {
			boolean hasWonOrDraw = false;

			if (score.equals(mostWon)) {
				if (toGo == 0) { // If there are no legs to be played the players having the most legs will have won or drew.
					hasWonOrDraw = true;
				} else if (playerScores.size() > 1) {
					// If no players can exceed or equalize the player with the most legs won. hasWon is true otherwise false.
					hasWonOrDraw = playerScores.entrySet().stream()
							.noneMatch(playerLegsWonEntry ->
									!playerLegsWonEntry.getKey().equals(player) &&
											playerLegsWonEntry.getValue() + toGo >= score);

				}
			}

			if (hasWonOrDraw) {
				winners.add(player);
			}
		});

		return winners;
	}

	/**
	 * Creates a X01SetPlayerResult for each player with their playerId, legs won and result.
	 *
	 * @param playersLegsWon Map<String, Integer> Containing the playerId as key and their number of legs won as value.
	 * @param winners        ArrayList<String> Containing the players that have won the set (multiple winners means a draw between these players).
	 * @return ArrayList<X01SetPlayerResult> Containing an X01SetPlayerResult for each of the players listed in the playerScores Map.
	 */
	public static ArrayList<X01SetPlayerResult> createSetPlayerResults(Map<String, Integer> playersLegsWon, ArrayList<String> winners) {
		ArrayList<X01SetPlayerResult> playerResults = new ArrayList<>();

		if (playersLegsWon == null || winners == null) return playerResults;

		// Iterate through each player score to determine their score and result. And add them to the playerResults.
		playersLegsWon.forEach((playerId, legsWon) -> {
			X01SetPlayerResult playerResult = new X01SetPlayerResult(
					playerId,
					legsWon,
					null
			);

			// If there are no winners then the result of each players stays null.
			if (!winners.isEmpty()) {
				if (winners.contains(playerId)) // When a player is the sole winner then he has won otherwise its a draw between all winners.
					playerResult.setResult(winners.size() > 1 ? ResultType.DRAW : ResultType.WIN);
				else
					playerResult.setResult(ResultType.LOSE); // When a player is not in the winners list then he has lost.
			}

			playerResults.add(playerResult);

		});

		return playerResults;
	}

	/**
	 * Creates a X01PlayerResult for each player with their playerId, score and result.
	 *
	 * @param playerSetsWon Map<String, Integer> Containing the playerId as key and their number of sets won as value.
	 * @param winners       ArrayList<String> Containing the players that have won the match (multiple winners means a draw between these players).
	 * @param playerLegsWon Map<String, Integer> Containing the playerId as key and their number of legs won as value.
	 * @return ArrayList<X01PlayerResult> Containing an X01PlayerResult for each of the players listed in the playerScores Map.
	 */
	public static ArrayList<X01PlayerResult> createPlayerResults(Map<String, Integer> playerSetsWon, ArrayList<String> winners, Map<String, Integer> playerLegsWon) {
		ArrayList<X01PlayerResult> playerResults = new ArrayList<>();

		if (playerSetsWon == null || winners == null || playerLegsWon == null) return playerResults;

		// Iterate through each player score to determine their score and result. And add them to the playerResults.
		playerSetsWon.forEach((playerId, setsWon) -> {
			X01PlayerResult playerResult = new X01PlayerResult(
					playerId,
					playerLegsWon.getOrDefault(playerId, 0),
					setsWon,
					null
			);

			// If there are no winners then the result of each players stays null.
			if (!winners.isEmpty()) {
				if (winners.contains(playerId)) // When a player is the sole winner then he has won otherwise its a draw between all winners.
					playerResult.setResult(winners.size() > 1 ? ResultType.DRAW : ResultType.WIN);
				else
					playerResult.setResult(ResultType.LOSE); // When a player is not in the winners list then he has lost.
			}

			playerResults.add(playerResult);

		});

		return playerResults;
	}
}
