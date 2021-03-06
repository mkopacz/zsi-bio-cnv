package pl.edu.pw.elka.cnv.utils

import org.scalatest.Matchers
import pl.edu.pw.elka.cnv.SparkFunSuite
import pl.edu.pw.elka.cnv.utils.ConversionUtils.{bedFileToChromosomesMap, bedFileToRegionChromosomes, bedFileToRegionCoords, bedFileToRegionLengths, chrStrToInt, coverageToRegionCoverage, decodeCoverageId, encodeCoverageId}

import scala.collection.mutable

/**
 * Created by mariusz-macbook on 30/04/15.
 */
class ConversionUtilsFunSuite extends SparkFunSuite with Matchers {

  test("bedFileToChromosomesMap test") {
    val input = mutable.HashMap(
      2429 ->(1, 19203909, 19204106),
      101874 ->(10, 113928069, 113928282),
      179177 ->(20, 47115835, 47116753))

    val output = bedFileToChromosomesMap(input)

    output.keys should have size (3)
    all(output.values) should have size (25000)

    output.keys should contain theSameElementsAs Array(1, 10, 20)
    output(1)(19203909 / 10000) should contain theSameElementsAs Array((2429, 19203909, 19204106))
    output(10)(113928069 / 10000) should contain theSameElementsAs Array((101874, 113928069, 113928282))
    output(20)(47115835 / 10000) should contain theSameElementsAs Array((179177, 47115835, 47116753))
  }

  test("bedFileToRegionChromosomes test") {
    val input = Array(
      (2429, 1, 19203909, 19204106),
      (101874, 10, 113928069, 113928282),
      (179177, 20, 47115835, 47116753))

    val output = bedFileToRegionChromosomes(input)

    output.keys should have size (3)

    output.keys should contain theSameElementsAs Array(2429, 101874, 179177)
    output(2429) should be(1)
    output(101874) should be(10)
    output(179177) should be(20)
  }

  test("bedFileToRegionLengths test") {
    val input = Array(
      (2429, 1, 19203909, 19204106),
      (101874, 10, 113928069, 113928282),
      (179177, 20, 47115835, 47116753))

    val output = bedFileToRegionLengths(input)

    output.keys should have size (3)

    output.keys should contain theSameElementsAs Array(2429, 101874, 179177)
    output(2429) should be(19204106 - 19203909 + 1)
    output(101874) should be(113928282 - 113928069 + 1)
    output(179177) should be(47116753 - 47115835 + 1)
  }

  test("bedFileToRegionCoords test") {
    val input = Array(
      (2429, 1, 19203909, 19204106),
      (101874, 10, 113928069, 113928282),
      (179177, 20, 47115835, 47116753))

    val output = bedFileToRegionCoords(input)

    output.keys should have size (3)

    output.keys should contain theSameElementsAs Array(2429, 101874, 179177)
    output(2429) should be((19203909, 19204106))
    output(101874) should be((113928069, 113928282))
    output(179177) should be((47115835, 47116753))
  }

  sparkTest("coverageToRegionCoverage test") {
    val input = sc parallelize {
      Array((0L, 100),
        (5000425385L, 200),
        (12092619574L, 300))
    }
    val output = coverageToRegionCoverage(input).collect.toMap

    output.keys should have size (3)
    all(output.values) should have size (1)

    output.keys should contain theSameElementsAs Array(0, 425385, 92619574)
    output(0) should contain theSameElementsAs Array((0, 100))
    output(425385) should contain theSameElementsAs Array((5, 200))
    output(92619574) should contain theSameElementsAs Array((12, 300))
  }

  test("encodeCoverageId test") {
    encodeCoverageId(0, 0) should be(0L)
    encodeCoverageId(5, 425385) should be(5000425385L)
    encodeCoverageId(12, 92619574) should be(12092619574L)
  }

  test("decodeCoverageId test") {
    decodeCoverageId(0L) should be((0, 0))
    decodeCoverageId(5000425385L) should be((5, 425385))
    decodeCoverageId(12092619574L) should be((12, 92619574))
  }

  test("chrStrToInt test") {
    chrStrToInt("chr10") should be(10)
    chrStrToInt("chrX") should be(23)
    chrStrToInt("test") should be(0)
  }

}
