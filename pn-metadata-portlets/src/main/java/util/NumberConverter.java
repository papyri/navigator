package util;

import java.text.DecimalFormat;
import java.util.Arrays;




public class NumberConverter {
    private static final char NEGATIVE_PREFIX = '-';
    // NB: NEGATIVE_PREFIX must be < POSITIVE_PREFIX
    private static final char POSITIVE_PREFIX = '0';
    public static final String MAX_STRING = "9999"; 
    public static final String MIN_STRING = "-10000";
    public static final int MAX_INT = Integer.parseInt(MAX_STRING);
    public static final int MIN_INT = Integer.parseInt(MIN_STRING);
    private static final String FORMAT = "00000";

    /**
     * Converts a long to a String suitable for indexing.
     */
    public static String encode(int i)  throws OutOfRangeException {
        if ((i < MIN_INT) || (i > MAX_INT)) {
            throw new OutOfRangeException("" + i + " out of allowed range [" + MIN_STRING + "," + MAX_STRING + "]");
        }
        char prefix;
        if (i < 0) {
            prefix = NEGATIVE_PREFIX;
            i = MAX_INT + i + 1;
        } else {
            prefix = POSITIVE_PREFIX;
        }
        DecimalFormat fmt = new DecimalFormat(FORMAT);
        return prefix + fmt.format(i);
    }

   /**
     * Converts a String that was returned by {@link #encode} back to
     * a long.
     */
    public static int decode(String str) {
        char prefix = str.charAt(0);
        int i = Integer.parseInt(str.substring(1));
        if (prefix == POSITIVE_PREFIX) {
            // nop
        } else if (prefix == NEGATIVE_PREFIX) {
            i = i - MAX_INT - 1;
        } else {
            throw new NumberFormatException("string does not begin with the correct prefix [" + POSITIVE_PREFIX + " | " + NEGATIVE_PREFIX + "]");
        }
        return i;
    }
    
public static String formatApis(int i){
    char prefix;
    if (i < 0) {
        prefix = NEGATIVE_PREFIX;
        i = -i;
    } else {
        prefix = POSITIVE_PREFIX;
    }
    DecimalFormat fmt = new DecimalFormat(FORMAT);
    return prefix + fmt.format(i);
}

public static String getRoman(String string){
    string = string.trim();
    try{
        int i = Integer.parseInt(string);
        int m = i/1000;
        i = i%1000;
        int c = i/100;
        i = i%100;
        int x = i/10;
        i = i%10;
        StringBuffer sb = new StringBuffer(20);
        char [] cb = new char[m];
        Arrays.fill(cb, 'M');
        sb.append(cb);
        if (c != 0){
            sb.append(NumberConverter.munge(c,'M','D','C'));
        }
        if (x != 0){
            sb.append(NumberConverter.munge(x,'C','L','X'));
        }
        if (i != 0){
            sb.append(NumberConverter.munge(i,'X','V','I'));
        }
        return sb.toString();
    }
    catch (NumberFormatException e){
        return string;
    }
}

public static char [] munge(int num, char high, char mid, char low){
    switch (num){
    case 1:
        return new char[]{low};
    case 2:
        return new char[]{low,low};
    case 3:
        return new char[]{low,low,low};
    case 4:
        return new char[]{low,mid};
    case 5:
        return new char[]{mid};
    case 6:
        return new char[]{mid,low};
    case 7:
        return new char[]{mid,low,low};
    case 8:
        return new char[]{mid,low,low,low};
    default:
        return new char[]{low,high};
    }
}

public static int getInt(String roman){
    roman = roman.trim();
    int val = 0;
    char rank = 'I';
    String ranks = "IVXLCDM";
    for (int i = roman.length() - 1; i >= 0; i--){
        boolean add = true;
        if (ranks.indexOf(roman.charAt(i)) >= ranks.indexOf(rank)){
            rank = roman.charAt(i);
            add = true;
        }
        else {
            add = false;
        }
        switch (roman.charAt(i)){
      case 'I':
          if (add) val += 1;
          else val -= 1;
          break;
      case 'V':
          if (add) val += 5;
          else val -= 5;
          break;
      case 'X':
          if (add) val += 10;
          else val -= 10;
          break;
      case 'L':
          if (add) val += 50;
          else val -= 50;
          break;
      case 'C':
          if (add) val += 100;
          else val -= 100;
          break;
      case 'D':
          if (add) val += 500;
          else val -= 500;
          break;
      case 'M':
          if (add) val += 1000;
          else val -= 1000;
          break;
      default:
      }
        
    }
    return val;
}

public static String encodeDate(int year, int month, int day) throws OutOfRangeException {
    StringBuffer encodedDate1 = new StringBuffer(32);
    encodedDate1.append(encode(year));
    if (month > 0) {
        encodedDate1.append('.').append(NumberConverter.encodedMonth[month - 1]);
    }
    if (day > 0){
        encodedDate1.append('.').append(NumberConverter.encodedDay[day - 1]);
    }
    return encodedDate1.toString();
}

public static String w3cdtf(int year, int month, int day){
    StringBuffer result = new StringBuffer();
    result.append(Integer.toString(year));
    if (month > 0){
        result.append('-');
        result.append(Integer.toString(month));
    }
    if (day > 0){
        result.append('-');
        result.append(Integer.toString(day));
    }
    return result.toString();
}

public static int [] decodeDate(String encoded) {
    int [] result = new int[] {-1,-1,-1};
    String [] parts = encoded.split("\\.");
    switch (parts.length){
    case 3:
        findDay:
        for (int i=0;i<NumberConverter.encodedDay.length;i++){
            if (NumberConverter.encodedDay[i].equals(parts[2])){
                result[2] = i + 1;
                break findDay;
            }
        }
    case 2:
        findMonth:
        for (int i = 0; i < NumberConverter.encodedMonth.length; i++){
            if (NumberConverter.encodedMonth[i].equals(parts[1])){
                result[1] = i + 1;
                break findMonth;
            }
        }
    case 1:
        int year = decode(parts[0]);
        result[0] = year;
    }
    return result;
}

public static final String[] encodedMonth = { "a", "b", "c", "d", "e", "f", "g",
"h", "i", "j", "k", "l", "m" };
public static final String[] encodedDay = { "a", "b", "c", "d", "e", "f", "g",
"h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t",
"u", "v", "w", "x", "y", "z", "za", "zb", "zc", "zd", "ze" };

}