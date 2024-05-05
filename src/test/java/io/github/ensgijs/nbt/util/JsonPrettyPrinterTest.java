package io.github.ensgijs.nbt.util;

import junit.framework.TestCase;
import static io.github.ensgijs.nbt.util.JsonPrettyPrinter.prettyPrintJson;

public class JsonPrettyPrinterTest extends TestCase {
    public void testFormatObject_strict() {
        String json = "{\"hello\":\"world\",\"stuff\":[42.5e+2,\"things\"]}";
        String expected = "{\n" +
                "  \"hello\": \"world\",\n" +
                "  \"stuff\": [\n" +
                "    42.5e+2,\n" +
                "    \"things\"\n" +
                "  ]\n" +
                "}";
        assertEquals(expected, prettyPrintJson(json));
    }

    public void testFormatObject_lose() {
        String json = "{hello:\"world\",stuff:[-42.5e-2,\"thang'z\", 'mo\"oo']}";
        String expected = "{\n" +
                "  hello: \"world\",\n" +
                "  stuff: [\n" +
                "    -42.5e-2,\n" +
                "    \"thang'z\",\n" +
                "    'mo\"oo'\n" +
                "  ]\n" +
                "}";
        assertEquals(expected, prettyPrintJson(json));
    }

    public void testFormatArray() {
        String json = "[0,\"one\",\"o\":{},\"a\":[]]";
        String expected = "[\n" +
                "  0,\n" +
                "  \"one\",\n" +
                "  \"o\": {},\n" +
                "  \"a\": []\n" +
                "]";
        assertEquals(expected, prettyPrintJson(json));
    }

    public void testFormatLongSNBTArray() {
        String json = "[L;0, 42, 99]";
        String expected = "[L;\n" +
                "  0,\n" +
                "  42,\n" +
                "  99\n" +
                "]";
        assertEquals(expected, prettyPrintJson(json));
    }
    public void testFormatEmptyLongSNBTArray() {
        String json = "[L;]";
        String expected = "[L;]";
        assertEquals(expected, prettyPrintJson(json));
    }

    public void testFormatIntSNBTArray() {
        String json = "[I;0, 42, 99]";
        String expected = "[I;\n" +
                "  0,\n" +
                "  42,\n" +
                "  99\n" +
                "]";
        assertEquals(expected, prettyPrintJson(json));
    }
    public void testFormatEmptyIntSNBTArray() {
        String json = "[I;]";
        String expected = "[I;]";
        assertEquals(expected, prettyPrintJson(json));
    }

    public void testFormatByteSNBTArray() {
        String json = "[B;0, 42, 99]";
        String expected = "[B;\n" +
                "  0,\n" +
                "  42,\n" +
                "  99\n" +
                "]";
        assertEquals(expected, prettyPrintJson(json));
    }
    public void testFormatEmptyByteSNBTArray() {
        String json = "[B;]";
        String expected = "[B;]";
        assertEquals(expected, prettyPrintJson(json));
    }


    public void testFormatJustAString() {
        String json = "\"hello world\"";
        assertEquals(json, prettyPrintJson(json));
        json = "'hello world'";
        assertEquals(json, prettyPrintJson(json));
    }

    public void testFormatStringContainingNewline() {
        String json = "\"hello\nworld\"";
        assertEquals("\"hello\\nworld\"", prettyPrintJson(json));
    }

    public void testFormatJustANumber() {
        String json = "42";
        assertEquals(json, prettyPrintJson(json));
    }
}
