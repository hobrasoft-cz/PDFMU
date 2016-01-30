package cz.hobrasoft.pdfmu;

import java.io.Flushable;
import java.io.IOException;
import java.io.PrintStream;
import org.apache.commons.lang3.StringUtils;

/**
 * Prints indented messages to a {@link PrintStream}
 *
 * <p>
 * The indentation level starts at 0 and can be changed using
 * {@link #indentMore} and {@link #indentLess}.
 *
 * <p>
 * Example of usage:
 * <pre>
 * {@code
 * TextOutput to = new TextOutput(System.out);
 * to.println("Hello!");
 * to.indentMore("My favorite fruits:");
 * to.println("Banana");
 * to.println("Apple");
 * to.indentLess();
 * to.flush();
 * }
 * </pre>
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public class TextOutput implements Flushable {

    private final PrintStream ps;
    private int indentLevel = 0;
    private static final String indentString = "  ";

    /**
     * Creates a {@link TextOutput} bound to a {@link PrintStream}
     *
     * @param ps the {@link PrintStream} to print messages to
     */
    public TextOutput(PrintStream ps) {
        // ps may be null
        this.ps = ps;
    }

    /**
     * Creates a {@link TextOutput} that discards all the messages
     */
    public TextOutput() {
        this(null);
    }

    /**
     * Prints a message on a separate line
     *
     * <p>
     * The message is indented by the current indentation level (starting at 0)
     * and printed using the specified {@link PrintStream}.
     *
     * <p>
     * Mimics {@link PrintStream#println(String x)}.
     *
     * @param x message to print
     */
    public void println(String x) {
        if (ps != null) {
            assert indentLevel >= 0;
            String prefix = StringUtils.repeat(indentString, indentLevel);
            ps.println(String.format("%s%s", prefix, x));
        }
    }

    /**
     * Increments the indentation level
     *
     * <p>
     * If the code between {@link #indentMore} and {@link #indentLess} may throw
     * a (checked) exception, surround the code with a
     * {@code try}-{@code finally} block like this to maintain consistency:
     * <pre>
     * {@code
     * TextOutput to;
     * to.indentMore();
     * try {
     *   // Do stuff, possibly throwing an exception
     * // Possibly catch the exception
     * } finally {
     *   to.indentLess();
     * }
     * }
     * </pre>
     */
    public void indentMore() {
        ++indentLevel;
    }

    /**
     * Prints a message and increments the indentation level
     *
     * @param message message to print
     */
    public void indentMore(String message) {
        println(message);
        indentMore();
    }

    /**
     * Decrements the indentation level
     */
    public void indentLess() {
        if (indentLevel > 0) {
            --indentLevel;
        }
        assert indentLevel >= 0;
    }

    /**
     * Flushes the underlying {@link PrintStream}
     *
     * @throws IOException if the underlying {@link PrintStream#flush} throws an
     * {@link IOException}
     */
    @Override
    public void flush() throws IOException {
        if (ps != null) {
            ps.flush();
        }
    }
}
