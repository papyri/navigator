package info.papyri.util;

public class EditDistance {
/**
 * This implementation of the Levenshtein algorithm is adapted from
 * Michael Gilleland's reference example at http://www.merriampark.com/ld.htm 
 */

        public static int distance (char[] source, char[] target) {
        int d[][];
        int i; // source index
        int j; // target index
        int cost;

          int n = source.length;
          int m = target.length;
          if (n == 0) {
            return m;
          }
          if (m == 0) {
            return n;
          }
          d = new int[n+1][m+1];

          for (i = 0; i <= n; i++) {
            d[i][0] = i;
          }

          for (j = 1; j < m; j++) {
            d[0][j] = j;
          }

          for (i = 0; i < n; i++) {

            for (j = 0; j < m; j++) {

              cost = (source[i] == target[j])?0:1;

              d[i + 1][j + 1] = Math.min (Math.min(d[i][j + 1]+1, d[i + 1][j]+1), d[i][j] + cost);

            }

          }

          return d[n][m];

        }
        
        private static String [] bigrams(char [] src){
            String [] result = new String [src.length + 1];
            result[0] = new String(new char[]{'#',src[0]});
            result[result.length-1] = new String(new char[]{src[src.length-1],'#'});
            for(int i=1;i<src.length;i++){
                result[i] = new String(new char[]{src[i-1],src[i]});
            }
            return result;
        }
        
        public static int shingleDistance (char[] source, char[] target) {
            int d[][];
            int i; // source index
            int j; // target index
            int cost;
            String [] sShingles = bigrams(source);
            String [] tShingles = bigrams(target);
              int n = sShingles.length;
              int m = tShingles.length;
              if (n == 0) {
                return m;
              }
              if (m == 0) {
                return n;
              }
              d = new int[n+1][m+1];

              for (i = 0; i <= n; i++) {
                d[i][0] = i;
              }

              for (j = 1; j < m; j++) {
                d[0][j] = j;
              }

              for (i = 0; i < n; i++) {

                for (j = 0; j < m; j++) {

                  cost = (sShingles[i].equals( tShingles[j]))?0:1;

                  d[i + 1][j + 1] = Math.min (Math.min(d[i][j + 1]+1, d[i + 1][j]+1), d[i][j] + cost);

                }

              }

              return d[n][m];

            }

      }