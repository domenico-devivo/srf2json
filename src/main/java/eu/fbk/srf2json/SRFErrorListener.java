package eu.fbk.srf2json;

import org.antlr.v4.runtime.ConsoleErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import java.util.logging.Level;
import java.util.logging.Logger;

public class SRFErrorListener extends ConsoleErrorListener {
    private String className = null;
    private String enclosingObjectName = null;

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer,
                            Object offendingSymbol,
                            int line,
                            int charPositionInLine,
                            String msg,
                            RecognitionException e)
    {
        String customPart;
        if (className != null || enclosingObjectName != null) {
            customPart =
                "==========================================================\n" +
                "==========================================================\n" +
                "==========================================================\n" +
                "Problem in the enclosing object '" + enclosingObjectName + "' of class " + className
            ;
        } else {
            customPart = "Problem in file " + recognizer.getInputStream().getSourceName();
        }
        String inheritedPart = "line " + line + ":" + charPositionInLine + " " + msg;

        String combinedMsg =
            customPart + "\n" +
            inheritedPart + "\n"
        ;
        Logger.getLogger("eu.fbk.srf2json").log(Level.SEVERE, combinedMsg, e);
        throw new IncompleteOutputException(e);
    }

    public SRFErrorListener setErrorContext(String className, String enclosingObjectName) {
        this.className = className;
        this.enclosingObjectName = enclosingObjectName;

        return this;
    }

    public SRFErrorListener resetErrorContext() {
        this.className = null;
        this.enclosingObjectName = null;

        return this;
    }
}
