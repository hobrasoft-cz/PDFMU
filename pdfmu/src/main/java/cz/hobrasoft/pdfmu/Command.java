package cz.hobrasoft.pdfmu;

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
}
