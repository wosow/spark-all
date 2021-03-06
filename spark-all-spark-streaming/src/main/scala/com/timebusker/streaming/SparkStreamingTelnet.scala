package com.timebusker.streaming

import com.timebusker.stream.common.MySQLExecuteUtil
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
 * @DESC:SparkStreamingTelnet
 * @author:timebusker
 * @date:2019/8/30
 */
object SparkStreamingTelnet {

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setMaster("local[2]").setAppName("NetworkWordCount")
    val streaming = new StreamingContext(conf, Seconds(1))

    val lines = streaming.socketTextStream("12.12.12.3", 9999)
    val words = lines.flatMap(_.split(" "))
    val map = words.map(word => (word, 1))
    val count = map.reduceByKey(_ + _)

    val res = count.mapPartitions(_.map(x => {
      val word = x._1
      val cnt = x._2
      MySQLExecuteUtil.execute(word, cnt)
    }))
    res.print()
    streaming.start()
    streaming.awaitTermination()
  }
}
