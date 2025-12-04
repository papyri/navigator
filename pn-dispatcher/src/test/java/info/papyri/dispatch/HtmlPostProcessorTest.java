package info.papyri.dispatch;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test cases for HtmlPostProcessor.
 */
public class HtmlPostProcessorTest {

    @Test
    public void testFixMisnestedLineDivs_withMisnesting() {
        // This is the actual problematic case from the issue - including the empty div created by XSLT
        String input = "<div class=\"text-line\" data-line=\"8\" aria-label=\"Line 8\">" +
                "<a id=\"al8\"><!--line-break--></a><span class=\"linenumber visually-hidden\">8</span>" +
                "ἐξέτασιν ποιή̣[σ]ασθαι τοῦ γ[ε]νομένου ἀ[ργυ]ρικοῦ " +
                "<span data-bs-original-title=\"number: 60\">ἑξηκονταδρ[ά-]" +
                "<a id=\"al9\"><!--line-break--></a><span class=\"linenumber visually-hidden\">9</span>χμου</span> " +
                "μερισμ[οῦ] εἰς τὸ κατʼ οἰ[κ]ίαν τῆς πό[λε]ως ὑπὲρ τοῦ εἰσ[ιέ]ναι\n" +
                "</div>\n" +
                "<div class=\"text-line\" data-line=\"10\" aria-label=\"Line 10\">" +
                "<a id=\"al10\"><!--line-break--></a><span class=\"linenumber\">10</span>" +
                "τὰ ἐκ τούτου τοῦ λόγου εἰσπεπραγμένα ἀργύρια ἔτι τε καὶ τ[οῦ] " +
                "</div>";

        String result = HtmlPostProcessor.fixMisnestedLineDivs(input);

        // Line 9 should now be in its own div
        assertTrue("Result should contain a div for line 9", result.contains("data-line=\"9\""));

        // Line 9 should have the line-break anchor right after the opening div tag
        assertTrue("Line 9 div should be properly structured",
                result.contains("<div class=\"text-line\" data-line=\"9\" aria-label=\"Line 9\"><a id=\"al9\">"));

        // The word "χμου" should be in the line 9 div
        int line9DivStart = result.indexOf("data-line=\"9\"");
        int line9DivEnd = result.indexOf("<div class=\"text-line\" data-line=\"10\"");
        String line9Content = result.substring(line9DivStart, line9DivEnd);
        assertTrue("Line 9 should contain χμου", line9Content.contains("χμου"));
    }

    @Test
    public void testRemoveEmptyDuplicateLineDivs() {
        // Test with the pattern where XSLT creates empty divs that get duplicated when we fix mis-nesting
        String input = "<div class=\"text-line\" data-line=\"2\" aria-label=\"Line 2\"></div>\n" +
                "                           <div class=\"text-line\" data-line=\"2\" aria-label=\"Line 2\">" +
                "<a id=\"al2\"><!--line-break--></a><span class=\"linenumber\">2</span>Content here</div>\n" +
                "<div class=\"text-line\" data-line=\"3\" aria-label=\"Line 3\"></div>\n" +
                "<div class=\"text-line\" data-line=\"3\" aria-label=\"Line 3\">" +
                "<a id=\"al3\"><!--line-break--></a><span class=\"linenumber\">3</span>More content</div>";

        String result = HtmlPostProcessor.fixMisnestedLineDivs(input);

        // Empty duplicate divs should be removed
        assertFalse("Should not contain empty div for line 2 before populated one",
                result.contains("<div class=\"text-line\" data-line=\"2\" aria-label=\"Line 2\"></div>\n" +
                        "                           <div class=\"text-line\" data-line=\"2\""));

        // Content should still be there
        assertTrue("Should still have line 2 content", result.contains("Content here"));
        assertTrue("Should still have line 3 content", result.contains("More content"));

        // Should only have one div per line
        assertEquals("Should have exactly one div for line 2", 1,
                countOccurrences(result, "data-line=\"2\""));
        assertEquals("Should have exactly one div for line 3", 1,
                countOccurrences(result, "data-line=\"3\""));
    }

    private int countOccurrences(String str, String substring) {
        int count = 0;
        int index = 0;
        while ((index = str.indexOf(substring, index)) != -1) {
            count++;
            index += substring.length();
        }
        return count;
    }

    @Test
    public void testFixMisnestedLineDivs_alreadyCorrect() {
        // Already correctly nested HTML shouldn't be changed
        String input = "<div class=\"text-line\" data-line=\"8\" aria-label=\"Line 8\">" +
                "<a id=\"al8\"><!--line-break--></a><span class=\"linenumber\">8</span>" +
                "Some content here" +
                "</div>\n" +
                "<div class=\"text-line\" data-line=\"9\" aria-label=\"Line 9\">" +
                "<a id=\"al9\"><!--line-break--></a><span class=\"linenumber\">9</span>" +
                "More content" +
                "</div>";

        String result = HtmlPostProcessor.fixMisnestedLineDivs(input);

        // The structure should remain essentially the same (may have minor whitespace differences)
        assertTrue("Should still have line 8 div", result.contains("data-line=\"8\""));
        assertTrue("Should still have line 9 div", result.contains("data-line=\"9\""));
        assertTrue("Line 8 content preserved", result.contains("Some content here"));
        assertTrue("Line 9 content preserved", result.contains("More content"));
    }

    @Test
    public void testFixMisnestedLineDivs_emptyInput() {
        assertEquals("", HtmlPostProcessor.fixMisnestedLineDivs(""));
    }

    @Test
    public void testFixMisnestedLineDivs_nullInput() {
        assertNull(HtmlPostProcessor.fixMisnestedLineDivs(null));
    }

    @Test
    public void testFixMisnestedLineDivs_noLineDivs() {
        String input = "<div>Some other content</div>";
        String result = HtmlPostProcessor.fixMisnestedLineDivs(input);
        assertEquals("Content without line divs should be unchanged", input, result);
    }

    @Test
    public void testProcess_delegatesToFixMisnestedLineDivs() {
        String input = "<div class=\"text-line\" data-line=\"8\" aria-label=\"Line 8\">" +
                "<a id=\"al8\"><!--line-break--></a><span class=\"linenumber\">8</span>" +
                "Content <span>nested <a id=\"al9\"><!--line-break--></a>" +
                "<span class=\"linenumber\">9</span>content</span>" +
                "</div>";

        String result = HtmlPostProcessor.process(input);

        // Should apply the fix
        assertTrue("Process should fix mis-nested divs", result.contains("data-line=\"9\""));
    }

    @Test
    public void testProcess_nullInput() {
        assertNull(HtmlPostProcessor.process(null));
    }

    @Test
    public void testFixMisnestedLineDivs_multipleNestedBreaks() {
        // Test case with multiple mis-nested line breaks
        String input = "<div class=\"text-line\" data-line=\"5\" aria-label=\"Line 5\">" +
                "<a id=\"al5\"><!--line-break--></a><span class=\"linenumber\">5</span>" +
                "Start <span class=\"num\">crossing" +
                "<a id=\"al6\"><!--line-break--></a><span class=\"linenumber\">6</span> line six " +
                "<a id=\"al7\"><!--line-break--></a><span class=\"linenumber\">7</span> and seven</span> end" +
                "</div>";

        String result = HtmlPostProcessor.fixMisnestedLineDivs(input);

        // All three lines should have their own divs
        assertTrue("Should have div for line 5", result.contains("data-line=\"5\""));
        assertTrue("Should have div for line 6", result.contains("data-line=\"6\""));
        assertTrue("Should have div for line 7", result.contains("data-line=\"7\""));
    }

    @Test
    public void testFixMisnestedLineDivs_visibilityClasses() {
        // Test that both visible and visually-hidden line numbers are handled
        String input = "<div class=\"text-line\" data-line=\"8\" aria-label=\"Line 8\">" +
                "<a id=\"al8\"><!--line-break--></a><span class=\"linenumber visually-hidden\">8</span>" +
                "Content <span>nested <a id=\"al9\"><!--line-break--></a>" +
                "<span class=\"linenumber\">9</span>content</span>" +
                "</div>";

        String result = HtmlPostProcessor.fixMisnestedLineDivs(input);

        assertTrue("Should handle visually-hidden class", result.contains("data-line=\"8\""));
        assertTrue("Should handle regular linenumber class", result.contains("data-line=\"9\""));
    }
}
