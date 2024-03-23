package net.querz.util;

import junit.framework.TestCase;
import static net.querz.util.JsonPrettyPrinter.prettyPrintJson;

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
