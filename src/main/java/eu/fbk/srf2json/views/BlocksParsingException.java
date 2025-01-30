package eu.fbk.srf2json.views;

public class BlocksParsingException extends RuntimeException {
    private final String sbt;
    private final String stringBlock;
    private final String hierarchy;
    private final String contextTree;
    private final String parsedSoFar;

    public BlocksParsingException(String sbt, String stringBlock, String hierarchy, String contextTree, String parsedSoFar) {
        this.sbt = sbt;
        this.stringBlock = stringBlock;
        this.hierarchy = hierarchy;
        this.contextTree = contextTree;
        this.parsedSoFar = parsedSoFar;
    }

    public String prepareMessage(String className, String enclosingObjectName) {
        return prepareGeneralMessage(sbt, stringBlock, className, enclosingObjectName) + prepareSpecificMessage();
    }

    public static String prepareGeneralMessage(String sbt, String stringBlock, String className, String enclosingObjectName) {
        return
            "\nAn error occurred while parsing a(n) " +
            sbt +
            " string block:\n" +
            stringBlock +
            "\nIn enclosing object '" +
            enclosingObjectName +
            "' of class " +
            className
        ;
    }

    public String prepareSpecificMessage() {
        return
            "\nThe context hierarchy of the produced parse tree is:\n" +
            hierarchy +
            "\nThe context tree is:\n" +
            contextTree +
            "\nand covers the following part of the string block:\n" +
            parsedSoFar
        ;
    }
}
