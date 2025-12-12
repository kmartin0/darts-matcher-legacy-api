package com.dartsmatcher.dartsmatcherapi.features;

import com.dartsmatcher.legacy.features.dartboard.Dart;
import com.dartsmatcher.legacy.features.dartboard.DartBoard;
import com.dartsmatcher.legacy.features.dartboard.DartBoardSectionArea;
import com.dartsmatcher.legacy.utils.CartesianCoordinate;
import com.dartsmatcher.legacy.utils.PolarCoordinate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DartBoardTests {

    private DartBoard dartBoard;

    @BeforeEach
    void beforeEach() {
        dartBoard = new DartBoard();
    }

    @Test
    void test_0_0_isBull() {
        Dart expectedDart = new Dart(50, DartBoardSectionArea.DOUBLE_BULL);
        Assertions.assertEquals(expectedDart, dartBoard.getScoreCartesian(new CartesianCoordinate(0, 0)));
    }

    @Test
    void test_2_101_isTriple20() {
        Dart expectedDart = new Dart(20, DartBoardSectionArea.TRIPLE);
        Assertions.assertEquals(expectedDart, dartBoard.getScoreCartesian(new CartesianCoordinate(2, 101)));
    }

    @Test
    void test_Neg163_Neg1_isDouble11() {
        Dart expectedDart = new Dart(11, DartBoardSectionArea.DOUBLE);
        Assertions.assertEquals(expectedDart, dartBoard.getScoreCartesian(new CartesianCoordinate(-163, -1)));
    }

    @Test
    void test_6_18_isInnerSingle1() {
        Dart expectedDart = new Dart(1, DartBoardSectionArea.INNER_SINGLE);
        Assertions.assertEquals(expectedDart, dartBoard.getScoreCartesian(new CartesianCoordinate(6, 18)));
    }

    @Test
    void test_Neg6_18_isInnerSingle5() {
        Dart expectedDart = new Dart(5, DartBoardSectionArea.INNER_SINGLE);
        Assertions.assertEquals(expectedDart, dartBoard.getScoreCartesian(new CartesianCoordinate(-6, 18)));
    }

    @Test
    void test_45_Neg169_isMiss() {
        Dart expectedDart = new Dart(17, DartBoardSectionArea.MISS);
        Assertions.assertEquals(expectedDart, dartBoard.getScoreCartesian(new CartesianCoordinate(45, -169)));
    }

    @Test
    void test_22_22_isInnerSingle4() {
        Dart expectedDart = new Dart(4, DartBoardSectionArea.INNER_SINGLE);
        Assertions.assertEquals(expectedDart, dartBoard.getScoreCartesian(new CartesianCoordinate(22, 22)));
    }

    @Test
    void test_Neg150_0_isOuterSingle11() {
        Dart expectedDart = new Dart(11, DartBoardSectionArea.OUTER_SINGLE);
        Assertions.assertEquals(expectedDart, dartBoard.getScoreCartesian(new CartesianCoordinate(-150, 0)));
    }

    @Test
    void test_Neg100_5_isTriple17() {
        Dart expectedDart = new Dart(17, DartBoardSectionArea.TRIPLE);
        Assertions.assertEquals(expectedDart, dartBoard.getScoreCartesian(new CartesianCoordinate(16, -101)));
    }

    @Test
    void test_8_0_isSingleBull() {
        Dart expectedDart = new Dart(25, DartBoardSectionArea.SINGLE_BULL);
        Assertions.assertEquals(expectedDart, dartBoard.getScoreCartesian(new CartesianCoordinate(8, 0)));
    }

    @Test
    void testGetCenter_6_InnerSingle() {
        PolarCoordinate expectedPolarCoordinate = new PolarCoordinate(57, 0);
        Assertions.assertEquals(expectedPolarCoordinate, dartBoard.getCenter(6, DartBoardSectionArea.INNER_SINGLE));
    }

    @Test
    void testGetCenter_15_OuterSingle() {
        PolarCoordinate expectedPolarCoordinate = new PolarCoordinate(133.5, 5.654866776461628);
        Assertions.assertEquals(expectedPolarCoordinate, dartBoard.getCenter(15, DartBoardSectionArea.OUTER_SINGLE));
    }

    @Test
    void testGetCenter_17_Triple() {
        PolarCoordinate expectedPolarCoordinate = new PolarCoordinate(102, 5.026548245743669);
        Assertions.assertEquals(expectedPolarCoordinate, dartBoard.getCenter(17, DartBoardSectionArea.TRIPLE));
    }

    @Test
    void testGetCenter_5_Double() {
        PolarCoordinate expectedPolarCoordinate = new PolarCoordinate(165, 1.8849555921538759);
        Assertions.assertEquals(expectedPolarCoordinate, dartBoard.getCenter(5, DartBoardSectionArea.DOUBLE));
    }

    @Test
    void testGetCenter_20_All() {
        PolarCoordinate expectedCenterDoubleBull20 = new PolarCoordinate(0, 0);
        PolarCoordinate expectedCenterSingleBull20 = new PolarCoordinate(12, 1.5707963267948966);
        PolarCoordinate expectedCenterInnerSingle20 = new PolarCoordinate(57, 1.5707963267948966);
        PolarCoordinate expectedCenterTriple20 = new PolarCoordinate(102, 1.5707963267948966);
        PolarCoordinate expectedCenterOuterSingle20 = new PolarCoordinate(133.5, 1.5707963267948966);
        PolarCoordinate expectedCenterDouble20 = new PolarCoordinate(165, 1.5707963267948966);


        Assertions.assertEquals(expectedCenterDoubleBull20, dartBoard.getCenter(20, DartBoardSectionArea.DOUBLE_BULL));
        Assertions.assertEquals(expectedCenterSingleBull20, dartBoard.getCenter(20, DartBoardSectionArea.SINGLE_BULL));
        Assertions.assertEquals(expectedCenterInnerSingle20, dartBoard.getCenter(20, DartBoardSectionArea.INNER_SINGLE));
        Assertions.assertEquals(expectedCenterTriple20, dartBoard.getCenter(20, DartBoardSectionArea.TRIPLE));
        Assertions.assertEquals(expectedCenterOuterSingle20, dartBoard.getCenter(20, DartBoardSectionArea.OUTER_SINGLE));
        Assertions.assertEquals(expectedCenterDouble20, dartBoard.getCenter(20, DartBoardSectionArea.DOUBLE));
    }

    @Test
    void testGetCenter_11_All() {
        PolarCoordinate expectedCenterDoubleBull20 = new PolarCoordinate(0, 0);
        PolarCoordinate expectedCenterSingleBull20 = new PolarCoordinate(12, Math.PI);
        PolarCoordinate expectedCenterInnerSingle20 = new PolarCoordinate(57, Math.PI);
        PolarCoordinate expectedCenterTriple20 = new PolarCoordinate(102, Math.PI);
        PolarCoordinate expectedCenterOuterSingle20 = new PolarCoordinate(133.5, Math.PI);
        PolarCoordinate expectedCenterDouble20 = new PolarCoordinate(165, Math.PI);

        Assertions.assertEquals(expectedCenterDoubleBull20, dartBoard.getCenter(11, DartBoardSectionArea.DOUBLE_BULL));
        Assertions.assertEquals(expectedCenterSingleBull20, dartBoard.getCenter(11, DartBoardSectionArea.SINGLE_BULL));
        Assertions.assertEquals(expectedCenterInnerSingle20, dartBoard.getCenter(11, DartBoardSectionArea.INNER_SINGLE));
        Assertions.assertEquals(expectedCenterTriple20, dartBoard.getCenter(11, DartBoardSectionArea.TRIPLE));
        Assertions.assertEquals(expectedCenterOuterSingle20, dartBoard.getCenter(11, DartBoardSectionArea.OUTER_SINGLE));
        Assertions.assertEquals(expectedCenterDouble20, dartBoard.getCenter(11, DartBoardSectionArea.DOUBLE));
    }

}
