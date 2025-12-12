package com.dartsmatcher.legacy.features.x01.x01match;

import com.dartsmatcher.legacy.exceptionhandler.exception.InvalidArgumentsException;
import com.dartsmatcher.legacy.exceptionhandler.exception.ResourceNotFoundException;
import com.dartsmatcher.legacy.exceptionhandler.response.TargetError;
import com.dartsmatcher.legacy.features.basematch.MatchPlayerInviteStatusEnum;
import com.dartsmatcher.legacy.features.basematch.MatchPlayer;
import com.dartsmatcher.legacy.features.basematch.MatchStatus;
import com.dartsmatcher.legacy.features.basematch.PlayerType;
import com.dartsmatcher.legacy.features.user.IUserService;
import com.dartsmatcher.legacy.features.user.User;
import com.dartsmatcher.legacy.features.x01.x01checkout.IX01CheckoutService;
import com.dartsmatcher.legacy.features.x01.x01livematch.dto.X01DeleteLeg;
import com.dartsmatcher.legacy.features.x01.x01livematch.dto.X01DeleteSet;
import com.dartsmatcher.legacy.features.x01.x01livematch.dto.X01DeleteThrow;
import com.dartsmatcher.legacy.features.x01.x01livematch.dto.X01Throw;
import com.dartsmatcher.legacy.features.x01.x01match.models.X01Match;
import com.dartsmatcher.legacy.features.x01.x01match.models.checkout.X01Checkout;
import com.dartsmatcher.legacy.features.x01.x01match.models.leg.X01Leg;
import com.dartsmatcher.legacy.features.x01.x01match.models.leg.X01LegRound;
import com.dartsmatcher.legacy.features.x01.x01match.models.leg.X01LegRoundScore;
import com.dartsmatcher.legacy.features.x01.x01match.models.set.X01Set;
import com.dartsmatcher.legacy.utils.MessageResolver;
import com.dartsmatcher.legacy.utils.X01TimelineUtils;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

// TODO: Implement MatchLobby's and create CreateMatchDto so matches can be made using a room.
// TODO: Remove redundant methods (saveMatch, updateMatch) after implementing lobby's
// TODO: Add soft deletes for matches (so registered users can effectively only hide a match)
// TODO: Error Handling
// TODO: MatchRoomServiceImpl.saveMatchRoom(X01MatchRoom)
// TODO: saveMatch(X01MatchRoom)
@Service
public class X01MatchServiceImpl implements IX01MatchService {

	private final X01MatchRepository x01MatchRepository;

	private final IUserService userService;

	private final IX01CheckoutService checkoutService;

	private final MessageResolver messageResolver;

	public X01MatchServiceImpl(X01MatchRepository x01MatchRepository, IUserService userService, IX01CheckoutService checkoutService, MessageResolver messageResolver) {
		this.x01MatchRepository = x01MatchRepository;
		this.userService = userService;
		this.checkoutService = checkoutService;
		this.messageResolver = messageResolver;
	}

	@Override
	public X01Match saveMatch(@Valid X01Match x01Match) {
		// Prevent users from assigning an id.
		x01Match.setId(null);

		// Check if players exist and fill their information.
		for (MatchPlayer player : x01Match.getPlayers()) {
			if (player.getPlayerType() == PlayerType.REGISTERED) {
				User user = userService.getUser(new ObjectId(player.getPlayerId()));
				player.setUserName(user.getUserName());
				player.setFirstName(user.getFirstName());
				player.setLastName(user.getLastName());
			}
		}

		// Add statistics and result.
		x01Match.updateAll();

		return x01MatchRepository.save(x01Match);
	}

	@Override
	public X01Match getMatch(@NotNull ObjectId matchId) {
		// Find the match.
		return x01MatchRepository.findById(matchId).orElseThrow(() ->
				new ResourceNotFoundException(X01Match.class, matchId));
	}

	@Override
	public X01Match startMatch(ObjectId x01MatchId) {
		X01Match match = getMatch(x01MatchId);

		match.setMatchStatus(MatchStatus.IN_PLAY);

		ArrayList<MatchPlayer> playersToRemove = new ArrayList<>();

		// Remove the match players that haven't accepted their invite.
		match.getPlayers().forEach(matchPlayer -> {
			if (!matchPlayer.getInviteStatus().equals(MatchPlayerInviteStatusEnum.ACCEPTED)) {
				match.getX01Result().removeIf(x01PlayerResult -> x01PlayerResult.getPlayerId().equals(matchPlayer.getPlayerId()));
				match.getStatistics().removeIf(x01PlayerStatistics -> x01PlayerStatistics.getPlayerId().equals(matchPlayer.getPlayerId()));
				match.getTimeline().stream()
						.flatMap(x01Set -> {
							x01Set.getResult().removeIf(x01SetPlayerResult -> x01SetPlayerResult.getPlayerId().equals(matchPlayer.getPlayerId()));
							return x01Set.getLegs().stream();
						})
						.flatMap(x01Leg -> x01Leg.getRounds().stream())
						.forEach(x01LegRound -> x01LegRound.getPlayerScores().removeIf(x01LegRoundScore -> x01LegRoundScore.getPlayerId().equals(matchPlayer.getPlayerId())));

				playersToRemove.add(matchPlayer);
			}
		});

		match.getPlayers().removeAll(playersToRemove);
		match.updateAll();

		return x01MatchRepository.save(match);
	}

	@Override
	public X01Match updateMatch(@Valid X01Match x01Match, @NotNull ObjectId matchId) {
		// Check if match exists.
		X01Match matchToUpdate = getMatch(matchId);

		// Set id to new match object.
		x01Match.setId(matchToUpdate.getId());

		// Check if players exist and fill their information.
		for (MatchPlayer player : x01Match.getPlayers()) {
			if (player.getPlayerType() == PlayerType.REGISTERED) {
				User user = userService.getUser(new ObjectId(player.getPlayerId()));
				player.setUserName(user.getUserName());
				player.setFirstName(user.getFirstName());
				player.setLastName(user.getLastName());
			}
		}

		// Add statistics.
		x01Match.updateAll();

		return x01MatchRepository.save(x01Match);
	}

	@Override
	public X01Match updateMatch(@Valid X01Throw x01Throw) throws ResourceNotFoundException, IOException, InvalidArgumentsException {
		//TODO: Change to addThrowLiveMatch and use matchService.updateMatch

		X01Match match = getMatch(x01Throw.getMatchId());

		// Check if the throwing player is in the match.
		match.getPlayers().stream()
				.filter(_player -> Objects.equals(_player.getPlayerId().toLowerCase(), x01Throw.getPlayerId().toLowerCase()))
				.findFirst()
				.orElseThrow(() -> new ResourceNotFoundException(MatchPlayer.class, x01Throw.getPlayerId()));

		// If there is no timeline, create one.
		if (match.getTimeline() == null) {
			match.setTimeline(new ArrayList<>());
		}

		// Get the set, a new set is created if it doesn't exist yet.
		X01Set set = getOrAddSet(match, x01Throw.getSet());

		// Get the leg, a new leg is created if it doesn't exist yet.
		X01Leg leg = getOrAddLeg(set, x01Throw.getLeg());
		checkLegEditable(leg, x01Throw.getPlayerId());

		// Get the leg round, a new leg round is created if it doesn't exist yet.
		X01LegRound legRound = getOrAddLegRound(leg, x01Throw.getRound());

		// TODO: If it is a dart bot throw update the X01Throw with a the dart bot turn.
//		dartBotService.dartBotThrow()

		// Add the throw to the leg round.
		playerAddThrow(leg, legRound, x01Throw, match.getX01MatchSettings().getX01());

		// Update Statistics and Result of the match.
		match.updateAll(set.getSet());

		// Save and return the updated match.
		return x01MatchRepository.save(match);
	}

	@Override
	public X01Match updateMatch(@Valid X01DeleteLeg x01DeleteLeg) {
		X01Match match = getMatch(x01DeleteLeg.getMatchId());

		// TODO: Verify PlayerId so that not everyone can simply delete a leg.

		// Delete the queried throw.
		match.getSet(x01DeleteLeg.getSet()).ifPresent(x01Set -> x01Set.getLeg(x01DeleteLeg.getLeg())
				.ifPresent(x01Leg -> x01Set.getLegs().remove(x01Leg)));

		// Update Statistics and Result of the match.
		match.updateAll();

		return x01MatchRepository.save(match);
	}

	@Override
	public X01Match updateMatch(@Valid X01DeleteSet x01DeleteSet) {
		X01Match match = getMatch(x01DeleteSet.getMatchId());

		// TODO: Verify PlayerId so that not everyone can simply delete a set.

		// Delete the queried throw.
		match.getSet(x01DeleteSet.getSet()).ifPresent(set -> match.getTimeline().remove(set));

		// Update Statistics and Result of the match.
		match.updateAll();

		return x01MatchRepository.save(match);
	}

	@Override
	public X01Match updateMatch(@Valid X01DeleteThrow x01DeleteThrow) {
		X01Match match = getMatch(x01DeleteThrow.getMatchId());

		// Delete the queried throw.
		match.getSet(x01DeleteThrow.getSet())
				.flatMap(set -> set.getLeg(x01DeleteThrow.getLeg()))
				.ifPresent(x01Leg ->
						x01Leg.getRound(x01DeleteThrow.getRound())
								.ifPresent(x01LegRound -> {
											checkLegEditable(x01Leg, x01DeleteThrow.getPlayerId());
											if (x01LegRound.getPlayerScores() == null || x01LegRound.getPlayerScores().isEmpty()) {
												x01Leg.getRounds().remove(x01LegRound);
											} else {
												x01LegRound.getPlayerScores().stream()
														.filter(x01LegRoundScore -> Objects.equals(x01LegRoundScore.getPlayerId(), x01DeleteThrow.getPlayerId()))
														.findFirst()
														.ifPresent(x01LegRoundScore -> {
															if (x01LegRound.getPlayerScores().size() > 1) {
																x01LegRound.getPlayerScores().remove(x01LegRoundScore);
															} else {
																x01Leg.getRounds().remove(x01LegRound);
															}
														});
											}
										}
								)

				);

		// Update Statistics and Result of the match.
		match.updateAll();

		return x01MatchRepository.save(match);
	}

	// Create set, if already exists replace with empty set.
	private X01Set getOrAddSet(X01Match match, int setNumber) {
		Optional<X01Set> set = match.getSet(setNumber);

		if (set.isPresent()) {
			return set.get();
		} else {
			X01Set newSet = X01TimelineUtils.createSet(match.getPlayers(), setNumber);
			match.getTimeline().add(newSet);
			return newSet;

		}
	}

	// Create leg in a set, if already exists replace with empty leg.
	private X01Leg getOrAddLeg(X01Set set, int legNumber) {
		Optional<X01Leg> leg = set.getLeg(legNumber);

		if (leg.isPresent()) {
			return leg.get();
		} else {
			X01Leg newLeg = X01TimelineUtils.createLeg(legNumber);
			set.getLegs().add(newLeg);
			return newLeg;
		}
	}

	// Create leg round in a leg, if already exists replace with empty leg round.
	private X01LegRound getOrAddLegRound(X01Leg leg, int roundNumber) {
		Optional<X01LegRound> round = leg.getRound(roundNumber);

		if (round.isPresent()) {
			return round.get();
		} else {
			X01LegRound newRound = X01TimelineUtils.createLegRound(roundNumber);
			leg.getRounds().add(newRound);
			return newRound;
		}
	}

	private void checkLegEditable(X01Leg x01Leg, String playerId) {

		// If the leg is already won by another player then the throw cannot be added.
		if (x01Leg.getWinner() != null && !Objects.equals(x01Leg.getWinner(), playerId)) {
			throw new InvalidArgumentsException(new TargetError("score", messageResolver.getMessage("message.leg.already.won")));
		}
	}

	private void playerAddThrow(X01Leg x01Leg, X01LegRound x01LegRound, X01Throw x01Throw, Integer x01) throws IOException {

		// If the player isn't in the leg round then add him.
		final X01LegRoundScore x01LegRoundScore = x01LegRound.getPlayerScore(x01Throw.getPlayerId())
				.orElseGet(() -> {
							X01LegRoundScore newScore = new X01LegRoundScore(x01Throw.getPlayerId(), 0, 0, 0);
							x01LegRound.getPlayerScores().add(newScore);
							return newScore;
						}
				);

		// Initialize the scoring values.
		x01LegRoundScore.setScore(x01Throw.getScore());
		x01LegRoundScore.setDoublesMissed(x01Throw.getDoublesMissed());
		x01LegRoundScore.setDartsUsed(x01Throw.getDartsUsed());

		int playerScore = x01Leg.getRounds().stream()
				.flatMap(x01LegRound1 -> x01LegRound1.getPlayerScores()
						.stream()
						.filter(x01LegRoundScore1 -> Objects.equals(x01LegRoundScore1.getPlayerId(), x01Throw.getPlayerId()))
				)
				.mapToInt(X01LegRoundScore::getScore).sum();

		// If the new scoring result is negative don't allow it (player gets bust which is zero score).
		int remaining = x01 - playerScore;
		if (remaining == 0) {
			// Validate the checkout
			validateCheckout(x01Throw.getScore(), x01Throw.getDartsUsed());

			// When a throw is updated which results in a total remaining of 0, set the darts used to the last throw.
			Optional<X01LegRoundScore> playerScoreLastThrow = x01Leg.getRounds().stream()
					.max(Comparator.comparing(X01LegRound::getRound))
					.flatMap(_x01LegRound -> _x01LegRound.getPlayerScore(x01LegRoundScore.getPlayerId()));

			playerScoreLastThrow.ifPresent(_x01LegRoundScore -> {
				if (!_x01LegRoundScore.equals(x01LegRoundScore)) {
					_x01LegRoundScore.setDartsUsed(x01LegRoundScore.getDartsUsed());
					x01LegRoundScore.setDartsUsed(3);
				}
			});


		} else if (remaining < 0 || remaining == 1) {
			x01LegRoundScore.setScore(0);
		}

	}

	private void validateCheckout(int score, int dartsUsed) throws IOException, InvalidArgumentsException {
		if (score > 170)
			throw new InvalidArgumentsException(new TargetError("score", messageResolver.getMessage("message.impossible.checkout", score)));

		X01Checkout checkout = checkoutService.getCheckout(score)
				.orElseThrow(() -> new InvalidArgumentsException(new TargetError("score", messageResolver.getMessage("message.impossible.checkout", score))));

		if (dartsUsed < checkout.getMinDarts())
			throw new InvalidArgumentsException(new TargetError("dartsUsed", messageResolver.getMessage("message.impossible.checkout.min.darts", score, dartsUsed)));

	}
}
