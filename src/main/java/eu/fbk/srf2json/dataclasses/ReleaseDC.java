package eu.fbk.srf2json.dataclasses;

import com.fasterxml.jackson.annotation.*;

import java.util.ArrayList;
import java.util.List;

@JsonPropertyOrder({ "logicRelease" })
public class ReleaseDC extends ExtensibleDC {

    @JsonPropertyOrder({ "label", "version", "note" })
    public static class SubReleaseDC extends ExtensibleDC {
        public String label = "";
        public String version = "";
        public String note = "";
    }

    @JsonPropertyOrder({ "logicVersion", "author", "description", "subReleases" })
    public static class LogicReleaseDC extends ExtensibleDC {
        public String logicVersion = "";
        public String author = "";
        public String description = "";
        public List<SubReleaseDC> subReleases = new ArrayList<>();
    }

    public LogicReleaseDC logicRelease;
}
