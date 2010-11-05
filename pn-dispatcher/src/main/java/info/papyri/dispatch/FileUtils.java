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
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.antlr.runtime.*;

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
                  + "." + parts[2].replaceAll(",", "-").replaceAll("/", "_").replace(" ", "%2B")  + ".html");
        } else {
          return new File(htmlPath + "/DDB_EpiDoc_XML/" + parts[0] + "/" + parts[0]
                  + "." + parts[1] + "/" + parts[0] + "." + parts[1] + "." 
                  + parts[2].replaceAll(",", "-").replaceAll("/", "_").replace(" ", "%2B")  + ".html");
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
                  + "." + parts[2].replaceAll(",", "-").replaceAll("/", "_").replace(" ", "%2B").replace("+", "%2B")  + ".txt");
        } else {
          return new File(htmlPath + "/DDB_EpiDoc_XML/" + parts[0] + "/" + parts[0]
                  + "." + parts[1] + "/" + parts[0] + "." + parts[1] + "." 
                  + parts[2].replaceAll(",", "-").replaceAll("/", "_").replace(" ", "%2B").replace("+", "%2B")  + ".txt");
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
                  + "." + parts[2].replaceAll(",", "-").replaceAll("/", "_")
                  .replace(" ", "+") + ".xml");
        } else {
          return new File(xmlPath + "/DDB_EpiDoc_XML/" + parts[0] + "/" + parts[0]
                  + "." + parts[1] + "/" + parts[0] + "." + parts[1] + "." 
                  + parts[2].replaceAll(",", "-").replaceAll("/", "_").replace(" ", "+") + ".xml");
        }
      }
    } else if ("hgv".equals(collection)) {
      if (item.matches("\\d+[a-z]*")) {
        return new File(xmlPath + "/HGV_meta_EpiDoc/HGV"
                + (int) Math.ceil(Double.parseDouble(item.replaceAll("[a-z]", "")) / 1000) + "/" + item + ".xml");
      }
    } else if ("hgvtrans".equals(collection)) {
      return new File(xmlPath + "/HGV_trans_EpiDoc/" + item + ".xml");
    } else if ("apis".equals(collection)) {
      if (item.contains(".")) {
        String[] parts = item.split("\\.");
        return new File(xmlPath + "/APIS/" + parts[0] + "/xml/" + parts[0] + ".apis." + parts[2] + ".xml");
      }
    }
    return null;
  }

  public String loadTextFromId(String id) {
    return loadFile(getTextFile(substringBefore(id.substring("http://papyri.info/".length()), "/"), substringAfter(id.substring("http://papyri.info/".length()), "/")));
  }

  public String loadHtmlFromId(String id) {
    return loadFile(getHtmlFile(substringBefore(id.substring("http://papyri.info/".length()), "/"), substringAfter(id.substring("http://papyri.info/".length()), "/")));
  }

  public String loadFile(File f) {
    StringBuilder t = new StringBuilder();
    try {
      InputStreamReader reader = new InputStreamReader(new FileInputStream(f), Charset.forName("UTF-8"));
      char[] buffer = new char[8192];
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
    String text = t.toString().replaceAll(exclude, "ЖЖЖ");
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
    Pattern p = Pattern.compile("ЖЖЖ");
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
      if (patternMap.containsKey(query)) {
        return patternMap.get(query);
      } else {
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
                  .replace("^ ", sigla + "\\s+")
                  .replaceAll("\\s", "\\\\s+").replace("^", sigla + "\\b")
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
            patterns[i] = Pattern.compile(find.get(i).toLowerCase()
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
        patternMap.put(query, patterns);
        return patterns;
      }
  }

    public static String stripDiacriticals(String in) {
      return Normalizer.normalize(in, Normalizer.Form.NFD).replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
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

  public static String substringAfter(String in, String find) {
    if (in.contains(find)) {
      return in.substring(in.indexOf(find) + find.length());
    } else {
      return in;
    }
  }
  
  public static String substringBefore (String in, String find) {
    if (in.contains(find)) {
      return in.substring(0, in.indexOf(find));
    } else {
      return in;
    }
  }

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
  private static String sigla = "([-’ʼ\\\\[\\\\]()\u0323〚〛\\\\\\\\/\"|?*Ж]|&gt;|&lt;)*";
  private static String exclude = "(-(\\s|\\r|\\n)+[0-9]*\\s*|-<[^>]+>(\\s|\\r|\\n)*<span class=\"linenumber\">\\d+</span>\\s*|<[^>]+>|&\\w+;)";
  private static String excludeTxt = "(-(\\s|\\r|\\n)+[0-9]*\\s*)";
  private static String hlStart = "<span class=\"highlight\">";
  private static String hlEnd = "</span>";
  private Map<String,Pattern[]> patternMap = new HashMap<String,Pattern[]>();
}
