package cz.hobrasoft.pdfmu;

import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;

/**
 * PDFMU operation
 * 
 * A PDFMU operation typically manipulates a PDF file.
 * 
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public interface Operation {

    /**
     * Execute the operation
     * 
     * @param namespace Parsed command line arguments
     */
    public void execute(Namespace namespace);
    
    /**
     * Add this operation's specific subparser to a given subparsers object
     * 
     * This subparser should handle the arguments specific to this operation.
     * 
     * @param subparsers subparsers object to add the subparser to
     * @return the created subparser
     */
    public Subparser addParser(Subparsers subparsers);
}
