package cz.hobrasoft.pdfmu.version;

import cz.hobrasoft.pdfmu.Operation;
import cz.hobrasoft.pdfmu.OperationException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;

/**
 * Gets or sets the version of a PDF file
 *
 * <p>
 * Usage:
 * <ul>
 * <li>{@code pdfmu version set in.pdf --out out.pdf --version 1.6}</li>
 * <li>{@code pdfmu version get in.pdf}</li>
 * <li>{@code pdfmu version set inout.pdf --force}</li>
 * </ul>
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public class OperationVersion implements Operation {

    private final Operation operationSet = new OperationVersionSet();
    private final Operation operationGet = new OperationVersionGet();
    private final Operation[] operations = {operationSet, operationGet};

    @Override
    public String getCommandName() {
        return "version";
    }

    @Override
    public Subparser configureSubparser(Subparser subparser) {
        String help = "Set or display PDF version of a PDF document";

        // Configure the subparser
        subparser.help(help)
                .description(help)
                .defaultHelp(true)
                .setDefault("command", OperationVersion.class);

        // Add subparsers group
        Subparsers subparsersVersion = subparser.addSubparsers()
                .help("version operation to execute")
                .metavar("OPERATION")
                .dest("versionOperation");

        // Configure the subparsers
        for (Operation operation : operations) {
            operation.configureSubparser(subparsersVersion.addParser(operation.getCommandName()));
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
