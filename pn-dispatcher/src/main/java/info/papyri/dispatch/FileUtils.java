/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.papyri.dispatch;

import java.io.*;
import java.nio.charset.Charset;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author hcayless
 */
public class FileUtils {

  public FileUtils(String xmlPath, String htmlPath) {
    this.xmlPath = xmlPath;
    this.htmlPath = htmlPath;
  }

  public File getHtmlFile(String collection, String item) {
    if ("ddbdp".equals(collection)) {
      if (item.contains(";")) {
        String[] parts = item.split(";");
        if (parts.length == 2) {
          return new File(htmlPath + "/DDB_EpiDoc_XML/" + parts[0] + "/" + parts[0]
                  + "." + parts[1] + "/index.html");
        } else if ("".equals(parts[1])) {
          return new File(htmlPath + "/DDB_EpiDoc_XML/" + parts[0] + "/" + parts[0]
                  + "." + parts[2] + ".html");
        } else {
          return new File(htmlPath + "/DDB_EpiDoc_XML/" + parts[0] + "/" + parts[0]
                  + "." + parts[1] + "/" + parts[0] + "." + parts[1] + "." + parts[2] + ".html");
        }
      } else {
        if ("".equals(item)) {
          return new File(htmlPath + "/DDB_EpiDoc_XML/index.html");
        } else {
          return new File(htmlPath + "/DDB_EpiDoc_XML/" + item + "/index.html");
        }
      }
    } else if ("hgv".equals(collection)) {
      if (item.matches("\\d+[a-z]*")) {
        return new File(htmlPath + "/HGV_meta_EpiDoc/HGV"
                + (int) Math.ceil(Double.parseDouble(item.replaceAll("[a-z]", "")) / 1000) + "/" + item + ".html");
      } else {
        if ("".equals(item)) {
          return new File(htmlPath + "/HGV_meta_EpiDoc/index.html");
        } else {
          return new File(htmlPath + "/HGV_meta_EpiDoc/" + item + "/index.html");
        }
      }
    } else if ("apis".equals(collection)) {
      if ("".equals(item)) {
        return new File(htmlPath + "/APIS/index.html");
      } else if (item.contains(".")) {
        String[] parts = item.split("\\.");
        return new File(htmlPath + "/APIS/" + parts[0] + "/" + parts[0] + ".apis." + parts[2] + ".html");
      } else {
        return new File(htmlPath + "/APIS/" + item + "/index.html");
      }
    }
    return null;
  }

  public File getTextFile(String collection, String item) {
    if ("ddbdp".equals(collection)) {
      if (item.contains(";")) {
        String[] parts = item.split(";");
        if ("".equals(parts[1])) {
          return new File(htmlPath + "/DDB_EpiDoc_XML/" + parts[0] + "/" + parts[0]
                  + "." + parts[2] + ".txt");
        } else {
          return new File(htmlPath + "/DDB_EpiDoc_XML/" + parts[0] + "/" + parts[0]
                  + "." + parts[1] + "/" + parts[0] + "." + parts[1] + "." + parts[2] + ".txt");
        }
      }
    } else if ("hgv".equals(collection)) {
      if (item.matches("\\d+[a-z]*")) {
        return new File(htmlPath + "/HGV_meta_EpiDoc/HGV"
                + (int) Math.ceil(Double.parseDouble(item.replaceAll("[a-z]", "")) / 1000) + "/" + item + ".txt");
      }
    } else if ("apis".equals(collection)) {
      if (item.contains(".")) {
        String[] parts = item.split("\\.");
        return new File(htmlPath + "/APIS/" + parts[0] + "/" + parts[0] + ".apis." + parts[2] + ".txt");
      }
    }
    return null;
  }

  public File getXmlFile(String collection, String item) {
    if ("ddbdp".equals(collection)) {
      if (item.contains(";")) {
        String[] parts = item.split(";");
        if ("".equals(parts[1])) {
          return new File(xmlPath + "/DDB_EpiDoc_XML/" + parts[0] + "/" + parts[0]
                  + "." + parts[2] + ".xml");
        } else {
          return new File(xmlPath + "/DDB_EpiDoc_XML/" + parts[0] + "/" + parts[0]
                  + "." + parts[1] + "/" + parts[0] + "." + parts[1] + "." + parts[2] + ".xml");
        }
      }
    } else if ("hgv".equals(collection)) {
      if (item.matches("\\d+[a-z]*")) {
        return new File(xmlPath + "/HGV_meta_EpiDoc/HGV"
                + (int) Math.ceil(Double.parseDouble(item.replaceAll("[a-z]", "")) / 1000) + "/" + item + ".xml");
      }
    } else if ("apis".equals(collection)) {
      if (item.contains(".")) {
        String[] parts = item.split("\\.");
        return new File(xmlPath + "/APIS/" + parts[0] + "/xml/" + parts[0] + ".apis." + parts[2] + ".xml");
      }
    }
    return null;
  }

  public String loadTextFromId(String id) {
    String[] parts = id.substring("http://papyri.info/".length()).split("/");
    return loadFile(getTextFile(parts[0], parts[1]));
  }

  public String loadHtmlFromId(String id) {
    String[] parts = id.substring("http://papyri.info/".length()).split("/");
    return loadFile(getHtmlFile(parts[0], parts[1]));
  }

  public String loadFile(File f) {
    StringBuilder t = new StringBuilder();
    try {
      InputStreamReader reader = new InputStreamReader(new FileInputStream(f), Charset.forName("UTF-8"));
      char[] buffer = new char[1024];
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

  public String highlight(String query, String t) {
    Pattern[] patterns = getPatterns(query);
    List<String> exclusions = getExclusions(t);
    String text = t.toString().replaceAll(exclude, "@@@");
    StringBuilder hl = new StringBuilder();
    int index = 0;
    for (Pattern pattern : patterns) {
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
      }
    }
    String result;
    if (hl.length() > 0) {
      result = hl.toString();
    } else { // if we didn't find anything, we're done
      return t;
    }
    for (String ex : exclusions) {
      result = result.replaceFirst("@@@", ex);
    }
    return result;
  }
  
  public List<String> highlightMatches(String query, String t) {
    //System.out.println(query + " " + id);
    List<String> result = new ArrayList<String>();
    Pattern[] patterns = getPatterns(query);
    String text = t.toString().replaceAll(exclude, "");
    for (Pattern pattern : patterns) {
      Matcher m = pattern.matcher(text);
      while (m.find()) {
        int start = m.toMatchResult().start();
        if (start > 20) {
          start -= 20;
          start = text.indexOf(' ', start) + 1;
        } else {
          start = 0;
        }
        int end = m.toMatchResult().end();
        if (end > text.length() - 20) {
          end = text.length();
        } else {
          end += 20;
          if (text.indexOf(' ', end) > 0) {
            end = text.indexOf(' ', end) + 1;
          }
        }
        StringBuilder hit = new StringBuilder();
        if (m.toMatchResult().start() > 0) {
          hit.append(text.substring(start, m.toMatchResult().start()));
        }
        hit.append(hlStart);
        hit.append(text.substring(m.toMatchResult().start(), m.toMatchResult().end()));
        hit.append(hlEnd);
        if (m.toMatchResult().end() < text.length()) {
          hit.append(text.substring(m.toMatchResult().end(), end));
        }
        result.add(hit.toString());
        if (result.size() > 2) {
          return result;
        }
      }
    }
    return result;
  }

    private Pattern[] getPatterns(String query) {
      String q = query.replace("*", "£").replace("?", "#");
      if (q.contains(":")) {
        q = q.substring(q.indexOf(":") + 1);
      }
      String[] find;
      if (q.startsWith("\"") && q.endsWith("\"")) {
        q = q.replaceAll("\\s", " ").replaceAll("[?*()\\\\/\"'~^0-1]", "");
        if (q.length() == 0) {
          return new Pattern[0];
        }
        find = new String[]{q};
      } else {
        q = q.replaceAll("[\\\\/()\"'~^0-1]", "").replaceAll("(AND|OR|TO)", "");
        if (q.length() == 0) {
          return new Pattern[0];
        }
        find = q.split("\\s+");
      }
      Pattern[] patterns = new Pattern[find.length];
      for (int i = 0; i < find.length; i++) {
        patterns[i] = Pattern.compile(find[i].toLowerCase()
                .replaceAll("(\\S)", sigla + "$1")
                .replace("£", "\\S*").replace("#", "\\S")
                .replace("α", "(α|ἀ|ἁ|ἂ|ἃ|ἄ|ἅ|ἆ|ἇ|ὰ|ά|ᾀ|ᾁ|ᾂ|ᾃ|ᾄ|ᾅ|ᾆ|ᾇ|ᾲ|ᾳ|ᾴ|ᾶ|ᾷ)")
                .replace("ε", "(ε|ἐ|ἑ|ἒ|ἓ|ἔ|ἕ|έ|ὲ)")
                .replace("η", "(η|ἠ|ἡ|ἢ|ἣ|ἤ|ἥ|ἦ|ἧ|ή|ὴ|ᾐ|ᾑ|ᾒ|ᾓ|ᾔ|ᾕ|ᾖ|ᾗ|ῂ|ῃ|ῄ|ῆ|ῇ)")
                .replace("ι", "(ι|ί|ὶ|ἰ|ἱ|ἲ|ἳ|ἴ|ἵ|ἶ|ἷ|ῒ|ΐ|ῖ|ῗ)")
                .replace("ο", "(ο|ὸ|ό|ὀ|ὁ|ὂ|ὃ|ὄ|ὅ)")
                .replace("υ", "(υ|ύ|ὐ|ὑ|ὒ|ὓ||ὔ|ὕ|ὖ|ὗ|ῢ|ΰ|ῦ|ῧ)")
                .replace("ω", "(ω|ώ|ὼ|ὠ|ὡ|ὢ|ὣ|ὤ|ὥ|ὦ|ὧ|ᾠ|ᾡ|ᾢ|ᾣ|ᾤ|ᾥ|ᾦ|ᾧ|ῲ|ῳ|ῴ|ῶ|ῷ)")
                .replace("ρ", "(ρ|ῥ)").replaceAll("(σ|ς)", "(σ|ς)" + sigla),
                Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.UNIX_LINES);
      }
    return patterns;
  }

  public List<int[]> getDivIndexes(String text) {
    List<int[]> divIndexes = new ArrayList<int[]>();
    int index = 0;
    while ((index = text.indexOf("<div", index) + 1) > 0) {
      if (text.substring(index, text.indexOf(">", index)).contains(" data")) { //divs with @class="<something> data" are where the target text occurs
        divIndexes.add(new int[] {text.indexOf(">", index) + 1, getClosingDivLocation(text.toString(), text.indexOf(">", index) + 1)});
      }
    }
    return divIndexes;
  }

  public int getClosingDivLocation(String t, int start) {
    int end = t.indexOf("</div>", start);
    while (t.indexOf("<div", start) < end && t.indexOf("<div", start) > 0) {
      start = t.indexOf("<div", start) + 1;
      end = t.indexOf("</div>", end + 1);
    }
    return end - 1;
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



  private String xmlPath;
  private String htmlPath;
  private static String sigla = "[-’\\\\[\\\\]()<>\u0323〚〛\\\\\\\\/\"|?*@]*";
  private static String exclude = "(-(\\s|\\r|\\n)+[0-9]*\\s*|<[^>]+>|&\\w+;)";
  private static String hlStart = "<span class=\"highlight\">";
  private static String hlEnd = "</span>";
}
