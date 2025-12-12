package com.dartsmatcher.legacy.validators.anonymousname;

import com.dartsmatcher.legacy.features.basematch.MatchPlayer;
import com.dartsmatcher.legacy.features.basematch.PlayerType;
import org.bson.types.ObjectId;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.*;

public class ValidMatchPlayerIdsValidator implements ConstraintValidator<ValidMatchPlayerIds, ArrayList<MatchPlayer>> {

	@Override
	public void initialize(ValidMatchPlayerIds constraintAnnotation) {

	}

	@Override
	public boolean isValid(ArrayList<MatchPlayer> matchPlayers, ConstraintValidatorContext context) {

		HibernateConstraintValidatorContext hibernateContext = context.unwrap(HibernateConstraintValidatorContext.class);
		hibernateContext.disableDefaultConstraintViolation();

		ArrayList<String> reservedNames = new ArrayList<>(Collections.singletonList("admin"));
		Set<Object> tmpPlayerIds = new HashSet<>();

		for (MatchPlayer matchPlayer : matchPlayers) {

			String playerId = matchPlayer.getPlayerId();
			String violatedPlayerId = "";

			if (matchPlayer.getPlayerType() == PlayerType.REGISTERED && !ObjectId.isValid(playerId) || // Registered players should have a valid object id.
					reservedNames.stream().anyMatch(playerId::equalsIgnoreCase)) { // Player id's cannot be the same as the reserved names.
				violatedPlayerId = playerId;
			}

			if (!violatedPlayerId.isEmpty()) {
				hibernateContext
						.addMessageParameter("name", matchPlayer.getPlayerId())
						.buildConstraintViolationWithTemplate("{message.player.name.not.allowed}")
						.addConstraintViolation();
				return false;
			}

			if (!tmpPlayerIds.add(playerId.toLowerCase())) {
				hibernateContext
						.addMessageParameter("name", matchPlayer.getPlayerId())
						.buildConstraintViolationWithTemplate("{message.player.name.duplicate}")
						.addConstraintViolation();
				return false;
			}
		}

		return true;
	}
}
