package eu.fbk.srf2json.views.stringblocktypes;

import eu.fbk.srf2json.dataclasses.ClassDC;
import eu.fbk.srf2json.dataclasses.fsm.TransitionDC;
import eu.fbk.srf2json.parsing.SRF_blocksParser;

public class ConditionStringBlockType extends TransitionStringBlockType {
    public ConditionStringBlockType(TransitionDC transitionDC) {
        super(transitionDC);
    }

    @Override
    public void parse(SRF_blocksParser parser) {
        parser.root_condition();
    }

    @Override
    public String label() {
        return "CONDITION";
    }

    @Override
    public String extractStringBlock() {
        return transitionDC.getRawConditions();
    }
}
