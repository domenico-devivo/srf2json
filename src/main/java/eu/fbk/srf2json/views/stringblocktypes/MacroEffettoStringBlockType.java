package eu.fbk.srf2json.views.stringblocktypes;

import eu.fbk.srf2json.dataclasses.definitions.MacroDefinitionDC;
import eu.fbk.srf2json.parsing.SRF_blocksParser;

public class MacroEffettoStringBlockType extends MacroStringBlockType {
    public MacroEffettoStringBlockType(MacroDefinitionDC macroDefinitionDC) {
        super(macroDefinitionDC);
    }

    @Override
    public void parse(SRF_blocksParser parser) {
        parser.root_macro_effetto();
    }

    @Override
    public String label() {
        return "MACRO_EFFETTO";
    }
}
