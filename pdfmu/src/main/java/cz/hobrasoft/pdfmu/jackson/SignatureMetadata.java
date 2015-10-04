package cz.hobrasoft.pdfmu.jackson;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Date;

/**
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public class SignatureMetadata {

    public String name = null;
    public String reason = null;
    public String location = null;
    public String date = null;
}
