package info.papyri.antlr;
// TODO parse ':' as raised dot combination
public abstract class BetaChars {
    public static final char A_LOWER = '\u03B1';
    public static final char A_UPPER = '\u0391';
    public static final char A_LOWER_BASE = '\u1EEE';
    public static final char A_UPPER_BASE = '\u1F07';
    public static final char B_LOWER = '\u03B2';
    public static final char B_UPPER = '\u0392';
    public static final char C_UPPER = '\u039E';
    public static final char C_LOWER = '\u03BE';
    public static final char D_UPPER = '\u0394';
    public static final char D_LOWER = '\u03B4';
    public static final char E_UPPER = '\u0395';
    public static final char E_LOWER = '\u03B5';
    public static final char F_UPPER = '\u03A6';
    public static final char F_LOWER = '\u03C6';
    public static final char G_UPPER = '\u0393';
    public static final char G_LOWER = '\u03B3';
    public static final char H_UPPER = '\u0397';
    public static final char H_LOWER = '\u03B7';
    public static final char I_UPPER = '\u0399';
    public static final char I_LOWER = '\u03B9';
    public static final char K_UPPER = '\u039A';
    public static final char K_LOWER = '\u03BA';
    public static final char L_UPPER = '\u039B';
    public static final char L_LOWER = '\u03BB';
    public static final char M_UPPER = '\u039C';
    public static final char M_LOWER = '\u03BC';
    public static final char N_UPPER = '\u039D';
    public static final char N_LOWER = '\u03BD';
    public static final char O_UPPER = '\u039F';
    public static final char O_LOWER = '\u03BF';
    public static final char P_UPPER = '\u03A0';
    public static final char P_LOWER = '\u03C0';
    public static final char Q_UPPER = '\u0398';
    public static final char Q_LOWER = '\u03B8';
    public static final char R_UPPER = '\u03A1';
    public static final char R_LOWER = '\u03C1';
    public static final char S_UPPER = '\u03A3';
    public static final char S_LOWER = '\u03C3'; // 03C2 FOR FINAL SIGMA
    public static final char S1 = '\u03C3';
    public static final char S2 = '\u03C2';
    public static final char S3_UPPER = '\u03F9';
    public static final char S3_LOWER = '\u03F2';
    public static final char T_UPPER = '\u03A4';
    public static final char T_LOWER = '\u03C4';
    public static final char U_UPPER = '\u03A5';
    public static final char U_LOWER = '\u03C5';
    public static final char V_UPPER = '\u03DC';
    public static final char V_LOWER = '\u03DD';
    public static final char W_UPPER = '\u03A9';
    public static final char W_LOWER = '\u03C9';
    public static final char X_UPPER = '\u03A7';
    public static final char X_LOWER = '\u03C7';
    public static final char Y_UPPER = '\u03A8';
    public static final char Y_LOWER = '\u03C8';
    public static final char Z_UPPER = '\u0396';
    public static final char Z_LOWER = '\u03B6';
    public static final char CHIRHO = '\u2627';
    public static char map(String token, boolean capital){
        //char [] chars = token.toCharArray();
        char s = token.charAt(0);
        switch (s){
        case 'A':
          return capital?BetaChars.A_UPPER:BetaChars.A_LOWER;
        case 'B':
            return capital?BetaChars.B_UPPER:BetaChars.B_LOWER;
        case 'C':
            return capital?BetaChars.C_UPPER:BetaChars.C_LOWER;
        case 'D':
            return capital?BetaChars.D_UPPER:BetaChars.D_LOWER;
        case 'E':
            return capital?BetaChars.E_UPPER:BetaChars.E_LOWER;
        case 'F':
            return capital?BetaChars.F_UPPER:BetaChars.F_LOWER;
        case 'G':
            return capital?BetaChars.G_UPPER:BetaChars.G_LOWER;
        case 'H':
            return capital?BetaChars.H_UPPER:BetaChars.H_LOWER;
        case 'I':
            return capital?BetaChars.I_UPPER:BetaChars.I_LOWER;
        case 'J':
          return s; // no Greek beta code
        case 'K':
            return capital?BetaChars.K_UPPER:BetaChars.K_LOWER;
        case 'L':
            return capital?BetaChars.L_UPPER:BetaChars.L_LOWER;
        case 'M':
            return capital?BetaChars.M_UPPER:BetaChars.M_LOWER;
        case 'N':
            return capital?BetaChars.N_UPPER:BetaChars.N_LOWER;
        case 'O':
            return capital?BetaChars.O_UPPER:BetaChars.O_LOWER;
        case 'P':
            return capital?BetaChars.P_UPPER:BetaChars.P_LOWER;
        case 'Q':
            return capital?BetaChars.Q_UPPER:BetaChars.Q_LOWER;
        case 'R':
            return capital?BetaChars.R_UPPER:BetaChars.R_LOWER;
        case 'S':
            if (token.length() == 1) return capital?BetaChars.S_UPPER:BetaChars.S1; 
            if (token.charAt(1) == '2'){
                return capital?BetaChars.S_UPPER:BetaChars.S2;
            }
            else {
                return capital?BetaChars.S_UPPER:BetaChars.S1;
            }
        case 'T':
            return capital?BetaChars.T_UPPER:BetaChars.T_LOWER;
        case 'U':
            return capital?BetaChars.U_UPPER:BetaChars.U_LOWER;
        case 'V':
            return capital?BetaChars.V_UPPER:BetaChars.V_LOWER;
        case 'W':
            return capital?BetaChars.W_UPPER:BetaChars.W_LOWER;
        case 'X':
            return capital?BetaChars.X_UPPER:BetaChars.X_LOWER;
        case 'Y':
            return capital?BetaChars.Y_UPPER:BetaChars.Y_LOWER;
        case 'Z':
            return capital?BetaChars.Z_UPPER:BetaChars.Z_LOWER;
        case ')':
          return PSILI;
        case '(':
            return DASIA;
        case '/':
            return OXIA;
        case '=':
            return PERIS;
        case '\\':
            return VARIA;
        case '+':
            return DIALYTIKA;
        case '|':
            return YPO_PROSGE;
        case '?':
            return '\u0323';
        case ':':
            return '\u00B7'; 
//        case '\u00BD': // Base Plane vulgar fraction 1/2 "\uD800\uDD75"
//            return Character.;
//        case '\u00BE': // Base Plane vulgar fraction 3/4 "\uD800\uDD78"
//            return '\u10178';
//        case '\u2ce9':
//            return CHIRHO;
        default:
            //System.err.print("Unexpected character : ");
            //char[] chars = token.toCharArray();
            //for (char c:chars){
            //    System.err.print(Integer.toHexString(c));
            //}
            //System.err.println(" (\""  + s + "\")" + " in full token \"" + token + "\"");
            return s;
    }
    }
    public static String symbol(String token){
        int symbol = 0;
        try {
            symbol = Integer.parseInt(token);
        }
        catch (NumberFormatException nfe){return token;}
        switch (symbol){
        case 336:
            return "#336"; // "S"
        case 322:
            return "#322"; // CHIRHO
        default:
            System.err.println("Unexpected symbol token: " + token);
            return "#" + token;
        }
    }
    
    public static char [] combine(char [] input){
        if (input == null || input.length < 2 ) return input;
        
        int offset = 0;
        int nonmod = 0;
        boolean [] keep = new boolean [input.length];
        for (int i=1;i<input.length;i++){
            switch (input[i]){
            case OXIA:
                keep[i] = false;
                offset += oxia;
                break;
            case DASIA:
                keep[i] = false;
                offset += dasia;
                break;
            case PERIS:
                keep[i] = false;
                offset += perispomeni;
                break;
            case PSILI:
                keep[i] = false;
                offset += psili;
                break;
            case VARIA:
                keep[i] = false;
                offset += varia;
                break;
            case DIALYTIKA:
                keep[i] = false;
                offset += dialytika;
                break;
            case YPO_PROSGE:
                keep[i] = false;
                offset += ypo_prosge;
                break;
            default:
                keep[i] = true;
                nonmod++;
            }
        }
        char [] result = new char [nonmod + 1];
        int keep_next = 0;
        for (int i=0;i<input.length;i++){
            if (keep[i]) result[++keep_next] = input[i];
        }
        switch (input[0]){
        case '\u03B1':
            result[0] = a[offset];
            break;
        case '\u0391':
            result[0] = A[offset];
            break;
        case '\u03B5':
            result[0] = e[offset];
            break;
        case '\u0395':
            result[0] = E[offset];
            break;
        case '\u03B7':
            result[0] = h[offset];
            break;
        case '\u0397':
            result[0] = H[offset];
            break;
        case '\u03B9':
            result[0] = i[offset];
            break;
        case '\u03BF':
            result[0] = o[offset];
            break;
        case '\u039F':
            result[0] = O[offset];
            break;
        case '\u0399':
            result[0] = I[offset];
            break;
        case '\u03C1':
            result[0] = r[offset];
            break;
        case '\u03A1':
            result[0] = R[offset];
            break;
        case '\u03C5':
            result[0] = u[offset];
            break;
        case '\u03A5':
            result[0] = U[offset];
            break;
        case '\u03C9':
            result[0] = w[offset];
            break;
        case '\u03A9':
            result[0] = W[offset];
            break;
        default:
            return input;
        }
        return result;
        
    }
    
    private final static char OXIA = '\u0301';
    private final static char DASIA = '\u0314';
    private final static char PERIS = '\u0342';
    private final static char PSILI = '\u0313';
    private final static char VARIA = '\u0300';
    private final static char DIALYTIKA = '\u0308';
    private final static char YPO_PROSGE = '\u0345';
    private final static int oxia = 1;
    private final static int perispomeni = 2;
    private final static int varia = 3;
    private final static int dasia = 4;
    private final static int psili = 8;
    private final static int dialytika = 12;
    private final static int ypo_prosge = 16;

    private static final char [] a = {
        '\u03B1', // 00,  non-combined
        '\u1F71', // 01,  oxia
        '\u1FB6', // 02,  peris
        '\u1F70', // 03, varia
        '\u1F01', // 04, dasia
        '\u1F05', // 05, dasia oxia
        '\u1F07', // 06, dasia peris
        '\u1F03', // 07, dasia varia
        '\u1F00', // 08, psili
        '\u1F04', // 09, psili oxia
        '\u1F06', // 10, psili peris
        '\u1F02', // 11, psili varia
        '\u0000', // 12, dialy
        '\u0000', // 13, dialy oxia
        '\u0000', // 14, dialy peris
        '\u0000', // 15, dialy varia
        '\u1FB3', // 12, ypog
        '\u1FB4', // 13, ypog oxia
        '\u1FB7', // 14, ypog peris
        '\u1FB2', // 15, ypog varia
        '\u1F81', // 16, ypog dasia
        '\u1F85', // 17, ypog dasia oxia
        '\u1F87', // 18, ypog dasia peris
        '\u1F83', // 19, ypog dasia varia
        '\u1F80', // 20, ypog psili
        '\u1F84', // 21, ypog psili oxia
        '\u1F86', // 22, ypog psili peris
        '\u1F82', // 23, ypog psili varia
        '\u1FB1', // 24, macron
        '\u1FB0'  // 25, vrachy
        };
    private static final char [] A = {
        '\u0391', // 00,  non-combined
        '\u1F71', // 01,  oxia
        '\u1FB6', // 02,  peris
        '\u1F70', // 03, varia
        '\u1F09', // 04, dasia
        '\u1F0D', // 05, dasia oxia
        '\u1F0F', // 06, dasia peris
        '\u1F0B', // 07, dasia varia
        '\u1F08', // 08, psili
        '\u1F0C', // 09, psili oxia
        '\u1F0E', // 10, psili peris
        '\u1F0A', // 11, psili varia
        '\u0000', // 12, dialy, not used alone
        '\u0000', // 13, dialy oxia
        '\u0000', // 14, dialy peris
        '\u0000', // 15, dialy varia
        '\u1FBC', // 12, prosg
        '\u1FB4', // 13, prosg oxia
        '\u1FB7', // 14, prosg peris
        '\u1FB2', // 15, prosg varia
        '\u1F89', // 16, prosg dasia
        '\u1F8D', // 17, prosg dasia oxia
        '\u1F8F', // 18, prosg dasia peris
        '\u1F8B', // 19, prosg dasia varia
        '\u1F88', // 20, prosg psili
        '\u1F8C', // 21, prosg psili oxia
        '\u1F8E', // 22, prosg psili peris
        '\u1F8A', // 23, prosg psili varia
        '\u1FB9', // 24, macron
        '\u1FB8'  // 25, vrachy
        };
    private static final char [] e = {
        '\u03B5', // 00,  non-combined
        '\u1F73', // 01,  oxia
        '\u03B5', // 02,  peris
        '\u1F72', // 03, varia
        '\u1F11', // 04, dasia
        '\u1F15', // 05, dasia oxia
        '\u0000', // 06, dasia peris
        '\u1F13', // 07, dasia varia
        '\u1F10', // 08, psili
        '\u1F14', // 09, psili oxia
        '\u03B5', // 10, psili peris
        '\u1F12', // 11, psili varia
        '\u03B5', // 12, dialy, not used alone
        '\u03B5', // 13, dialy oxia
        '\u03B5', // 14, dialy peris
        '\u03B5', // 15, dialy varia
        '\u03B5', // 12, prosg
        '\u03B5', // 13, prosg oxia
        '\u03B5', // 14, prosg peris
        '\u03B5', // 15, prosg varia
        '\u03B5', // 16, prosg dasia
        '\u03B5', // 17, prosg dasia oxia
        '\u03B5', // 18, prosg dasia peris
        '\u03B5', // 19, prosg dasia varia
        '\u03B5', // 20, prosg psili
        '\u03B5', // 21, prosg psili oxia
        '\u03B5', // 22, prosg psili peris
        '\u03B5', // 23, prosg psili varia
        '\u03B5', // 24, macron
        '\u03B5'  // 25, vrachy
        };
    private static final char [] E = {
        '\u0395', // 00,  non-combined
        '\u1FC9', // 01,  oxia
        '\u0395', // 02,  peris
        '\u1FC8', // 03, varia
        '\u1F19', // 04, dasia
        '\u1F1D', // 05, dasia oxia
        '\u0395', // 06, dasia peris
        '\u1F1B', // 07, dasia varia
        '\u1F18', // 08, psili
        '\u1F1C', // 09, psili oxia
        '\u0395', // 10, psili peris
        '\u1F1A', // 11, psili varia
        '\u0395', // 12, dialy, not used alone
        '\u0395', // 13, dialy oxia
        '\u0395', // 14, dialy peris
        '\u0395', // 15, dialy varia
        '\u0395', // 12, prosg
        '\u0395', // 13, prosg oxia
        '\u0395', // 14, prosg peris
        '\u0395', // 15, prosg varia
        '\u0395', // 16, prosg dasia
        '\u0395', // 17, prosg dasia oxia
        '\u0395', // 18, prosg dasia peris
        '\u0395', // 19, prosg dasia varia
        '\u0395', // 20, prosg psili
        '\u0395', // 21, prosg psili oxia
        '\u0395', // 22, prosg psili peris
        '\u0395', // 23, prosg psili varia
        '\u0395', // 24, macron
        '\u0395'  // 25, vrachy
        };
    private static final char [] i = {
        '\u03B9', // 00,  non-combined
        '\u1F77', // 01,  oxia
        '\u1FD6', // 02,  peris
        '\u1F76', // 03, varia
        '\u1F31', // 04, dasia
        '\u1F35', // 05, dasia oxia
        '\u1F37', // 06, dasia peris
        '\u1F33', // 07, dasia varia
        '\u1F30', // 08, psili
        '\u1F34', // 09, psili oxia
        '\u1F36', // 10, psili peris
        '\u1F32', // 11, psili varia
        '\u0000', // 12, dialy, not used alone
        '\u1FD3', // 13, dialy oxia
        '\u1FD7', // 14, dialy peris
        '\u1FD2', // 15, dialy varia
        '\u0000', // 12, prosg
        '\u0000', // 13, prosg oxia
        '\u0000', // 14, prosg peris
        '\u0000', // 15, prosg varia
        '\u0000', // 16, prosg dasia
        '\u0000', // 17, prosg dasia oxia
        '\u0000', // 18, prosg dasia peris
        '\u0000', // 19, prosg dasia varia
        '\u0000', // 20, prosg psili
        '\u0000', // 21, prosg psili oxia
        '\u0000', // 22, prosg psili peris
        '\u0000', // 23, prosg psili varia
        '\u1FD1', // 24, macron
        '\u1FD0'  // 25, vrachy
        };
    private static final char [] I = {
        '\u0399', // 00,  non-combined
        '\u1FDB', // 01,  oxia
        '\u0000', // 02,  peris
        '\u1FDA', // 03, varia
        '\u1F39', // 04, dasia
        '\u1F3D', // 05, dasia oxia
        '\u1F3F', // 06, dasia peris
        '\u1F3B', // 07, dasia varia
        '\u1F38', // 08, psili
        '\u1F3C', // 09, psili oxia
        '\u1F3E', // 10, psili peris
        '\u1F3A', // 11, psili varia
        '\u0000', // 12, dialy, not used alone
        '\u0000', // 13, dialy oxia
        '\u0000', // 14, dialy peris
        '\u0000', // 15, dialy varia
        '\u0000', // 12, prosg
        '\u0000', // 13, prosg oxia
        '\u0000', // 14, prosg peris
        '\u0000', // 15, prosg varia
        '\u0000', // 16, prosg dasia
        '\u0000', // 17, prosg dasia oxia
        '\u0000', // 18, prosg dasia peris
        '\u0000', // 19, prosg dasia varia
        '\u0000', // 20, prosg psili
        '\u0000', // 21, prosg psili oxia
        '\u0000', // 22, prosg psili peris
        '\u0000', // 23, prosg psili varia
        '\u1FD9', // 24, macron
        '\u1FD8'  // 25, vrachy
        };
    private static final char [] h = {
        '\u03B7', // 00,  non-combined
        '\u1F75', // 01,  oxia
        '\u1FC6', // 02,  peris
        '\u1F74', // 03, varia
        '\u1F21', // 04, dasia
        '\u1F25', // 05, dasia oxia
        '\u1F27', // 06, dasia peris
        '\u1F23', // 07, dasia varia
        '\u1F20', // 08, psili
        '\u1F24', // 09, psili oxia
        '\u1F26', // 10, psili peris
        '\u1F22', // 11, psili varia
        '\u0000', // 12, dialy, not used alone
        '\u0000', // 13, dialy oxia
        '\u0000', // 14, dialy peris
        '\u0000', // 15, dialy varia
        '\u1FC3', // 12, ypog
        '\u1FC4', // 13, ypog oxia
        '\u1FC7', // 14, ypog peris
        '\u1FC2', // 15, ypog varia
        '\u1F91', // 16, ypog dasia
        '\u1F95', // 17, ypog dasia oxia
        '\u1F97', // 18, ypog dasia peris
        '\u1F93', // 19, ypog dasia varia
        '\u1F90', // 20, ypog psili
        '\u1F94', // 21, ypog psili oxia
        '\u1F96', // 22, ypog psili peris
        '\u1F92', // 23, ypog psili varia
        '\u0000', // 24, macron
        '\u0000'  // 25, vrachy
        };
    private static final char [] H = {
        '\u0397', // 00,  non-combined
        '\u1FCB', // 01,  oxia
        '\u0397', // 02,  peris
        '\u1FCA', // 03, varia
        '\u1F29', // 04, dasia
        '\u1F2D', // 05, dasia oxia
        '\u1F2F', // 06, dasia peris
        '\u1F2B', // 07, dasia varia
        '\u1F28', // 08, psili
        '\u1F2C', // 09, psili oxia
        '\u1F2E', // 10, psili peris
        '\u1F2A', // 11, psili varia
        '\u0397', // 12, dialy, not used alone
        '\u0397', // 13, dialy oxia
        '\u0397', // 14, dialy peris
        '\u0397', // 15, dialy varia
        '\u1FCC', // 12, ypog
        '\u0397', // 13, ypog oxia
        '\u0397', // 14, ypog peris
        '\u0397', // 15, ypog varia
        '\u0397', // 16, ypog dasia
        '\u1F9D', // 17, ypog dasia oxia
        '\u1F9F', // 18, ypog dasia peris
        '\u1F9B', // 19, ypog dasia varia
        '\u1F98', // 20, ypog psili
        '\u1F9C', // 21, ypog psili oxia
        '\u1F9E', // 22, ypog psili peris
        '\u1F9A', // 23, ypog psili varia
        '\u0397', // 24, macron
        '\u0397'  // 25, vrachy
    };
    private static final char [] u = {
        '\u03C5', // 00,  non-combined
        '\u1F7B', // 01,  oxia
        '\u1FE6', // 02,  peris
        '\u1F7A', // 03, varia
        '\u1F51', // 04, dasia
        '\u1F55', // 05, dasia oxia
        '\u1F57', // 06, dasia peris
        '\u1F53', // 07, dasia varia
        '\u1F50', // 08, psili
        '\u1F54', // 09, psili oxia
        '\u1F56', // 10, psili peris
        '\u1F52', // 11, psili varia
        '\u0000', // 12, dialy, not used alone
        '\u1FE3', // 13, dialy oxia
        '\u1FE7', // 14, dialy peris
        '\u1FE2', // 15, dialy varia
        '\u03C5', // 16, ypog
        '\u03C5', // 17, ypog oxia
        '\u03C5', // 18, ypog peris
        '\u03C5', // 19, ypog varia
        '\u03C5', // 20, ypog dasia
        '\u03C5', // 21, ypog dasia oxia
        '\u03C5', // 22, ypog dasia peris
        '\u03C5', // 23, ypog dasia varia
        '\u03C5', // 24, ypog psili
        '\u03C5', // 25, ypog psili oxia
        '\u03C5', // 26, ypog psili peris
        '\u03C5', // 27, ypog psili varia
        '\u1FE1', // 28, macron
        '\u1FE0'  // 29, vrachy
        };
    private static final char [] U = {
        '\u03A5', // 00,  non-combined
        '\u1FEB', // 01,  oxia
        '\u03A5', // 02,  peris
        '\u1FEA', // 03, varia
        '\u03A5', // 04, dasia
        '\u03A5', // 05, dasia oxia
        '\u03A5', // 06, dasia peris
        '\u03A5', // 07, dasia varia
        '\u03A5', // 08, psili
        '\u03A5', // 09, psili oxia
        '\u03A5', // 10, psili peris
        '\u03A5', // 11, psili varia
        '\u03A5', // 12, dialy, not used alone
        '\u03A5', // 13, dialy oxia
        '\u03A5', // 14, dialy peris
        '\u03A5', // 15, dialy varia
        '\u03A5', // 16, ypog
        '\u03A5', // 17, ypog oxia
        '\u03A5', // 18, ypog peris
        '\u03A5', // 19, ypog varia
        '\u03A5', // 20, ypog dasia
        '\u03A5', // 21, ypog dasia oxia
        '\u03A5', // 22, ypog dasia peris
        '\u03A5', // 23, ypog dasia varia
        '\u03A5', // 24, ypog psili
        '\u03A5', // 25, ypog psili oxia
        '\u03A5', // 26, ypog psili peris
        '\u03A5', // 27, ypog psili varia
        '\u1FE9', // 28, macron
        '\u1FE8'  // 29, vrachy
    };
    private static final char [] o = {
        '\u03BF', // 00,  non-combined
        '\u1F79', // 01,  oxia
        '\u03BF', // 02,  peris
        '\u1F78', // 03, varia
        '\u1F41', // 04, dasia
        '\u1F45', // 05, dasia oxia
        '\u03BF', // 06, dasia peris
        '\u1F43', // 07, dasia varia
        '\u1F40', // 08, psili
        '\u1F44', // 09, psili oxia
        '\u03BF', // 10, psili peris
        '\u1F42', // 11, psili varia
        '\u03BF', // 12, dialy
        '\u03BF', // 13, dialy oxia
        '\u03BF', // 14, dialy peris
        '\u03BF', // 15, dialy varia
        '\u03BF', // 12, ypog
        '\u03BF', // 13, ypog oxia
        '\u03BF', // 14, ypog peris
        '\u03BF', // 15, ypog varia
        '\u03BF', // 16, ypog dasia
        '\u03BF', // 17, ypog dasia oxia
        '\u03BF', // 18, ypog dasia peris
        '\u03BF', // 19, ypog dasia varia
        '\u03BF', // 20, ypog psili
        '\u03BF', // 21, ypog psili oxia
        '\u03BF', // 22, ypog psili peris
        '\u03BF', // 23, ypog psili varia
        '\u03BF', // 24, macron
        '\u03BF'  // 25, vrachy        
    };
    private static final char [] O = {
        '\u039F', // 00,  non-combined
        '\u1FF9', // 01,  oxia
        '\u039F', // 02,  peris
        '\u1FF8', // 03, varia
        '\u1F49', // 04, dasia
        '\u1F4D', // 05, dasia oxia
        '\u039F', // 06, dasia peris
        '\u1F4B', // 07, dasia varia
        '\u1F48', // 08, psili
        '\u1F4C', // 09, psili oxia
        '\u039F', // 10, psili peris
        '\u1F4A', // 11, psili varia
        '\u039F', // 12, dialy
        '\u039F', // 13, dialy oxia
        '\u039F', // 14, dialy peris
        '\u039F', // 15, dialy varia
        '\u039F', // 12, ypog
        '\u039F', // 13, ypog oxia
        '\u039F', // 14, ypog peris
        '\u039F', // 15, ypog varia
        '\u039F', // 16, ypog dasia
        '\u039F', // 17, ypog dasia oxia
        '\u039F', // 18, ypog dasia peris
        '\u039F', // 19, ypog dasia varia
        '\u039F', // 20, ypog psili
        '\u039F', // 21, ypog psili oxia
        '\u039F', // 22, ypog psili peris
        '\u039F', // 23, ypog psili varia
        '\u039F', // 24, macron
        '\u039F'  // 25, vrachy        
    };
    private static final char [] w = {
        '\u03C9', // 00,  non-combined
        '\u1F7D', // 01,  oxia
        '\u1FF6', // 02,  peris
        '\u1F7C', // 03, varia
        '\u1F61', // 04, dasia
        '\u1F65', // 05, dasia oxia
        '\u1F67', // 06, dasia peris
        '\u1F63', // 07, dasia varia
        '\u1F60', // 08, psili
        '\u1F64', // 09, psili oxia
        '\u1F66', // 10, psili peris
        '\u1F62', // 11, psili varia
        '\u03C9', // 12, dialy
        '\u03C9', // 13, dialy oxia
        '\u03C9', // 14, dialy peris
        '\u03C9', // 15, dialy varia
        '\u1FF3', // 12, ypog
        '\u1FF4', // 13, ypog oxia
        '\u1FF7', // 14, ypog peris
        '\u1FF2', // 15, ypog varia
        '\u1FA1', // 16, ypog dasia
        '\u1FA5', // 17, ypog dasia oxia
        '\u1FA7', // 18, ypog dasia peris
        '\u1FA3', // 19, ypog dasia varia
        '\u1FA0', // 20, ypog psili
        '\u1FA4', // 21, ypog psili oxia
        '\u1FA6', // 22, ypog psili peris
        '\u1FA2', // 23, ypog psili varia
        '\u03C9', // 24, macron
        '\u03C9'  // 25, vrachy
    };
    private static final char [] W = {
        '\u03A9', // 00,  non-combined
        '\u1FFB', // 01,  oxia
        '\u03A9', // 02,  peris
        '\u1FFA', // 03, varia
        '\u1F69', // 04, dasia
        '\u1F6D', // 05, dasia oxia
        '\u1F6F', // 06, dasia peris
        '\u1F6B', // 07, dasia varia
        '\u1F68', // 08, psili
        '\u1F6C', // 09, psili oxia
        '\u1F6E', // 10, psili peris
        '\u1F6A', // 11, psili varia
        '\u03A9', // 12, dialy
        '\u03A9', // 13, dialy oxia
        '\u03A9', // 14, dialy peris
        '\u03A9', // 15, dialy varia
        '\u1FFC', // 12, prosg
        '\u03A9', // 13, prosg oxia
        '\u03A9', // 14, prosg peris
        '\u03A9', // 15, prosg varia
        '\u1FA9', // 16, prosg dasia
        '\u1FAD', // 17, prosg dasia oxia
        '\u1FAF', // 18, prosg dasia peris
        '\u1FAB', // 19, prosg dasia varia
        '\u1FA8', // 20, prosg psili
        '\u1FAC', // 21, prosg psili oxia
        '\u1FAE', // 22, prosg psili peris
        '\u1FAA', // 23, prosg psili varia
        '\u03A9', // 24, macron
        '\u03A9'  // 25, vrachy
    };
    private static final char [] r = {
        '\u03C1', // 00,  non-combined
        '\u03C1', // 01,  oxia
        '\u03C1', // 02,  peris
        '\u03C1', // 03, varia
        '\u1FE5', // 04, dasia
        '\u03C1', // 05, dasia oxia
        '\u03C1', // 06, dasia peris
        '\u03C1', // 07, dasia varia
        '\u1FE4', // 08, psili
        '\u03C1', // 09, psili oxia
        '\u03C1', // 10, psili peris
        '\u03C1', // 11, psili varia
        '\u03C1', // 12, dialy
        '\u03C1', // 13, dialy oxia
        '\u03C1', // 14, dialy peris
        '\u03C1', // 15, dialy varia
        '\u03C1', // 12, ypog
        '\u03C1', // 13, ypog oxia
        '\u03C1', // 14, ypog peris
        '\u03C1', // 15, ypog varia
        '\u03C1', // 16, ypog dasia
        '\u03C1', // 17, ypog dasia oxia
        '\u03C1', // 18, ypog dasia peris
        '\u03C1', // 19, ypog dasia varia
        '\u03C1', // 20, ypog psili
        '\u03C1', // 21, ypog psili oxia
        '\u03C1', // 22, ypog psili peris
        '\u03C1', // 23, ypog psili varia
        '\u03C1', // 24, macron
        '\u03C1'  // 25, vrachy
        
    };
    private static final char [] R = {
        '\u03A1', // 00,  non-combined
        '\u03A1', // 01,  oxia
        '\u03A1', // 02,  peris
        '\u03A1', // 03, varia
        '\u1FEC', // 04, dasia
        '\u03A1', // 05, dasia oxia
        '\u03A1', // 06, dasia peris
        '\u03A1', // 07, dasia varia
        '\u03A1', // 08, psili
        '\u03A1', // 09, psili oxia
        '\u03A1', // 10, psili peris
        '\u03A1', // 11, psili varia
        '\u03A1', // 12, dialy
        '\u03A1', // 13, dialy oxia
        '\u03A1', // 14, dialy peris
        '\u03A1', // 15, dialy varia
        '\u03A1', // 12, ypog
        '\u03A1', // 13, ypog oxia
        '\u03A1', // 14, ypog peris
        '\u03A1', // 15, ypog varia
        '\u03A1', // 16, ypog dasia
        '\u03A1', // 17, ypog dasia oxia
        '\u03A1', // 18, ypog dasia peris
        '\u03A1', // 19, ypog dasia varia
        '\u03A1', // 20, ypog psili
        '\u03A1', // 21, ypog psili oxia
        '\u03A1', // 22, ypog psili peris
        '\u03A1', // 23, ypog psili varia
        '\u03A1', // 24, macron
        '\u03A1'  // 25, vrachy
    };
}
