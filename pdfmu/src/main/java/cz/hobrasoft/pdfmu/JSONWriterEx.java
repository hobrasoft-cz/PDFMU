package cz.hobrasoft.pdfmu;

import java.io.Flushable;
import java.io.IOException;
import java.io.Writer;
import org.json.JSONException;
import org.json.JSONWriter;

/**
 * An extension of {@link JSONWriter}
 *
 * <p>
 * Exposes writer flushing in {@link #flush()}. Adds shortcuts for common
 * combinations of operations.
 *
 * <p>
 * Note that the methods do not return the {@link JSONWriter} instance, so
 * cascade style chains of operations are not possible using
 * {@link JSONWriterEx}.
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public class JSONWriterEx extends JSONWriter implements Flushable {

    /**
     * Makes a fresh JSONWriter.
     *
     * @param w writer to write the JSON-formatted data to
     */
    public JSONWriterEx(Writer w) {
        super(w);
    }

    /**
     * Flushes the underlying writer by writing any buffered output to the
     * underlying stream.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void flush() throws IOException {
        writer.flush();
    }

    /**
     * Appends a key-value pair.
     *
     * <p>
     * Appends a key and its associated atomic value.
     *
     * <p>
     * Wraps {@link JSONWriter#key(String)} and one of the following:
     * <ul>
     * <li>{@link JSONWriter#value(boolean)}
     * <li>{@link JSONWriter#value(double)}
     * <li>{@link JSONWriter#value(long)}
     * <li>{@link JSONWriter#value(Object)}
     * </ul>
     *
     * <p>
     * Inspiration:
     * {@link javax.json.stream.JsonGenerator#write(String, String)}
     *
     * @param <V> value type (supported types: boolean, double, long,
     * {@link Object})
     * @param key key string
     * @param value value
     */
    public <V> void write(String key, V value) {
        this.key(key);
        // `this.value` accepts `Object`, so the following call will always succeed.
        // `this.value` also accepts (more) primitive types, so genericity is useful.
        this.value(value);
    }

    /**
     * Begins appending a new array associated with a given key.
     *
     * <p>
     * Appends a key and begins appending a new array as the value associated
     * with the key.
     *
     * <p>
     * Wraps {@link JSONWriter#key(String)} and {@link JSONWriter#array()}.
     *
     * <p>
     * Inspiration:
     * {@link javax.json.stream.JsonGenerator#writeStartArray(String)}
     *
     * @param key key string
     */
    public void array(String key) {
        this.key(key);
        array();
    }

    /**
     * Begin appending a new object associated with a given key.
     *
     * <p>
     * Appends a key and begins appending a new object as the value associated
     * with the key.
     *
     * <p>
     * Wraps {@link JSONWriter#key(String)} and {@link JSONWriter#object()}.
     *
     * <p>
     * Inspiration:
     * {@link javax.json.stream.JsonGenerator#writeStartObject(String)}
     *
     * @param key key string
     */
    public void object(String key) {
        this.key(key);
        object();
    }

    /**
     * Ends the current array or object.
     *
     * <p>
     * This method must be called to balance a call to
     * {@link JSONWriter#array()} or {@link JSONWriter#object()}. Calls either
     * {@link JSONWriter#endArray()} or {@link JSONWriter#endObject()} based on
     * context.
     *
     * @throws JSONException if the current context of the JSON document does
     * not allow ending and array or an object
     */
    public void end() throws JSONException {
        switch (mode) {
            case 'k':
                // Expecting key in an object
                endObject();
                break;
            case 'a':
                // Expecting an array element
                endArray();
                break;
            default:
                throw new JSONException("Misplaced end.");
        }
    }

}
