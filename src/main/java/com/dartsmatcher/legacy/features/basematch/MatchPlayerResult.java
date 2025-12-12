package com.dartsmatcher.legacy.features.basematch;

import com.dartsmatcher.legacy.features.x01.x01match.models.bestof.X01BestOf;
import com.dartsmatcher.legacy.features.x01.x01match.models.playerresult.X01PlayerResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchPlayerResult {

    public MatchPlayerResult(X01PlayerResult x01PlayerResult, X01BestOf x01BestOf) {
        this.playerId = x01PlayerResult.getPlayerId();
        this.score = x01BestOf.getSets() > 1 ? x01PlayerResult.getSetsWon() : x01PlayerResult.getLegsWon();
        this.result = x01PlayerResult.getResult();
    }

    private String playerId;

    private int score;

    private ResultType result;

}
