package cz.hobrasoft.pdfmu;

import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;

/**
 * Adds a digital signature to a PDF document
 * 
 * @author Filip
 */
public class CommandSign implements Command {

    @Override
    public void execute(String[] args) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Subparser addParser(Subparsers subparsers) {
        Subparser parserSign = subparsers.addParser("sign")
                .help("add a digital signature to a PDF file")
                .defaultHelp(true)
                .setDefault("command", CommandSign.class);
        
        return parserSign;
    }
    
}
