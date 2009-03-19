package util.jsp.el;
import java.net.URLEncoder;
import java.net.URLDecoder;

public abstract class Functions {
    public static String DEFAULT_CHARSET = "UTF-8";
    public static String encode(String in){
        try{
            return URLEncoder.encode(in,DEFAULT_CHARSET);
        }
        catch(Throwable t){
            return in;
        }
    }
    public static String decode(String in){
        try{
            return URLDecoder.decode(in,DEFAULT_CHARSET);
        }
        catch(Throwable t){
            return in;
        }
    }
    public static String selectedOption(String option, String value){
        return selectedOption(option,option,value);
    }
    public static String selectedOption(String option, String label, String value){
        try{
            value = decode(value);
            String selected = (option != null && option.equals(value))?"selected=\"selected\"":"";
            return "<option value=\"" + encode(option) + "\" " + selected + ">" + label + "</option>";
        }
        catch (Throwable t){
            t.printStackTrace();
            return "<option value=\"" + option + "\">" + label + "</option>";
        }
    }
    public static String selectedOption(String option, String [] values){
        return selectedOption(option,option,values);
    }
    public static String selectedOption(String option, String label, String [] values){
        boolean matched = false;
        try{
        for (String value:values){
            if (decode(value).equals(option)){
                matched  = true;
                break;
            }
        }
        }
        catch (Throwable t){}
        String selected = (matched)?"selected=\"selected\"":"";
        try{
            return "<option value=\"" + encode(option) + "\" " + selected + ">" + label + "</option>";
        }
        catch (Throwable t){
            t.printStackTrace();
            return "<option value=\"" + option + "\" " + selected + ">" + label + "</option>";
        }
    }
}
