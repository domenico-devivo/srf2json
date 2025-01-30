package eu.fbk.srf2json.views.stringblocktypes;

import eu.fbk.srf2json.dataclasses.fsm.TransitionDC;

public abstract class TransitionStringBlockType implements StringBlockType {
    protected final TransitionDC transitionDC;

    public TransitionStringBlockType(TransitionDC transitionDC) {
        this.transitionDC = transitionDC;
    }

    @Override
    public String getEnclosingObjectName() {
        return transitionDC.getType() + " wrt to state " + transitionDC.getToState();
    }

    @Override
    public int hashCode() {
        return transitionDC.hashCode() + label().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        return transitionDC.equals(((TransitionStringBlockType)o).transitionDC);
    }
}
