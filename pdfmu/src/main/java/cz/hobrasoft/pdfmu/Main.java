package cz.hobrasoft.pdfmu;

import static cz.hobrasoft.pdfmu.error.PdfmuError.INPUT_NOT_FOUND;
import static cz.hobrasoft.pdfmu.error.PdfmuError.PARSER_INVALID_CHOICE;
import static cz.hobrasoft.pdfmu.error.PdfmuError.PARSER_UNKNOWN;
import cz.hobrasoft.pdfmu.operation.Operation;
import cz.hobrasoft.pdfmu.operation.OperationAttach;
import cz.hobrasoft.pdfmu.operation.OperationException;
import cz.hobrasoft.pdfmu.operation.metadata.OperationMetadata;
import cz.hobrasoft.pdfmu.operation.signature.OperationSignature;
import cz.hobrasoft.pdfmu.operation.version.OperationVersion;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparsers;
import static cz.hobrasoft.pdfmu.error.PdfmuError.PARSER_UNRECOGNIZED_ARGUMENT;

/**
 * The main class of PDFMU
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public class Main {

    private static void disableLoggers() {
        // http://stackoverflow.com/a/3363747
        LogManager.getLogManager().reset(); // Remove the handlers
    }

    private static final Logger logger = Logger.getLogger(Main.class.getName());

    static {
        // Configure log message format
        // Arguments:
        // http://docs.oracle.com/javase/7/docs/api/java/util/logging/SimpleFormatter.html#format%28java.util.logging.LogRecord%29
        // %4$s: level
        // %5$s: message
        System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s%n");
    }

    private static ArgumentParser createBasicParser() {
        // Create a command line argument parser
        ArgumentParser parser = ArgumentParsers.newArgumentParser("pdfmu")
                .description("Manipulate a PDF document")
                .defaultHelp(true);
        // TODO: Use an enum
        parser.addArgument("-of", "--output_format")
                .choices("text", "json")
                .setDefault("text")
                .type(String.class)
                .help("format of stderr output");
        return parser;
    }

    private static ArgumentParser createFullParser(SortedMap<String, Operation> operations) {
        // Create a command line argument parser
        ArgumentParser parser = createBasicParser();

        // TODO: Set pdfmu version in `parser`. For example:
        // parser.version("1.0");
        // http://argparse4j.sourceforge.net/usage.html#argumentparser-version
        // Try to extract the version from `pom.xml`.
        // Once the version has been set, enable a CL argument:
//        parser.addArgument("-v", "--version")
//                .help("print version of pdfmu")
//                .action(Arguments.version());
        // Create a Subparsers instance for operation subparsers
        Subparsers subparsers = parser.addSubparsers()
                .help("operation to execute")
                .metavar("OPERATION")
                .dest("operation");

        // Configure operation subparsers
        for (Map.Entry<String, Operation> e : operations.entrySet()) {
            String name = e.getKey();
            Operation operation = e.getValue();
            operation.configureSubparser(subparsers.addParser(name));
        }

        return parser;
    }

    private static OperationException apeToOe(ArgumentParserException e) {
        Set<ExceptionMessagePattern> patterns = new HashSet<>();
        patterns.add(new ExceptionMessagePattern(INPUT_NOT_FOUND,
                "argument (?<argument>.*): Insufficient permissions to read file: \'(?<file>.*)\'",
                Arrays.asList(new String[]{"argument", "file"})));
        patterns.add(new ExceptionMessagePattern(PARSER_UNRECOGNIZED_ARGUMENT,
                "unrecognized arguments: '(?<argument>.*)'",
                Arrays.asList(new String[]{"argument"})));
        patterns.add(new ExceptionMessagePattern(PARSER_INVALID_CHOICE,
                "argument (?<argument>.*): invalid choice: '(?<choice>.*)' \\(choose from \\{(?<validChoices>.*)\\}\\)",
                Arrays.asList(new String[]{"argument", "choice", "validChoices"})));

        OperationException oe = null;
        for (ExceptionMessagePattern p : patterns) {
            oe = p.getOperationException(e);
            if (oe != null) {
                break;
            }
        }

        if (oe == null) {
            // Unknown parser exception
            oe = new OperationException(PARSER_UNKNOWN, e);
        }

        assert oe != null;
        return oe;
    }

    /**
     * The main entry point of PDFMU
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        int exitStatus = 0; // Default: 0 (normal termination)

        // Create a map of operations
        SortedMap<String, Operation> operations = new TreeMap<>();
        operations.put("version", OperationVersion.getInstance());
        operations.put("signature", OperationSignature.getInstance());
        operations.put("attach", OperationAttach.getInstance());
        operations.put("metadata", OperationMetadata.getInstance());

        // Create a command line argument parser
        ArgumentParser parser = createFullParser(operations);

        // Parse command line arguments
        Namespace namespace = null;
        try {
            // If help is requested,
            // `parseArgs` prints help message and throws `ArgumentParserException`
            // (so `namespace` stays null).
            // If insufficient or invalid `args` are given,
            // `parseArgs` throws `ArgumentParserException`.
            namespace = parser.parseArgs(args);
        } catch (ArgumentParserException ape) {
            OperationException oe = apeToOe(ape);
            exitStatus = oe.getCode();
            // We could also write `oe` as a JSON document,
            // but we do not know whether JSON output was requested,
            // so we use the text output (default).

            // Print the error in human-readable format
            parser.handleError(ape);
        }

        // Handle command line arguments
        if (namespace != null) {
            WritingMapper wm = null;

            // Extract operation name
            String operationName = namespace.getString("operation");
            assert operationName != null; // The argument "operation" is a sub-command, thus it is required

            // Select the operation from `operations`
            assert operations.containsKey(operationName); // Only supported operation names are allowed
            Operation operation = operations.get(operationName);
            assert operation != null;

            // Choose the output format
            String outputFormat = namespace.getString("output_format");
            switch (outputFormat) {
                case "json":
                    // Disable loggers
                    disableLoggers();
                    // Initialize the JSON serializer
                    wm = new WritingMapper();
                    operation.setWritingMapper(wm); // Configure the operation
                    break;
                case "text":
                    // Initialize the text output
                    TextOutput to = new TextOutput(System.err); // Bind to `System.err`
                    operation.setTextOutput(to); // Configure the operation
                    break;
                default:
                    assert false; // The option has limited choices
            }

            // Execute the operation
            try {
                operation.execute(namespace);
            } catch (OperationException ex) {
                exitStatus = ex.getCode();

                // Log the exception
                logger.severe(ex.getLocalizedMessage());
                Throwable cause = ex.getCause();
                if (cause != null && cause.getMessage() != null) {
                    logger.severe(cause.getLocalizedMessage());
                }

                if (wm != null) {
                    // JSON output is enabled
                    ex.writeInWritingMapper(wm);
                }
            }
        }
        System.exit(exitStatus);
    }
}
