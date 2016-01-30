package cz.hobrasoft.pdfmu.jackson;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

/**
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
// HACK: Adding a Jackson annotation to a class with no fields makes it parsable.
// Source: http://stackoverflow.com/a/21319219/4054250
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE)
public class EmptyResult extends Result {
}
