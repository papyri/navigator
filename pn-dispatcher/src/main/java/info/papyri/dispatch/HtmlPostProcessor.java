package info.papyri.dispatch;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Post-processes HTML output to fix mis-nested line divs that can occur when
 * inline elements (like &lt;num&gt;) span multiple lines in the source TEI XML.
 *
 * The XSLT transformation wraps each line in a &lt;div class="text-line"&gt;,
 * but when an inline element crosses line boundaries, the line-break anchor
 * gets nested inside the inline element, breaking the div structure.
 *
 * This class detects such cases and restructures the HTML to ensure proper nesting.
 *
 * @author Claude Code
 */
public class HtmlPostProcessor {

    // Pattern to match line-break anchors with line numbers
    private static final Pattern LINE_BREAK_PATTERN = Pattern.compile(
        "<a id=\"al(\\d+)\"><!--line-break--></a><span class=\"linenumber[^\"]*\">\\d+</span>"
    );

    /**
     * Fixes mis-nested line divs in the rendered HTML.
     *
     * The problem occurs when inline elements (spans) contain line-break anchors.
     * This method:
     * 1. Finds all line-break anchors that are NOT immediately after a &lt;div class="text-line"&gt;
     * 2. Closes the current div before the line-break anchor
     * 3. Opens a new div with appropriate attributes after the line number span
     * 4. Removes empty duplicate divs created by the XSLT
     *
     * @param html the HTML content to post-process
     * @return the corrected HTML with properly nested line divs
     */
    public static String fixMisnestedLineDivs(String html) {
        if (html == null || html.isEmpty()) {
            return html;
        }

        StringBuilder result = new StringBuilder(html.length() + 1024);
        Matcher matcher = LINE_BREAK_PATTERN.matcher(html);

        int lastEnd = 0;

        while (matcher.find()) {
            int lineNumber = Integer.parseInt(matcher.group(1));
            int breakStart = matcher.start();
            int breakEnd = matcher.end();

            // Check if this line-break is already properly positioned
            // (i.e., immediately after opening a text-line div)
            String beforeBreak = html.substring(Math.max(0, breakStart - 200), breakStart);

            // If the line-break comes right after a <div class="text-line"...>, it's already correct
            if (beforeBreak.matches(".*<div class=\"text-line\"[^>]*>\\s*$")) {
                result.append(html, lastEnd, breakEnd);
                lastEnd = breakEnd;
                continue;
            }

            // This is a mis-nested line-break - we need to close the current div and open a new one

            // Append everything up to the line-break
            result.append(html, lastEnd, breakStart);

            // Close the previous div
            result.append("</div>\n");

            // Open a new div with proper attributes
            result.append("<div class=\"text-line\" data-line=\"")
                  .append(lineNumber)
                  .append("\" aria-label=\"Line ")
                  .append(lineNumber)
                  .append("\">");

            // Append the line-break anchor and line number span
            result.append(html, breakStart, breakEnd);

            lastEnd = breakEnd;
        }

        // Append any remaining content
        if (lastEnd < html.length()) {
            result.append(html, lastEnd, html.length());
        }

        // Clean up empty duplicate divs created by XSLT
        String cleaned = removeEmptyDuplicateLineDivs(result.toString());

        return cleaned;
    }

    /**
     * Removes empty or continuation-only text-line divs that appear immediately before populated divs with the same line number.
     * The XSLT creates divs for line continuations (containing just "-"), and when we split mis-nested content,
     * we create new divs, resulting in duplicates.
     *
     * @param html the HTML content
     * @return HTML with duplicate divs removed
     */
    private static String removeEmptyDuplicateLineDivs(String html) {
        // Pattern to match a text-line div with empty content or just a hyphen,
        // followed by whitespace and another div with same line number
        // Example: <div class="text-line" data-line="7" aria-label="Line 7">-</div>
        //          <div class="text-line" data-line="7" aria-label="Line 7"><a id="al7">...
        Pattern continuationDivPattern = Pattern.compile(
            "<div class=\"text-line\" data-line=\"(\\d+(?:bis)?)\" aria-label=\"Line \\1\">-?\\s*</div>\\s*(?=<div class=\"text-line\" data-line=\"\\1\")"
        );

        return continuationDivPattern.matcher(html).replaceAll("");
    }

    /**
     * Performs all post-processing steps on the HTML content.
     * Currently only fixes mis-nested line divs, but can be extended
     * to handle other post-processing tasks.
     *
     * @param html the HTML content to post-process
     * @return the post-processed HTML
     */
    public static String process(String html) {
        if (html == null || html.isEmpty()) {
            return html;
        }

        return fixMisnestedLineDivs(html);
    }
}
