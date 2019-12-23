import com.google.common.io.Files;
import com.opencsv.CSVWriter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class RecordMerger {

    public static final String FILENAME_COMBINED = "combined.csv";

    /**
     * Entry point of this test.
     *
     * @param args command line arguments: first.html and second.csv.
     * @throws Exception bad things had happened.
     */
    public static void main(final String[] args) throws Exception {

        HashMap<String, Person> people = new HashMap<>();

        if (args.length == 0) {
            System.err.println("Usage: java RecordMerger file1 [ file2 [...] ]");
            System.exit(1);
        }

        for(String arg: args){
            String type = Files.getFileExtension(arg);
            if(type.equals("html")){
                extractHTML(arg, people);
            }else if(type.equals("csv")){
                extractCSV(arg, people);
            }else if(type.equals("xml")){
                extractXML(arg, people);
            }
        }

        writeToCSV(people);
    }



    private static void extractHTML(String inputFile, HashMap<String,Person> people) throws IOException {
        //Getting the HTML file from resources folder and parse it with JSoup
        ClassLoader classLoader = new RecordMerger().getClass().getClassLoader();
        File pathName = new File(classLoader.getResource(inputFile).getFile());
        Document htmlFile = null;
        htmlFile = Jsoup.parse(new File(String.valueOf(pathName)), "ISO-8859-1");
        Element table = htmlFile.getElementById("directory");

        for(Element row : table.select("tr")){
            Elements tds = row.select("td");
            if(tds.size() == 0){
                continue;
            }else{
                String ID = String.valueOf(tds.get(0).text());
                if(people.containsKey(ID)){
                    Person person = people.get(ID);
                    person.setAddress(String.valueOf(tds.get(2).text()));
                    person.setPhoneNumber(String.valueOf(tds.get(3).text()));
                }else{
                    Person person = new Person(null,null,null,null,null,null);
                    person.setID(String.valueOf(tds.get(0).text()));
                    person.setName(String.valueOf(tds.get(1).text()));
                    person.setAddress(String.valueOf(tds.get(2).text()));
                    person.setPhoneNumber(String.valueOf(tds.get(3).text()));
                    people.put(ID, person);
                }
            }

        }
    }

    private static void extractCSV(String inputFile, HashMap<String,Person> people) throws IOException {
        //Getting the CSV file from resources folder and parse it with Apache common library
        ClassLoader classLoader = new RecordMerger().getClass().getClassLoader();
        File pathName = new File(classLoader.getResource(inputFile).getFile());
        CSVParser parser = new CSVParser(new FileReader(pathName), CSVFormat.DEFAULT.withHeader());
        for (CSVRecord record : parser) {
            String ID = record.get("ID");
            if(people.containsKey(ID)){
                Person person = people.get(ID);
                person.setOccupation(record.get("Occupation"));
                person.setGender(record.get("Gender"));
            }else{
                Person person = new Person(null,null,null,null,null,null);
                person.setID(record.get("ID"));
                person.setName(record.get("Name"));
                person.setOccupation(record.get("Occupation"));
                person.setGender(record.get("Gender"));
                people.put(ID, person);
            }
        }
        parser.close();
    }

    private static void extractXML(String inputFile, HashMap<String,Person> people) {
        //If it's needed in the future
    }

    private static void writeToCSV(HashMap<String,Person> people) throws IOException {
        //Sorts keys in hashMap in TreeMap
        Map<String, Person> sortedPeople = new TreeMap<String, Person>(people);

        //Gets the path and creates a new CSV file.
        Path source = Paths.get(new RecordMerger().getClass().getResource("/").getPath());
        Files.touch(new File(source.toAbsolutePath() + "/" + FILENAME_COMBINED));
        System.out.println("The path to the file is : " + source.toAbsolutePath() + "/" + FILENAME_COMBINED);

        FileWriter outputFile = new FileWriter(source.toAbsolutePath() + "/" + FILENAME_COMBINED);
        CSVWriter writer = new CSVWriter(outputFile);
        List<String[]> data = new ArrayList<>();
        String ID, name, gender, occupation, address, phone;
        data.add(new String[] {"ID"+"\t","Name"+"\t","Gender"+"\t","Occupation"+"\t","Address"+"\t","Phone Number"+"\t" });
        for (Map.Entry<String,Person> personEntry : sortedPeople.entrySet()){
            ID = personEntry.getValue().getID();
            name = personEntry.getValue().getName();
            if(personEntry.getValue().getGender()==null){
                gender = "Not Specified";
            }else{
                gender = personEntry.getValue().getGender();
            }

            if(personEntry.getValue().getOccupation()==null){
                occupation = "Not Specified";
            }else{
                occupation = personEntry.getValue().getOccupation();
            }

            if(personEntry.getValue().getAddress()==null){
                address = "Not Specified";
            }else{
                address = personEntry.getValue().getAddress();
            }

            if(personEntry.getValue().getPhoneNumber()==null){
                phone = "Not Specified";
            }else{
                phone = personEntry.getValue().getPhoneNumber();
            }
            data.add(new String[] {ID.trim()+"\t",name.trim()+"\t",gender.trim()+"\t",occupation+"\t",address+"\t",phone+"\t"});
        }

        writer.writeAll(data);
        writer.close();
    }
}