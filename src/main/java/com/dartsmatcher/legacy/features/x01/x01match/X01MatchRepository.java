package com.dartsmatcher.legacy.features.x01.x01match;

import com.dartsmatcher.legacy.features.x01.x01match.models.X01Match;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.ArrayList;

public interface X01MatchRepository extends MongoRepository<X01Match, ObjectId> {
	ArrayList<X01Match> findAll();

	@Query("{players: {$elemMatch: {playerId: ?0, playerType:\"REGISTERED\"}}}")
	ArrayList<X01Match> findAllByRegisteredPlayerId(ObjectId playerId);
}
