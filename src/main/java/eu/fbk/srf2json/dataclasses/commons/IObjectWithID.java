package eu.fbk.srf2json.dataclasses.commons;

public interface IObjectWithID<ID extends Comparable<ID>> {
    ID getSubTreeID();

    static <StID extends Comparable<StID>> int compareTwoIDs(StID a, StID b) {
        return a.compareTo(b);
    }
}
