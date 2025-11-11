import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.HashPartitioner

object HashPartitionExample {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("HashPartitionExample").setMaster("local[*]")
    val sc = new SparkContext(conf)
    
    // Read the text file
    val lines = sc.textFile("/input/word1.txt")
    
    // Split lines into words
    val words = lines.flatMap(line => line.split(" "))
    
    // Count the individual words using map and reduceByKey operation
    val wordCount = words.map(word => (word, 1)).reduceByKey((a, b) => a + b)
    
    // Apply HashPartitioner with 2 partitions
    val parti = wordCount.partitionBy(new HashPartitioner(2))
    
    // Save the result
    parti.saveAsTextFile("/output/output_hash_partition")
    
    // Stop the spark context
    sc.stop()
  }
}
