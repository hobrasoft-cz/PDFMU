package cz.hobrasoft.pdfmu.jackson;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.factories.SchemaFactoryWrapper;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public class SchemaGenerator {

    public static void main(String[] args) throws JsonMappingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT); // nice formatting

        SchemaFactoryWrapper visitor = new SchemaFactoryWrapper();

        Map<String, Type> types = new HashMap<>();
        types.put("RpcResponse", RpcResponse.class);
        types.put("result/version get", VersionGet.class);
        types.put("result/signature display", SignatureDisplay.class);
        types.put("result/metadata get",  MetadataGet.class);

        for (Map.Entry<String, Type> e : types.entrySet()) {
            String name = e.getKey();
            String filename = String.format("schema/%s.json", name);
            Type type = e.getValue();
            mapper.acceptJsonFormatVisitor(mapper.constructType(type), visitor);
            JsonSchema jsonSchema = visitor.finalSchema();
            mapper.writeValue(new File(filename), jsonSchema);
        }
    }
}
