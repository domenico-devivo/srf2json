package eu.fbk.srf2json;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

public class ResultWriter implements AutoCloseable {
    Path path;
    BufferedWriter writer;

    public ResultWriter(String fileName) throws IOException {
        path = Paths.get(fileName);
        this.writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
    }

    public Path writeResult(Object outputObject) throws JsonProcessingException, IOException {
        writer.write(produceJson(outputObject));
        return path;
    }

    public static String produceJson(Object obj) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new SimpleModule())
                .setVisibility(PropertyAccessor.FIELD, Visibility.ANY)
                .setVisibility(PropertyAccessor.CREATOR, Visibility.NONE)
                .setVisibility(PropertyAccessor.GETTER, Visibility.NONE)
                .setVisibility(PropertyAccessor.SETTER, Visibility.NONE)
                .setVisibility(PropertyAccessor.IS_GETTER, Visibility.NONE)
                .setSerializationInclusion(Include.NON_NULL);

        CustomPrettyPrinter prettyPrinter = new CustomPrettyPrinter();
        return mapper.writer(prettyPrinter).writeValueAsString(obj);
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }

    private static class CustomPrettyPrinter extends DefaultPrettyPrinter {
        public CustomPrettyPrinter() {
            super();
            this._objectFieldValueSeparatorWithSpaces = ": ";
            DefaultIndenter indenter = new DefaultIndenter("  ", "\n");
            this.indentArraysWith(indenter);
            this.indentObjectsWith(indenter);
        }
    
        @Override
        public CustomPrettyPrinter createInstance() {
            return new CustomPrettyPrinter();
        }
    
        @Override
        public void writeStartArray(JsonGenerator g) throws IOException {
            g.writeRaw("[");
            if (!_arrayIndenter.isInline()) {
                _nesting++;
            }
        }
    
        @Override
        public void writeEndArray(JsonGenerator g, int nrOfValues) throws IOException {
            if (!_arrayIndenter.isInline()) {
                _nesting--;
            }
            if (nrOfValues > 0) {
                _arrayIndenter.writeIndentation(g, _nesting);
            }
            g.writeRaw("]");
        }
    }    
}