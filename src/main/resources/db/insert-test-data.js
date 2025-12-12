// Database connection
conn = new Mongo()
db = conn.getDB("darts-matcher-legacy")

// ========================= CLIENT DETAILS =========================

// Insert Client Details (clientSecret = secret)
clientDetailsResult = db.client_details.insertMany([
    {
        "clientId": "darts-matcher-dev",
        "clientSecret": "$2a$12$o3dmbF3ElqPL1ApJ.9R/Qu7cVBMyV8pn80.HPFPdKO/jerqGJiXZe",
        "scope": "all",
        "authorizedGrantTypes": "password,refresh_token,client_credentials",
        "authorities": "ROLE_CLIENT",
        "accessTokenValidity": 172800,
        "refreshTokenValidity": 604800,
        "_class": "ClientDetailsImpl"
    },
    {
        "clientId": "darts-matcher-web",
        "clientSecret": "$2a$12$o3dmbF3ElqPL1ApJ.9R/Qu7cVBMyV8pn80.HPFPdKO/jerqGJiXZe",
        "scope": "all",
        "authorizedGrantTypes": "password,refresh_token,client_credentials",
        "authorities": "ROLE_CLIENT",
        "accessTokenValidity": 172800,
        "refreshTokenValidity": 604800,
        "_class": "ClientDetailsImpl"
    }
])
print(clientDetailsResult)

// ========================= USERS =========================

// Create reference user id's
user1ObjectId = ObjectId()
user2ObjectId = ObjectId()

// Insert users (password = secret)
usersResult = db.users.insert([
    {
        "_id": user1ObjectId,
        "userName": "JohnDoe",
        "firstName": "John",
        "lastName": "Doe",
        "email": "johndoe@email.com",
        "password": "$2a$12$o3dmbF3ElqPL1ApJ.9R/Qu7cVBMyV8pn80.HPFPdKO/jerqGJiXZe",
        "friends": [],
        "_class": "User"
    },
    {
        "_id": user2ObjectId,
        "userName": "JaneDoe",
        "firstName": "Jane",
        "lastName": "Doe",
        "email": "janedoe@email.com",
        "password": "$2a$12$o3dmbF3ElqPL1ApJ.9R/Qu7cVBMyV8pn80.HPFPdKO/jerqGJiXZe",
        "friends": [],
        "_class": "User"
    }
])
print(usersResult)

// ========================= PASSWORD TOKENS =========================

// Insert Password Tokens
passwordResult = db.password_tokens.insert([
    {
        "user": user1ObjectId,
        "token": new UUID('7f019466-3845-443c-abf6-09780ca64fc2'),
        "expiration": new Date(ISODate().getTime() - 1000 * 3600 * 24 * 7),
        "_class": "PasswordToken"
    }
])

print(passwordResult)

// ========================= MATCHES =========================

// Insert Matches
matchesResult = db.matches.insert([
    {
        "startDate": new Date("2021-01-02T14:12:00Z"),
        "endDate": new Date("2021-01-02T14:32:00Z"),
        "x01MatchSettings": {
            "x01": 501,
            "trackDoubles": false,
            "bestOf": {
                "legs": 3,
                "sets": 1,
            },
        },
        "matchType": "X01",
        "matchStatus": "CONCLUDED",
        "currentThrower": null,
        "players": [
            {"playerId": user1ObjectId, "firstName": "John", "lastName": "Doe", "playerType": "REGISTERED", "inviteStatus": "ACCEPTED"},
            {"playerId": user2ObjectId, "firstName": "Jane", "lastName": "Doe", "playerType": "REGISTERED", "inviteStatus": "ACCEPTED"}
        ],
        "matchResult": [
            {"playerId": user1ObjectId, "score": 2, "result": "WIN"},
            {"playerId": user2ObjectId, "score": 1, "result": "LOSE"}
        ],
        "x01Result": [
            {"playerId": user1ObjectId, "legsWon": 2, "setsWon": 1, "result": "WIN"},
            {"playerId": user2ObjectId, "legsWon": 1, "setsWon": 0, "result": "LOSE"}
        ],
        "statistics": [
            {
                "playerId": user1ObjectId,
                "averageStats": {
                    "pointsThrown": 1242,
                    "dartsThrown": 34,
                    "average": 109,
                    "pointsThrownFirstNine": 961,
                    "dartsThrownFirstNine": 24,
                    "averageFirstNine": 120
                },
                "checkoutStats": {
                    "checkoutHighest": 31,
                    "checkoutTonPlus": 0,
                    "checkoutPercentage": 33,
                    "checkoutsMissed": 4,
                    "checkoutsHit": 2
                },
                "scoresStats": {
                    "fortyPlus": 0,
                    "sixtyPlus": 0,
                    "eightyPlus": 3,
                    "tonPlus": 3,
                    "tonForty": 3,
                    "tonEighty": 1
                },
            },
            {
                "playerId": user2ObjectId,
                "averageStats": {
                    "pointsThrown": 1194,
                    "dartsThrown": 33,
                    "average": 108,
                    "pointsThrownFirstNine": 971,
                    "dartsThrownFirstNine": 27,
                    "averageFirstNine": 107
                },
                "checkoutStats": {
                    "checkoutHighest": 141,
                    "checkoutTonPlus": 1,
                    "checkoutPercentage": 100,
                    "checkoutsMissed": 0,
                    "checkoutsHit": 1
                },
                "scoresStats": {
                    "fortyPlus": 1,
                    "sixtyPlus": 0,
                    "eightyPlus": 2,
                    "tonPlus": 2,
                    "tonForty": 1,
                    "tonEighty": 3,
                },
            }
        ],
        "timeline": [
            {
                "set": 1,
                "result": [
                    {"playerId": user1ObjectId, "legsWon": 2, "result": "WIN"},
                    {"playerId": user2ObjectId, "legsWon": 1, "result": "LOSE"},
                ],
                "legs": [
                    {
                        "leg": 1,
                        "winner": user1ObjectId,
                        "throwsFirst": user1ObjectId,
                        "rounds": [
                            {
                                "round": 1,
                                "playerScores": [
                                    {"playerId": user1ObjectId, "doublesMissed": 0, "dartsUsed": 3, "score": 140},
                                    {"playerId": user2ObjectId, "doublesMissed": 0, "dartsUsed": 3, "score": 120},
                                ]
                            },
                            {
                                "round": 2,
                                "playerScores": [
                                    {"playerId": user1ObjectId, "doublesMissed": 0, "dartsUsed": 3, "score": 100},
                                    {"playerId": user2ObjectId, "doublesMissed": 0, "dartsUsed": 3, "score": 88},
                                ]
                            },
                            {
                                "round": 3,
                                "playerScores": [
                                    {"playerId": user1ObjectId, "doublesMissed": 0, "dartsUsed": 3, "score": 91},
                                    {"playerId": user2ObjectId, "doublesMissed": 0, "dartsUsed": 3, "score": 27},
                                ]
                            },
                            {
                                "round": 4,
                                "playerScores": [
                                    {"playerId": user1ObjectId, "doublesMissed": 1, "dartsUsed": 3, "score": 140},
                                    {"playerId": user2ObjectId, "doublesMissed": 0, "dartsUsed": 3, "score": 180},
                                ]
                            },
                            {
                                "round": 5,
                                "playerScores": [
                                    {"playerId": user1ObjectId, "doublesMissed": 1, "dartsUsed": 2, "score": 30}
                                ]
                            }
                        ]
                    },
                    {
                        "leg": 2,
                        "winner": user2ObjectId,
                        "throwsFirst": user2ObjectId,
                        "rounds": [
                            {
                                "round": 1,
                                "playerScores": [
                                    {"playerId": user1ObjectId, "doublesMissed": 0, "dartsUsed": 3, "score": 140},
                                    {"playerId": user2ObjectId, "doublesMissed": 0, "dartsUsed": 3, "score": 180},
                                ]
                            },
                            {
                                "round": 2,
                                "playerScores": [
                                    {"playerId": user1ObjectId, "doublesMissed": 0, "dartsUsed": 3, "score": 100},
                                    {"playerId": user2ObjectId, "doublesMissed": 0, "dartsUsed": 3, "score": 180},
                                ]
                            },
                            {
                                "round": 3,
                                "playerScores": [
                                    {"playerId": user2ObjectId, "doublesMissed": 0, "dartsUsed": 3, "score": 141},
                                ]
                            }
                        ]
                    },
                    {
                        "leg": 3,
                        "winner": user1ObjectId,
                        "throwsFirst": user1ObjectId,
                        "rounds": [
                            {
                                "round": 1,
                                "playerScores": [
                                    {"playerId": user1ObjectId, "doublesMissed": 0, "dartsUsed": 3, "score": 121},
                                    {"playerId": user2ObjectId, "doublesMissed": 0, "dartsUsed": 3, "score": 120},
                                ]
                            },
                            {
                                "round": 2,
                                "playerScores": [
                                    {"playerId": user1ObjectId, "doublesMissed": 0, "dartsUsed": 3, "score": 180},
                                    {"playerId": user2ObjectId, "doublesMissed": 0, "dartsUsed": 3, "score": 88},
                                ]
                            },
                            {
                                "round": 3,
                                "playerScores": [
                                    {"playerId": user1ObjectId, "doublesMissed": 0, "dartsUsed": 3, "score": 90},
                                    {"playerId": user2ObjectId, "doublesMissed": 0, "dartsUsed": 3, "score": 27},
                                ]
                            },
                            {
                                "round": 4,
                                "playerScores": [
                                    {"playerId": user1ObjectId, "doublesMissed": 1, "dartsUsed": 3, "score": 90},
                                    {"playerId": user2ObjectId, "doublesMissed": 0, "dartsUsed": 3, "score": 43},
                                ]
                            },
                            {
                                "round": 5,
                                "playerScores": [
                                    {"playerId": user1ObjectId, "doublesMissed": 1, "dartsUsed": 2, "score": 20}
                                ]
                            }
                        ]
                    }
                ]
            }
        ],
        "_class": "X01Match"
    }
])
print(matchesResult)