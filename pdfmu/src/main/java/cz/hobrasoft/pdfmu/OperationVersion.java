package cz.hobrasoft.pdfmu;

import net.sourceforge.argparse4j.inf.Namespace;
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
public class OperationVersion implements Operation {

    private final Operation operationSet = new OperationVersionSet();
    private final Operation operationGet = new OperationVersionGet();
    private final Operation[] operations = {operationSet, operationGet};

    @Override
    public Subparser addParser(Subparsers subparsers) {
        String help = "Set or display PDF version of a PDF document";

        // Add the subparser
        Subparser subparser = subparsers.addParser("version")
                .help(help)
                .description(help)
                .defaultHelp(true)
                .setDefault("command", OperationVersion.class);

        Subparsers subparsersVersion = subparser.addSubparsers()
                .help("version operation to execute")
                .metavar("OPERATION")
                .dest("versionOperation");

        for (Operation operation : operations) {
            operation.addParser(subparsersVersion);
        }

        return subparser;
    }

    @Override
    public void execute(Namespace namespace) throws OperationException {
        Operation operation = null;
        switch (namespace.getString("versionOperation")) {
            case "set":
                operation = operationSet;
                break;
            case "get":
                operation = operationGet;
                break;
            default:
                // Invalid or none operation was specified,
                // so `parser.parseArgs` should have thrown an exception.
                assert false;
        }
        assert operation != null;

        // If `operation` throws an `OperationException`, we pass it on.
        operation.execute(namespace);
    }

}
