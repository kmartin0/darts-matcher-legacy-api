package com.dartsmatcher.legacy.features.x01.x01livematch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import javax.validation.constraints.Min;

@Data
@AllArgsConstructor
@NoArgsConstructor
// TODO: Add PlayerId so that not everyone can simply delete a set.
public class X01DeleteSet {

	private ObjectId matchId;

	@Min(0)
	private int set;

}
