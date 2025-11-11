# Spark Partitioning Examples - Hash & Custom Partitioner

This repository demonstrates two types of partitioning strategies in Apache Spark using Scala:
1. **Hash Partitioner** - Default Spark partitioning using hash functions
2. **Custom Partitioner** - User-defined partitioning logic based on specific word matching

## 📋 Prerequisites

- **Apache Spark**: 3.5.7
- **Scala**: 2.12.18
- **Java**: 1.8 or higher
- **Hadoop HDFS**: Running and accessible
- **Operating System**: Ubuntu (tested on Ubuntu with VM)

## 🛠️ Environment Setup

### 1. Fix Scala Version Compatibility Issue

If you encounter the following error:
```
java.lang.NoSuchMethodError: scala.Predef$.refArrayOps(...)
```

This means your Spark (3.5.7) is built with Scala 2.12.18, but your compiler (scalac) is an older version (e.g., 2.11.12).

**Solution: Install Scala 2.12.18**

```bash
# Step 1: Remove old Scala version
sudo apt remove scala -y

# Step 2: Download and install Scala 2.12.18
wget https://downloads.lightbend.com/scala/2.12.18/scala-2.12.18.tgz
tar -xvzf scala-2.12.18.tgz
sudo mv scala-2.12.18 /usr/local/scala

# Step 3: Add Scala to PATH
nano ~/.bashrc

# Add this line at the bottom of .bashrc:
export PATH=/usr/local/scala/bin:$PATH

# Reload the bashrc
source ~/.bashrc

# Step 4: Verify Scala version
scalac -version
# Expected output: Scala compiler version 2.12.18
```

### 2. Verify Spark Environment

```bash
# Check Spark version
$SPARK_HOME/bin/spark-submit --version

# Verify SPARK_HOME is set
echo $SPARK_HOME
```

## 📁 Project Structure

```
spark-partition-demo/
├── HashPartitionExample.scala
└── CustomPartitionExample.scala
```

## 📁 Note
I made these files inside spark-partition-demo in ubuntu(VMFusion) using Text Editor with .scala extension instead of creating files in my actual host system using some IDE and taking it to ubuntu

## 🚀 Getting Started

### Step 1: Prepare HDFS Input Directory

Execute these commands from **any terminal location**:

```bash
# Create input directory in HDFS
hdfs dfs -mkdir -p /input

# Create a sample input file locally
cat > /tmp/word1.txt << 'EOF'
hi hello hi hello welcome welcome
EOF

# Upload the file to HDFS
hdfs dfs -put /tmp/word1.txt /input/

# Verify the file is uploaded
hdfs dfs -ls /input/
hdfs dfs -cat /input/word1.txt
```

### Step 2: Create Project Directory and Files

Execute from **home directory**:

```bash
# Create project directory
mkdir -p /home/danny/spark-partition-demo
cd /home/danny/spark-partition-demo

# Create the Scala files using nano or any text editor
nano HashPartitionExample.scala
# Copy the HashPartitionExample.scala content

nano CustomPartitionExample.scala
# Copy the CustomPartitionExample.scala content

# Verify files are created
ls -la
```

### Step 3: Compile the Scala Files

Execute from **`/home/danny/spark-partition-demo`**:

```bash
cd /home/danny/spark-partition-demo

# Compile Hash Partition Example
mkdir -p classes
scalac -target:jvm-1.8 -d classes -classpath "$SPARK_HOME/jars/*" HashPartitionExample.scala
jar -cvf HashPartition.jar -C classes .

# Compile Custom Partition Example
rm -rf classes
mkdir -p classes
scalac -target:jvm-1.8 -d classes -classpath "$SPARK_HOME/jars/*" CustomPartitionExample.scala
jar -cvf CustomPartition.jar -C classes .

# Verify JAR files are created
ls -lh *.jar
```

### Step 4: Clean Previous Outputs (if any)

Execute from **any terminal location**:

```bash
# Remove previous output directories from HDFS
hdfs dfs -rm -r /output/output_hash_partition
hdfs dfs -rm -r /output/output_custom_partition
```

### Step 5: Run Hash Partition Example

Execute from **`/home/danny/spark-partition-demo`**:

```bash
cd /home/danny/spark-partition-demo

$SPARK_HOME/bin/spark-submit \
  --class HashPartitionExample \
  --master local[*] \
  HashPartition.jar
```

**Expected Output**: Job should complete successfully with logs showing shuffle operations.

### Step 6: Run Custom Partition Example

Execute from **`/home/danny/spark-partition-demo`**:

```bash
cd /home/danny/spark-partition-demo

$SPARK_HOME/bin/spark-submit \
  --class CustomPartitionExample \
  --master local[*] \
  CustomPartition.jar
```

**Expected Output**: Job should complete successfully with custom partitioning logic applied.

## 🔍 Verifying Results

### Check Output in HDFS

Execute from **any terminal location**:

```bash
# List output directories
hdfs dfs -ls /output/

# Check Hash Partition output
echo "=== Hash Partition Output ==="
hdfs dfs -ls /output/output_hash_partition/
hdfs dfs -cat /output/output_hash_partition/part-00000
hdfs dfs -cat /output/output_hash_partition/part-00001

# Check Custom Partition output
echo "=== Custom Partition Output ==="
hdfs dfs -ls /output/output_custom_partition/
hdfs dfs -cat /output/output_custom_partition/part-00000
hdfs dfs -cat /output/output_custom_partition/part-00001
```

### Copy Output to Local Filesystem (Optional)

Execute from **any terminal location**:

```bash
# Create local output directory
mkdir -p /home/danny/local_output

# Copy from HDFS to local
hdfs dfs -get /output/output_hash_partition /home/danny/local_output/
hdfs dfs -get /output/output_custom_partition /home/danny/local_output/

# View locally
cat /home/danny/local_output/output_hash_partition/part-00000
cat /home/danny/local_output/output_custom_partition/part-00000
```

## 📊 Understanding the Output

### Hash Partition Output
- Words are distributed across 2 partitions using Spark's default hash function
- Distribution is pseudo-random but consistent for the same key
- Files: `part-00000` and `part-00001`

### Custom Partition Output
- Partition 0: Contains word **"welcome"**
- Partition 1: Contains words **"hi"** and **"hello"**
- Other words: Distributed using modulo operation on hash code

Example output format: `(word,count)`

## 🔧 Troubleshooting

### Issue 1: `NoSuchMethodError: scala.Predef$.refArrayOps`
**Solution**: Install Scala 2.12.18 (see Environment Setup section)

### Issue 2: `HDFS not found` or connection errors
```bash
# Check if HDFS is running
jps
# Should show NameNode and DataNode

# Start HDFS if not running
$HADOOP_HOME/sbin/start-dfs.sh
```

### Issue 3: Output directory already exists
```bash
# Remove existing output directories
hdfs dfs -rm -r /output/output_hash_partition
hdfs dfs -rm -r /output/output_custom_partition
```

### Issue 4: Permission denied in HDFS
```bash
# Check HDFS permissions
hdfs dfs -ls /

# Create directories with proper permissions
hdfs dfs -mkdir -p /input
hdfs dfs -mkdir -p /output
hdfs dfs -chmod -R 755 /input /output
```

## 📝 Code Explanation

### HashPartitionExample.scala
- Reads text file from HDFS
- Splits text into words
- Counts word occurrences using `map` and `reduceByKey`
- Applies `HashPartitioner` with 2 partitions
- Saves results back to HDFS

### CustomPartitionExample.scala
- Same word count logic as Hash Partition
- Implements `MyCustomPartitioner` class
- Custom logic:
  - "welcome" → Partition 0
  - "hi" or "hello" → Partition 1
  - Other words → Hash-based distribution

## 🎯 Key Learning Points

1. **Hash Partitioner**: Ensures even distribution of data across partitions using hash functions
2. **Custom Partitioner**: Allows business logic-based partitioning for optimized data processing
3. **Scala Compatibility**: Binary compatibility between Scala versions is critical in Spark applications
4. **HDFS Integration**: Spark seamlessly reads from and writes to HDFS using standard paths

## 📚 References

- [Apache Spark Documentation](https://spark.apache.org/docs/3.5.7/)
- [Spark RDD Programming Guide](https://spark.apache.org/docs/latest/rdd-programming-guide.html)
- [Scala 2.12 Documentation](https://docs.scala-lang.org/scala3/book/introduction.html)
