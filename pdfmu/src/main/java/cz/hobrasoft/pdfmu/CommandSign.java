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
        String help = "Digitally sign a PDF document";
        
        Subparser subparser = subparsers.addParser("sign")
                .help(help)
                .description(help)
                .defaultHelp(true)
                .setDefault("command", CommandSign.class);
        
        return subparser;
    }
    
}
