package cz.hobrasoft.pdfmu;

import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;

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

    @Override
    public Subparser addParser(Subparsers subparsers) {
        Subparser parserVersion = subparsers.addParser("version")
                .help("set or get version of a PDF file")
                .defaultHelp(true)
                .setDefault("command", CommandVersion.class);
        parserVersion.addArgument("-i", "--in")
                .type(String.class)
                // TODO: Consider using `FileInputStream.class` as type
                // http://argparse4j.sourceforge.net/usage.html#argument-nargs
                // Also see:
                // http://argparse4j.sourceforge.net/usage.html#filetype
                .help("input PDF file")
                .required(true)
                .nargs("?");
        parserVersion.addArgument("-o", "--out")
                .type(String.class)
                .help("output PDF file")
                .nargs("?");
        parserVersion.addArgument("-s", "--set")
                .type(String.class)
                .help("PDF version to set")
                .nargs("?")
                .setDefault("1.6");
        parserVersion.addArgument("-g", "--get")
                .type(boolean.class)
                .help("display version of IN without setting the version")
                .action(Arguments.storeTrue());
        parserVersion.addArgument("--force")
                .help("overwrite OUT if already present")
                .type(boolean.class)
                .action(Arguments.storeTrue());
        
        return parserVersion;
    }
    
}
