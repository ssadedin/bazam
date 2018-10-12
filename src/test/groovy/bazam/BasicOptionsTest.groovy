package bazam

import static org.junit.Assert.*

import org.junit.Test

class Command {
    
    int exitCode
    
    String out
    
    String err
    
}

class BasicOptionsTest {

    @Test
    public void 'Help displays and returns exit code zero'() {
        Command command = run("java -jar build/libs/bazam.jar -h")
        assert command.exitCode == 0
        assert command.err.contains("usage: java -jar bazam.jar")
        
        command = run("java -jar build/libs/bazam.jar --help")
        assert command.exitCode == 0
        assert command.err.contains("usage: java -jar bazam.jar")
    }
    
    @Test
    void 'Version displays and returns exit code zero'() {
        Command command = run("java -jar build/libs/bazam.jar -v")
        assert command.exitCode == 0
        assert command.out.contains("Bazam ") // We can't actually say what the version will be
    }
    
    @Test
    void 'Runs normally with no options'() {
        Command command = run("java -jar build/libs/bazam.jar -dr hello -bam test-data/test.bam")
        assert !command.err.contains('missing options')
        
        println command.err
    }
    
    Command run(String command) {
        Process p = command.execute()
        StringBuilder out = new StringBuilder()
        StringBuilder err = new StringBuilder()
        p.consumeProcessOutput(out, err)
        int exitCode = p.waitFor()
        
        return new Command(exitCode: exitCode, err: err.toString(), out: out.toString())
        
    }

}
