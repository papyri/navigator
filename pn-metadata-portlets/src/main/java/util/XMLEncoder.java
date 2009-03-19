package util;

public class XMLEncoder {
    public static String encode(String parm){
        if (parm == null) return parm;
        return parm.replaceAll("\"","&quot;").replaceAll("<","&lt;").replaceAll(">","&gt;").replaceAll("'","&apos;");
    }
    
    public static String insertLinks(String baseVal){
        return insertLinks(baseVal,"external link");
    }
    
    public static String insertLinks(String baseVal, String linkLabel){
        int httpIndex = -1;
        while ((httpIndex = baseVal.indexOf("http", httpIndex + 1)) != -1){
            String first = baseVal.substring(0,httpIndex);
            String second = baseVal.substring(httpIndex);
            String third = null;
            if (second.indexOf(' ') != -1){
                third = second.substring(second.indexOf(' '));
                second = second.substring(0,second.indexOf(' '));
            }
            baseVal = first + "<a href=\"" + second + "\">" + linkLabel + "</a>" + ((third == null)?"":third);
            httpIndex += "<a href=\"".length();
        }
        String result = baseVal;
        return result;
    }

}
