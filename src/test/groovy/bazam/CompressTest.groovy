package bazam

import static org.junit.Assert.*
import org.junit.Test
import gngs.*

class CompressTest {

    @Test
    public void test() {
        FASTQRead fastq = FASTQ.consumeRead(new File('src/test/data/test_compress.fastq').newReader())
        
        BaseCompactor compactor = new BaseCompactor()
        byte [] baseBytes = fastq.bases.bytes
        byte []  bytes = compactor.compact(baseBytes)
        byte [] uncompressed = compactor.expand(bytes)
        
        assert uncompressed.length == baseBytes.length
        assert uncompressed == baseBytes
    }

}
