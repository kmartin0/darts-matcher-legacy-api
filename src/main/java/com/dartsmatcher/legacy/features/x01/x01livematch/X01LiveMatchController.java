package com.dartsmatcher.legacy.features.x01.x01livematch;

import com.dartsmatcher.legacy.features.x01.x01Dartbot.X01DartBotService;
import com.dartsmatcher.legacy.features.x01.x01Dartbot.X01DartBotThrow;
import com.dartsmatcher.legacy.features.x01.x01livematch.dto.X01DeleteLeg;
import com.dartsmatcher.legacy.features.x01.x01livematch.dto.X01DeleteSet;
import com.dartsmatcher.legacy.features.x01.x01livematch.dto.X01DeleteThrow;
import com.dartsmatcher.legacy.features.x01.x01livematch.dto.X01Throw;
import com.dartsmatcher.legacy.features.x01.x01match.IX01MatchService;
import com.dartsmatcher.legacy.features.x01.x01match.models.X01Match;
import com.dartsmatcher.legacy.utils.Websockets;
import org.bson.types.ObjectId;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import javax.validation.Valid;
import java.io.IOException;

@Controller
public class X01LiveMatchController {

	private final IX01MatchService matchService;

	private final X01DartBotService dartBotService;

	public X01LiveMatchController(IX01MatchService matchService, X01DartBotService dartBotService) {
		this.matchService = matchService;
		this.dartBotService = dartBotService;
	}

	@MessageMapping(Websockets.X01_THROW_DART_BOT)
	@SendTo(Websockets.X01_MATCH)
	public X01Match throwDartBot(@Valid @Payload X01DartBotThrow x01DartBotThrow) throws IOException {

		X01Throw x01Throw = dartBotService.dartBotThrow(x01DartBotThrow);

		return matchService.updateMatch(x01Throw);
	}

	@MessageMapping(Websockets.X01_START_MATCH)
	@SendTo(Websockets.X01_MATCH)
	public X01Match startMatch(@DestinationVariable ObjectId matchId) {

		return matchService.startMatch(matchId);
	}

	@MessageMapping(Websockets.X01_ADD_THROW)
	@SendTo(Websockets.X01_MATCH)
	public X01Match addThrow(@Valid @Payload X01Throw x01Throw) throws IOException {

		return matchService.updateMatch(x01Throw);
	}

	@MessageMapping(Websockets.X01_DELETE_THROW)
	@SendTo(Websockets.X01_MATCH)
	public X01Match deleteThrowLiveMatch(@Valid @Payload X01DeleteThrow x01DeleteThrow) {

		return matchService.updateMatch(x01DeleteThrow);
	}

	@MessageMapping(Websockets.X01_DELETE_SET)
	@SendTo(Websockets.X01_MATCH)
	public X01Match deleteSetLiveMatch(@Valid @Payload X01DeleteSet x01DeleteSet) {

		return matchService.updateMatch(x01DeleteSet);
	}

	@MessageMapping(Websockets.X01_DELETE_LEG)
	@SendTo(Websockets.X01_MATCH)
	public X01Match deleteLegLiveMatch(@Valid @Payload X01DeleteLeg x01DeleteLeg) {

		return matchService.updateMatch(x01DeleteLeg);
	}

	@SubscribeMapping(Websockets.X01_MATCH)
	public X01Match subscribeMatch(@DestinationVariable ObjectId matchId) {

		return matchService.getMatch(matchId);
	}

}

// http://jxy.me/websocket-debug-tool/

// Websocket Url:
// ws://localhost:8080/darts-matcher-websocket/websocket

// STOMP subscribe destination:
// /live/matches/60548c38ccf8f5463f9d3402

// STOMP send destination:
// /live/matches/60548c38ccf8f5463f9d3402:update

// message content:
/*
{
	"matchId": "60548c38ccf8f5463f9d3402",
	"playerId": "Kevinmartin0",
	"leg": 1,
	"set": 1,
	"round": 1,
	"score": 60
}
 */