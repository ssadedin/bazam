package bazam

import static org.junit.Assert.*

import org.junit.Test

class OriginalQualitiesTest {

    @Test
    public void 'test that original qualities can be retrieved from the OQ tag'() {
        Bazam bazam = new Bazam()
        bazam.test(['-bqtag', 'OQ', '-bam', 'src/test/data/small.test.bam']) {
            StringWriter out = new StringWriter()
            bazam.run(bazam.opts, out, out)
            
            def lines = out.toString().readLines()
            
            def reads = lines.collate(4).collect {
                [ 
                    name: it[0].tokenize()[0],
                    bases: it[1],
                    quals: it[3]
                ]
            }
            
            Map read = reads.find { it.name == '@DB7DT8Q1:280:HA91LADXX:1:2214:19260:25740' }
            
            assert read.quals == '?><B?@CC?>D=DD>DCC?DCCB@@@B?B@D@@B?DBDBCECDDABAC@BBBB@CBBB@@D?BDF@BEAEE@CE@BCDF@DDDDA@A@CCCCADCB>;:>'
        }
    }

    @Test
    public void 'test that recalibrated base qualities are retrieved unless bqtag flag is specified'() {
        Bazam bazam = new Bazam()
        bazam.test(['-bam', 'src/test/data/small.test.bam']) {
            StringWriter out = new StringWriter()
            bazam.run(bazam.opts, out, out)
            
            def lines = out.toString().readLines()
            
            def reads = lines.collate(4).collect {
                [ 
                    name: it[0].tokenize()[0],
                    bases: it[1],
                    quals: it[3]
                ]
            }
            
            Map read = reads.find { it.name == '@DB7DT8Q1:280:HA91LADXX:1:2214:19260:25740' }
            
            assert read.quals == '@@>D@AFF@?F>FG@GEF@GEFEABBE@EAFABE@GDFEFFEFGBDBEADDDDBFDEEBAF@DEDADFBFF@FF@DFED@FEFEBAAAFDFDAFED@>>@'
        }
    }
}
