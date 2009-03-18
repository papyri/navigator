package info.papyri.epiduke.lucene.analysis;

public class AncientGreekCharsets {
    public static final char ALPHA_tonos = '\u0386';
    public static final char EPSILON_tonos = '\u0388';
    public static final char ETA_tonos = '\u0389';
    public static final char IOTA_tonos = '\u038A';
    public static final char OMICRON_tonos = '\u038C';
    public static final char UPSILON_tonos = '\u038E';
    public static final char OMEGA_tonos = '\u038F';
    public static final char iota_dialytika_tonos = '\u0390';
    public static final char ALPHA = '\u0391';
    public static final char BETA = '\u0392';
    public static final char GAMMA = '\u0393';
    public static final char DELTA = '\u0394';
    public static final char EPSILON = '\u0395';
    public static final char ZETA = '\u0396';
    public static final char ETA = '\u0397';
    public static final char THETA = '\u0398';
    public static final char IOTA = '\u0399';
    public static final char KAPPA = '\u039A';
    public static final char LAMDA = '\u039B';
    public static final char MU = '\u039C';
    public static final char NU = '\u039D';
    public static final char XI = '\u039E';
    public static final char OMICRON = '\u039F';
    public static final char PI = '\u03A0';
    public static final char RHO = '\u03A1';
    public static final char SIGMA = '\u03A3';
    public static final char TAU = '\u03A4';
    public static final char UPSILON = '\u03A5';
    public static final char PHI = '\u03A6';
    public static final char CHI = '\u03A7';
    public static final char PSI = '\u03A8';
    public static final char OMEGA = '\u03A9';
    public static final char IOTA_dialytika = '\u03AA';
    public static final char UPSILON_dialytika = '\u03AB';
    public static final char alpha_tonos = '\u03AC';
    public static final char epsilon_tonos = '\u03AD';
    public static final char eta_tonos = '\u03AE';
    public static final char iota_tonos = '\u03AF';
    public static final char upsilon_dialytika_tonos = '\u03B0';
    public static final char alpha = '\u03B1';
    public static final char beta = '\u03B2';
    public static final char gamma = '\u03B3';
    public static final char delta = '\u03B4';
    public static final char epsilon = '\u03B5';
    public static final char zeta = '\u03B6';
    public static final char eta = '\u03B7';
    public static final char theta = '\u03B8';
    public static final char iota = '\u03B9';
    public static final char kappa = '\u03BA';
    public static final char lamda = '\u03BB';
    public static final char mu = '\u03BC';
    public static final char nu = '\u03BD';
    public static final char xi = '\u03BE';
    public static final char omicron = '\u03BF';
    public static final char pi = '\u03C0';
    public static final char rho = '\u03C1';
    public static final char sigma_final = '\u03C2';
    public static final char sigma_medial = '\u03C3';
    public static final char tau = '\u03C4';
    public static final char upsilon = '\u03C5';
    public static final char phi = '\u03C6';
    public static final char chi = '\u03C7';
    public static final char psi = '\u03C8';
    public static final char omega = '\u03C9';
    public static final char iota_dialytika = '\u03CA';
    public static final char upsilon_dialytika = '\u03CB';
    public static final char omicron_tonos = '\u03CC';
    public static final char upsilon_tonos = '\u03CD';
    public static final char omega_tonos = '\u03CE';

    
    public static char[] UnicodeGreek = {
        // lower case
        '\u0390',
        '\u03AC',
        '\u03AD',
        '\u03AE',
        '\u03AF',
        '\u03B0',
        '\u03B1',
        '\u03B2',
        '\u03B3',
        '\u03B4',
        '\u03B5',
        '\u03B6',
        '\u03B7',
        '\u03B8',
        '\u03B9',
        '\u03BA',
        '\u03BB',
        '\u03BC',
        '\u03BD',
        '\u03BE',
        '\u03BF',
        '\u03C0',
        '\u03C1',
        '\u03C2',
        '\u03C3',
        '\u03C4',
        '\u03C5',
        '\u03C6',
        '\u03C7',
        '\u03C8',
        '\u03C9',
        '\u03CA',
        '\u03CB',
        '\u03CC',
        '\u03CD',
        '\u03CE',
        // upper case
        '\u0386',
        '\u0388',
        '\u0389',
        '\u038A',
        '\u038C',
        '\u038E',
        '\u038F',
        '\u0391',
        '\u0392',
        '\u0393',
        '\u0394',
        '\u0395',
        '\u0396',
        '\u0397',
        '\u0398',
        '\u0399',
        '\u039A',
        '\u039B',
        '\u039C',
        '\u039D',
        '\u039E',
        '\u039F',
        '\u03A0',
        '\u03A1',
        '\u03A3',
        '\u03A4',
        '\u03A5',
        '\u03A6',
        '\u03A7',
        '\u03A8',
        '\u03A9',
        '\u03AA',
        '\u03AB'
    };
    public static char toUnaccented(char letter)
    {
        // lower-case, unaccented return
            if (letter >= ALPHA && letter <= OMEGA) return letter;
            // upper-case, unaccented return
            if (letter >= alpha && letter <= omega) return letter;
            // lower-case accented in basic Greek
            switch (letter){
            case '\u00B7':
            case '\u0323': // combining dot below
            case '\u0374':
            case '\u0375':
            case '\u037A':
            case '\u0384':
            case '\u0385':
                return '\u0000';
            case ALPHA_tonos:
                return ALPHA;
            case '\u0387':
                return '\u0000';
            case EPSILON_tonos:
                return EPSILON;
            case ETA_tonos:
                return ETA;
            case IOTA_tonos:
                return IOTA;
            case OMICRON_tonos:
                return OMICRON;
            case iota_dialytika_tonos:
                return iota;
            case IOTA_dialytika:
                return IOTA;
            case UPSILON_dialytika:
                return UPSILON;
            case alpha_tonos:
                return alpha;
            case epsilon_tonos:
                return epsilon; 
            case eta_tonos:
                return eta;
            case iota_tonos:
                return iota;
            case upsilon_dialytika_tonos:   
                return upsilon;
            case iota_dialytika:
                return iota;
            case upsilon_dialytika:
                return upsilon;
            case omicron_tonos:
                return omicron;
            case upsilon_tonos:
                return upsilon;
            case omega_tonos:
                return omega;
            case '\u1F00':
            case '\u1F01':
            case '\u1F02':
            case '\u1F03':
            case '\u1F04':
            case '\u1F05':
            case '\u1F06':
            case '\u1F07':
                return alpha;
            case '\u1F08':
            case '\u1F09':
            case '\u1F0A':
            case '\u1F0B':
            case '\u1F0C':
            case '\u1F0D':
            case '\u1F0E':
            case '\u1F0F':
                return ALPHA;
            case '\u1F10':
            case '\u1F11':
            case '\u1F12':
            case '\u1F13':
            case '\u1F14':
            case '\u1F15':
                return epsilon;
            case '\u1F18':
            case '\u1F19':
            case '\u1F1A':
            case '\u1F1B':
            case '\u1F1C':
            case '\u1F1D':
                return EPSILON;
            case '\u1F20':
            case '\u1F21':
            case '\u1F22':
            case '\u1F23':
            case '\u1F24':
            case '\u1F25':
            case '\u1F26':
            case '\u1F27':
                return eta;
            case '\u1F28':
            case '\u1F29':
            case '\u1F2A':
            case '\u1F2B':
            case '\u1F2C':
            case '\u1F2D':
            case '\u1F2E':
            case '\u1F2F':
                return ETA;
            case '\u1F30':
            case '\u1F31':
            case '\u1F32':
            case '\u1F33':
            case '\u1F34':
            case '\u1F35':
            case '\u1F36':
            case '\u1F37':
                return iota;
            case '\u1F38':
            case '\u1F39':
            case '\u1F3A':
            case '\u1F3B':
            case '\u1F3C':
            case '\u1F3D':
            case '\u1F3E':
            case '\u1F3F':
                return IOTA;
            case '\u1F40':
            case '\u1F41':
            case '\u1F42':
            case '\u1F43':
            case '\u1F44':
            case '\u1F45':
                return omicron;
            case '\u1F48':
            case '\u1F49':
            case '\u1F4A':
            case '\u1F4B':
            case '\u1F4C':
            case '\u1F4D':
                return OMICRON;
            case '\u1F50':
            case '\u1F51':
            case '\u1F52':
            case '\u1F53':
            case '\u1F54':
            case '\u1F55':
            case '\u1F56':
            case '\u1F57':
                return upsilon;
            case '\u1F59':
            case '\u1F5B':
            case '\u1F5D':
            case '\u1F5F':
                return UPSILON;
            case '\u1F60':
            case '\u1F61':
            case '\u1F62':
            case '\u1F63':
            case '\u1F64':
            case '\u1F65':
            case '\u1F66':
            case '\u1F67':
                return omega;
            case '\u1F68':
            case '\u1F69':
            case '\u1F6A':
            case '\u1F6B':
            case '\u1F6C':
            case '\u1F6D':
            case '\u1F6E':
            case '\u1F6F':
                return OMEGA;
            case '\u1F70':
            case '\u1F71':
                return alpha;
            case '\u1F72':
            case '\u1F73':
                return epsilon;
            case '\u1F74':
            case '\u1F75':
                return eta;
            case '\u1F76':
            case '\u1F77':
                return iota;
            case '\u1F78':
            case '\u1F79':
                return omicron;
            case '\u1F7A':
            case '\u1F7B':
                return upsilon;
            case '\u1F7C':
            case '\u1F7D':
                return omega;
            case '\u1F80':
            case '\u1F81':
            case '\u1F82':
            case '\u1F83':
            case '\u1F84':
            case '\u1F85':
            case '\u1F86':
            case '\u1F87':
                return alpha;
            case '\u1F88':
            case '\u1F89':
            case '\u1F8A':
            case '\u1F8B':
            case '\u1F8C':
            case '\u1F8D':
            case '\u1F8E':
            case '\u1F8F':
                return ALPHA;
            case '\u1F90':
            case '\u1F91':
            case '\u1F92':
            case '\u1F93':
            case '\u1F94':
            case '\u1F95':
            case '\u1F96':
            case '\u1F97':
                return eta;
            case '\u1F98':
            case '\u1F99':
            case '\u1F9A':
            case '\u1F9B':
            case '\u1F9C':
            case '\u1F9D':
            case '\u1F9E':
            case '\u1F9F':
                return ETA;
            case '\u1FA0':
            case '\u1FA1':
            case '\u1FA2':
            case '\u1FA3':
            case '\u1FA4':
            case '\u1FA5':
            case '\u1FA6':
            case '\u1FA7':
                return omega;
            case '\u1FA8':
            case '\u1FA9':
            case '\u1FAA':
            case '\u1FAB':
            case '\u1FAC':
            case '\u1FAD':
            case '\u1FAE':
            case '\u1FAF':
                return OMEGA;
            case '\u1FB0':
            case '\u1FB1':
            case '\u1FB2':
            case '\u1FB3':
            case '\u1FB4':
            case '\u1FB6':
            case '\u1FB7':
                return alpha;
            case '\u1FB8':
            case '\u1FB9':
            case '\u1FBA':
            case '\u1FBB':
            case '\u1FBC':
                return ALPHA;
            case '\u1FBD':
            case '\u1FBE':
            case '\u1FBF':
            case '\u1FC0':
            case '\u1FC1':
                return '\u0000';
            case '\u1FC2':
            case '\u1FC3':
            case '\u1FC4':
            case '\u1FC6':
            case '\u1FC7':
                return eta;
            case '\u1FC8':
            case '\u1FC9':
                return EPSILON;
            case '\u1FCA':
            case '\u1FCB':
            case '\u1FCC':
                return ETA;
            case '\u1FCD':
            case '\u1FCE':
            case '\u1FCF':
                return '\u0000';
            case '\u1FD0':
            case '\u1FD1':
            case '\u1FD2':
            case '\u1FD3':
            case '\u1FD6':
            case '\u1FD7':
                return iota;
            case '\u1FD8':
            case '\u1FD9':
            case '\u1FDA':
            case '\u1FDB':
                return IOTA;
            case '\u1FDD':
            case '\u1FDE':
            case '\u1FDF':
                return '\u0000';
            case '\u1FE0':
            case '\u1FE1':
            case '\u1FE2':
            case '\u1FE3':
                return upsilon;
            case '\u1FE4':
            case '\u1FE5':
                return rho;
            case '\u1FE6':
            case '\u1FE7':
                return upsilon;
            case '\u1FE8':
            case '\u1FE9':
            case '\u1FEA':
            case '\u1FEB':
                return UPSILON;
            case '\u1FEC':
                return RHO;
            case '\u1FED':
            case '\u1FEE':
            case '\u1FEF':
                return '\u0000';
            case '\u1FF2':
            case '\u1FF3':
            case '\u1FF4':
            case '\u1FF6':
            case '\u1FF7':
                return omega;
            case '\u1FF8':
            case '\u1FF9':
                return OMICRON;
            case '\u1FFA':
            case '\u1FFB':
            case '\u1FFC':
                return OMEGA;
            case '\u1FFD':
            case '\u1FFE':
                return '\u0000';
            default:
                return (letter);
            }

        }
    public static char toLowerCase(char letter){
        // First deal with lower case, not accented letters
        if (letter >= '\u03AC' && letter <= '\u03D1')
        {
            switch (letter){
            case sigma_final:
                return sigma_medial;
            case '\u03D0':
                return beta;
            case '\u03D1':
                return theta;
            default:
                return letter;
            }
        }

        if (letter >= ALPHA && letter <= OMEGA) return (char)(letter + 32);
        // upper-case, unaccented return
        if (letter >= alpha && letter <= omega) return letter;
        // lower-case accented in basic Greek
        switch (letter){
        case ALPHA_tonos:
            return alpha_tonos;
        case EPSILON_tonos:
            return epsilon_tonos;
        case ETA_tonos:
            return eta_tonos;
        case IOTA_tonos:
            return iota_tonos;
        case OMICRON_tonos:
            return omicron_tonos;
        case IOTA_dialytika:
            return iota_dialytika;
        case UPSILON_dialytika:
            return upsilon_dialytika;
        case '\u1F08':
        case '\u1F09':
        case '\u1F0A':
        case '\u1F0B':
        case '\u1F0C':
        case '\u1F0D':
        case '\u1F0E':
        case '\u1F0F':
        case '\u1F18':
        case '\u1F19':
        case '\u1F1A':
        case '\u1F1B':
        case '\u1F1C':
        case '\u1F1D':
        case '\u1F28':
        case '\u1F29':
        case '\u1F2A':
        case '\u1F2B':
        case '\u1F2C':
        case '\u1F2D':
        case '\u1F2E':
        case '\u1F2F':
        case '\u1F38':
        case '\u1F39':
        case '\u1F3A':
        case '\u1F3B':
        case '\u1F3C':
        case '\u1F3D':
        case '\u1F3E':
        case '\u1F3F':
        case '\u1F48':
        case '\u1F49':
        case '\u1F4A':
        case '\u1F4B':
        case '\u1F4C':
        case '\u1F4D':
        case '\u1F59':
        case '\u1F5B':
        case '\u1F5D':
        case '\u1F5F':
        case '\u1F68':
        case '\u1F69':
        case '\u1F6A':
        case '\u1F6B':
        case '\u1F6C':
        case '\u1F6D':
        case '\u1F6E':
        case '\u1F6F':
        case '\u1F98':
        case '\u1F99':
        case '\u1F9A':
        case '\u1F9B':
        case '\u1F9C':
        case '\u1F9D':
        case '\u1F9E':
        case '\u1F9F':
        case '\u1FA8':
        case '\u1FA9':
        case '\u1FAA':
        case '\u1FAB':
        case '\u1FAC':
        case '\u1FAD':
        case '\u1FAE':
        case '\u1FAF':
            return (char)(letter - 8);
        case '\u1FB8':
            return '\u1FB0';
        case '\u1FB9':
            return '\u1FB1';
        case '\u1FBA':
            return '\u1F70';
        case '\u1FBB':
            return '\u1F71';
        case '\u1FBC':
            return '\u1FB3';
        case '\u1FC8':
            return '\u1F72';
        case '\u1FC9':
            return '\u1F72';
        case '\u1FCA':
            return '\u1F74';
        case '\u1FCB':
            return '\u1F75';
        case '\u1FCC':
            return '\u1FC3';
        case '\u1FD8':
            return '\u1FD0';
        case '\u1FD9':
            return '\u1FD1';
        case '\u1FDA':
            return '\u1F76';
        case '\u1FDB':
            return '\u1F77';
        case '\u1FE8':
            return '\u1FE0';
        case '\u1FE9':
            return '\u1FE1';
        case '\u1FEA':
            return '\u1F7A';
        case '\u1FEB':
            return '\u1F7B';
        case '\u1FEC':
            return '\u1FE5';
        case '\u1FF8':
            return '\u1F78';
        case '\u1FF9':
            return '\u1F79';
        case '\u1FFA':
            return '\u1F7C';
        case '\u1FFB':
            return '\u1F7D';
        case '\u1FFC':
            return '\u1FF3';
        default:
            return Character.toLowerCase(letter);
        }

    }
}
