package com.dartsmatcher.legacy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// TODO: Copy ConstrainViolationException handler into boilerplate
@SpringBootApplication
public class DartsMatcherLegacyApplication {

	public static void main(String[] args) {
		SpringApplication.run(DartsMatcherLegacyApplication.class, args);
	}


}

/*
Step 1: Create dartboard using polar scale.
		Step 2: Determine center of aim (i.e. aiming for T20 will need coordinates of middle of T20)
		Step 3: Using a deviation determined by average randomize a hit.
		Step 4: Determine the score that was hit using the polar coordinates.

		Things to consider:
		- When not on a checkout always aim for T20, T19 or T18
		- When on a checkout aim for the suggested checkout
		- https://dodona.ugent.be/en/activities/960794524/#submission-card
 */

// leg 2: no border for current thrower
//edit mode turns on with enter
//keyboard weg bij game end