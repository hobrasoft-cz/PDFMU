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
        types.put("result/version set", VersionSet.class);
        types.put("result/signature show", SignatureDisplay.class);
        types.put("result/signature add", SignatureAdd.class);
        types.put("result/metadata get", MetadataGet.class);
        types.put("result/empty", EmptyResult.class);

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
