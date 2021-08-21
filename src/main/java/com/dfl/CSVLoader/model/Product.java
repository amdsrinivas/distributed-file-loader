package com.dfl.CSVLoader.model;

import java.util.Scanner;

public class Product {
    private Integer id ;
    private String name ;
    private String sku ;
    private String description ;
    private Integer lineNumber ;
    private String header = "name,sku,description" ;

    public Product() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

    public boolean fromString(String line, Integer lineNumber, Scanner fileScanner) {
        if(line.equals(header)){
            System.out.println("Header ignored : " + line);
            return false;
        }
        String[] columns = line.split(",") ;
        if(columns.length != 3){
            System.out.println("Not in required format : " + line);
            return false ;
        }

        // For multiline description.
        String nextLine ;
        if(columns[2].startsWith("\"")){
            while (fileScanner.hasNextLine() ){
                nextLine = fileScanner.nextLine() ;
                columns[2] = columns[2] + nextLine ;
                if(nextLine.endsWith("\"")) {
                    break;
                }
            }
        }
        setId(columns[1].hashCode());
        setName(columns[0]);
        setSku(columns[1]);
        setDescription(columns[2]);
        setLineNumber(lineNumber);
        return true;
    }
}
