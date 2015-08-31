package cz.hobrasoft.pdfmu;

import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;

/**
 * PDFMU command
 * 
 * A PDFMU command typically manipulates a PDF file.
 * 
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public interface Command {

    /**
     * Execute the command
     * 
     * @param args Command line arguments
     */
    public void execute(String[] args);
    
    /**
     * Add a command subparser to given subparsers
     * 
     * This subparser should handle the arguments specific to this command.
     * 
     * @param subparsers subparsers object to add the parser to
     * @return the created subparser
     */
    public Subparser addParser(Subparsers subparsers);
}
