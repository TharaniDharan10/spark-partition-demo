import org.apache.spark.{SparkConf, SparkContext, Partitioner}

object CustomPartitionExample {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("CustomPartitionExample").setMaster("local[*]")
    val sc = new SparkContext(conf)
    
    // Read the text file
    val lines = sc.textFile("/input/word1.txt")
    
    // Split lines into words
    val words = lines.flatMap(line => line.split(" "))
    
    // Count the individual words using map and reduceByKey operation
    val wordCount = words.map(word => (word, 1)).reduceByKey((a, b) => a + b)
    
    // Apply custom partitioner with 2 partitions
    val parti = wordCount.partitionBy(new MyCustomPartitioner(2))
    
    // Save the result
    parti.saveAsTextFile("/output/output_custom_partition")
    
    // Stop the spark context
    sc.stop()
  }
}

class MyCustomPartitioner(numParts: Int) extends Partitioner {
  override def numPartitions: Int = numParts
  
  override def getPartition(key: Any): Int = {
    val k = key.toString()
    if(k.equalsIgnoreCase("welcome"))
      0
    else if(k.equalsIgnoreCase("hi") || k.equalsIgnoreCase("hello"))
      1 % numPartitions
    else {
      val hash = k.hashCode()
      val mod = hash % numPartitions
      if (mod < 0) mod + numPartitions else mod
    }
  }
}
