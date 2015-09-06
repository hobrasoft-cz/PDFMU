package cz.hobrasoft.pdfmu;

import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;

/**
 * Manipulates a PDF file
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public interface Operation {

    /**
     * Executes the operation
     *
     * @param namespace parsed command line arguments
     * @throws OperationException if some condition prevents the operation from
     * finishing
     */
    public void execute(Namespace namespace) throws OperationException;

    /**
     * Adds this operation's subparser to a given Subparsers object
     *
     * <p>
     * The subparser handles the arguments accepted by this operation.
     *
     * @param subparsers Subparsers object to add the subparser to
     * @return the subparser added to {@code subparsers}
     */
    public Subparser addParser(Subparsers subparsers);
}
