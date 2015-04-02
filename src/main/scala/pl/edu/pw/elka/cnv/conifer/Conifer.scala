package pl.edu.pw.elka.cnv.conifer

import htsjdk.samtools.SAMRecord
import org.apache.hadoop.io.LongWritable
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext.rddToPairRDDFunctions
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.seqdoop.hadoop_bam.{BAMInputFormat, SAMRecordWritable}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class Conifer(@transient sc: SparkContext, probesFilePath: String, bamFilePaths: Array[String]) extends Serializable with CNVUtils {

  private val probes: Array[(String, Long, Long)] =
    sc.textFile(probesFilePath) map {
      line => line.split("\t") match {
        case Array(chr, start, stop, _*) =>
          (chr, start.toLong, stop.toLong)
      }
    } collect

  private val exonsByChromosome: Broadcast[mutable.HashMap[String, ArrayBuffer[(Long, Long, Long)]]] = {
    val result = new mutable.HashMap[String, ArrayBuffer[(Long, Long, Long)]]
    var counter = 1

    probes foreach {
      case (chr, start, stop) => {
        if (!result.contains(chr))
          result(chr) = new ArrayBuffer[(Long, Long, Long)]()
        result(chr) += ((counter, start, stop))
        counter = counter + 1
      }
    }

    sc.broadcast(result)
  }

  val calculateRPKMs: RDD[(Long, Iterable[Float])] =
    bamFilePaths.map(loadBAMFile).map(getRPKMs).reduce(_ ++ _).groupByKey

  def calculateZRPKMs(minMedian: Float): RDD[(Long, Iterable[Float])] =
    calculateRPKMs mapValues {
      case rpkms => (rpkms, median(rpkms.toSeq), stddev(rpkms.toSeq))
    } filter {
      case (_, (_, median, _)) => median >= minMedian
    } flatMap {
      case (id, (rpkms, median, stddev)) =>
        rpkms.map(rpkm => (id, zrpkm(rpkm, median, stddev)))
    } groupByKey

  private def loadBAMFile(path: String): RDD[SAMRecord] =
    sc.newAPIHadoopFile[LongWritable, SAMRecordWritable, BAMInputFormat](path) map {
      read => read._2.get
    }

  private def getRPKMs(bamFile: RDD[SAMRecord]): RDD[(Long, Float)] = {
    val total = bamFile.count.toFloat
    getCoverage(bamFile) map {
      case ((id, start, stop), count) =>
        (id, rpkm(count, stop - start, total))
    }
  }

  private def getCoverage(bamFile: RDD[SAMRecord]): RDD[((Long, Long, Long), Long)] =
    bamFile.mapPartitions(partition =>
      for {
        read <- partition
        (id, start, stop) <- exonsByChromosome.value(read.getReferenceName)
        if (read.getAlignmentStart >= start && read.getAlignmentStart <= stop)
      } yield ((id, start, stop), 1L)
    ).reduceByKey(_ + _)

}
