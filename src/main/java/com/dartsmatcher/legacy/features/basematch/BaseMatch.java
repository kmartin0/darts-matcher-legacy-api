package com.dartsmatcher.legacy.features.basematch;

import com.dartsmatcher.legacy.validators.anonymousname.ValidMatchPlayerIds;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.MongoId;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaseMatch {

	@MongoId
	private ObjectId id;

	@NotNull
	private LocalDateTime startDate;

	private LocalDateTime endDate;

	private String currentThrower;

	@NotNull
	@Setter(AccessLevel.NONE)
	private MatchStatus matchStatus;

	@NotNull
	@ValidMatchPlayerIds
	@Valid
	private ArrayList<MatchPlayer> players;

	@Valid
	private ArrayList<MatchPlayerResult> matchResult;

	@NotNull
	private MatchType matchType;

	public void setMatchStatus(MatchStatus matchStatus) {
		if (!matchStatus.equals(getMatchStatus())) {
			switch (matchStatus) {
				case LOBBY:
				case IN_PLAY:
					setEndDate(null);
					break;
				case CONCLUDED:
					setEndDate(LocalDateTime.now());
					break;
			}
		}

		this.matchStatus = matchStatus;
	}

}
