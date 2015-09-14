package cz.hobrasoft.pdfmu;

import cz.hobrasoft.pdfmu.sign.OperationSignature;
import cz.hobrasoft.pdfmu.version.OperationVersion;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparsers;

/**
 * The main class of PDFMU
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public class Main {

    static {
        // http://stackoverflow.com/a/3363747
        LogManager.getLogManager().reset(); // Remove default handler(s)

        Handler handler = new ConsoleHandler(); // Prints to `System.err`
        handler.setFormatter(new VerbatimFormatter());
        // http://stackoverflow.com/questions/2533227/how-can-i-disable-the-default-console-handler-while-using-the-java-logging-api/2533250#comment34752130_2533250
        Logger rootLogger = Logger.getLogger("");
        rootLogger.addHandler(handler);
    }

    private static final Logger logger = Logger.getLogger(Main.class.getName());

    /**
     * The main entry point of PDFMU
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ArgumentParser parser = ArgumentParsers.newArgumentParser("pdfmu")
                .description("Manipulate a PDF document")
                .defaultHelp(true);
        parser.addArgument("-of", "--output-format")
                .choices("text", "json")
                .setDefault("text")
                .type(String.class)
                .nargs("?")
                .help("format of stdout output");

        // TODO: Set pdfmu version in `parser`. For example:
        // parser.version("1.0");
        // http://argparse4j.sourceforge.net/usage.html#argumentparser-version
        // Try to extract the version from `pom.xml`.
        // Once the version has been set, enable a CL argument:
//        parser.addArgument("-v", "--version")
//                .help("print version of pdfmu")
//                .action(Arguments.version());
        Subparsers subparsers = parser.addSubparsers()
                .help("operation to execute")
                .metavar("OPERATION")
                .dest("operation");

        Operation operationVersion = new OperationVersion();
        Operation operationSignature = new OperationSignature();
        Operation operationAttach = new OperationAttach();
        Operation[] operations = {operationVersion, operationSignature, operationAttach};

        for (Operation operation : operations) {
            operation.configureSubparser(subparsers.addParser(operation.getCommandName()));
        }

        Namespace namespace = null;
        try {
            // If help is requested,
            // `parseArgs` prints help message and throws `ArgumentParserException`
            // (so `namespace` stays null).
            // If insufficient or invalid `args` are given,
            // `parseArgs` throws `ArgumentParserException`.
            namespace = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            // Prints information about the parsing error (missing argument etc.)
            parser.handleError(e);
        }
        if (namespace != null) {
            Operation operation = null;
            switch (namespace.getString("operation")) {
                case "version":
                    operation = operationVersion;
                    break;
                case "signature":
                    operation = operationSignature;
                    break;
                case "attach":
                    operation = operationAttach;
                    break;
                default:
                    // Invalid or none operation was specified,
                    // so `parser.parseArgs` should have thrown an exception.
                    assert false;
            }
            assert operation != null;
            try {
                operation.execute(namespace);
            } catch (OperationException ex) {
                logger.info(ex.getMessage());
                Throwable cause = ex.getCause();
                if (cause != null && cause.getMessage() != null) {
                    logger.info(cause.getMessage());
                }
            }
        }
    }
}
