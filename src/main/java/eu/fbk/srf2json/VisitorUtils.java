package eu.fbk.srf2json;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import eu.fbk.srf2json.parsing.SRFParser;
import eu.fbk.srf2json.parsing.SRF_definitionsParser;

public class VisitorUtils {
	public static String getTextReplacingWSWithASpace(ParseTree ctx, Class<? extends Parser> parserClass, boolean removeComments) {
        if (ctx == null || ctx.getChildCount() == 0) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < ctx.getChildCount(); i++) {
        	ParseTree child = ctx.getChild(i);
        	
        	String text = null;
        	//Adapted from org.antlr.v4.runtime.ParserRuleContext.getTokens()
        	if ( child instanceof TerminalNode ) {
                TerminalNode tnode = (TerminalNode)child;
                Token symbol = tnode.getSymbol();
                if (isPureWS(symbol.getType(), parserClass)) {
                    text = " ";
                } else {
                    if (removeComments && isComment(symbol.getType(), parserClass)) {
                        text = "";
                    } else {
                        text = child.getText();
                    }
                }
            }
        	if (text == null) {
        		text = getTextReplacingWSWithASpace(child, parserClass, removeComments);
        	}
        	
            builder.append(text);
        }

        return builder.toString().replace("\t", " ").replace("\r", "").replaceAll("\\s+", " ");
    }

    public static String getTextRemovingComments(ParseTree ctx, Class<? extends Parser> parserClass) {
        if (ctx == null || ctx.getChildCount() == 0) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);

            String text = null;
            //Adapted from org.antlr.v4.runtime.ParserRuleContext.getTokens()
            if ( child instanceof TerminalNode ) {
                Token symbol = ((TerminalNode)child).getSymbol();
                if (isComment(symbol.getType(), parserClass)) {
                    text = "";
                } else {
                    text = child.getText();
                }
            }
            if (text == null) {
                text = getTextRemovingComments(child, parserClass);
            }

            builder.append(text);
        }

        return builder.toString();
    }

    public static String processName(String nameToProcess) {
        if (nameToProcess == null) {
            return null;
        }

        nameToProcess = removeAccents(nameToProcess);

        boolean firstPart = true;

        StringBuilder sb = new StringBuilder();
        for (String namePart : nameToProcess.split("\\s+")) {
            if (!namePart.isEmpty()) {
                // The entire name starts with a lowercase letter
                if (firstPart) {
                    sb.append(namePart.toLowerCase());
                    firstPart = false;
                } else {
                    sb.append(namePart.substring(0, 1).toUpperCase());
                    sb.append(namePart.substring(1).toLowerCase());
                }
            }
        }

        return sb.toString();
    }

    public static String eliminateSpaces(String nameToProcess) {
        if (nameToProcess == null) {
            return null;
        }

        nameToProcess = removeAccents(nameToProcess);

        StringBuilder sb = new StringBuilder();
        for (String namePart : nameToProcess.split("\\s+")) {
            if (!namePart.isEmpty()) {
                sb.append(namePart);
            }
        }

        return sb.toString();
    }

    public static String capitalize(String nameToProcess) {
        if (nameToProcess == null) {
            return null;
        }

        nameToProcess = removeAccents(nameToProcess);

        return nameToProcess.substring(0, 1).toUpperCase() + nameToProcess.substring(1).toLowerCase();
    }

    public static String removeAccents(String nameToProcess) {
        if (nameToProcess == null) {
            return null;
        }

        return nameToProcess
                .replace("à", "a ")
                .replace("è", "e ")
                .replace("é", "e ")
                .replace("ò", "o ")
                .replace("ì", "i ")
                .replace("/", "")
                .replace("'", " ")
                ;
    }

    public static boolean isPureWS(int symbolType, Class<? extends Parser> parserClass) {
        return (
                (symbolType == SRFParser.PURE_WS && parserClass.equals(SRFParser.class)) ||
                (symbolType == SRF_definitionsParser.PURE_WS && parserClass.equals(SRF_definitionsParser.class))
        );
    }

    public static boolean isComment(int symbolType, Class<? extends Parser> parserClass) {
        return (
                ((symbolType == SRFParser.COMMENT_BLOCK || symbolType == SRFParser.COMMENT_LINE) && parserClass.equals(SRFParser.class)) ||
                ((symbolType == SRF_definitionsParser.COMMENT_BLOCK || symbolType == SRF_definitionsParser.COMMENT_LINE) && parserClass.equals(SRF_definitionsParser.class))
        );
    }
}
