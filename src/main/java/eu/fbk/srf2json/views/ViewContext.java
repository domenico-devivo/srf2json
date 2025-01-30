package eu.fbk.srf2json.views;

import org.apache.commons.lang3.tuple.ImmutableTriple;

public class ViewContext extends ImmutableTriple<String, String, String> {
    public ViewContext(String declName, String fieldName, String listName) {
        super(declName, fieldName, listName);
    }

    public String getDeclName() {
        return left;
    }
    public String getFieldName() {
        return middle;
    }
    public String getListName() {
        return right;
    }
}
