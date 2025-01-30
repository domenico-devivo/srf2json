package eu.fbk.srf2json;

public class IncompleteOutputException extends RuntimeException {
    public IncompleteOutputException(Throwable t) {
        super(t);
    }
}
