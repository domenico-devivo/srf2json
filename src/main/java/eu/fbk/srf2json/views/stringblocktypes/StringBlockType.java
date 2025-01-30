package eu.fbk.srf2json.views.stringblocktypes;

import eu.fbk.srf2json.dataclasses.ClassDC;
import eu.fbk.srf2json.parsing.SRF_blocksParser;

public interface StringBlockType {
    void parse(SRF_blocksParser parser);

    String label();

    String extractStringBlock();

    String getEnclosingObjectName();
}
