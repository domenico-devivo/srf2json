package eu.fbk.srf2json.views.stringblocktypes;

import eu.fbk.srf2json.dataclasses.definitions.MacroDefinitionDC;
import eu.fbk.srf2json.parsing.SRF_blocksParser;

public class MacroVerificaStringBlockType extends MacroStringBlockType {
    public MacroVerificaStringBlockType(MacroDefinitionDC macroDefinitionDC) {
        super(macroDefinitionDC);
    }

    @Override
    public void parse(SRF_blocksParser parser) {
        parser.root_macro_verifica();
    }

    @Override
    public String label() {
        return "MACRO_VERIFICA";
    }
}
