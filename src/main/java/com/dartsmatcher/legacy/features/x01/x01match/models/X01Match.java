package com.dartsmatcher.legacy.features.x01.x01match.models;

import com.dartsmatcher.legacy.features.basematch.*;
import com.dartsmatcher.legacy.features.x01.x01match.models.playerresult.X01PlayerResult;
import com.dartsmatcher.legacy.features.x01.x01match.models.set.X01Set;
import com.dartsmatcher.legacy.features.x01.x01match.models.statistics.X01PlayerStatistics;
import com.dartsmatcher.legacy.features.x01.x01match.models.x01settings.X01MatchSettings;
import com.dartsmatcher.legacy.utils.X01ResultUtils;
import com.dartsmatcher.legacy.utils.X01StatisticsUtils;
import com.dartsmatcher.legacy.utils.X01ThrowerUtils;
import com.dartsmatcher.legacy.utils.X01TimelineUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "matches")
@TypeAlias("X01Match")
public class X01Match extends BaseMatch {

    public X01Match(ObjectId id, @NotNull LocalDateTime startDate, LocalDateTime endDate, String currentThrower,
                    @NotNull @Valid ArrayList<MatchPlayer> players, @Valid ArrayList<MatchPlayerResult> matchResult,
                    @NotNull MatchType matchType, @NotNull MatchStatus matchStatus, @Valid @NotNull X01MatchSettings x01MatchSettings,
                    @Valid ArrayList<X01PlayerResult> x01Result, @Valid ArrayList<X01PlayerStatistics> statistics,
                    @Valid ArrayList<X01Set> timeline) {
        super(id, startDate, endDate, currentThrower, matchStatus, players, matchResult, matchType);
        this.x01MatchSettings = x01MatchSettings;
        this.x01Result = x01Result;
        this.statistics = statistics;
        this.timeline = timeline;
    }

    @Valid
    @NotNull
    private X01MatchSettings x01MatchSettings;

    //TODO: This could be removed (total legs/sets won can be moved to statistics). Because match score is in base match.
    @Valid
    @Setter(AccessLevel.NONE)
    private ArrayList<X01PlayerResult> x01Result;

    @Valid
    private ArrayList<X01PlayerStatistics> statistics;

    @Valid
    private ArrayList<X01Set> timeline;

    @JsonIgnore
    public Optional<X01Set> getSet(int set) {
        if (getTimeline() == null) return Optional.empty();

        return getTimeline().stream()
                .filter(x01PlayerSet -> x01PlayerSet.getSet() == set)
                .findFirst();
    }

    @JsonIgnore
    public void updateThrower() {
        X01ThrowerUtils.updateThrower(this);
    }

    @JsonIgnore
    public void updateResult() {
        X01ResultUtils.updateMatchResult(this);
        updateMatchStatus();
    }

    @JsonIgnore
    public void updateResult(int set) {
        X01ResultUtils.updateMatchResult(this, getSet(set).orElse(null));
        updateMatchStatus();
    }

    @JsonIgnore
    public void updateStatistics() {
        X01StatisticsUtils.updateStatistics(this);
    }

    @JsonIgnore
    public void updateTimeline() {
        X01TimelineUtils.updateTimeline(this);
    }

    @JsonIgnore
    public void updateAll() {
        updateResult();
        updateTimeline();
        updateThrower();
        updateStatistics();
    }

    @JsonIgnore
    public void updateAll(int set) {
        updateResult(set);
        updateTimeline();
        updateThrower();
        updateStatistics();
    }

    @JsonIgnore
    public void updateMatchStatus() {
        if (MatchStatus.LOBBY.equals(getMatchStatus())) return;

        if (this.getX01Result() != null && this.getX01Result().stream().anyMatch(playerResult -> playerResult.getResult() != null)) {
            setMatchStatus(MatchStatus.CONCLUDED);
        } else {
            setMatchStatus(MatchStatus.IN_PLAY);
        }
    }

    public void setX01Result(ArrayList<X01PlayerResult> x01Result) {
        // Set the base match result.
        ArrayList<MatchPlayerResult> matchPlayerResults = new ArrayList<>();
        x01Result.forEach(x01PlayerResult -> matchPlayerResults.add(new MatchPlayerResult(x01PlayerResult, getX01MatchSettings().getBestOf())));

        super.setMatchResult(matchPlayerResults);

        // Set the x01 result.
        this.x01Result = x01Result;
    }
}
