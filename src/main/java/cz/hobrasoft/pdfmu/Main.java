/* 
 * Copyright (C) 2016 Hobrasoft s.r.o.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cz.hobrasoft.pdfmu;

import static cz.hobrasoft.pdfmu.error.ErrorType.INPUT_NOT_FOUND;
import static cz.hobrasoft.pdfmu.error.ErrorType.PARSER_INVALID_CHOICE;
import static cz.hobrasoft.pdfmu.error.ErrorType.PARSER_TOO_FEW_ARGUMENTS;
import static cz.hobrasoft.pdfmu.error.ErrorType.PARSER_UNKNOWN;
import static cz.hobrasoft.pdfmu.error.ErrorType.PARSER_UNRECOGNIZED_ARGUMENT;
import static cz.hobrasoft.pdfmu.error.ErrorType.PARSER_UNRECOGNIZED_COMMAND;
import cz.hobrasoft.pdfmu.operation.Operation;
import cz.hobrasoft.pdfmu.operation.OperationAttach;
import cz.hobrasoft.pdfmu.operation.OperationException;
import cz.hobrasoft.pdfmu.operation.OperationInspect;
import cz.hobrasoft.pdfmu.operation.metadata.OperationMetadataSet;
import cz.hobrasoft.pdfmu.operation.signature.OperationSignatureAdd;
import cz.hobrasoft.pdfmu.operation.version.OperationVersionSet;
import java.io.IOException;
import java.io.InputStream;
import static java.nio.charset.StandardCharsets.US_ASCII;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparsers;
import net.sourceforge.argparse4j.internal.HelpScreenException;
import net.sourceforge.argparse4j.internal.UnrecognizedArgumentException;
import net.sourceforge.argparse4j.internal.UnrecognizedCommandException;
import org.apache.commons.io.IOUtils;

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
                .description("PDF Manipulation Utility")
                .defaultHelp(true);

        parser.version(getProjectVersion());
        parser.addArgument("-v", "--version")
                .help("show version and exit")
                .action(Arguments.version());

        parser.addArgument("--legal-notice")
                .help("show legal notice and exit")
                .action(new PrintAndExitAction(getLegalNotice()));

        // TODO: Use an enum
        parser.addArgument("--output-format")
                .choices("text", "json")
                .setDefault("text")
                .type(String.class)
                .help("format of stderr output");

        return parser;
    }

    private static final String POM_PROPERTIES_RESOURCE_NAME = "pom.properties";
    private static final Properties POM_PROPERTIES = new Properties();

    private static void loadPomProperties() {
        ClassLoader classLoader = Main.class.getClassLoader();
        InputStream in = classLoader.getResourceAsStream(POM_PROPERTIES_RESOURCE_NAME);
        if (in != null) {
            try {
                POM_PROPERTIES.load(in);
            } catch (IOException ex) {
                logger.severe(String.format("Could not load the POM properties file: %s", ex));
            }
            try {
                in.close();
            } catch (IOException ex) {
                logger.severe(String.format("Could not close the POM properties file: %s", ex));
            }
        } else {
            logger.severe("Could not open the POM properties file.");
        }
    }

    private static final String LEGAL_NOTICE_RESOURCE_NAME = "cz/hobrasoft/pdfmu/legalNotice.txt";
    private static String legalNotice;

    private static void loadLegalNotice() {
        ClassLoader classLoader = Main.class.getClassLoader();
        InputStream in = classLoader.getResourceAsStream(LEGAL_NOTICE_RESOURCE_NAME);
        if (in != null) {
            try {
                legalNotice = IOUtils.toString(in, US_ASCII);
            } catch (IOException ex) {
                logger.severe(String.format("Could not load the legal notice file: %s", ex));
            }
            try {
                in.close();
            } catch (IOException ex) {
                logger.severe(String.format("Could not close the legal notice file: %s", ex));
            }
        } else {
            logger.severe("Could not open the legal notice file.");
        }
    }

    static {
        loadPomProperties();
        loadLegalNotice();
        assert legalNotice != null;
    }

    public static String getProjectVersion() {
        return POM_PROPERTIES.getProperty("projectVersion");
    }

    public static String getProjectCopyright() {
        return POM_PROPERTIES.getProperty("copyright");
    }

    public static String getLegalNotice() {
        return String.format("%1$s\n\n%2$s", getProjectCopyright(), legalNotice);
    }

    /**
     * Use {@link LinkedHashMap} or {@link java.util.TreeMap} as operations to
     * specify the order in which the operations are printed.
     *
     * @param operations a map that assigns the operations to their names
     * @return an argument parser with the operations attached as sub-commands
     */
    private static ArgumentParser createFullParser(Map<String, Operation> operations) {
        // Create a command line argument parser
        ArgumentParser parser = createBasicParser();

        // Create a Subparsers instance for operation subparsers
        Subparsers subparsers = parser.addSubparsers()
                .title("operations")
                .metavar("OPERATION")
                .help("operation to execute")
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

        // UnrecognizedArgumentException
        patterns.add(new ExceptionMessagePattern(PARSER_UNRECOGNIZED_ARGUMENT,
                "unrecognized arguments: '(?<argument>.*)'",
                Arrays.asList(new String[]{"argument"})));

        // ArgumentParserException
        patterns.add(new ExceptionMessagePattern(PARSER_INVALID_CHOICE,
                "argument (?<argument>.*): invalid choice: '(?<choice>.*)' \\(choose from \\{(?<validChoices>.*)\\}\\)",
                Arrays.asList(new String[]{"argument", "choice", "validChoices"})));

        // UnrecognizedCommandException
        patterns.add(new ExceptionMessagePattern(PARSER_UNRECOGNIZED_COMMAND,
                "invalid choice: '(?<command>.*)' \\(choose from (?<validCommands>.*)\\)",
                Arrays.asList(new String[]{"command", "validCommands"})));

        // ArgumentParserException
        patterns.add(new ExceptionMessagePattern(PARSER_TOO_FEW_ARGUMENTS,
                "too few arguments",
                new ArrayList<String>()));

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
        Map<String, Operation> operations = new LinkedHashMap<>();
        operations.put("inspect", OperationInspect.getInstance());
        operations.put("update-version", OperationVersionSet.getInstance());
        operations.put("update-properties", OperationMetadataSet.getInstance());
        operations.put("attach", OperationAttach.getInstance());
        operations.put("sign", OperationSignatureAdd.getInstance());

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
        } catch (HelpScreenException e) {
            parser.handleError(e); // Do nothing
        } catch (UnrecognizedCommandException e) {
            exitStatus = PARSER_UNRECOGNIZED_COMMAND.getCode();
            parser.handleError(e); // Print the error in human-readable format
        } catch (UnrecognizedArgumentException e) {
            exitStatus = PARSER_UNRECOGNIZED_ARGUMENT.getCode();
            parser.handleError(e); // Print the error in human-readable format
        } catch (ArgumentParserException ape) {
            OperationException oe = apeToOe(ape);
            exitStatus = oe.getCode();
            // We could also write `oe` as a JSON document,
            // but we do not know whether JSON output was requested,
            // so we use the text output (default).

            parser.handleError(ape); // Print the error in human-readable format
        }

        if (namespace == null) {
            System.exit(exitStatus);
        }

        assert exitStatus == 0;

        // Handle command line arguments
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
        System.exit(exitStatus);
    }
}
