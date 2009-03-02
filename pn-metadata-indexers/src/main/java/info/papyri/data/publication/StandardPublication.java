package info.papyri.data.publication;
import info.papyri.util.NumberConverter;
public class StandardPublication {
    private static final String TEMPLATE = "%s %v %d %u"; 
    private static final String RTEMPLATE = "%s %v %d %u.%r"; 
    private String series = "";
    private String volume = "";
    private String document = "";
    private String subDocument = "";
    private String lineRange;
    private String columnRange;
    private boolean range = false;
    public void setSeries(String series){
        this.series = series;
    }
    public void setVolume(String vol){
        this.volume = vol;
    }
    public void setDocument(String doc){
        this.document = doc;
    }
    public void setSubDocument(String sub){
        this.subDocument = sub;
    }
    public void setLineRange(String range){
        this.lineRange = range;
        if (range != null && !this.range) this.range = true;
    }
    public void setColumnRange(String range){
        this.columnRange = range;
        if (range != null && !this.range) this.range = true;
    }
    public String toString(){
        String result = (range)?RTEMPLATE:TEMPLATE;
        result = result.replace("%s", this.series);
        result = result.replaceAll("%v", this.volume);
        result = result.replaceAll("%d",this.document);
        result = result.replaceAll("%u", this.subDocument);
        if (this.lineRange != null )result = result.replace("%r", this.lineRange);
        if (this.columnRange != null )result = result.replace("%r", this.columnRange);
        return result.trim();
    }
    public static String normalizePub(String series, String volume, String document){
        series = normalizeSeries(series);
        volume = normalizeVolume(volume);
        document = normalizeDocument(document);

        
        
      if ("*".equals(volume) && "*".equals(document)){
          return series + " " + volume;
      }else{
          return series + " " + volume + " " + document;    
      }
    }
    
    public static String normalizeSeries(String series){
        series = series.trim();
        if (series.indexOf('*') != -1){
            return series;
        }
        if ("".equals(series)){
            return "*";
        }
        else {
            String normal = series.replaceAll(" ","");
            char [] lc = new char[0];
            normal = normal.replaceAll("\\.","\\. ").trim();
            normal = normal.replaceFirst("\\. ", ".");
            String [] seriesC = normal.split("\\.(\\s)?");
            switch (seriesC.length){
            case 3:
                lc = seriesC[2].toLowerCase().toCharArray();
                lc[0] = Character.toUpperCase(lc[0]);
                normal = normal.replaceAll(seriesC[2],new String(lc));
            case 2:
                lc = seriesC[1].toLowerCase().toCharArray();
                lc[0] = Character.toUpperCase(lc[0]);
                normal = normal.replaceAll(seriesC[1],new String(lc));
            case 1:
                lc = seriesC[0].toLowerCase().toCharArray();
                lc[0] = Character.toUpperCase(lc[0]);
                normal = normal.replaceAll(seriesC[0],new String(lc));
            default:
                break;
            }
            series = normal;
        }
        return series;
    }

    public static String normalizeVolume(String volume){
        if (volume == null || "".equals(volume.trim())) return "*";
        return NumberConverter.getRoman(volume);
    }
    
    public static String normalizeDocument(String document){
        if (document == null || "".equals(document.trim())) return "*";
        return document.trim();
    }
}
