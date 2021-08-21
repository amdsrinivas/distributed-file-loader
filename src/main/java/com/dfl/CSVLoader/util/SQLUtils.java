package com.dfl.CSVLoader.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SQLUtils {
    public static String getSql(String path) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(path));
        StringBuilder sb = new StringBuilder() ;
        String line;
        while ((line = in.readLine()) != null)
        {
            sb.append(line);
            sb.append("\n");
        }
        return sb.toString();
    }
}
