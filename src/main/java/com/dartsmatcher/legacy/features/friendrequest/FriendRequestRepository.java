package com.dartsmatcher.legacy.features.friendrequest;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.ArrayList;
import java.util.Optional;

public interface FriendRequestRepository extends MongoRepository<FriendRequest, ObjectId> {

	@Query("{ '$and': [ { 'sender' : { id: ?0 } }, { 'receiver' : { id: ?1 } } ] }")
	ArrayList<FriendRequest> findBySenderAndReceiver(ObjectId sender, ObjectId receiver);

	// TODO: Sort by date
	@Aggregation({
			"{ $match: { $or: [ { 'sender._id': '?0' }, { 'receiver._id': '?1' } ] } }",
			"{ $lookup: { " +
					"from : 'users', " +
					"let: { receiver: '$receiver' }, " +
					"pipeline: [ { $match: { $expr: { $eq: [ '$_id', '$$receiver._id' ] } } } ], " +
					"as: 'receiver' } " +
					"}",
			"{ $lookup: { " +
					"from : 'users', " +
					"let: { sender: '$sender' }, " +
					"pipeline: [ { $match: { $expr: { $eq: [ '$_id', '$$sender._id' ] } } } ], " +
					"as: 'sender' } " +
					"}",
			"{ $addFields: {" +
					"'receiver': { $arrayElemAt: [ '$receiver', 0 ] }, " +
					"'sender': { $arrayElemAt: [ '$sender', 0 ] }" +
					"} }"
	})
	Optional<ArrayList<FriendRequest>> findBySenderOrReceiver(ObjectId sender, ObjectId receiver);
}
