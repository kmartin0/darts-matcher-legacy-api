package com.dartsmatcher.legacy.features.x01.x01match.models.set;

import com.dartsmatcher.legacy.features.x01.x01match.models.leg.X01Leg;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Optional;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class X01Set {

	@Min(1)
	private int set;

	private ArrayList<X01SetPlayerResult> result;

	@NotNull
	@Valid
	private ArrayList<X01Leg> legs;

	@JsonIgnore
	public Optional<X01Leg> getLeg(int legNumber) {
		return getLegs().stream()
				.filter(leg -> leg.getLeg() == legNumber)
				.findFirst();
	}

}
