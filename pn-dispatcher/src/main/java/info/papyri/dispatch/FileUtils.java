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
            return new File(htmlPath + "/DDB_EpiDoc_XML/" + parts[0] + "/" + parts[0] +
                    "." + parts[1] + "/index.html");
          } else if ("".equals(parts[1])) {
            return new File(htmlPath + "/DDB_EpiDoc_XML/" + parts[0] + "/" + parts[0] +
                    "." + parts[2] + ".html");
          } else {
            return new File(htmlPath + "/DDB_EpiDoc_XML/" + parts[0] + "/" + parts[0] +
                    "." + parts[1] + "/" + parts[0] + "." + parts[1] + "." + parts[2] + ".html");
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
          return new File(htmlPath + "/HGV_meta_EpiDoc/HGV" +
                  (int)Math.ceil(Double.parseDouble(item.replaceAll("[a-z]", "")) / 1000) + "/" + item + ".html");
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
            return new File(htmlPath + "/DDB_EpiDoc_XML/" + parts[0] + "/" + parts[0] +
                    "." + parts[2] + ".txt");
          } else {
            return new File(htmlPath + "/DDB_EpiDoc_XML/" + parts[0] + "/" + parts[0] +
                    "." + parts[1] + "/" + parts[0] + "." + parts[1] + "." + parts[2] + ".txt");
          }
        }
      } else if ("hgv".equals(collection)) {
        if (item.matches("\\d+[a-z]*")) {
          return new File(htmlPath + "/HGV_meta_EpiDoc/HGV" +
                  (int)Math.ceil(Double.parseDouble(item.replaceAll("[a-z]", "")) / 1000) + "/" + item + ".txt");
        }
      } else if ("apis".equals(collection)) {
        if (item.contains(".")) {
          String[] parts = item.split(".");
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
            return new File(xmlPath + "/DDB_EpiDoc_XML/" + parts[0] + "/" + parts[0] +
                    "." + parts[2] + ".xml");
          } else {
            return new File(xmlPath + "/DDB_EpiDoc_XML/" + parts[0] + "/" + parts[0] +
                    "." + parts[1] + "/" + parts[0] + "." + parts[1] + "." + parts[2] + ".xml");
          }
        }
      } else if ("hgv".equals(collection)) {
        if (item.matches("\\d+[a-z]*")) {
          return new File(xmlPath + "/HGV_meta_EpiDoc/HGV" +
                  (int)Math.ceil(Double.parseDouble(item.replaceAll("[a-z]", "")) / 1000) + "/" + item + ".xml");
        }
      } else if ("apis".equals(collection)) {
        if (item.contains(".")) {
          String[] parts = item.split("\\.");
          return new File(xmlPath + "/APIS/" + parts[0] + "/xml/" + parts[0] + ".apis." + parts[2] + ".xml");
        }
      }
      return null;
  }

  public List<String> findMatches(String query, String id) {
    //System.out.println(query + " " + id);
    List<String> result = new ArrayList<String>();
    String q = query;
    if (q.contains(":")) {
      q = q.substring(q.indexOf(":") + 1);
    }
    String[] find;
    if (q.startsWith("\"") && q.endsWith("\"")) {
      find = new String[] {q.replaceAll("\\s", " ").replaceAll("[?*()\\\\/\"'~^0-1]", "")};
    } else {
      find = q.replaceAll("[?*\\\\/()\"'~^0-1]", "").replaceAll("(AND|OR|TO)", "").split("\\s+");
    }
    String[] parts = id.substring("http://papyri.info/".length()).split("/");
    Pattern[] patterns = new Pattern[find.length];
    for (int i = 0; i < find.length; i++) {
      patterns[i] = Pattern.compile(find[i].toLowerCase()
              .replaceAll("(\\S)", sigla + "$1")
              .replace("α", "(α|ἀ|ἁ|ἂ|ἃ|ἄ|ἅ|ἆ|ἇ|ὰ|ά|ᾀ|ᾁ|ᾂ|ᾃ|ᾄ|ᾅ|ᾆ|ᾇ|ᾲ|ᾳ|ᾴ|ᾶ|ᾷ)")
              .replace("ε", "(ε|ἐ|ἑ|ἒ|ἓ|ἔ|ἕ|έ|ὲ)")
              .replace("η", "(η|ἠ|ἡ|ἢ|ἣ|ἤ|ἥ|ἦ|ἧ|ή|ὴ|ᾐ|ᾑ|ᾒ|ᾓ|ᾔ|ᾕ|ᾖ|ᾗ|ῂ|ῃ|ῄ|ῆ|ῇ)")
              .replace("ι", "(ι|ί|ὶ|ἰ|ἱ|ἲ|ἳ|ἴ|ἵ|ἶ|ἷ|ῒ|ΐ|ῖ|ῗ)")
              .replace("ο", "(ο|ὸ|ό|ὀ|ὁ|ὂ|ὃ|ὄ|ὅ)")
              .replace("υ", "(υ|ύ|ὐ|ὑ|ὒ|ὓ||ὔ|ὕ|ὖ|ὗ|ῢ|ΰ|ῦ|ῧ)")
              .replace("ω", "(ω|ώ|ὼ|ὠ|ὡ|ὢ|ὣ|ὤ|ὥ|ὦ|ὧ|ᾠ|ᾡ|ᾢ|ᾣ|ᾤ|ᾥ|ᾦ|ᾧ|ῲ|ῳ|ῴ|ῶ|ῷ)")
              .replace("ρ", "(ρ|ῥ)").replaceAll("(σ|ς)", "(σ|ς)") + sigla,
              Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.UNIX_LINES);
    }
    try {
      InputStreamReader reader = new InputStreamReader(new FileInputStream(getTextFile(parts[0], parts[1])), Charset.forName("UTF-8"));
      char[] buffer = new char[1024];
      StringBuilder t = new StringBuilder();
      int size = -1;
      while((size = reader.read(buffer)) > 0) {
        t.append(buffer, 0, size);
      }
      String text = t.toString().replaceAll("-(\\s|\\r|\\n)+[0-9]*\\s*", "");
      for (Pattern pattern : patterns) {
        Matcher m = pattern.matcher(text);
        if (m.find()) {
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
          result.add(text.substring(start, end));
        }
        if (result.size() > 2) return result;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return result;
  }



  private String xmlPath;
  private String htmlPath;
  private static String sigla = "[-\\\\[\\\\]()<>\u0323〚〛]*";
}


