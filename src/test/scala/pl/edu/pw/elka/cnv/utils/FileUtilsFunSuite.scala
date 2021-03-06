package pl.edu.pw.elka.cnv.utils

import org.scalatest.Matchers
import pl.edu.pw.elka.cnv.SparkFunSuite
import pl.edu.pw.elka.cnv.utils.FileUtils.{loadReads, readRegionFile, scanForSamples}

/**
 * Created by mariusz-macbook on 25/08/15.
 */
class FileUtilsFunSuite extends SparkFunSuite with Matchers {

  test("scanForSamples test") {
    val samples = scanForSamples(getClass.getResource("/").toString)

    samples.keys should have size (1)
    samples.keys should contain theSameElementsAs Array(0)

    samples(0) should be(getClass.getResource("/test_bam_file.bam").toString)
  }

  sparkTest("loadReads test") {
    val samples = Map(0 -> getClass.getResource("/test_bam_file.bam").toString)
    val reads = loadReads(sc, samples).collect

    reads should have size (385721)

    reads(0)._1 should be(0)
    reads(0)._2.getAlignmentStart should be(809713)
    reads(0)._2.getAlignmentEnd should be(809812)

    reads(192860)._1 should be(0)
    reads(192860)._2.getAlignmentStart should be(21026808)
    reads(192860)._2.getAlignmentEnd should be(21026907)

    reads(385720)._1 should be(0)
    reads(385720)._2.getAlignmentStart should be(90251)
    reads(385720)._2.getAlignmentEnd should be(90353)
  }

  sparkTest("readRegionFile test") {
    val bedFile = readRegionFile(sc, getClass.getResource("/test_bed_file.bed").toString)

    bedFile.keys should have size (223378)
    bedFile.keys.min should be(0)
    bedFile.keys.max should be(223377)

    bedFile(0) should be(1, 69091, 69290)
    bedFile(55844) should be(12, 104378527, 104378698)
    bedFile(111688) should be(19, 42471401, 42471492)
    bedFile(167533) should be(5, 14366969, 14367088)
    bedFile(223377) should be(24, 27770601, 27770674)
  }

}
