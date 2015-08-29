package cz.hobrasoft.pdfmu;

/**
 * Attaches one or more files to a PDF document
 * 
 * <p>
 * Usage:
 * <ul>
 * <li>{@code pdfmu attach --to in.pdf --out out.pdf --attachment a.pdf}</li>
 * <li>{@code pdfmu attach --to in.pdf --out out.pdf --attachment a0.pdf --attachment a1.pdf}</li>
 * <li>{@code pdfmu attach --to inout.pdf --force --attachment a.pdf}</li>
 * </ul>
 * 
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public class CommandAttach implements Command {

    @Override
    public void execute(String[] args) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
