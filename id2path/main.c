/* 
 * File:   main.c
 * Author: hcayless
 *
 * Created on May 12, 2010, 9:05 AM
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>

/*
 * 
 */
int main() {

    char id[50];
    id[0] = '\0';
    char out[50];
    out[0] = '\0';
    char series[20];
    series[0] = '\0';
    char volume[10];
    volume[0] = '\0';
    char item[20];
    item[0] = '\0';
    char hgv[10];
    hgv[0] = '\0';
    char hgvdir[10];
    hgvdir[0] = '\0';
    char collection[20];
    collection[0] = '\0';
    int i;
    int s;

    while (1) {
        fgets(id, 50, stdin);
        if (strncmp(id, "ddbdp", 5) == 0) {
            strcat(out, "DDB_EpiDoc_XML/");
            
            for (i = 0; i < 50; i++) {
                if (id[i] == ',') {
                    id[i] = '-';
                }
                if (id[i] == '/') {
                    id[i] = '_';
                }
            }
            s = 5;
            for (i = 5; i < 50; i++) {
                if (id[i] == ';') {
                    if (s == 5) {
                        addsub(series, id, s, i);
                        s = i;
                        continue;
                    }
                    if (id[i - 1] != ';') {
                        addsub(volume, id, s, i);
                        s = i;
                        continue;
                    }
                    s = i;
                }
            }
            addsub(item, id, s, strlen(id) - 1);
            if (volume[0] == '\0') {
                strcat(out, series);
                strcat(out, "/");
                strcat(out, series);
                strcat(out, ".");
                strcat(out, item);
            } else {
                strcat(out, series);
                strcat(out, "/");
                strcat(out, series);
                strcat(out, ".");
                strcat(out, volume);
                strcat(out, "/");
                strcat(out, series);
                strcat(out, ".");
                strcat(out, volume);
                strcat(out, ".");
                strcat(out, item);
            }
            puts(out);
            series[0] = '\0';
            volume[0] = '\0';
            item[0] = '\0';
        }
        if (strncmp(id, "hgv", 3) == 0) {
            strcat(out, "HGV_meta_EpiDoc/HGV");
            addsub(hgv, id, 3, strlen(id) - 1);
            int hgvid = atoi(hgv);
            sprintf(hgvdir, "%d", (int)ceil((double)hgvid / 1000));
            strcat(out, hgvdir);
            strcat(out, "/");
            strcat(out, hgv);
            puts(out);
            hgv[0] = '\0';
            hgvdir[0] = '\0';
        }
        if (strncmp(id, "apis", 4) == 0) {
            strcat(out, "APIS/");
            s = 4;
            for (i = 4; i < 50; i++) {
                if (id[i] == '.') {
                    if (s == 4) {
                        addsub(collection, id, s, i);
                    }
                    s = i;
                }
            }
            addsub(item, id, s, strlen(id) - 1);
            strcat(out, collection);
            strcat(out, "/xml/");
            strcat(out, collection);
            strcat(out, ".apis.");
            strcat(out, item);
            puts(out);
            collection[0] = '\0';
            item[0] = '\0';
        }
        
        id[0] = '\0';
        out[0] = '\0';
        
    }


    return (EXIT_SUCCESS);
}

int addsub(char *add, char *in, int start, int end) {
    int i;
    int j;
    for (i = start + 1, j = 0; i < end; i++, j++) {
        if (in[i] == '\0')
            break;
        if (in[i] != '\n') {
            add[j] = in[i];
            add[j + 1] = '\0';
        }
    }
    return 1;
}

