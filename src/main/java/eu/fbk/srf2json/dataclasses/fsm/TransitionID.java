package eu.fbk.srf2json.dataclasses.fsm;

public class TransitionID implements Comparable<TransitionID> {
    private int typeIndex;
    private String toState;
    private String conditions;
    private String effects;

    public TransitionID(String type, String toState, String conditions, String effects) {
        super();

        switch (type) {
            case "initial":
                this.typeIndex = 1;
                break;
            case "permanenza":
                this.typeIndex = 2;
                break;
            case "attuazione":
                this.typeIndex = 3;
                break;
            case "normalizzazione":
                this.typeIndex = 4;
                break;
            case "attuazione con soccorso":
                this.typeIndex = 5;
                break;
            default:
                throw new IllegalArgumentException("Unknown transition type: " + type);
        }
        this.toState = toState;
        this.conditions = conditions;
        this.effects = effects;
    }

    @Override
    public int compareTo(TransitionID o) {
        int tmp = Integer.compare(this.typeIndex, o.typeIndex);
        if (tmp != 0) {
            return tmp;
        }

        tmp = this.toState.compareTo(o.toState);
        if (tmp != 0) {
            return tmp;
        }

        tmp = this.conditions.compareTo(o.conditions);
        if (tmp != 0) {
            return tmp;
        }

        return this.effects.compareTo(o.effects);
    }
}
