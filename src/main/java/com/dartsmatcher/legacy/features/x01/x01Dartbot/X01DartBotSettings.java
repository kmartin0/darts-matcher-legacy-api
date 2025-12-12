package com.dartsmatcher.legacy.features.x01.x01Dartbot;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class X01DartBotSettings {

    public static final int MINIMUM_BOT_AVG = 1;

    @Min(MINIMUM_BOT_AVG)
    private int expectedThreeDartAverage;

}
