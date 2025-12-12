# Darts Matcher Legacy Api

Deprecated Api for the legacy Darts Matcher platform made using Spring Boot.

## Features
- Authenticate and register users
- Recover from lost passwords
- Create darts matches with up to 4 players.
- Create darts matches with bots.
- Configure and play against a darts bot.
- Recover from mistakes during matches.
- Generate statistics for each match.

## Technical Overview
- Layered Architecture.
- MongoDB database communication using Spring Data
- Globalized error handling.
- Localization.
- OAuth 2 and JWT for user authentication/authorization (Spring Security).
- Usage of websocket for live updates (Spring Websockets)
