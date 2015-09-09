package cz.hobrasoft.pdfmu;

import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;

/**
 * Adds a digital signature to a PDF document
 *
 * @author Filip
 */
public class OperationSign implements Operation {

    @Override
    public void execute(Namespace namespace) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Subparser addParser(Subparsers subparsers) {
        String help = "Digitally sign a PDF document";
        String metavarIn = "IN.pdf";
        String metavarOut = "OUT.pdf";
        String metavarCertificate = "CERTIFICATE";

        Subparser subparser = subparsers.addParser("sign")
                .help(help)
                .description(help)
                .defaultHelp(true)
                .setDefault("command", OperationSign.class);
        subparser.addArgument("-i", "--in")
                .help("input PDF file")
                .type(String.class)
                .required(true)
                .nargs("?")
                .metavar(metavarIn);
        subparser.addArgument("-c", "--certificate")
                .help("certificate file to sign with")
                .type(String.class)
                .required(true)
                .nargs("?")
                .metavar(metavarCertificate);
        subparser.addArgument("-o", "--out")
                .help("output PDF file")
                .type(String.class)
                .nargs("?")
                .metavar(metavarOut);
        subparser.addArgument("--force")
                .help(String.format("overwrite %s if it exists", metavarOut))
                .type(boolean.class)
                .action(Arguments.storeTrue());

        return subparser;
    }

}
