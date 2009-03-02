package info.papyri.util;

public abstract class VolumeUtil {
    
    public static final String ONE = "I";
    public static final String TWO = "II";
    public static final String THREE = "III";
    public static final String FOUR = "IV";
    public static final String FIVE = "V";
    public static final String SIX = "VI";
    public static final String SEVEN = "VII";
    public static final String EIGHT = "VIII";
    public static final String NINE = "IX";
    public static final String TEN = "X";
    
    public static final String ELEVEN = "XI";
    public static final String TWELVE = "XII";
    public static final String THIRTEEN = "XIII";
    public static final String FOURTEEN = "XIV";
    public static final String FIFTEEN = "XV";
    public static final String SIXTEEN = "XVI";
    public static final String SEVENTEEN = "XVII";
    public static final String EIGHTEEN = "XVIII";
    public static final String NINETEEN = "XIX";
    public static final String TWENTY = "XX";
    
    public static final String TWENTY_ONE = "XXI";
    public static final String TWENTY_TWO = "XXII";
    public static final String TWENTY_THREE = "XXIII";
    public static final String TWENTY_FOUR = "XXIV";
    public static final String TWENTY_FIVE = "XXV";
    public static final String TWENTY_SIX = "XXVI";
    public static final String TWENTY_SEVEN = "XXVII";
    public static final String TWENTY_EIGHT = "XXVIII";
    public static final String TWENTY_NINE = "XXIX";
    public static final String THIRTY = "XXX";
    
    public static final String THIRTY_ONE = "XXXI";
    public static final String THIRTY_TWO = "XXXII";
    public static final String THIRTY_THREE = "XXXIII";
    public static final String THIRTY_FOUR = "XXXIV";
    public static final String THIRTY_FIVE = "XXXV";
    public static final String THIRTY_SIX = "XXXVI";
    public static final String THIRTY_SEVEN = "XXXVII";
    public static final String THIRTY_EIGHT = "XXXVIII";
    public static final String THIRTY_NINE = "XXXIX";
    public static final String FORTY = "XL";
    
    public static final String FORTY_ONE = "XLI";
    public static final String FORTY_TWO = "XLII";
    public static final String FORTY_THREE = "XLIII";
    public static final String FORTY_FOUR = "XLIV";
    public static final String FORTY_FIVE = "XLV";
    public static final String FORTY_SIX = "XLVI";
    public static final String FORTY_SEVEN = "XLVII";
    public static final String FORTY_EIGHT = "XLVIII";
    public static final String FORTY_NINE = "XLIX";
    public static final String FIFTY = "L";
    
    public static final String FIFTY_ONE = "LI";
    public static final String FIFTY_TWO = "LII";
    public static final String FIFTY_THREE = "LIII";
    public static final String FIFTY_FOUR = "LIV";
    public static final String FIFTY_FIVE = "LV";
    public static final String FIFTY_SIX = "LVI";
    public static final String FIFTY_SEVEN = "LVII";
    public static final String FIFTY_EIGHT = "LVIII";
    public static final String FIFTY_NINE = "LIX";
    public static final String SIXTY = "LX";
    
    public static final String SIXTY_ONE = "LXI";
    public static final String SIXTY_TWO = "LXII";
    public static final String SIXTY_THREE = "LXIII";
    public static final String SIXTY_FOUR = "LXIV";
    public static final String SIXTY_FIVE = "LXV";
    public static final String SIXTY_SIX = "LXVI";
    public static final String SIXTY_SEVEN = "LXVII";
    public static final String SIXTY_EIGHT = "LXVIII";
    public static final String SIXTY_NINE = "LXIX";
    public static final String SEVENTY= "LXX";
    
    public static final String SEVENTY_ONE = "LXXI";
    public static final String SEVENTY_TWO = "LXXII";
    public static final String SEVENTY_THREE = "LXXIII";
    public static final String SEVENTY_FOUR = "LXXIV";
    public static final String SEVENTY_FIVE = "LXXV";
    public static final String SEVENTY_SIX = "LXXVI";
    public static final String SEVENTY_SEVEN = "LXXVII";
    public static final String SEVENTY_EIGHT = "LXXVIII";
    public static final String SEVENTY_NINE = "LXXIX";
    public static final String EIGHTY = "LXXX";
    public static final String [] ROMAN = new String [] {
        "",ONE, TWO,THREE,FOUR,FIVE,SIX,SEVEN,EIGHT,
        NINE,TEN,ELEVEN,TWELVE,THIRTEEN,FOURTEEN,FIFTEEN,SIXTEEN,SEVENTEEN,
        EIGHTEEN,NINETEEN,TWENTY,TWENTY_ONE,TWENTY_TWO,TWENTY_THREE,TWENTY_FOUR,TWENTY_FIVE,TWENTY_SIX,
        TWENTY_SEVEN,TWENTY_EIGHT,TWENTY_NINE,THIRTY,THIRTY_ONE,THIRTY_TWO,THIRTY_THREE,THIRTY_FOUR,THIRTY_FIVE,
        THIRTY_SIX,THIRTY_SEVEN,THIRTY_EIGHT,THIRTY_NINE,FORTY,FORTY_ONE,FORTY_TWO,FORTY_THREE,FORTY_FOUR,
        FORTY_FIVE,FORTY_SIX,FORTY_SEVEN,FORTY_EIGHT,FORTY_NINE,FIFTY,FIFTY_ONE,FIFTY_TWO,FIFTY_THREE,
        FIFTY_FOUR,FIFTY_FIVE,FIFTY_SIX,FIFTY_SEVEN,FIFTY_EIGHT,FIFTY_NINE,SIXTY,SIXTY_ONE,SIXTY_TWO,
        SIXTY_THREE,SIXTY_FOUR,SIXTY_FIVE,SIXTY_SIX,SIXTY_SEVEN,SIXTY_EIGHT,SIXTY_NINE,SEVENTY,SEVENTY_ONE,
        SEVENTY_TWO,SEVENTY_THREE,SEVENTY_FOUR,SEVENTY_FIVE,SEVENTY_SIX,SEVENTY_SEVEN,SEVENTY_EIGHT,SEVENTY_NINE,EIGHTY
    };

    public static int arabicForPOxyDoc(int doc){
        switch(doc/1000){
        case 0:
            if(doc < 1) return -1;
            if(doc < 208) return 1;
            if(doc < 401) return 2;
            if(doc<654) return 3;
            if(doc<840) return 4;
            if(doc < 845) return 5;
            if(doc < 1007) return 6;
        case 1:
            if(doc < 1007) return 6;
            if(doc<1073) return 7;
            if(doc<1166) return 8;
            if(doc<1224)return 9;
            if(doc<1351)return 10;
            if(doc<1405)return 11;
            if(doc<1594)return 12;
            if(doc<1626)return 13;
            if(doc<1778)return 14;
            if(doc<1829)return 15;
            if(doc<2064)return 16;
        case 2:
            if(doc<2064)return 16;
            if(doc<2065)return -1; // no. 2064 is published in Two Theocritus Papyri
            if(doc<2157)return 17;
            if(doc<2208)return 18;
            if(doc<2245)return 19;
            if(doc<2288)return 20;
            if(doc<2309)return 21;
            if(doc<2354)return 22;
            if(doc<2383)return 23;
            if(doc<2426)return 24;
            if(doc<2438)return 25;
            if(doc<2452)return 26;
            if(doc<2481)return 27;
            if(doc<2506)return 28;
            if(doc<2507)return 29;
            if(doc<2531)return 30;
            if(doc<2617)return 31;
            if(doc<2654)return 32;
            if(doc<2683)return 33;
            if(doc<2733)return 34;
            if(doc<2745)return 35;
            if(doc<2801)return 36;
            if(doc<2824)return 37;
            if(doc<2878)return 38;
            if(doc<2892)return 39;
            if(doc<2943)return 40;
            if(doc<2999)return 41;
            if(doc<3088)return 42;
        case 3:
            if(doc<3088)return 42;
            if(doc<3151)return 43;
            if(doc<3209)return 44;
            if(doc<3267)return 45;
            if(doc<3316)return 46;
            if(doc<3368)return 47;
            if(doc<3431)return 48;
            if(doc<3522)return 49;
            if(doc<3601)return 50;
            if(doc<3647)return 51;
            if(doc<3695)return 52;
            if(doc<3722)return 53;
            if(doc<3777)return 54;
            if(doc<3822)return 55;
            if(doc<3876)return 56;
            if(doc<3915)return 57;
            if(doc<3963)return 58;
            if(doc<4009)return 59;
        case 4:
            if(doc<4009)return 59;
            if(doc<4093)return 60;
            if(doc<4301)return 61;
            if(doc<4352)return 62;
            if(doc<4401)return 63;
            if(doc<4442)return 64;
            if(doc<4494)return 65;
            if(doc<4545)return 66;
            if(doc<4630)return 67;
            if(doc<4639)return -1; // nos. 4630—4638, now published as Nine Homeric Papyri from Oxyrhynchus
            if(doc<4705)return 68;
            if(doc<4759) return 69;
            if(doc < 4803) return 70;
            if (doc < 4844) return 71;
            else return 72;
        default:
            return 0;
        }
    }
    public static String romanForPOxyDoc(int doc){
        return ROMAN[arabicForPOxyDoc(doc)];
        }
    
    public static String romanForSBDoc(int doc){
        return ROMAN[arabicForSBDoc(doc)];
    }

    public static int arabicForSBDoc(int doc){
                switch(doc/1000){
        case 0:
            if(doc<1) return -1;
        case 1:
        case 2:
        case 3:
        case 4:
        case 5:
            return 1;
        case 6:
            if(doc<6001)return 1;
            if(doc<7270)return 3;
        case 7:
            if(doc<7270)return 3;
            if(doc<7515)return 4;
            if(doc<8964)return 5;
        case 8:
            if(doc<8964)return 5;
            if(doc<9642)return 6;
        case 9:
            if(doc<9642)return 6;
            if(doc<10209)return 8;
        case 10:
            if(doc<10209)return 8;
            if(doc<10764)return 10;
            if(doc<11264)return 12;
        case 11:
            if(doc<11264)return 12;
            if(doc<12220) return 14;
        case 12:
            if(doc<12220) return 14;
            if(doc<13085)return 16;
        case 13:
            if(doc<13085)return 16;
            if(doc<14069)return 18;
        case 14:
            if(doc<14069)return 18;
            if(doc<15203)return 20;
        case 15:
            if(doc<15203)return 20;
            if(doc<15875)return 22;
            if(doc<16341)return 24;
        case 16:
            if(doc<16341)return 24;
            if(doc<16832)return 26;
         default:
             return 0;
        }
        
    }
}
