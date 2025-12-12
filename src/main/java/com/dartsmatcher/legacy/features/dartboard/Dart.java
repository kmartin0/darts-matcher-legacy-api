package com.dartsmatcher.legacy.features.dartboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Dart {

	public Dart(Dart dart) {
		this(dart.getSection(), dart.getArea());
	}

	// TODO: Custom validator checks if valid section (1...20, 25, 50)
	private int section;

	private DartBoardSectionArea area;

	public int getScore() {
		return section * area.getMultiplier();
	}
}
