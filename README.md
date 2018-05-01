# Bazam

A read extraction and realignment tool for next generation sequencing data

## Why?

Most sequencing data is stored in coordinate sorted BAM files, because that's
how most analyses want to use it. However, when reads are paired, seeing both
reads of the pair at the same time also important for some applications. For
example, if you want to realign the reads to a different genome reference, or
do other actions such as trimming them based on overlap, etc., then you need
this.  Doing this when the reads are stored in coordinate order is
unfortunately not easy. Most approaches involve leaving out some of the
reads,  require re-sorting the BAM file (requiring substantial time and space) or
are unacceptable slow. Bazam is a tool that efficiently extracts read pairs from 
BAM files that are stored in coordinate sorted order. In particular, Bazam
makes it especially easy to realign reads to a new genome without any need for 
temporary intermediate files.

## Why is this hard?

You might think this problem should be easy. However it's difficult because
when stored in coordinate order the only efficient way to read the file is 
in coordinate order. Yet a read's mate may be stored at a significant
coordinate distance, meaning that to see both a read and it's mate 
you either need to do an expensive random lookup of each mate
(too slow), or buffer reads in memory until their mate becomes
available in the stream (too memory expensive). 

## How Does Bazam Solve this?

Bazam doesn't do any magic: it just carefully buffers reads using various different 
strategies for caching to try and minimise memory use. It is also careful designed
to run fast so that it's probably faster than any downstream application (such as 
realignment) that you are trying to do on the data.


## Getting it

You can clone it and build with zero install this way:

```
git clone git@github.com:ssadedin/bazam.git
cd bazam
./gradlew clean jar
```

## Running it

To get the help, you just execute the JAR file:

```
java -jar build/libs/bazam.jar
```

## Simple Example

Let's get all the read pairs from a BAM File in interleaved FASTQ format:

```
java -jar build/libs/bazam.jar -bam  test.bam  > tmp.fastq
```

## Using Filtering

You can add a filter to select which reads you want extracted. The filter is a
groovy expression that is executed with a variable called "pair". The "pair"
variable has attributes `r1` and `r2` which are
[SAMRecord](https://samtools.github.io/htsjdk/javadoc/htsjdk/htsjdk/samtools/SAMRecord.html)
objects, enabling you to access all the attributes of the reads. If the
expression returns `true` then the read is emitted, otherwise it is dropped:

Get only the reads where the pair spans different chromosomes:

```
java -jar build/libs/bazam.jar -f "pair.r1.referenceIndex != pair.r2.referenceIndex" -bam  test.bam > chimeric_reads.fastq
```

## Other Options

For greater performance, you can shard reads using `-s` which will cause only
every nth out of N reads to be emitted (specified in the form `-s n,N`). For
example, if you give it `-s 2,4` then the second out of every 4 read pairs will
selected for output, `-s 3,4` would select the 3rd out of every 4 pairs, etc.
This allows you to use scatter-gather or map/reduce style concurrency to
process reads in a distributed manner.

You can supply a BED file to restrict regions that reads are harvested from
with `-L` (note: both reads of a pair are emitted if either one overlaps the
regions at all). 

Use the `-namepos` option to cause reads to be emitted with modified read names
that embed their original positions in the source BAM file. This is useful if
you want to track where reads came from after downstream processing (for
example, comparing two aligners, etc).

## Advanced Example

You can see an advanced example of Bazam being used in the STRetch pipeline:

https://github.com/Oshlack/STRetch/blob/feat-faster-bam-pipeline/pipelines/pipeline_stages.groovy#L101


