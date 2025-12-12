package com.dartsmatcher.legacy.features.x01.x01Dartbot;

import com.dartsmatcher.legacy.exceptionhandler.exception.ResourceNotFoundException;
import com.dartsmatcher.legacy.features.dartboard.DartBoard;
import com.dartsmatcher.legacy.features.dartboard.DartBoardSectionArea;
import com.dartsmatcher.legacy.features.dartboard.Dart;
import com.dartsmatcher.legacy.features.basematch.MatchPlayer;
import com.dartsmatcher.legacy.features.x01.x01checkout.IX01CheckoutService;
import com.dartsmatcher.legacy.features.x01.x01livematch.dto.X01Throw;
import com.dartsmatcher.legacy.features.x01.x01match.IX01MatchService;
import com.dartsmatcher.legacy.features.x01.x01match.models.X01Match;
import com.dartsmatcher.legacy.features.x01.x01match.models.checkout.X01Checkout;
import com.dartsmatcher.legacy.features.x01.x01match.models.leg.X01Leg;
import com.dartsmatcher.legacy.features.x01.x01match.models.leg.X01LegRoundScore;
import com.dartsmatcher.legacy.utils.PolarCoordinate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Objects;

@Service
public class X01DartBotService {

    public static final double MAX_ONE_DART_AVG = 167 / 3.0;

    public static final int NUM_OF_DARTS_IN_A_ROUND = 3;

    public static final double MAX_ONE_DART_AVG_DEVIATION = .075;

    private final IX01CheckoutService checkoutService;

    private final IX01MatchService matchService;

    public X01DartBotService(IX01CheckoutService checkoutService, IX01MatchService matchService) {
        this.checkoutService = checkoutService;
        this.matchService = matchService;
    }

    /**
     * Simulate a dart bot throwing at a dartboard for a round (3 darts).
     *
     * @param x01DartBotThrow X01DartBotThrow containing information about the throw
     * @return X01Throw the object containing the throw information of the round.
     * @throws IOException throws exception when an error occurred reading the checkouts file.
     */
    public X01Throw dartBotThrow(X01DartBotThrow x01DartBotThrow) throws IOException {

        // Fetch information about the match and dart bot.
        String dartBotId = x01DartBotThrow.getDartBotId();
        X01Match match = matchService.getMatch(x01DartBotThrow.getMatchId());
        MatchPlayer dartBotPlayer = match.getPlayers().stream().filter(matchPlayer -> matchPlayer.getPlayerId().equals(dartBotId)).findFirst().orElse(null);

        X01Leg x01Leg = match.getSet(x01DartBotThrow.getSet())
                .flatMap(x01Set -> x01Set.getLeg(x01DartBotThrow.getLeg()))
                .orElseThrow(() -> new ResourceNotFoundException(X01Leg.class, x01DartBotThrow.getLeg()));

        X01DartBotSettings dartBotSettings = Objects.requireNonNull(dartBotPlayer).getDartBotSettings();
        if (dartBotSettings == null) throw new ResourceNotFoundException(X01DartBotSettings.class, null);

        double targetOneDartAvg = (double) dartBotSettings.getExpectedThreeDartAverage() / NUM_OF_DARTS_IN_A_ROUND; // TODO: Method

        // Fetch the current leg standing.
        int legScored = x01Leg.getScored(dartBotId);
        int legDartsUsed = x01Leg.getDartsUsed(dartBotId);

//        System.out.println("=== Round: " + x01DartBotThrow.getRound() + "===");

        // Generate the X01LegRoundScore to store the score, darts used and doubles missed for this round.
        X01LegRoundScore roundScore = new X01LegRoundScore(dartBotId, 0, 0, 0);

        // Populate the roundScore with the simulated throws for this round.
        roundScore = createRoundScore(match.getX01MatchSettings().getX01(), legScored, legDartsUsed, targetOneDartAvg, roundScore);

        return new X01Throw(x01DartBotThrow.getMatchId(), dartBotId, x01Leg.getLeg(), x01DartBotThrow.getSet(),
                x01DartBotThrow.getRound(), roundScore.getScore(), roundScore.getDartsUsed(), roundScore.getDoublesMissed());
    }

    /**
     * Recursively populates an X01LegRoundScore object with simulated throws at a dartboard until no darts are left.
     *
     * @param x01                int the x01 starting point.
     * @param legScored          int the score in the leg excluding this round.
     * @param legDartsUsed       int the darts used in the leg excluding this round.
     * @param expectedOneDartAvg double the expected one dart average.
     * @param x01LegRoundScore   X01LegRoundScore to populate with the values for this round.
     * @return X01LegRoundScore containing the results for a round.
     * @throws IOException throws exception when an error occurred reading the checkouts file.
     */
    private X01LegRoundScore createRoundScore(int x01, int legScored, int legDartsUsed, double expectedOneDartAvg, X01LegRoundScore x01LegRoundScore) throws IOException {

//        System.out.println("Dart: " + (x01LegRoundScore.getDartsUsed() + 1));

        double actualOneDartAvg = calcOneDartAvg(legScored + x01LegRoundScore.getScore(),
                legDartsUsed + x01LegRoundScore.getDartsUsed());

        int remaining = x01 - legScored - x01LegRoundScore.getScore();

        X01Checkout checkout = checkoutService.getCheckout(remaining).orElse(null);

        // When the bot is outside checkout range, create a scoring throw. Otherwise, create a checkout throw.
        x01LegRoundScore = (checkout == null)
                ? createScoringThrowResult(x01LegRoundScore, expectedOneDartAvg, actualOneDartAvg, legDartsUsed, remaining, x01)
                : createCheckoutThrowResult(x01LegRoundScore, expectedOneDartAvg, actualOneDartAvg, legDartsUsed, remaining, x01);

        remaining = x01 - legScored - x01LegRoundScore.getScore();
        int roundDartsLeft = 3 - x01LegRoundScore.getDartsUsed();

        // When no darts are left or the remaining score reaches zero, return the round score. Otherwise, recursively call this method until no darts are left.
        return roundDartsLeft == 0 || remaining <= 0
                ? x01LegRoundScore
                : createRoundScore(x01, legScored, legDartsUsed, expectedOneDartAvg, x01LegRoundScore);
    }

    /**
     * Creates a biased random dartboard target intended for scoring. Will choose between
     * Treble 20 (85%), Treble 19 (12.5%), Treble 18 (5%), Treble 16 (2.5%). Always returns treble 20 if the target
     * average is a nine darter.
     *
     * @param targetOneDartAvg double   The target one dart average to determine
     * @return Dart Treble target used for scoring (T20, T19, T18 or T17).
     */
    private Dart createScoringTarget(double targetOneDartAvg) {

        Dart target = new Dart(20, DartBoardSectionArea.TRIPLE);

        // Return Treble 20 if the target average is a nine darter or better.
        if (targetOneDartAvg >= MAX_ONE_DART_AVG) return target;

        // Generate a biased random treble. // TODO: Move to method.
        int[] trebles = {20, 19, 18, 17};
        double[] probabilities = {0.85, 0.125, 0.05, 0.025};
        double rand = Math.random(); // Generate a random value between 0 and 1

        double cumulativeProbability = 0.0;

        for (int i = 0; i < trebles.length; i++) {
            cumulativeProbability += probabilities[i];
            if (rand <= cumulativeProbability) {
                target = new Dart(trebles[i], DartBoardSectionArea.TRIPLE);
                break;
            }
        }

        return target;
    }

    /**
     * Simulates a scoring result by throwing a dart at a virtual dartboard.
     *
     * @param x01LegRoundScore X01LegRoundScore Object to populate with the result.
     * @param targetOneDartAvg double The target one dart average.
     * @param actualOneDartAvg double the actual one dart average.
     * @param dartsThrownInLeg int the number of darts thrown in the leg.
     * @param remaining        int the remaining score in the leg.
     * @param x01              int the starting score of the leg.
     * @return X01LegRoundScore populated with the result from the simulated throw.
     */
    private X01LegRoundScore createScoringThrowResult(X01LegRoundScore x01LegRoundScore, double targetOneDartAvg, double actualOneDartAvg, int dartsThrownInLeg, int remaining, int x01) {

        // Create a scoring target and throw at it.
        int dartsThrownInLegAndRound = dartsThrownInLeg + x01LegRoundScore.getDartsUsed();
        Dart target = createScoringTarget(targetOneDartAvg);
        Dart result = throwAtTarget(targetOneDartAvg, actualOneDartAvg, target, dartsThrownInLegAndRound);

        // Create a bust if the score would result in an invalid checkout or an accidental checkout.
        int remainingAfterResult = remaining - result.getScore();

        if (remainingAfterResult < 2) {
            int minTargetNumOfDarts = (int) Math.round((double) x01 / (targetOneDartAvg * 1.075)); // TODO: Magic numbers

            if (!checkValidCheckout(target, result, remaining, dartsThrownInLegAndRound, minTargetNumOfDarts)) {
                result = new Dart(result.getSection(), DartBoardSectionArea.MISS);
            }
        }

//        System.out.println("Scoring Target: " + target + " Scoring Result: " + result);

        // Update x01LegRoundScore with the thrown score and dart used.
        x01LegRoundScore.setDartsUsed(x01LegRoundScore.getDartsUsed() + 1);
        x01LegRoundScore.setScore(x01LegRoundScore.getScore() + result.getScore());

        return x01LegRoundScore;
    }

    /**
     * Simulates a result intended for when the bot is within checkout range. Checking out is only possible when the bot
     * is within a certain range of the target average. Within this range, checking out is based on chance (hitting/missing the double).
     * The bot will always check out before the maximum number of darts is reached.
     *
     * @param x01LegRoundScore X01LegRoundScore Object to populate with the result.
     * @param targetOneDartAvg double The target one dart average.
     * @param actualOneDartAvg double the actual one dart average.
     * @param dartsThrownInLeg int the number of darts thrown in the leg.
     * @param remaining        int the remaining score in the leg.
     * @param x01              int the starting score of the leg.
     * @return X01LegRoundScore populated with the result from the simulated throw at a checkout sequence.
     * @throws IOException Throw an IOException if an error occurs during the fetching of checkouts.
     */
    private X01LegRoundScore createCheckoutThrowResult(X01LegRoundScore x01LegRoundScore, double targetOneDartAvg, double actualOneDartAvg, int dartsThrownInLeg, int remaining, int x01) throws IOException {

        X01Checkout checkout = checkoutService.getCheckout(remaining).orElse(null);

        // When there is no checkout, return a scoring throw.
        if (checkout == null) {
            return createScoringThrowResult(x01LegRoundScore, targetOneDartAvg, actualOneDartAvg, dartsThrownInLeg, remaining, x01);
        }

        int roundDartsLeft = 3 - x01LegRoundScore.getDartsUsed(); // TODO: Magic Number
        int dartsThrownInLegAndRound = dartsThrownInLeg + x01LegRoundScore.getDartsUsed();
        int minTargetNumOfDarts = (int) Math.round((double) x01 / (targetOneDartAvg * 1.075));  // TODO: Magic Number
        int maxTargetNumOfDarts = (int) Math.round((double) x01 / (targetOneDartAvg * 0.925));  // TODO: Magic Number
        int targetNumOfDarts = getRandomIntegerWithBias(minTargetNumOfDarts, maxTargetNumOfDarts);
        int dartsThrownAfterCheckout = dartsThrownInLegAndRound + checkout.getSuggested().size();

        // Check if the dart bot must check out. When true return the populated x01LegRoundScore.
        if (checkHasToCheckout(x01LegRoundScore, checkout, dartsThrownAfterCheckout, targetNumOfDarts, roundDartsLeft)) {
            return x01LegRoundScore;
        }

        // Simulate throwing at the next target in the checkout sequence.
        Dart target = checkout.getSuggested().get(0); // TODO: Change CreateScoringTarget into createTarget
        Dart result = throwAtTarget(targetOneDartAvg, actualOneDartAvg, target, dartsThrownInLegAndRound);

        // When the next checkout after this throw is outside the target maximum number of darts. Re-assign the result hit the target.
        if (!checkNextCheckoutWithinTargetAvg(remaining + result.getScore(), target, result, dartsThrownInLegAndRound, targetNumOfDarts)) {
            result = new Dart(target);
        }

        // Create a bust if the score would result in an invalid checkout or an accidental checkout.
        if (remaining - result.getScore() < 2) {
            if (!checkValidCheckout(target, result, remaining, dartsThrownInLegAndRound, minTargetNumOfDarts)) {
                result.setArea(DartBoardSectionArea.MISS);
            }
        }

        // TODO: AFTER PUSHING THESE CHANGES LOOK INTO REDUCING COMPLEXITY BY ADDING HANDLERS example move the if statement to: "handleMissedDoubles"
        // When the bot misses a double, increment the doubles missed.
        if (checkout.getSuggested().size() == 1 &&
                (target.getArea().equals(DartBoardSectionArea.DOUBLE) && !result.getArea().equals(DartBoardSectionArea.DOUBLE))) {
            x01LegRoundScore.setDoublesMissed(x01LegRoundScore.getDoublesMissed() + 1);
        }

        // Update x01LegRoundScore with the thrown score and dart used.
        x01LegRoundScore.setDartsUsed(x01LegRoundScore.getDartsUsed() + 1);
        x01LegRoundScore.setScore(x01LegRoundScore.getScore() + result.getScore());

        return x01LegRoundScore;
    }

    /**
     * Check whether the bot must check out as soon as possible. If the checkout must be completed, will finish this
     * round if enough darts remaining. Otherwise, hits the first target in the checkout sequence.
     *
     * @param x01LegRoundScore         X01LegRoundScore Object to populate with the result.
     * @param x01Checkout              X01Checkout The checkout to complete.
     * @param dartsThrownAfterCheckout int The number of darts that would be thrown if the checkout sequence is followed.
     * @param maxTargetNumOfDarts      int The maximum target number of darts the bot should finish the leg in.
     * @param roundDartsLeft           int The number of darts the bot can throw this round.
     * @return boolean Update x01LegRoundScore with the checkout sequence if a check-out must be done. True when updated.
     */
    private boolean checkHasToCheckout(X01LegRoundScore x01LegRoundScore, X01Checkout x01Checkout, int dartsThrownAfterCheckout, int maxTargetNumOfDarts, int roundDartsLeft) {

        // When the maximum number of darts is reached or exceeded after the checkout. Then it must check out as soon as possible.
        if (dartsThrownAfterCheckout >= maxTargetNumOfDarts) {

            if (x01Checkout.getSuggested().size() <= roundDartsLeft) { // If a checkout can be completed within this round, do it.
                for (Dart dart : x01Checkout.getSuggested()) {
                    x01LegRoundScore.setDartsUsed(x01LegRoundScore.getDartsUsed() + 1);
                    x01LegRoundScore.setScore(x01LegRoundScore.getScore() + dart.getScore());
                }
            } else { // If there aren't enough darts this round, hit the next target.
                Dart result = new Dart(x01Checkout.getSuggested().get(0));
                x01LegRoundScore.setDartsUsed(x01LegRoundScore.getDartsUsed() + 1);
                x01LegRoundScore.setScore(x01LegRoundScore.getScore() + result.getScore());
            }

            return true;
        }

        return false;
    }

    /**
     * Check whether a checkout thrown by the bot is valid
     *
     * @param target              Dart The target that's been thrown at.
     * @param result              Dart The result of the simulated throw at the target.
     * @param remaining           int The remaining score before the checkout.
     * @param dartsThrown         int The darts thrown until the checkout.
     * @param minTargetNumOfDarts int The minimum number of darts that should be thrown.
     * @return boolean Whether the result is a valid checkout.
     */
    private boolean checkValidCheckout(Dart target, Dart result, int remaining, int dartsThrown, int minTargetNumOfDarts) {

        int remainingAfterResult = remaining - result.getScore();
        int dartsThrownAfterResult = dartsThrown + 1;

        // A checkout must leave a remaining score of zero.
        if (remainingAfterResult != 0) {
            return false;
        }

        // If the bot reaches 0 without hitting a double return false.
        if (!(result.getArea().equals(DartBoardSectionArea.DOUBLE) || result.getArea().equals(DartBoardSectionArea.DOUBLE_BULL))) {
            return false;
        }

        // If the bot checks out before the minimum number of darts return false.
        if (dartsThrownAfterResult < minTargetNumOfDarts) {
            return false;
        }

        // If the bot accidentally checks out return false.
        if ((target.getArea() != DartBoardSectionArea.DOUBLE && result.getArea() == DartBoardSectionArea.DOUBLE) ||
                (target.getArea() != DartBoardSectionArea.DOUBLE_BULL && result.getArea() == DartBoardSectionArea.DOUBLE_BULL)) {
            return false;
        }

        return true;
    }

    /**
     * Checks if the remaining checkout after a result can still be thrown within the target maximum number of darts.
     *
     * @param remainingAfterResult     int Remaining score after a throw.
     * @param target                   Dart the target that's been thrown at.
     * @param result                   Dart the result of the throw.
     * @param dartsThrownInLegAndRound int The number of darts thrown in the leg and round, including the result throw.
     * @param maxTargetNumOfDarts      int The target of maximum darts the bot should complete the leg in.
     * @return boolean Whether the next checkout can still be thrown within the target maximum number of darts.
     * @throws IOException throws exception when an error occurred reading the checkouts file.
     */
    private boolean checkNextCheckoutWithinTargetAvg(int remainingAfterResult, Dart target, Dart result, int dartsThrownInLegAndRound, int maxTargetNumOfDarts) throws IOException {

        X01Checkout nextCheckout = checkoutService.getCheckout(remainingAfterResult).orElse(null);

        // To prevent unwanted behaviour, only check when the target has been missed.
        if (nextCheckout != null && !result.equals(target)) {
            int dartsThrownAfterNextCheckout = dartsThrownInLegAndRound + nextCheckout.getSuggested().size() + 1;
            return dartsThrownAfterNextCheckout <= maxTargetNumOfDarts;
        }

        return true;
    }

    /**
     * Virtually throws a dart at a target on a dartboard with a deviation based on the target and actual average.
     *
     * @param targetOneDartAvg double The expected one dart average of bot.
     * @param actualOneDartAvg double The actual one dart average of bot in the leg.
     * @param target           Dart The target to be thrown at.
     * @return Dart The result of where the dart landed on the board.
     */
    private Dart throwAtTarget(double targetOneDartAvg, double actualOneDartAvg, Dart target, int dartsThrown) {

        // Calculate the deviation of the angle and radial.
        double deviationR = getDeviationR(targetOneDartAvg, actualOneDartAvg, dartsThrown);
        double deviationTheta = getDeviationTheta(targetOneDartAvg, actualOneDartAvg, dartsThrown);

        return getScore(target, deviationR, deviationTheta);
    }

    /**
     * Virtually throws a dart at a target on the dartboard and returns a Dart with the result. Generates the result of
     * the center of the target area + the deviation angle and radius.
     *
     * @param target         Dart the target that will be aimed for.
     * @param deviationR     double the radial deviation in mm.
     * @param deviationTheta double the theta (angle) deviation in radian between -pi and pi.
     * @return Dart The result of where the dart landed on the board.
     */
    private Dart getScore(Dart target, double deviationR, double deviationTheta) {
        DartBoard dartBoard = new DartBoard();

        // Create a polar coordinate from the center of the section area.
        PolarCoordinate polarTarget = dartBoard.getCenter(target.getSection(), target.getArea());

        // Add the deviation radial and angle to the target's polar coordinate.
        double r = polarTarget.getR() + deviationR;
        double theta = PolarCoordinate.normalizeTheta(polarTarget.getTheta() + deviationTheta);

        // Return the Dart containing the result of the polar coordinate with deviation.
        return dartBoard.getScorePolar(new PolarCoordinate(r, theta));
    }

    /**
     * returns random integer between minimum and maximum range with a bias towards the higher numbers.
     *
     * @param min int The minimum number in the range (inclusive)
     * @param max int The maximum number in the range (inclusive)
     * @return int A random number within the range with a bias towards the higher numbers.
     * @throws IllegalArgumentException When the minimum number is higher than the maximum number.
     */
    public int getRandomIntegerWithBias(int min, int max) throws IllegalArgumentException {

        // Throws IllegalArgumentException when the range is invalid.
        if (min > max) {
            throw new IllegalArgumentException("min must be less than or equal to max");
        }

        // The exponent used to create bias towards higher numbers.
        double exponent = 0.5;

        // Generate a random value, greater than or equal to 0.0 and less than 1.0. To increase the bias towards higher numbers, raise it by the power of exponent.
        double rand = Math.pow(Math.random(), exponent);

        // Convert the random value to an integer within the specified range [min (inclusive), max (inclusive] and return the result.
        return (int) (rand * (max - min + 1)) + min;
    }

    /**
     * Generates the radial (r) deviation associated with a target average. This deviation is calibrated to be preciser the further away
     * the actual average is from the target.
     *
     * @param targetOneDartAvg double The target one dart average.
     * @param actualOneDartAvg double The actual one dart average.
     * @return double The radial (r) deviation in mm.
     */
    private double getDeviationR(double targetOneDartAvg, double actualOneDartAvg, int dartsThrown) {

        // Get the (calibrated) deviation based on the target and actual avg.
        double deviation = calcDeviation(targetOneDartAvg, actualOneDartAvg, dartsThrown);
//        System.out.println("Deviation Radial: " + deviation);

        // Return a random number within a range from negative deviation to positive deviation.
        return Math.random() * (deviation + deviation) - deviation;
    }

    /**
     * Generates the angle (theta) deviation in radian units associated with a target average. This deviation is
     * calibrated to be preciser the further away the actual average is from the target.
     *
     * @param targetOneDartAvg double The expected one dart average.
     * @param actualOneDartAvg double The actual one dart average.
     * @return double The angle (theta) deviation.
     */
    private double getDeviationTheta(double targetOneDartAvg, double actualOneDartAvg, int dartsThrown) {

        // Get the (calibrated) deviation in degrees based on the target and actual avg.
        double deviation = calcDeviation(targetOneDartAvg, actualOneDartAvg, dartsThrown);
//        System.out.println("Deviation Angle: " + deviation);

        // Adjust deviation to be a random number within a range from negative deviation to positive deviation.
        deviation = Math.random() * (deviation + deviation) - deviation;

        // Return the angle deviation in radian units.
        return PolarCoordinate.degreeToRadian(deviation);
    }

    /**
     * Calculates what the deviation of a throw is in mm based on the expected one dart average. Will calibrate the
     * deviation to be preciser when the bot scores worse than expected.
     *
     * @param targetOneDartAvg double The expected one dart average.
     * @param actualOneDartAvg double The actual one dart average.
     * @param dartsThrown      int The number of darts thrown to reach the current average.
     * @return double the deviation in mm.
     */
    private double calcDeviation(double targetOneDartAvg, double actualOneDartAvg, int dartsThrown) {

        double minOneDartAvg = X01DartBotSettings.MINIMUM_BOT_AVG / 3.0;
        double deviationScalingFactor = 20.0; // The scaling factor used in the adjusted logarithmic function.

        // When the average is higher or equal than the maximum, there is no deviation.
        if (targetOneDartAvg >= MAX_ONE_DART_AVG) return 0;

        // When the dart bot is scoring worse than the target avg, adjust the scaling factor, resulting in a preciser deviation.
        // Only calibrate after the first round has been thrown.
        if (dartsThrown >= 3 && actualOneDartAvg < targetOneDartAvg) {

            // Reduces the base scaling factor closer to zero, the further away the actual avg is from the target avg.
            // Using a scaling modifier and a slight adjustment to prevent division by zero.
            double calibrationModifier = 0.75;
            double diff = Math.abs(actualOneDartAvg - targetOneDartAvg);
            deviationScalingFactor = deviationScalingFactor / (calibrationModifier * diff + 1);
        }

        // Calculate deviation using adjusted logarithmic function with a scaling factor.
        return deviationScalingFactor * Math.log(MAX_ONE_DART_AVG / Math.max(1, (targetOneDartAvg - minOneDartAvg) + 1));
    }

    /**
     * Calculates the one dart average of a score.
     *
     * @param totalPoints double The number of points that have been scored.
     * @param dartsThrown int The number darts thrown needed to get the points.
     * @return double one dart average of the points.
     */
    private double calcOneDartAvg(double totalPoints, int dartsThrown) {

        // When no darts were thrown, return zero to prevent division by zero
        if (dartsThrown == 0) {
            return 0;
        }

        // Calculate and return the one-dart average.
        return totalPoints / dartsThrown;
    }

}
