package info.papyri.dispatch.browse;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author thill
 */
public class IdComparator implements Comparator {

    @Override
    public int compare(Object t, Object t1) {
        
        String id1 = (String) t;
        String id2 = (String) t1;
        
        ArrayList<String> ids1 = splitIntoNumericAndAlphabeticComponents(id1);
        ArrayList<String> ids2 = splitIntoNumericAndAlphabeticComponents(id2);
        
        Pattern letterCheck = Pattern.compile("^\\D.*");
        Pattern numberCheck = Pattern.compile("^\\d.*");
  
        
        // loop through all components
        
        // make sure no index errors possible by choosing shorter of two arrays
        
        int limit = ids1.size() < ids2.size() ? ids1.size() : ids2.size();

        for(int i = 0; i < limit; i++){
            
            String id1chunk = ids1.get(i);
            String id2chunk = ids2.get(i);
            
            // if the two are of different types, return numerical types first
            
            if(numberCheck.matcher(id1chunk).matches() && letterCheck.matcher(id2chunk).matches()) return -1;
            if(letterCheck.matcher(id1chunk).matches() && numberCheck.matcher(id2chunk).matches()) return 1;

            // if they're numerical, do numerical sort
            
            if(numberCheck.matcher(id1chunk).matches()){

                if(Long.valueOf(id1chunk) < Long.valueOf(id2chunk)) return -1;
                if(Long.valueOf(id1chunk) > Long.valueOf(id2chunk)) return 1;
                
            }
            else{

                String value1 = id1chunk.replaceAll("\\p{Punct}", "");
                String value2 = id2chunk.replaceAll("\\p{Punct}", "");
                int compareValue = value1.compareToIgnoreCase(value2);
                if(compareValue != 0) return compareValue;
                
            }
            
            
        }
        
        // prioritise shorter over longer
        
        if(id1.length() < id2.length()) return -1;
        if(id1.length() > id2.length()) return 1;
        
        return 0;
        
    }
    
    ArrayList<String> splitIntoNumericAndAlphabeticComponents(String mixedString){
        
        ArrayList<String> components = new ArrayList<String>();
        Pattern findFirstAlphabetical = Pattern.compile("([\\D]+)");
        int findIndex = 0;
        
        Matcher matcher = findFirstAlphabetical.matcher(mixedString);
        
        while(matcher.find()){
            
            String numbers = mixedString.substring(findIndex, matcher.start());
            if(!"".equals(numbers)) components.add(numbers); 
            String letters = matcher.group(1);
            components.add(letters);
            findIndex = matcher.end();
            
        }
         
        if(findIndex < mixedString.length()) components.add(mixedString.substring(findIndex));
        
        return components;
        
    }
    
}
