package pl.edu.pw.elka.cnv.conifer

import org.apache.hadoop.io.LongWritable
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.seqdoop.hadoop_bam.{BAMInputFormat, SAMRecordWritable}

object Test {

  def main(args: Array[String]) {
    val conf = new SparkConf()
      .setAppName("CNV")
      .setMaster("local")
    val sc = new SparkContext(conf)

    val probesFile = loadProbesFile(sc,
      "/Users/mariusz-macbook/IdeaProjects/zsi-bio-cnv/resources/probes.txt")
    val bamFile = loadBAMFile(sc,
      "/Users/mariusz-macbook/IdeaProjects/zsi-bio-cnv/resources/NA12878.chrom11.ILLUMINA.bwa.CEU.low_coverage.20121211.bam")

    val conifer = new Conifer(probesFile, bamFile)
    saveRPKMs(conifer.calculateRPKMs)
  }


  private def loadBAMFile(sc: SparkContext, path: String): RDD[(LongWritable, SAMRecordWritable)] =
    sc.newAPIHadoopFile[LongWritable, SAMRecordWritable, BAMInputFormat](path)

  private def loadProbesFile(sc: SparkContext, path: String): RDD[String] =
    sc.textFile(path)

  private def saveRPKMs(rpkms: RDD[(Long, Long, Long)]) =
    rpkms.saveAsObjectFile("/Users/mariusz-macbook/IdeaProjects/zsi-bio-cnv/resources/rpkms.txt")

}
