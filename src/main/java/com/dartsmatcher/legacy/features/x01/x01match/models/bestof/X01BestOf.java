package com.dartsmatcher.legacy.features.x01.x01match.models.bestof;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class X01BestOf {

	@Min(1)
	private int legs;

	@Min(1)
	private int sets;

}
