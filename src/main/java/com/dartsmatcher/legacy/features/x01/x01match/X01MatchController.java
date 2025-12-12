package com.dartsmatcher.legacy.features.x01.x01match;

import com.dartsmatcher.legacy.features.x01.x01checkout.IX01CheckoutService;
import com.dartsmatcher.legacy.features.x01.x01match.models.X01Match;
import com.dartsmatcher.legacy.features.x01.x01match.models.checkout.X01Checkout;
import com.dartsmatcher.legacy.utils.Endpoints;
import com.dartsmatcher.legacy.utils.Websockets;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.ArrayList;

@RestController
@Controller
public class X01MatchController {

	private final IX01MatchService matchService;

	private final IX01CheckoutService checkoutService;

	private final SimpMessagingTemplate simpMessagingTemplate;

	public X01MatchController(IX01MatchService matchService, IX01CheckoutService checkoutService, SimpMessagingTemplate simpMessagingTemplate) {
		this.matchService = matchService;
		this.checkoutService = checkoutService;
		this.simpMessagingTemplate = simpMessagingTemplate;
	}

	@PostMapping(path = Endpoints.SAVE_MATCH, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
//	@PreAuthorize("isAuthenticated()")
	public X01Match saveMatch(@Valid @RequestBody X01Match x01Match) {

		return matchService.saveMatch(x01Match);
	}

	@GetMapping(path = Endpoints.GET_MATCH, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.OK)
	public X01Match getMatch(@PathVariable @NotNull ObjectId matchId) {

		return matchService.getMatch(matchId);
	}

	@PutMapping(path = Endpoints.UPDATE_MATCH, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
//	@PreAuthorize("isAuthenticated()")
	public X01Match updateMatch(@Valid @RequestBody X01Match x01Match, @PathVariable ObjectId matchId) {
		X01Match updatedMatch = matchService.updateMatch(x01Match, matchId);

		simpMessagingTemplate.convertAndSend(Websockets.X01_MATCH.replace("{matchId}", updatedMatch.getId().toString()), updatedMatch);

		return matchService.updateMatch(x01Match, matchId);
	}

	@GetMapping(path = Endpoints.GET_CHECKOUTS, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.OK)
	public ArrayList<X01Checkout> getCheckouts() throws IOException {

		return checkoutService.getCheckouts();
	}

}
