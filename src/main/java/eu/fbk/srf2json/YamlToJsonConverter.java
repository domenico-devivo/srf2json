package eu.fbk.srf2json;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import eu.fbk.srf2json.dataclasses.ReleaseDC;

import java.io.File;
import java.io.IOException;

public class YamlToJsonConverter {
    public static ReleaseDC convertYamlToJson(File yamlFile) throws IOException {
        return new YAMLMapper().readValue(yamlFile, ReleaseDC.class);
    }
}
