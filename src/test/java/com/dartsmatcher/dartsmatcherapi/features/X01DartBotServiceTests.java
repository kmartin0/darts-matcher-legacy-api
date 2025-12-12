package com.dartsmatcher.dartsmatcherapi.features;

import com.dartsmatcher.legacy.features.basematch.MatchPlayer;
import com.dartsmatcher.legacy.features.basematch.MatchPlayerInviteStatusEnum;
import com.dartsmatcher.legacy.features.basematch.PlayerType;
import com.dartsmatcher.legacy.features.x01.x01Dartbot.X01DartBotService;
import com.dartsmatcher.legacy.features.x01.x01Dartbot.X01DartBotSettings;
import com.dartsmatcher.legacy.features.x01.x01Dartbot.X01DartBotThrow;
import com.dartsmatcher.legacy.features.x01.x01checkout.X01CheckoutServiceImpl;
import com.dartsmatcher.legacy.features.x01.x01livematch.dto.X01Throw;
import com.dartsmatcher.legacy.features.x01.x01match.IX01MatchService;
import com.dartsmatcher.legacy.features.x01.x01match.models.X01Match;
import com.dartsmatcher.legacy.features.x01.x01match.models.bestof.X01BestOf;
import com.dartsmatcher.legacy.features.x01.x01match.models.leg.X01Leg;
import com.dartsmatcher.legacy.features.x01.x01match.models.leg.X01LegRound;
import com.dartsmatcher.legacy.features.x01.x01match.models.leg.X01LegRoundScore;
import com.dartsmatcher.legacy.features.x01.x01match.models.set.X01Set;
import com.dartsmatcher.legacy.features.x01.x01match.models.x01settings.X01MatchSettings;
import org.bson.types.ObjectId;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.*;

@ExtendWith({SpringExtension.class})
@ContextConfiguration(classes = {X01DartBotService.class, X01CheckoutServiceImpl.class})
public class X01DartBotServiceTests {

    @MockBean
    private IX01MatchService matchService;

    @InjectMocks
    @Autowired
    private X01DartBotService dartBotService;

    @Test
    void testAllExpectedAverages_dartsUsedWithinMarginOfError() throws IOException {

        final X01Match match = createTestMatch();
        Mockito.when(matchService.getMatch(Mockito.any())).thenReturn(match);

        final String dartBotId = "dartBot";
        final int x01 = 501;
        final double maxAvgDeviation = X01DartBotService.MAX_ONE_DART_AVG_DEVIATION;

        for(int l = 0; l < 1; l++) {

            for (int targetAvg = 180; targetAvg > 1; targetAvg--) {
                System.out.println("==========");
                System.out.println("TEST_TARGET: " + targetAvg);
                System.out.println("==========");
                // Initialize the match with the base settings.
                X01Leg x01Leg = new X01Leg(1, null, dartBotId, new ArrayList<>());
                X01Set x01Set = new X01Set(1, null, new ArrayList<>(Collections.singletonList(x01Leg)));

                X01DartBotSettings dartBotSettings = new X01DartBotSettings(targetAvg);
                MatchPlayer dartBotPlayer = new MatchPlayer(dartBotId, null, null, null, PlayerType.DART_BOT, dartBotSettings, MatchPlayerInviteStatusEnum.ACCEPTED);

                match.setTimeline(new ArrayList<>(Collections.singletonList(x01Set)));
                match.setPlayers(new ArrayList<>(Collections.singletonList(dartBotPlayer)));

                // Initialize the minimum and maximum number of darts a dart bot must complete a leg in.
                int minMaxDartsMarginOfError = 0;
                int minTargetNumOfDarts = (int) Math.round(((double) x01 / (targetAvg + (targetAvg * maxAvgDeviation))) * 3) - minMaxDartsMarginOfError;
                int maxTargetNumOfDarts = (int) Math.round(((double) x01 / (targetAvg - (targetAvg * maxAvgDeviation))) * 3) + minMaxDartsMarginOfError;

                if (minTargetNumOfDarts < 9) minTargetNumOfDarts = 9;

                // Let the dart bot complete x number of legs and for each leg check that the darts used is within the range of minimum and maximum darts.
                double totalDartsUsed = 0;
                int minDartsUsed = 0;
                int maxDartsUsed = 0;
                int numOfIterations = 150;
                Map<Integer, Integer> counter = new TreeMap<>();
                for (int j = 0; j < numOfIterations; j++) {
//                System.out.println("=== Iteration " + j + "===");
                    int round = 1;
                    int remaining = x01;
                    while (remaining > 0) {
                        X01Throw x01Throw = dartBotService.dartBotThrow(new X01DartBotThrow(dartBotId, match.getId(), 1, x01Leg.getLeg(), round));
                        X01LegRoundScore newRound = new X01LegRoundScore(dartBotId, 0, x01Throw.getDartsUsed(), x01Throw.getScore());

                        x01Leg.getRounds().add(new X01LegRound(round++, new ArrayList<>(Collections.singletonList(newRound))));

                        remaining -= x01Throw.getScore();
//                    System.out.println("Remaining: " + remaining + " " + newRound);
                    }

                    int dartsUsed = x01Leg.getDartsUsed(dartBotId);

                    totalDartsUsed += dartsUsed;
                    if (minDartsUsed == 0 || dartsUsed < minDartsUsed) minDartsUsed = dartsUsed;
                    if (dartsUsed > maxDartsUsed) maxDartsUsed = dartsUsed;

                    if (minTargetNumOfDarts != 9 && (dartsUsed < minTargetNumOfDarts))
                        System.out.println(" Darts Used: " + dartsUsed + " Minn Darts: " + minTargetNumOfDarts);
                    if (dartsUsed > maxTargetNumOfDarts)
                        System.out.println(" Darts Used: " + dartsUsed + " Maxx Darts: " + maxTargetNumOfDarts);

                    counter.put(dartsUsed, counter.getOrDefault(dartsUsed, 0) + 1);
                    MatcherAssert.assertThat(dartsUsed, Matchers.allOf(
                                    Matchers.greaterThanOrEqualTo(minTargetNumOfDarts),
                                    Matchers.lessThanOrEqualTo(maxTargetNumOfDarts)
                            )
                    );

                    x01Leg.setRounds(new ArrayList<>());

                }
                System.out.println("Min Target: " + (minTargetNumOfDarts) + " Min Darts Used: " + minDartsUsed);
                System.out.println("Max Target: " + (maxTargetNumOfDarts) + " Max Darts Used: " + maxDartsUsed);
                System.out.println("Target Average Darts: " + ((minTargetNumOfDarts + maxTargetNumOfDarts) / 2) + " Average Darts Used: " + String.format("%.2f", (totalDartsUsed / numOfIterations)));
                System.out.println(counter);
            }
        }
    }

    private X01Match createTestMatch() {
        X01Match match = new X01Match();
        match.setId(new ObjectId());
        match.setX01MatchSettings(new X01MatchSettings(501, false, new X01BestOf(1, 1)));

        return match;
    }
}
