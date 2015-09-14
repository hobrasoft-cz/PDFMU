package cz.hobrasoft.pdfmu.signature;

import cz.hobrasoft.pdfmu.Operation;
import cz.hobrasoft.pdfmu.OperationException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;

/**
 * Manipulates signatures of a PDF document
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public class OperationSignature implements Operation {

    private final Operation operationAdd = new OperationSignatureAdd();
    private final Operation operationDisplay = new OperationSignatureDisplay();
    private final Operation[] operations = {operationAdd, operationDisplay};

    @Override
    public String getCommandName() {
        return "signature";
    }

    @Override
    public Subparser configureSubparser(Subparser subparser) {
        String help = "Sign a PDF document or display signatures in a PDF document";

        // Configure the subparser
        subparser.help(help)
                .description(help)
                .defaultHelp(true)
                .setDefault("command", OperationSignature.class);

        // Add subparsers group
        Subparsers subparsersSignature = subparser.addSubparsers()
                .help("signature operation to execute")
                .metavar("OPERATION")
                .dest("signatureOperation");

        // Configure the subparsers
        for (Operation operation : operations) {
            operation.configureSubparser(subparsersSignature.addParser(operation.getCommandName()));
        }

        return subparser;
    }

    @Override
    public void execute(Namespace namespace) throws OperationException {
        Operation operation = null;
        switch (namespace.getString("signatureOperation")) {
            case "add":
                operation = operationAdd;
                break;
            case "display":
                operation = operationDisplay;
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
