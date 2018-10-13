# Bazam

A tool to extract paired reads in FASTQ format from coordinate sorted BAM files.

## What?

Bazam is a smarter way to realign reads from one genome to another. If you've tried to use 
Picard SAMtoFASTQ or `samtools bam2fq` before and 
ended up unsatisfied with complicated, long running inefficient pipelines, `bazam` might 
be what you wanted. Bazam will output
FASTQ in a form that can stream directly into common aligners such as
BWA or Bowtie2, so that you can quickly and easily realign reads without
extraction to any intermediate format. Bazam can target a specific region of the genome, 
specified as a region or a gene name if you prefer.

![Bazam workflow for realignment](docs/bazam_flow_diagram.png?raw=true "Bazam Realignment")

If you are writing a tool that works on BAM files then you might be interested in tapping into
Bazam as a library: Bazam can give the reads pairs to you directly
as Picard [SAMRecord](https://samtools.github.io/htsjdk/javadoc/htsjdk/htsjdk/samtools/SAMRecord.html)
objects.

## Why?

Getting read pairs out of most aligned sequencing files is hard, at least,
harder than you would think.

Most sequencing data is stored in coordinate sorted BAM files, because that's
how most analyses want to use it. But if you want to get back the original read pairs for some
other reason it is awkward from this format. For example,
if you want to realign the reads to a different genome reference, or do
other processing such as trimming them based on overlap, etc., then you need
this.  However you will find (or at least, I found) there actually aren't any good tools to do this
simple task (hence Bazam, which is a contraction of "bam to bam", based on one simple
application, which is re-aligning existing reads to a new genome).

## Why is this hard?

You might think this problem should be easy. However it's difficult because
when reads are stored in coordinate order the only efficient way to read them
is in that order. Yet a read's mate may be stored at a significant coordinate
distance, meaning that to see both a read and it's mate you either need to do
an expensive random lookup of each mate (too slow), or buffer reads in memory
until their mate becomes available in the stream (too memory expensive).

## How Does Bazam Solve this?

Bazam doesn't do any magic: it just carefully buffers reads and uses various
different strategies for caching to try and minimise memory use. Typically
realigning a whole genome sample (130G or so of data) could require up to 16G
of RAM to buffer reads. (Note that you can reduce this requirement by
"sharding" - see below). Bazam is also carefully designed to run fast
so that it's _probably_ faster than any downstream application (such as
realignment) that you are trying to do on the data. If not, again, sharding
is your answer (see below).

## Getting it

You can clone it and build with zero install this way:

```
git clone git@github.com:ssadedin/bazam.git
cd bazam
git submodule update --init --recursive
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

## Realigning a Genome to a New Reference using BWA

```
java -jar build/libs/bazam.jar -bam my.bam | \
         bwa mem -p ref.fa  - | \
         samtools view -bSu - | \
         samtools sort -o out.bam 
```

## Over a Specific Region

The `-L` flag works to give you just a region of interest. Note that read pairs with any overlap
by either read are emitted in full: 

```
java -jar build/libs/bazam.jar -bam my.bam -L chr1:5000000-6000000 | \
         bwa mem -p ref.fa  - | \
         samtools view -bSu - | \
         samtools sort -o out.bam 
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

## Sharding

Sharding means breaking up the data into pieces to process the parts separately.
By sharding the data you can both increase performance and reduce memory requirments
for any individual shard (while using more memory overall).

You can shard reads using `-s`: this will cause only every nth out of N reads
to be emitted (specified in the form `-s n,N`). For example, if you give it `-s
2,4` then the second out of every 4 read pairs will selected for output, `-s
3,4` would select the 3rd out of every 4 pairs, etc.  This allows you to use
scatter-gather or map/reduce style concurrency to process reads in a
distributed manner. In the case of realignment, you could run 4 instances of 
BWA in the above example to realign four times as fast, and then merge the 
BAM files afterwards.

# Other Options

You can supply a BED file to restrict regions that reads are harvested from
with `-L` (note: both reads of a pair are emitted if either one overlaps the
regions at all). 

Use the `-namepos` option to cause reads to be emitted with modified read names
that embed their original positions in the source BAM file. This is useful if
you want to track where reads came from after downstream processing (for
example, comparing two aligners, etc).

## Advanced Example

You can see an advanced example of Bazam being used in the STRetch pipeline:

https://github.com/Oshlack/STRetch/blob/master/pipelines/pipeline_stages.groovy#L121

