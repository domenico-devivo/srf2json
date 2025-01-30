package eu.fbk.srf2json.views.stringblocktypes;

import eu.fbk.srf2json.dataclasses.definitions.MacroDefinitionDC;
import eu.fbk.srf2json.parsing.SRF_blocksParser;

public class MacroValorizzataStringBlockType extends MacroStringBlockType {
    public MacroValorizzataStringBlockType(MacroDefinitionDC macroDefinitionDC) {
        super(macroDefinitionDC);
    }

    @Override
    public void parse(SRF_blocksParser parser) {
        parser.root_macro_valorizzata();
    }

    @Override
    public String label() {
        return "MACRO_VALORIZZATA";
    }
}
