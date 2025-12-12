package com.dartsmatcher.legacy.features.dartboard;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DartBoardSectionAreaDimen {

	private DartBoardSectionArea sectionArea;
	private int inner;
	private int outer;

}
