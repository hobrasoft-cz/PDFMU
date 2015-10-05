package cz.hobrasoft.pdfmu;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public class WritingMapper {

    private final ObjectMapper mapper;
    private final OutputStream os;

    public WritingMapper(ObjectMapper mapper, OutputStream os) {
        this.mapper = mapper;
        this.os = os;
    }

    public void writeValue(Object value) throws IOException {
        mapper.writeValue(os, value);
    }
}
