import com.google.common.io.Files;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MergeRecord {

    public static void main(final String[] args) {

        if (args.length == 0) {
            System.err.println("Usage: java RecordMerger file1 [ file2 [...] ]");
            System.exit(1);
        }

        Connection conn = null;
        try {
            conn = connectDatabase();
            for (String arg : args) {
                String type = Files.getFileExtension(arg);
                if (type.equals("html")) {
                    List<String> htmlQuery = extractHTML(arg);
                    for (String query : htmlQuery) {
                        Statement stmt;
                        stmt = conn.createStatement();
                        stmt.execute(query);
                    }
                } else if (type.equals("csv")) {
                    List<String> CSVQuery = extractCSV(arg);
                    for (String query : CSVQuery){
                        Statement stmt;
                        stmt = conn.createStatement();
                        stmt.execute(query);
                    }
                } else if (type.equals("xml")) {
                    //extractXML(arg);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    private static Connection connectDatabase() throws SQLException {
        Path filePath = Paths.get(System.getProperty("user.dir"), "merge.db");
        Connection conn = DriverManager.getConnection("jdbc:sqlite:" + filePath.toString());
        DatabaseMetaData meta = conn.getMetaData();
        System.out.println("The driver name is " + meta.getDriverName());
        System.out.println("A new database has been created.");
        return conn;
    }

    private static List<String> extractCSV(String inputFile) throws IOException {
        List<String> result = new ArrayList<>();
        ClassLoader classLoader = new RecordMerger().getClass().getClassLoader();
        File pathName = new File(classLoader.getResource(inputFile).getFile());
        CSVParser parser = new CSVParser(new FileReader(pathName), CSVFormat.DEFAULT.withHeader());
        String tableName = inputFile.replace(".","_");
        result.add("DROP TABLE IF EXISTS " + tableName + " ;");
        String createTable = "CREATE TABLE IF NOT EXISTS " + tableName + " (";
        List<String> buffer = new ArrayList<>();
        for (String record : parser.getHeaderNames()) {
            if(record.equals("ID")){
                buffer.add(record + " text PRIMARY KEY");
            } else {
                buffer.add(record + " text");
            }
        }

        String createTableQuery = createTable + String.join(",", String.join(",", buffer)) + ");";
        result.add(createTableQuery);

        List<String> queryBuffer = new ArrayList<>();
        for(CSVRecord record : parser.getRecords()){
            List<String> attributeBuffer = new ArrayList<>();
            for(int i=0; i<record.size(); i++){
                attributeBuffer.add("\"" + record.get(i) + "\"");
            }
            String attributeQuery = "(" + String.join(",", attributeBuffer) + ")";
            queryBuffer.add(attributeQuery);
        }
        String insertQuery = "INSERT INTO " + tableName + " VALUES " + String.join(",", queryBuffer) + ";";
        result.add(insertQuery);

        return result;
    }

    private static void extractXML(String arg, HashMap<String, Person> people) {
    }



    private static List<String> extractHTML(String inputFile) throws IOException {
        List<String> result = new ArrayList<>();
        //Getting the HTML file from resources folder and parse it with JSoup
        ClassLoader classLoader = new RecordMerger().getClass().getClassLoader();
        File pathName = new File(classLoader.getResource(inputFile).getFile());
        String tableName = inputFile.replace(".","_");
        result.add("DROP TABLE IF EXISTS " + tableName + " ;");
        Document htmlFile;
        htmlFile = Jsoup.parse(new File(String.valueOf(pathName)), "ISO-8859-1");
        Element table = htmlFile.getElementById("directory");

        String createTable = "CREATE TABLE IF NOT EXISTS " + tableName + " (";
        List<String> list = new ArrayList<>();
        for (Element attribute : table.select("th")) {
            if (attribute.text().equals("ID")) {
                list.add(attribute.text() + " text PRIMARY KEY");
            }else {
                list.add(attribute.text() + " text");
            }
        }
        String createTableQuery = createTable + String.join(",", list) + ");";
        result.add(createTableQuery);

        String insertTable = "INSERT INTO " + tableName + " VALUES ";
        List<String> outerBuffer = new ArrayList<>();
        for (Element entry : table.select("tr")) {
            List<String> innerBuffer = new ArrayList<>();
            if(entry.select("td").size()==0){
                continue;
            }
            for (Element attribute : entry.select("td")){
                innerBuffer.add("\""+attribute.text()+"\"");
            }
            String insertQuery ="(" + String.join(",", innerBuffer) + ")";
            outerBuffer.add(insertQuery);
        }
        String insertQuery = insertTable + String.join(",",outerBuffer) + ";";
        result.add(insertQuery);
        return result;
    }
}
