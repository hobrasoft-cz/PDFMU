package cz.hobrasoft.pdfmu;

/**
 * Gets or sets the version of a PDF file
 * 
 * <p>
 * Usage:
 * <ul>
 * <li>{@code pdfmu version --in in.pdf --out out.pdf --set 1.6}</li>
 * <li>{@code pdfmu version --in in.pdf --get}</li>
 * <li>{@code pdfmu version inout.pdf --force}</li>
 * </ul>
 * 
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public class CommandVersion implements Command {

    @Override
    public void execute(String[] args) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
