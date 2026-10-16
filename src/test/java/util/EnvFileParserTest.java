package util;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class EnvFileParserTest {
    @Test
    void parsesSpacesQuotesCommentsBlankLinesCrLfAndBom() throws Exception {
        String env = "\uFEFF# comment\r\n\r\n"
                + "SEPAY_ACCOUNT_NAME=\"PHAN TUAN ANH\"\r\n"
                + "SEPAY_BANK_CODE=BIDV\r\n"
                + "SEPAY_ACCOUNT_NUMBER='123 456'\r\n"
                + "; another comment\r\n";

        Map<String, String> values = EnvFileParser.parse(
                new BufferedReader(new StringReader(env)));

        assertEquals("PHAN TUAN ANH", values.get("SEPAY_ACCOUNT_NAME"));
        assertEquals("BIDV", values.get("SEPAY_BANK_CODE"));
        assertEquals("123 456", values.get("SEPAY_ACCOUNT_NUMBER"));
    }

    @Test
    void preservesUnquotedValueWithSpaces() throws Exception {
        Map<String, String> values = EnvFileParser.parse(new BufferedReader(
                new StringReader("SEPAY_ACCOUNT_NAME=PHAN TUAN ANH\n")));

        assertEquals("PHAN TUAN ANH", values.get("SEPAY_ACCOUNT_NAME"));
    }

    @Test
    void missingKeysAreNotInvented() throws Exception {
        Map<String, String> values = EnvFileParser.parse(new BufferedReader(
                new StringReader("SEPAY_BANK_CODE=BIDV\n")));

        assertFalse(values.containsKey("SEPAY_API_KEY"));
        assertFalse(values.containsKey("SEPAY_ACCOUNT_NUMBER"));
    }
}
