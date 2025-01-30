package eu.fbk.srf2json.views;

import eu.fbk.srf2json.dataclasses.ClassDC;
import org.apache.commons.lang3.tuple.ImmutablePair;

/**
 * A tuple that describes a context found in a class definition and needed to
 * populate a view.
 */
public class ViewListContext extends ImmutablePair<String, ClassDC> {
    public ViewListContext(String declName, ClassDC referencedClass) {
        super(declName, referencedClass);
    }

    /**
     * A getter for the name of the declaration (attribute / macro) of
     * the referenced class found in this context. This is needed to find out
     * which members of the referenced class should remain in the class copy to
     * add as a view class.
     * @return The name of the declaration (attribute / macro) of
     * the referenced class found in this context.
     */
    public String getDeclName() {
        return left;
    }

    /**
     * A getter for the {@link eu.fbk.srf2json.dataclasses.ClassDC} object that
     * contains the observed declaration that can be found by `declName`.
     * @return The {@link eu.fbk.srf2json.dataclasses.ClassDC} object, instance
     * of which has been referenced in the context (via a field of a list).
     */
    public ClassDC getReferencedClass() {
        return right;
    }
}
