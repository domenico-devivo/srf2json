package eu.fbk.srf2json.views.stringblocktypes;

import eu.fbk.srf2json.dataclasses.ClassDC;
import eu.fbk.srf2json.dataclasses.fsm.TransitionDC;
import eu.fbk.srf2json.parsing.SRF_blocksParser;

public class EffectStringBlockType extends TransitionStringBlockType {
    public EffectStringBlockType(TransitionDC transitionDC) {
        super(transitionDC);
    }

    @Override
    public void parse(SRF_blocksParser parser) {
        parser.root_effect();
    }

    @Override
    public String label() {
        return "EFFECT";
    }

    @Override
    public String extractStringBlock() {
        return transitionDC.getRawEffects();
    }
}
