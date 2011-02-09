/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.papyri.dispatch;

import java.io.*;
import java.nio.charset.Charset;
import java.text.Normalizer;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.antlr.runtime.*;

/**
 *
 * @author hcayless
 */
public class FileUtils {

  /**
   * FileUtils provides utility methods for dispatcher classes that deal with
   * files in a variety of ways.
   * @param xmlPath the root path where the XML sources are to be found
   * @param htmlPath the root path where the HTML and .txt files are located
   */
  public FileUtils(String xmlPath, String htmlPath) {
    this.xmlPath = xmlPath;
    this.htmlPath = htmlPath;
  }

  private char[] buffer = new char[8192];
  private StringBuilder pathname = new StringBuilder();

  /**
   * Returns the HTML <code>java.io.File</code> for the given collection
   * and item.
   * @param collection
   * @param item
   * @return the HTML file
   */
  public File getHtmlFile(String collection, String item) {
    if (pathname.length() > 0) pathname.delete(0, pathname.length());
    pathname.append(htmlPath);
    if ("ddbdp".equals(collection)) {
      if (item.contains(";")) {
        String[] parts = item.split(";");
        if (parts.length == 2) {
          return new File(pathname.append("/DDB_EpiDoc_XML/")
                  .append(parts[0])
                  .append("/")
                  .append(parts[0])
                  .append(".")
                  .append(parts[1])
                  .append("/index.html").toString());
        } else if ("".equals(parts[1])) {
          return new File(pathname.append("/DDB_EpiDoc_XML/")
                  .append(parts[0])
                  .append("/")
                  .append(parts[0])
                  .append(".")
                  .append(parts[2].replaceAll(",", "-").replaceAll("/", "_").replace(" ", "%2B"))
                  .append(".html").toString());
        } else {
          return new File(pathname.append("/DDB_EpiDoc_XML/")
                  .append(parts[0])
                  .append("/")
                  .append(parts[0])
                  .append(".")
                  .append(parts[1])
                  .append("/")
                  .append(parts[0])
                  .append(".")
                  .append(parts[1])
                  .append(".")
                  .append(parts[2].replaceAll(",", "-").replaceAll("/", "_").replace(" ", "%2B"))
                  .append(".html").toString());
        }
      } else {
        if ("".equals(item)) {
          return new File(pathname.append("/DDB_EpiDoc_XML/index.html").toString());
        } else {
          return new File(pathname.append("/DDB_EpiDoc_XML/")
                  .append(item)
                  .append("/index.html").toString());
        }
      }
    } else if ("hgv".equals(collection)) {
      if (item.matches("\\d+[a-z]*")) {
        return new File(pathname.append("/HGV_meta_EpiDoc/HGV")
                .append((int) Math.ceil(Double.parseDouble(item.replaceAll("[a-z]", "")) / 1000))
                .append("/")
                .append(item)
                .append(".html").toString());
      } else {
        if ("".equals(item)) {
          return new File(pathname.append("/HGV_meta_EpiDoc/index.html").toString());
        } else {
          return new File(pathname.append("/HGV_meta_EpiDoc/")
                  .append(item)
                  .append("/index.html").toString());
        }
      }
    } else if ("apis".equals(collection)) {
      if ("".equals(item)) {
        return new File(pathname.append("/APIS/index.html").toString());
      } else if (item.contains(".")) {
        String[] parts = item.split("\\.");
        return new File(pathname.append("/APIS/")
                .append(parts[0])
                .append("/")
                .append(parts[0])
                .append(".apis.")
                .append(parts[2])
                .append(".html").toString());
      } else {
        return new File(pathname.append("/APIS/")
                .append(item)
                .append("/index.html").toString());
      }
    }
    return null;
  }

  /**
   * Returns the text <code>java.io.File</code> for the given collection
   * and item.
   * @param collection
   * @param item
   * @return the text file
   */
  public File getTextFile(String collection, String item) {
    if (pathname.length() > 0) pathname.delete(0, pathname.length());
    pathname.append(htmlPath);
    if ("ddbdp".equals(collection)) {
      if (item.contains(";")) {
        String[] parts = item.split(";");
        if ("".equals(parts[1])) {
          return new File(pathname.append("/DDB_EpiDoc_XML/")
                  .append(parts[0])
                  .append("/")
                  .append(parts[0])
                  .append(".")
                  .append(parts[2].replaceAll(",", "-").replaceAll("/", "_").replace(" ", "%2B").replace("+", "%2B"))
                  .append(".txt").toString());
        } else {
          return new File(pathname.append("/DDB_EpiDoc_XML/")
                  .append(parts[0])
                  .append("/")
                  .append(parts[0])
                  .append(".")
                  .append(parts[1])
                  .append("/")
                  .append(parts[0])
                  .append(".")
                  .append(parts[1])
                  .append(".")
                  .append(parts[2].replaceAll(",", "-").replaceAll("/", "_").replace(" ", "%2B").replace("+", "%2B"))
                  .append(".txt").toString());
        }
      }
    } else if ("hgv".equals(collection)) {
      if (item.matches("\\d+[a-z]*")) {
        return new File(pathname.append("/HGV_meta_EpiDoc/HGV")
                .append((int) Math.ceil(Double.parseDouble(item.replaceAll("[a-z]", "")) / 1000))
                .append("/")
                .append(item)
                .append(".txt").toString());
      }
    } else if ("apis".equals(collection)) {
      if (item.contains(".")) {
        String[] parts = item.split("\\.");
        return new File(pathname.append("/APIS/")
                .append(parts[0])
                .append("/")
                .append(parts[0])
                .append(".apis.")
                .append(parts[2])
                .append(".txt").toString());
      }
    }
    return null;
  }

  /**
   * Returns the XML <code>java.io.File</code> for the given collection
   * and item.
   * @param collection
   * @param item
   * @return the XML file
   */
  public File getXmlFile(String collection, String item) {
    if (pathname.length() > 0) pathname.delete(0, pathname.length());
    pathname.append(xmlPath);
    if ("ddbdp".equals(collection)) {
      if (item.contains(";")) {
        String[] parts = item.split(";");
        if ("".equals(parts[1])) {
          return new File(pathname.append("/DDB_EpiDoc_XML/")
                  .append(parts[0]).append("/")
                  .append(parts[0])
                  .append(".")
                  .append(parts[2].replaceAll(",", "-").replaceAll("/", "_").replace(" ", "+"))
                  .append(".xml").toString());
        } else {
          return new File(pathname.append("/DDB_EpiDoc_XML/")
                  .append(parts[0])
                  .append("/")
                  .append(parts[0])
                  .append(".")
                  .append(parts[1])
                  .append("/")
                  .append(parts[0])
                  .append(".")
                  .append(parts[1])
                  .append(".")
                  .append(parts[2].replaceAll(",", "-").replaceAll("/", "_").replace(" ", "+")) + ".xml");
        }
      }
    } else if ("hgv".equals(collection)) {
      if (item.matches("\\d+[a-z]*")) {
        return new File(pathname.append("/HGV_meta_EpiDoc/HGV")
                .append((int) Math.ceil(Double.parseDouble(item.replaceAll("[a-z]", "")) / 1000))
                .append("/")
                .append(item)
                .append(".xml").toString());
      }
    } else if ("hgvtrans".equals(collection)) {
      return new File(pathname.append("/HGV_trans_EpiDoc/")
              .append(item)
              .append(".xml").toString());
    } else if ("apis".equals(collection)) {
      if (item.contains(".")) {
        String[] parts = item.split("\\.");
        return new File(pathname.append("/APIS/")
                .append(parts[0])
                .append("/xml/")
                .append(parts[0])
                .append(".apis.")
                .append(parts[2])
                .append(".xml").toString());
      }
    }
    return null;
  }

  /**
   * Given an id, loads the corresponding text file and returns it as a String
   * @param id
   * @return the text
   */
  public String loadTextFromId(String id) {
    return loadFile(getTextFile(substringBefore(id.substring("http://papyri.info/".length()), "/"), substringAfter(id.substring("http://papyri.info/".length()), "/")));
  }

  /**
   * Given an id, loads the corresponding HTML file and returns it as a String
   * @param id
   * @return the HTML
   */
  public String loadHtmlFromId(String id) {
    return loadFile(getHtmlFile(substringBefore(id.substring("http://papyri.info/".length()), "/"), substringAfter(id.substring("http://papyri.info/".length()), "/")));
  }

  /**
   * Reads the given File into a String and returns it
   * @param f
   * @return the File contents
   */
  public String loadFile(File f) {
    StringBuilder t = new StringBuilder();
    try {
      InputStreamReader reader = new InputStreamReader(new FileInputStream(f), Charset.forName("UTF-8"));
      int size = -1;
      while ((size = reader.read(buffer)) > 0) {
        t.append(buffer, 0, size);
      }
      reader.close();
    } catch (Exception e) {
      e.printStackTrace(System.out);
    }
    return t.toString();
  }

  /**
   * Takes a query string and any text or HTML and returns the string with
   * HTML spans that highlight the found query text.
   * @param query
   * @param t
   * @return the highlighted text
   */
  public String highlight(String query, String t) {
    Pattern[] patterns = getPatterns(query);
    List<String> exclusions = getExclusions(t);
    String text = t.toString().replaceAll(exclude, "ЖЖЖ\n");
    int index = 0;
    for (Pattern pattern : patterns) {
      StringBuilder hl = new StringBuilder();
      Matcher m = pattern.matcher(text);
      while (m.find()) {
        hl.append(text.substring(index, m.start()));
        hl.append(hlStart);
        hl.append(text.substring(m.start(), m.end()));
        hl.append(hlEnd);
        index = m.end();
      }
      if (hl.length() > 0) {
        hl.append(text.substring(index));
        text = hl.toString();
        index = 0;
      }
    }
    Pattern p = Pattern.compile("ЖЖЖ\\n?");
    int i = 0;
    int start = 0;
    Matcher m = p.matcher(text);
    StringBuilder result = new StringBuilder();
    while (m.find()) {
      result.append(text.substring(start, m.start()));
      result.append(exclusions.get(i));
      start = m.end();
      i++;
    }
    result.append(text.substring(start));
    return result.toString();
  }
  
  /**
   * Finds matches in a text file and returns the top 3 matches with HTML
   * highlighting applied and with context surrounding the highlighted text.
   * @param query the text to match
   * @param t the text
   * @return A <code>java.util.List</code> containing the top 3 matches plus
   * context
   */
  public List<String> highlightMatches(String query, String t) {
    List<String> result = new ArrayList<String>();
    Pattern[] patterns = getPatterns(query);
    String text = t.toString().replaceAll(excludeTxt, "").replace("<", "&lt;").replace(">", "&gt;");
    for (Pattern pattern : patterns) {
      Matcher m = pattern.matcher(text);
      int prevEnd = 0;
      while (m.find()) {
        int start = m.toMatchResult().start();
        if (start > 30) {
          start -= 30;
          if (text.indexOf(' ', start) > start) {
            start = text.indexOf(' ', start) + 1;
          }
        } else {
          start = 0;
        }
        int end = m.toMatchResult().end();
        if (end > text.length() - 30) {
          end = text.length();
        } else {
          end += 30;
          if (text.indexOf(' ', end) > 0) {
            end = text.indexOf(' ', end) + 1;
          }
        }
        if (start >= prevEnd) {
          result.add(highlight(query, text.substring(start, end)));
          if (result.size() > 2) {
            return result;
          }
          prevEnd = end;
        } else {
          String hit = result.remove(result.size() - 1) + text.substring(prevEnd, end);
          result.add(highlight(query, hit));
          if (result.size() > 2) {
            return result;
          }
        }
      }
    }
    return result;
  }

    private Pattern[] getPatterns(String query) {
      String q = query.replace("*", "£").replace("?", "#");
      ANTLRStringStream a = new ANTLRStringStream(q.replaceAll("[\\\\/]", "").replaceAll("\"([^\"]+)\"~\\d+", "$1"));
      QueryLexer ql = new QueryLexer(a);
      CommonTokenStream tokens = new CommonTokenStream(ql);
      QueryParser qp = new QueryParser(tokens);
      List<String> find = new ArrayList<String>();
      try {
        qp.query();
        find = qp.getStrings();
      } catch (RecognitionException e) {
        return new Pattern[0];
      }
      Pattern[] patterns = new Pattern[find.size()];
      for (int i = 0; i < find.size(); i++) {
        if (query.contains("^") || query.contains("ngram")) {
          patterns[i] = Pattern.compile(find.get(i).toLowerCase()
                .replaceAll("([^ ^])", sigla + "$1" + sigla)
                .replace("^ ^", "\\s+")
                .replace("^", "(\\b)")
                .replaceAll("\\s", "\\\\s+")
                .replaceAll("^\\^", "(\\s|^)")
                .replaceAll("^$", "(\\s|$)")
                .replace("£", "\\S*").replace("#", "\\S").replace("\"", "")
                .replace("α", "(α|ἀ|ἁ|ἂ|ἃ|ἄ|ἅ|ἆ|ἇ|ὰ|ά|ᾀ|ᾁ|ᾂ|ᾃ|ᾄ|ᾅ|ᾆ|ᾇ|ᾲ|ᾳ|ᾴ|ᾶ|ᾷ)")
                .replace("ε", "(ε|ἐ|ἑ|ἒ|ἓ|ἔ|ἕ|έ|ὲ)")
                .replace("η", "(η|ἠ|ἡ|ἢ|ἣ|ἤ|ἥ|ἦ|ἧ|ή|ὴ|ᾐ|ᾑ|ᾒ|ᾓ|ᾔ|ᾕ|ᾖ|ᾗ|ῂ|ῃ|ῄ|ῆ|ῇ)")
                .replace("ι", "(ι|ί|ὶ|ἰ|ἱ|ἲ|ἳ|ἴ|ἵ|ἶ|ἷ|ῒ|ΐ|ῖ|ῗ)")
                .replace("ο", "(ο|ὸ|ό|ὀ|ὁ|ὂ|ὃ|ὄ|ὅ)")
                .replace("υ", "(υ|ύ|ὺ|ὐ|ὑ|ὒ|ὓ|ὔ|ὕ|ὖ|ὗ|ῢ|ΰ|ῦ|ῧ)")
                .replace("ω", "(ω|ώ|ὼ|ὠ|ὡ|ὢ|ὣ|ὤ|ὥ|ὦ|ὧ|ᾠ|ᾡ|ᾢ|ᾣ|ᾤ|ᾥ|ᾦ|ᾧ|ῲ|ῳ|ῴ|ῶ|ῷ)")
                .replace("ρ", "(ρ|ῥ)").replaceAll("(σ|ς)", "(σ|ς)" + sigla),
                Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.UNIX_LINES);
        } else {
          patterns[i] = Pattern.compile("\\b" + find.get(i).toLowerCase()
                .replaceAll("(\\S)", sigla + "$1" + sigla)
                .replaceAll("([^£#])$", "$1\\\\b")
                .replaceAll("\\s", "\\\\s+")
                .replace("£", "\\S*").replace("#", "\\S").replace("\"", "")
                .replace("α", "(α|ἀ|ἁ|ἂ|ἃ|ἄ|ἅ|ἆ|ἇ|ὰ|ά|ᾀ|ᾁ|ᾂ|ᾃ|ᾄ|ᾅ|ᾆ|ᾇ|ᾲ|ᾳ|ᾴ|ᾶ|ᾷ)")
                .replace("ε", "(ε|ἐ|ἑ|ἒ|ἓ|ἔ|ἕ|έ|ὲ)")
                .replace("η", "(η|ἠ|ἡ|ἢ|ἣ|ἤ|ἥ|ἦ|ἧ|ή|ὴ|ᾐ|ᾑ|ᾒ|ᾓ|ᾔ|ᾕ|ᾖ|ᾗ|ῂ|ῃ|ῄ|ῆ|ῇ)")
                .replace("ι", "(ι|ί|ὶ|ἰ|ἱ|ἲ|ἳ|ἴ|ἵ|ἶ|ἷ|ῒ|ΐ|ῖ|ῗ)")
                .replace("ο", "(ο|ὸ|ό|ὀ|ὁ|ὂ|ὃ|ὄ|ὅ)")
                .replace("υ", "(υ|ύ|ὺ|ὐ|ὑ|ὒ|ὓ|ὔ|ὕ|ὖ|ὗ|ῢ|ΰ|ῦ|ῧ)")
                .replace("ω", "(ω|ώ|ὼ|ὠ|ὡ|ὢ|ὣ|ὤ|ὥ|ὦ|ὧ|ᾠ|ᾡ|ᾢ|ᾣ|ᾤ|ᾥ|ᾦ|ᾧ|ῲ|ῳ|ῴ|ῶ|ῷ)")
                .replace("ρ", "(ρ|ῥ)").replaceAll("(σ|ς)", "(σ|ς)" + sigla),
                Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.UNIX_LINES);
        }
    }
    return patterns;
  }

    /**
     * Removes combining diacritical marks from the input string and returns
     * the result.
     * @param in
     * @return in with combining diacriticals removed
     */
    public static String stripDiacriticals(String in) {
      return Normalizer.normalize(in, Normalizer.Form.NFD).replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
    }

  private List<String> getExclusions(String t) {
    List<String> exclusions = new ArrayList<String>();
    Pattern exPattern = Pattern.compile(exclude);
    Matcher exMatch = exPattern.matcher(t);
    while (exMatch.find()) {
      exclusions.add(t.substring(exMatch.start(), exMatch.end()));
    }
    return exclusions;
  }

  /**
   * Given an input string and a string to find within it, returns the
   * remainder of the input string after the first occurrence of the
   * search string.  NOTE: if the search string is not found, this method
   * returns the input string
   * @param in the string to search
   * @param find the string to search for
   * @return the remainder (or the input string if no match)
   */
  public static String substringAfter(String in, String find) {
    if (in.contains(find)) {
      return in.substring(in.indexOf(find) + find.length());
    } else {
      return in;
    }
  }
  
  /**
   * Given an input string and a string to find within it, returns the
   * beginning of the input string before the first occurrence of the
   * search string.  NOTE: if the search string is not found, this method
   * returns the input string
   * @param in the string to search
   * @param find the string to search for
   * @return the beginning (or the input string if no match)
   */
  public static String substringBefore(String in, String find) {
    if (in.contains(find)) {
      return in.substring(0, in.indexOf(find));
    } else {
      return in;
    }
  }

  /**
   * Utility function for mapping old PN static release URLs to new PN URLS
   * @param url
   * @return the rewritten URL
   */
  public static String rewriteOldUrl(String url) {
    String[] staticpath = substringAfter(url, "/html/").split("/");
    StringBuilder result = new StringBuilder();
    if (staticpath.length == 3) {
      result.append("http://papyri.info/ddbdp/").append(staticpath[0])
            .append(";").append(substringAfter(staticpath[1], staticpath[0] + "."))
            .append(";")
            .append(substringBefore(substringAfter(staticpath[2], staticpath[1] + ".").replace("_", "/").replace(",", "-"), ".html"));
    } else {
      result.append("http://papyri.info/ddbdp/").append(staticpath[0])
            .append(";;").append(substringBefore(substringAfter(staticpath[1], staticpath[0] + ".").replace("_", "/").replace(",", "-"), ".html"));
    }
    return result.toString();
  }

  private String xmlPath;
  private String htmlPath;
  private static String sigla = "([-’ʼ\\\\[\\\\]()\u0323〚〛\\\\\\\\/\"|?*Ж.]|&gt;|&lt;|ca\\.)*";
  private static String exclude = "(<span\\s[^>]+>[^<]+</span>|<a\\s[^>]+>[^<]+</a>|<[^>]+>|&\\w+;)";
  private static String excludeTxt = "(-(\\s|\\r|\\n)+[0-9]*\\s*)";
  private static String hlStart = "<span class=\"highlight\">";
  private static String hlEnd = "</span>";
}
