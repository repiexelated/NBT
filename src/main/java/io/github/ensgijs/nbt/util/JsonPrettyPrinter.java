package io.github.ensgijs.nbt.util;

/**
 * Very forgiving json parser and formatter.
 * Logic strongly based on implementation of https://jsonviewer.stack.hu/
 */
public final class JsonPrettyPrinter {
    private JsonPrettyPrinter() {}

    public static String prettyPrintJson(final String jsonText) {
        StringBuilder sb = new StringBuilder();
        int indentationLevel = 0;
        boolean inString = false;
        char strQuoteChar = '\0';
        for (int i = 0, jsonLen = jsonText.length(); i < jsonLen; i++) {
            char c = jsonText.charAt(i);
            if (inString && c == strQuoteChar) {
                if (jsonText.charAt(i - 1) != '\\') {
                    inString = false;
                }
                sb.append(c);
            } else if (!inString && (c == '"' || c == '\'')) {
                inString = true;
                strQuoteChar = c;
                sb.append(c);
            } else if (!inString && (c == ' ' || c == '\t' || c == '\n' || c == '\r')) {
                continue;
            } else if (!inString && c == ':') {
                sb.append(c).append(' ');
            } else if (!inString && c == ',') {
                appendIndent(sb.append(",\n"), indentationLevel);
            } else if (!inString && (c == '[' || c == '{')) {
                indentationLevel++;
                sb.append(c);
                // keep int/long tag array hint on same line next to open bracket
                if (i + 2 < jsonLen && jsonText.charAt(i + 2) == ';') {
                    sb.append(jsonText.charAt(i + 1)).append(';');
                    i += 2;
                }
                if (i + 1 < jsonLen && jsonText.charAt(i + 1) == ']') {
                    sb.append(']');
                    i ++;
                    indentationLevel--;
                } else {
                    appendIndent(sb.append('\n'), indentationLevel);
                }
            } else if (!inString && c == ']') {
                indentationLevel--;
                appendIndent(sb.append('\n'), indentationLevel);
//                if (jsonText.charAt(i - 1) != '[') {
//                    appendIndent(sb.append('\n'), indentationLevel);
//                } else {
//                    sb.setLength(sb.lastIndexOf("[") + 1);
//                }
                sb.append(c);
            } else if (!inString && c == '}') {
                indentationLevel--;
                if (jsonText.charAt(i - 1) != '{') {
                    appendIndent(sb.append('\n'), indentationLevel);
                } else {
                    sb.setLength(sb.lastIndexOf("{") + 1);
                }
                sb.append(c);
            } else if (inString && c == '\n') {
                sb.append("\\n");
            } else if (c != '\r') {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static void appendIndent(final StringBuilder output, final int indentationLevel) {
        for (int i = 0; i < indentationLevel * 2; i++) {
            output.append(' ');
        }
    }
}
