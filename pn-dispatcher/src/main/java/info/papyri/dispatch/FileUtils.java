package info.papyri.dispatch;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.antlr.runtime.*;
import java.util.logging.Level;
import java.util.logging.Logger;

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
  
  /*
   * Constructor for cases where only XML file resolution will be needed. 
   * NOTE: methods dealing with HTML files will throw a NullPointerException
   * @param xmlPath the root path where the XML sources are to be found
   */
  public FileUtils(String xmlPath) {
    this.xmlPath = xmlPath;
  }

  private final char[] buffer = new char[8192];
  private static Logger logger = Logger.getLogger("pn-dispatch");
  
  /**
   * Returns the HTML <code>java.io.File</code> for the given collection
   * and item.
   * @param collection
   * @param item
   * @return the HTML file
   */
  public File getHtmlFile(String collection, String item) {
    StringBuilder pathname = new StringBuilder();
    pathname.append(htmlPath);
    if ("ddbdp".equals(collection)) {
      if (item.contains(";")) {
        String[] parts = item.split(";");
        if (parts.length == 2) {
          return new File(pathname.append("DDB_EpiDoc_XML/")
                  .append(parts[0])
                  .append("/")
                  .append(parts[0])
                  .append(".")
                  .append(parts[1])
                  .append("/index.html").toString());
        } else if ("".equals(parts[1])) {
          return new File(pathname.append("DDB_EpiDoc_XML/")
                  .append(parts[0])
                  .append("/")
                  .append(parts[0])
                  .append(".")
                  .append(parts[2].replaceAll(",", "-").replaceAll("/", "_").replace(" ", "%2B"))
                  .append(".html").toString());
        } else {
          return new File(pathname.append("DDB_EpiDoc_XML/")
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
          return new File(pathname.append("DDB_EpiDoc_XML/index.html").toString());
        } else {
          return new File(pathname.append("DDB_EpiDoc_XML/")
                  .append(item)
                  .append("/index.html").toString());
        }
      }
    } else if ("hgv".equals(collection)) {
      if (item.matches("\\d+[a-z]*")) {
        return new File(pathname.append("HGV_meta_EpiDoc/HGV")
                .append((int) Math.ceil(Double.parseDouble(item.replaceAll("[a-z]", "")) / 1000))
                .append("/")
                .append(item)
                .append(".html").toString());
      } else {
        if ("".equals(item)) {
          return new File(pathname.append("HGV_meta_EpiDoc/index.html").toString());
        } else {
          return new File(pathname.append("HGV_meta_EpiDoc/")
                  .append(item)
                  .append("/index.html").toString());
        }
      }
    } else if ("dclp".equals(collection)) {
      if (item.matches("\\d+[a-z]*")) {
        return new File(pathname.append("DCLP/")
                .append((int) Math.ceil(Double.parseDouble(item.replaceAll("[a-z]", "")) / 1000))
                .append("/")
                .append(item)
                .append(".html").toString());
      } else {
        if ("".equals(item)) {
          return new File(pathname.append("DCLP/index.html").toString());
        } else {
          return new File(pathname.append("DCLP/")
                  .append(item)
                  .append("/index.html").toString());
        }
      }
    } else if ("apis".equals(collection)) {
      if ("".equals(item)) {
        return new File(pathname.append("APIS/index.html").toString());
      } else if (item.contains(".")) {
        String[] parts = item.split("\\.");
        return new File(pathname.append("APIS/")
                .append(parts[0])
                .append("/")
                .append(parts[0])
                .append(".apis.")
                .append(parts[2])
                .append(".html").toString());
      } else {
        return new File(pathname.append("APIS/")
                .append(item)
                .append("/index.html").toString());
      }
    } else if ("biblio".equals(collection)) {
      return new File(pathname.append("biblio/")
              .append((int)Math.ceil(Double.parseDouble(item) / 1000))
              .append("/")
              .append(item)
              .append(".html").toString());
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
    StringBuilder pathname = new StringBuilder();
    pathname.append(htmlPath);
    if ("ddbdp".equals(collection)) {
      if (item.contains(";")) {
        String[] parts = item.split(";");
        if ("".equals(parts[1])) {
          return new File(pathname.append("DDB_EpiDoc_XML/")
                  .append(parts[0])
                  .append("/")
                  .append(parts[0])
                  .append(".")
                  .append(parts[2].replaceAll(",", "-").replaceAll("/", "_").replace(" ", "%2B").replace("+", "%2B"))
                  .append(".txt").toString());
        } else {
          return new File(pathname.append("DDB_EpiDoc_XML/")
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
        return new File(pathname.append("HGV_meta_EpiDoc/HGV")
                .append((int) Math.ceil(Double.parseDouble(item.replaceAll("[a-z]", "")) / 1000))
                .append("/")
                .append(item)
                .append(".txt").toString());
      }
    } else if ("dclp".equals(collection)) {
      if (item.matches("\\d+[a-z]*")) {
        return new File(pathname.append("DCLP/")
                .append((int) Math.ceil(Double.parseDouble(item.replaceAll("[a-z]", "")) / 1000))
                .append("/")
                .append(item)
                .append(".txt").toString());
      }
    } else if ("apis".equals(collection)) {
      if (item.contains(".")) {
        String[] parts = item.split("\\.");
        return new File(pathname.append("APIS/")
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
    return new File(getXmlFilePath(collection, item));
  }
  
  public String getXmlFilePathFromId(String id) {
     return this.getXmlFilePath(substringBefore(id.replaceFirst("^https?://papyri.info/", ""), "/"), substringAfter(id.replaceFirst("^https?://papyri.info/", ""), "/"));
  }
  
  public String getXmlFilePath(String collection, String item) {
    StringBuilder pathname = new StringBuilder();
    pathname.append(xmlPath);
    if ("ddbdp".equals(collection)) {
      if (item.contains(";")) {
        String[] parts = item.split(";");
        if (parts.length != 3) { //exit early if not a well-formed name
          return null;
        }
        if ("".equals(parts[1])) {
          pathname.append("DDB_EpiDoc_XML/")
                  .append(parts[0]).append("/")
                  .append(parts[0])
                  .append(".")
                  .append(parts[2].replaceAll(",", "-").replaceAll("/", "_").replace(" ", "+"))
                  .append(".xml").toString();
        } else {
          pathname.append("DDB_EpiDoc_XML/")
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
                  .append(parts[2].replaceAll(",", "-").replaceAll("/", "_").replace(" ", "+"))
                  .append(".xml");
        }
      }
      return pathname.toString();
    } else if ("hgv".equals(collection)) {
      if (item.matches("\\d+[a-z]*")) {
        pathname.append("HGV_meta_EpiDoc/HGV")
                .append((int) Math.ceil(Double.parseDouble(item.replaceAll("[a-z]", "")) / 1000))
                .append("/")
                .append(item)
                .append(".xml").toString();
      }
      return pathname.toString();
    } else if ("hgvtrans".equals(collection)) {
      pathname.append("HGV_trans_EpiDoc/")
              .append(item)
              .append(".xml").toString();
      return pathname.toString();
    } else if ("dclp".equals(collection)) {
      if (item.matches("\\d+[a-z]*")) {
        pathname.append("DCLP/")
                .append((int) Math.ceil(Double.parseDouble(item.replaceAll("[a-z]", "")) / 1000))
                .append("/")
                .append(item)
                .append(".xml").toString();
      }
      return pathname.toString();
    } else if ("apis".equals(collection)) {
      if (item.contains(".")) {
        String[] parts = item.split("\\.");
        pathname.append("APIS/")
                .append(parts[0])
                .append("/xml/")
                .append(parts[0])
                .append(".apis.")
                .append(parts[2])
                .append(".xml").toString();
      }
      return pathname.toString();
    }  else if ("biblio".equals(collection)) {
      pathname.append("Biblio/")
              .append((int)Math.ceil(Double.parseDouble(item) / 1000))
              .append("/")
              .append(item)
              .append(".xml").toString();
      return pathname.toString();
    }
    return null;
  }

  /**
   * Given an id, loads the corresponding text file and returns it as a String
   * @param id
   * @return the text
   */
  public String loadTextFromId(String id) {
    return loadFile(getTextFile(substringBefore(id.replaceFirst("^https?://papyri.info/", ""), "/"), substringAfter(id.replaceFirst("^https?://papyri.info/", ""), "/")));
  }

  /**
   * Given an id, loads the corresponding HTML file and returns it as a String
   * @param id
   * @return the HTML
   */
  public String loadHtmlFromId(String id) {
    return loadFile(getHtmlFile(substringBefore(id.replaceFirst("^https?://papyri.info/", ""), "/"), substringAfter(id.replaceFirst("^https?://papyri.info/", ""), "/")));
  }

  public File getTextFileFromId(String id) {
    return this.getTextFile(substringBefore(id.replaceFirst("^https?://papyri.info/", ""), "/"), substringAfter(id.replaceFirst("^https?://papyri.info/", ""), "/"));
  }

  public File getHtmlFileFromId(String id) {
    return this.getHtmlFile(substringBefore(id.replaceFirst("^https?://papyri.info/", ""), "/"), substringAfter(id.replaceFirst("^https?://papyri.info/", ""), "/"));
  }

  public File getXmlFileFromId(String id) {
    return this.getXmlFile(substringBefore(id.replaceFirst("^https?://papyri.info/", ""), "/"), substringAfter(id.replaceFirst("^https?://papyri.info/", ""), "/"));
  }

  /**
   * Reads the given File into a String and returns it
   * @param f
   * @return the File contents
   */
  public String loadFile(File f) {
    if (!f.exists()) {
      return "";
    }
    StringBuilder t = new StringBuilder();
    InputStreamReader reader = null;
    try {
      reader = new InputStreamReader(new FileInputStream(f), Charset.forName("UTF-8"));
      int size = -1;
      while ((size = reader.read(buffer)) > 0) {
        t.append(buffer, 0, size);
      }
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Failed to read " + f.getAbsolutePath(), e);
    } finally {
      try {
        if (reader != null) {
          reader.close();
        }
      } catch (IOException e) {
        logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
      }
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
  public String highlightText(String query, String t) {
    String text = Normalizer.normalize(t, Normalizer.Form.NFD);
    int[] map;
    if (t.startsWith("<")) {
      text = text.replace("-<br", "- <br")
          .replace("<br", " <br")
          .replace("-  ", "-");
      map = mapHtml(text);
    } else {
      map = mapText(text);
    }
    StringBuilder processedText = new StringBuilder();
    for (int i = 0; i < map.length; i++) {
      processedText.append(text.charAt(map[i]));
    }
    return highlight(query, processedText.toString(), text, map);
  }

  private static int[] mapHtml(String text) {
    int[] letters = new int[5000];
    boolean skip = false;
    boolean inBody = false;
    int letterIndex = 0;
    for (int i = 0; i < text.length(); i++) {
      if (skip && text.charAt(i) != '>') {
        continue;
      }
      if (skip && text.charAt(i) == '>') {
        skip = false;
        continue;
      }
      if (text.charAt(i) == '<') {
        skip = true;
        // Skip line numbers
        if (text.startsWith("<span class=\"linenumber\">", i)) {
          i = text.indexOf("</span>", i);
        }
        if (text.startsWith("<body", i)) {
          inBody = true;
        }
        continue;
      }
      if (!inBody) {
        continue;
      }
      if (letterIndex >= letters.length - 1) {
        int[] exp = new int[letters.length * 2];
        System.arraycopy(letters, 0, exp, 0, letters.length);
        letters = exp;
      }
      if (Character.isAlphabetic(text.codePointAt(i)) ||
          Character.isDigit(text.codePointAt(i)) ||
          Character.isWhitespace(text.codePointAt(i)) ||
          text.codePointAt(i) == '.' ||
          text.codePointAt(i) == ',' ||
          text.codePointAt(i) == ';'
      ) {
        letters[letterIndex++] = i;
      }
    }
    return letters;
  }

  public int[] mapText(String text) {
    int[] letters = new int[5000];
    int letterIndex = 0;
    for (int i = 0; i < text.length(); i++) {
      // Treat word-breaking newlines as nothing;
      // these have the form wordpart-\n\d(\w|,|/)+\.\s+wordpart, so you find things like '2/3,md.'
      if (text.charAt(i) == '-') {
        if (text.startsWith("-\n", i)) {
          i++;
          while (text.charAt(i) != '.') {
            i++;
          }
          i++;
          while (Character.isWhitespace(text.charAt(i))) {
            i++;
          }
          continue;
        }
      }
      // Treat numbered lines as whitespace; these have the form \n\d(\w|,|/)+\. followed by spaces
      if (text.charAt(i) == '\n') {
        int next = text.length() - i;
        if (next > 10) {
          next = 10;
        }
        String foo = text.substring(i+1, i + next);
        boolean bar = foo.matches("^\\d(\\w|,|/)*\\.\\s{2}.*");
        if (i < text.length() - 1 && text.substring(i+1, i + next).matches("^\\d(\\w|,|/)*\\.\\s{2}.*")) {
          while (text.charAt(i) != '.') {
            i++;
          }
          i++;
          continue;
        }
      }
      if (letterIndex >= letters.length - 1) {
        int[] exp = new int[letters.length * 2];
        System.arraycopy(letters, 0, exp, 0, letters.length);
        letters = exp;
      }
      if (text.charAt(i) == 'ͅ') { // ignore U+0345, COMBINING GREEK YPOGEGRAMMENI
        continue;
      }
      if (Character.isLetterOrDigit(text.codePointAt(i)) ||
          Character.isWhitespace(text.codePointAt(i)) ||
          text.codePointAt(i) == '.' ||
          text.codePointAt(i) == ',' ||
          text.codePointAt(i) == ';'
      ) {
        letters[letterIndex++] = i;
      }
    }
    return letters;
  }

  public String highlight(String query, String t, String originalText, int[] map) {
    String searchText = t.toLowerCase();
    List<String> tokens = getTokensFromQuery(query);
    ArrayList<Hit> locations = new ArrayList<>();
    int start = 0;
    for (String token : tokens) {
      token = token.replaceAll("\"", "");
      if (token.contains("#")) {
        Pattern p = Pattern.compile(token
            .replaceAll("^#", "(?<=\\\\s|[,.;])")
            .replaceAll("#$", "(?=\\\\s|[,.;])")
            .replaceAll("# ", "(?=\\\\s|[,.;]) ")
            .replaceAll(" #", " (?<=\\\\s|[,.;])"),
            Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(searchText);
        while (m.find()) {
          locations.add(new Hit(m.start(), searchText.substring(m.start(), m.end())));
        }
        continue;
      }
      if (token.contains("£") || token.contains("¥")) {
        Pattern p = Pattern.compile(substituteWildcards(token));
        Matcher m = p.matcher(searchText);
        while (m.find()) {
          locations.add(new Hit(m.start(), searchText.substring(m.start(), m.end())));
        }
        continue;
      }
      int found = searchText.indexOf(token.toLowerCase(), start);
      while (found != -1) {
        locations.add(new Hit(found, token));
        start = found + token.length();
        found = searchText.indexOf(token, start);
      }
      start = 0;
    }
    locations.sort(new HitComparator());
    locations = prune(locations);

    StringBuilder result = new StringBuilder();
    start = 0;
    for (Hit hit : locations) {
      result.append(originalText.substring(start, map[hit.location]));
      start = map[hit.location];
      result.append(hlStart);
      result.append(originalText.substring(start, map[hit.location + hit.token.length()]));
      start = map[hit.location + hit.token.length()];
      result.append(hlEnd);
    }
    result.append(originalText.substring(start));
    return Normalizer.normalize(result, Normalizer.Form.NFC);
  }
  
  public String highlight(Pattern[] patterns, String t) {
    List<String> exclusions = getExclusions(t);
    String text = t.toString().replaceAll(exclude, "ⓐⓐⓐ\n");
    int index = 0;
    for (Pattern pattern : patterns) {
      // If pattern is something dumb, like '.', skip it.
      if (bustedRegexes.contains(pattern.toString())) {
        continue;
      }
      StringBuilder hl = new StringBuilder();
      Matcher m = pattern.matcher(text);
      while (m.find()) {
        hl.append(text.substring(index, m.start()));
        hl.append(hlStartMark);
        hl.append(text.substring(m.start(), m.end()));
        hl.append(hlEndMark);
        index = m.end();
      }
      if (hl.length() > 0) {
        hl.append(text.substring(index));
        text = hl.toString();
        index = 0;
      }
    }
    Pattern p = Pattern.compile("ⓐⓐⓐ\\n?");
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
    return result.toString().replaceAll("Ⓐ+", hlStart).replaceAll("Ⓑ+", hlEnd);
  }

  public List<String> highlightMatches(String t, Pattern[] patterns) {
    String highlightedText = highlight(patterns, t);
    return getNMatches(highlightedText, 3);
  }
    
  /**
   * Finds matches in a text file and returns the top 3 matches with HTML
   * highlighting applied and with context surrounding the highlighted text.
   * @param t the text
   * @param patterns the Regex patterns to match
   * @return A <code>java.util.List</code> containing the top 3 matches plus
   * context
   */
  public List<String> highlightMatches(String query, String t) {
    String text = Normalizer.normalize(t, Normalizer.Form.NFD);
    int[] map = mapText(text);
    StringBuilder processedText = new StringBuilder();
    for (int i = 0; i < map.length; i++) {
      processedText.append(text.charAt(map[i]));
    }
    String highlightedText = highlight(query, processedText.toString(), text, map);
    return getNMatches(highlightedText, 3);
  }

  private List<String> getNMatches(String text, int n) {
    String[] lines = text.split("\\n+");
    int found = 0;
    ArrayList<String> hits = new ArrayList<>();
    for (int i = 0; i < lines.length; i++) {
      String line = lines[i];
      if (found >= n) {
        break;
      }
      int start = line.indexOf(hlStart);
      if (start == -1) {
        continue;
      } else {
        found++;
      }
      String hitline;
      if (line.indexOf(hlEnd, start) != -1) { // end is on same line
        hitline = line;
      } else {
        int end = lines[i + 1].indexOf(hlStart);
        if (end > 0) {
          hitline = line + " | " + lines[i + 1].substring(start, end);
        } else {
          hitline = line + " | " + lines[i + 1];
        }
      }
      if (hitline.length() > 60) {
        start = hitline.indexOf(hlStart) - 10;
        if (start >= 0) {
          hitline = hitline.substring(start);
          hitline = '…' + hitline.substring(hitline.indexOf(" ") + 1);
        }
        int end = hitline.lastIndexOf(' ', hitline.lastIndexOf(hlEnd) + 12);
        if (end < hitline.length()) {
          hitline = hitline.substring(0, end);
        }
      }
      hits.add(hitline);
    }
    return hits;
  }
  
    public Pattern[] getPatterns(String query) {
      String q = query.replace("*", "£").replace("?", "¥");
      ANTLRStringStream a = new ANTLRStringStream(q.replaceAll("[\\\\/]", "")
              .replaceAll("\"([^\"]+)\"~\\d+", "$1")
              .replaceAll("^\\{![^}]+\\}", ""));
      QueryLexer ql = new QueryLexer(a);
      CommonTokenStream tokens = new CommonTokenStream(ql);
      QueryParser qp = new QueryParser(tokens);
      List<String> find;
      try {
        qp.query();
        find = qp.getStrings();
      } catch (RecognitionException e) {
        return new Pattern[0];
      }
      Pattern[] patterns = new Pattern[find.size()];
      for (int i = 0; i < find.size(); i++) {
        if (query.contains("#") || query.contains("ngram")) {
          patterns[i] = Pattern.compile(find.get(i).toLowerCase()
                .replaceAll("([^ #])", sigla + "$1" + sigla)
                .replace("# #", "\\s+")
                .replace("#", "(\\b)")
                .replaceAll("\\s", "\\\\s+")
                .replaceAll("^#", "(\\s|^)")
                .replaceAll("^$", "(\\s|$)")
                .replace("£", "\\S*").replace("¥", "\\S").replace("\"", "")
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
                .replaceAll("([^£¥])$", "$1\\\\b")
                .replaceAll("\\s", "\\\\s+")
                .replace("£", "\\S*").replace("¥", "\\S").replace("\"", "")
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
   
  public Pattern[] getSubstringHighlightPatterns(String query){
      List<String> tokens = getTokensFromQuery(query);
      Pattern[] patterns = new Pattern[tokens.size()];
      for(int i = 0; i < tokens.size(); i++){
          
          String token = tokens.get(i);
          token = substituteForSubstringPatternMatch(token);
          patterns[i] = Pattern.compile(token, Pattern.CASE_INSENSITIVE | Pattern.UNIX_LINES | Pattern.UNICODE_CASE);  
          
      }
      
      return patterns;
  }
  
  public Pattern[] getPhraseHighlightPatterns(String query){
      List<String> tokens = getTokensFromQuery(query);
      Pattern[] patterns = new Pattern[tokens.size()];
      for(int i = 0; i < tokens.size(); i++){
      
          String token = tokens.get(i);
          token = substituteForPhrasePatternMatch(token);
          patterns[i] = Pattern.compile(token, Pattern.CASE_INSENSITIVE | Pattern.UNIX_LINES | Pattern.UNICODE_CASE);
      
          
      }
      
      return patterns;
  }
  

    
   public List<String> getTokensFromQuery(String query){
      String q = query.replace("*", "£").replace("?", "¥");
      ANTLRStringStream a = new ANTLRStringStream(q.replaceAll("[\\\\/]", "").replaceAll("\"([^\"]+)\"~\\d+", "$1"));
      QueryLexer ql = new QueryLexer(a);
      CommonTokenStream tokens = new CommonTokenStream(ql);
      QueryParser qp = new QueryParser(tokens);
      List<String> find = new ArrayList<String>();
      try {
        qp.query();
        find = qp.getStrings();
      } catch (RecognitionException e) { }      
       
      return find; 
       
   }
    
   public String substituteDiacritics(String rawString){
        
        String transformedString = rawString
                .replace("α", "(α|ἀ|ἁ|ἂ|ἃ|ἄ|ἅ|ἆ|ἇ|ὰ|ά|ᾀ|ᾁ|ᾂ|ᾃ|ᾄ|ᾅ|ᾆ|ᾇ|ᾲ|ᾳ|ᾴ|ᾶ|ᾷ)")
                .replace("ε", "(ε|ἐ|ἑ|ἒ|ἓ|ἔ|ἕ|έ|ὲ)")
                .replace("η", "(η|ἠ|ἡ|ἢ|ἣ|ἤ|ἥ|ἦ|ἧ|ή|ὴ|ᾐ|ᾑ|ᾒ|ᾓ|ᾔ|ᾕ|ᾖ|ᾗ|ῂ|ῃ|ῄ|ῆ|ῇ)")
                .replace("ι", "(ι|ί|ὶ|ἰ|ἱ|ἲ|ἳ|ἴ|ἵ|ἶ|ἷ|ῒ|ΐ|ῖ|ῗ)")
                .replace("ο", "(ο|ὸ|ό|ὀ|ὁ|ὂ|ὃ|ὄ|ὅ)")
                .replace("υ", "(υ|ύ|ὺ|ὐ|ὑ|ὒ|ὓ|ὔ|ὕ|ὖ|ὗ|ῢ|ΰ|ῦ|ῧ)")
                .replace("ω", "(ω|ώ|ὼ|ὠ|ὡ|ὢ|ὣ|ὤ|ὥ|ὦ|ὧ|ᾠ|ᾡ|ᾢ|ᾣ|ᾤ|ᾥ|ᾦ|ᾧ|ῲ|ῳ|ῴ|ῶ|ῷ)")
                .replace("ρ", "(ρ|ῥ)");                  
        return transformedString;
        
    }
   
   public String stripOutDiacritcs(String rawString){
       
        String transformedString = rawString
                .replace("(α|ἀ|ἁ|ἂ|ἃ|ἄ|ἅ|ἆ|ἇ|ὰ|ά|ᾀ|ᾁ|ᾂ|ᾃ|ᾄ|ᾅ|ᾆ|ᾇ|ᾲ|ᾳ|ᾴ|ᾶ|ᾷ)", "α")
                .replace("(ε|ἐ|ἑ|ἒ|ἓ|ἔ|ἕ|έ|ὲ)", "ε")
                .replace("(η|ἠ|ἡ|ἢ|ἣ|ἤ|ἥ|ἦ|ἧ|ή|ὴ|ᾐ|ᾑ|ᾒ|ᾓ|ᾔ|ᾕ|ᾖ|ᾗ|ῂ|ῃ|ῄ|ῆ|ῇ)", "η")
                .replace("(ι|ί|ὶ|ἰ|ἱ|ἲ|ἳ|ἴ|ἵ|ἶ|ἷ|ῒ|ΐ|ῖ|ῗ)", "ι")
                .replace("(ο|ὸ|ό|ὀ|ὁ|ὂ|ὃ|ὄ|ὅ)", "ο")
                .replace("(υ|ύ|ὺ|ὐ|ὑ|ὒ|ὓ|ὔ|ὕ|ὖ|ὗ|ῢ|ΰ|ῦ|ῧ)", "υ")
                .replace("(ω|ώ|ὼ|ὠ|ὡ|ὢ|ὣ|ὤ|ὥ|ὦ|ὧ|ᾠ|ᾡ|ᾢ|ᾣ|ᾤ|ᾥ|ᾦ|ᾧ|ῲ|ῳ|ῴ|ῶ|ῷ)", "ω")
                .replace("(ρ|ῥ)", "ρ");                  
        return transformedString;
       
   }
   
   public String substituteForSubstringPatternMatch(String rawString){
       
       String transformedString = rawString;
       transformedString = transformedString.toLowerCase()
                .replaceAll("([^ #])", sigla + "$1" + sigla)
                .replace("# #", "\\s+")
                .replace("#", "(\\b)")
                .replaceAll("\\s", "\\\\s+")
                .replaceAll("^#", "(\\s|^)")
                .replaceAll("^$", "(\\s|$)")
                .replaceAll("°", "\\\\((?!\\\\*)")
                .replaceAll("£", "[^//s]*").replaceAll("¥", "[^//s]").replace("\"", "");
       transformedString = substituteDiacritics(transformedString);
       transformedString = swapInSigla(transformedString);
       return transformedString;
       
   }
   
   public String substituteForPhrasePatternMatch(String rawString){
       
       String transformedString = rawString;
       transformedString = transformedString.toLowerCase()
                .replaceAll("(\\S)", sigla + "$1" + sigla)
                .replace("^ #", "\\s+")
                .replace("#", "(\\b)")
                .replaceAll("\\s", "\\\\s+")
                .replaceAll("^#", "(\\s|^)")
                .replaceAll("^$", "(\\s|$)")
                .replaceAll("°", "\\\\((?!\\\\*)")
                .replace("£", "[^\\s]*").replaceAll("¥", "[^\\s]").replace("\"", "");
       transformedString = substituteDiacritics(transformedString);
       transformedString = swapInSigla(transformedString);
       transformedString = "(^|(?<=[\\s]))" + transformedString + "((?=[\\s])|$)";
       return transformedString;
       
   }
   
   public String substituteWildcards(String rawString){
       
      String transformedString = rawString.replaceAll("£", ".*");
      transformedString = transformedString.replaceAll("¥", ".");
      return transformedString;
       
   }
   
   public String swapInSigla(String rawString){
        
       return rawString.replaceAll("(σ|ς)", "(σ|ς)" + sigla);
       
   }
   
   public String stripOutSigla(String rawString){
       
       return rawString.replaceAll(sigla, "");
       
   }
   
    Pattern[] buildPatterns(String q){
      ArrayList<Pattern> patterns = new ArrayList<Pattern>();
      String[] qbits = q.split("\\)");
      for(int i = 0; i < qbits.length; i++){
                    
          String qbit = qbits[i];
          String term = qbit.substring(qbit.indexOf(":") + 2);
          try{
              
              term = URLDecoder.decode(term, "UTF-8");
              
          }catch(UnsupportedEncodingException uee){}

          if(qbit.contains("PHRASE:")){
              
              patterns.addAll(Arrays.asList(getPhraseHighlightPatterns(term)));
              
          }
             
         
          else if(qbit.contains("SUBSTRING")) {
              
               patterns.addAll(Arrays.asList(getSubstringHighlightPatterns(term)));
              
          }
          else{
              
              patterns.add(Pattern.compile(term));
              
          }
          
      }
      
      Pattern[] patt = new Pattern[patterns.size()];
      return patterns.toArray(patt);
      
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
    return substringAfter(in, find, true);
  }

  public static String substringAfter(String in, String find, boolean returnInput) {
    if (in.contains(find)) {
      return in.substring(in.indexOf(find) + find.length());
    } else {
      if (returnInput) return in; else return "";
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

  public static String substringBefore(String in, String find, boolean returnInput) {
    if (in.contains(find)) {
      return in.substring(0, in.indexOf(find));
    } else {
      if (returnInput) return in; else return "";
    }
  }
  
  public static String interpose(Collection<String> coll, String sep) {
    StringBuilder result = new StringBuilder();
    for (Iterator<String> i = coll.iterator(); i.hasNext();) {
      result.append(i.next());
      if (i.hasNext()) {
        result.append(sep);
      }
    }
    return result.toString();
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
      result.append("https://papyri.info/ddbdp/").append(staticpath[0])
            .append(";").append(substringAfter(staticpath[1], staticpath[0] + "."))
            .append(";")
            .append(substringBefore(substringAfter(staticpath[2], staticpath[1] + ".").replace("_", "/").replace(",", "-"), ".html"));
    } else {
      result.append("https://papyri.info/ddbdp/").append(staticpath[0])
            .append(";;").append(substringBefore(substringAfter(staticpath[1], staticpath[0] + ".").replace("_", "/").replace(",", "-"), ".html"));
    }
    return result.toString();
  }

  private ArrayList<Hit> prune(ArrayList<Hit> hits) {
    return prune(hits, 0);
  }

  private ArrayList<Hit> prune(ArrayList<Hit> hits, int start) {
    if (start >= hits.size() - 1) return hits;
    if (hits.get(start).location + hits.get(start).token.length() > hits.get(start + 1).location) {
      hits.remove(start + 1);
      return prune(hits, start);
    } else {
      return prune(hits, start + 1);
    }
  }

  private class Hit {
    Hit (int location, String token) {
      this.location = location;
      this.token = token;
    }

    int location;
    String token;
  }

  private class HitComparator implements Comparator<Hit> {

    public int compare(Hit lhs, Hit rhs) {
      return lhs.location - rhs.location;
    }
  }

  private String xmlPath;
  private String htmlPath;
  private static String sigla = "([-’ʼ\\\\[\\\\]()\u0323〚〛\\\\\\\\/\"|?*ⓐⒶⒷ.]|&gt;|&lt;|ca\\.|ⓝ[0-9a-z]+\\\\.ⓜ|Ⓝ[0-9a-z]+\\\\.ⓜ|Ⓜ[0-9a-z]+\\\\.ⓞ)*";
  private static String exclude = "(<span\\s[^>]+>[^<]+</span>|<a\\s[^>]+>[^<]+</a>|<[^>]+>|&\\w+;)";
  private static String lineNum = "((\\s)*(\\r|\\n)+([0-9]+\\.\\S*)\\s*)";
  private static String hyphenatedLineNumInSupplied = "((?<![-])-\\](\\s)*(\\r|\\n)+([0-9]+\\.\\S*)\\s*)";
  private static String hyphenatedLineNum = "(-(\\s)*(\\r|\\n)+([0-9]+\\.\\S*)\\s*)";
  private static String hlStart = "<span class=\"highlight\">";
  private static String hlStartMark = "Ⓐ";
  private static String hlEnd = "</span>";
  private static String hlEndMark = "Ⓑ";
  private static List<String> bustedRegexes = Arrays.asList(".","\\s",".*",".+",".?");
}
