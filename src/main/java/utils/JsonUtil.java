package utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import metro.Line;
import metro.Metro;
import metro.Station;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class JsonUtil {
    private static final Logger rootLogger = LogManager.getRootLogger();
    private final static Logger errorLogger = LogManager.getLogger("errorLogger");

    private static Metro metro = Metro.getInstance();
    private final static String KEY_STATIONS = "stations";
    private final static String KEY_CONNECTIONS = "connections";
    private final static String KEY_TRANSFER = "transfer";
    private final static String KEY_STATION_FROM = "stationFrom";
    private final static String KEY_LINE_NUMBER_FROM = "lineFrom";
    private final static String KEY_STATION_TO = "stationTo";
    private final static String KEY_LINE_NUMBER_TO = "lineTo";

    public static void createJsonFile(String fileName) {
        rootLogger.info("Создание JSON-файла");
        try (FileWriter fileWriter = new FileWriter(fileName)) {
            JSONObject json = createParentJsonObject();
            String jsonString = new ObjectMapper()
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(json);
            fileWriter.write(jsonString);
        } catch (IOException ex) {
            ex.printStackTrace();
            errorLogger.error(ex.getMessage(), ex);
        }
        rootLogger.info("JSON-файл готов!");
    }

    public static void showInfoAboutLinesAndStations(String fileName) {
        try {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(getJsonString(fileName));

            JSONObject stationsObject = (JSONObject) json.get(KEY_STATIONS);
            TreeSet<Line> lines = new TreeSet<>();
            stationsObject.keySet().forEach(lineNumberObject -> {
                String lineNumber = (String) lineNumberObject;
                Line line = metro.getLineByNumber(lineNumber);
                lines.add(line);
            });
            System.out.println("Вывод информации о метро:");
            for (Line line : lines) {
                String info = String.format("%40s (№ %4s)\t:\t%2d станций",
                        line.getName(),
                        line.getNumber(),
                        line.getStations().size()
                );
                System.out.println(info);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            errorLogger.error(ex.getMessage(), ex);
        }
    }

    public static void showInfoAboutConnections(String fileName) {
        try {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(getJsonString(fileName));

            JSONArray connectionsObj = (JSONArray) json.get(KEY_CONNECTIONS);
            String format = "Станция \"%s\" (%s) соединяется с:\n";
            System.out.println("\nПересадки:");
            for (Object connectionObj : connectionsObj) {
                JSONObject connection = (JSONObject) connectionObj;

                String stationFrom = (String) connection.get(KEY_STATION_FROM);
                String lineNumberFrom = (String) connection.get(KEY_LINE_NUMBER_FROM);
                Line lineFrom = metro.getLineByNumber(lineNumberFrom);

                JSONArray transfersObj = (JSONArray) connection.get(KEY_TRANSFER);
                System.out.printf(format, stationFrom, lineFrom.getName());
                for (Object transferObj : transfersObj) {
                    JSONObject transfer = (JSONObject) transferObj;
                    String stationTo = (String) transfer.get(KEY_STATION_TO);
                    String lineNumberTo = (String) transfer.get(KEY_LINE_NUMBER_TO);
                    Line lineTo = metro.getLineByNumber(lineNumberTo);
                    System.out.printf("\tСтанция \"%s\" (%s)\n", stationTo, lineTo.getName());
                }
                System.out.println();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            errorLogger.error(ex.getMessage(), ex);
        }
    }

    //==================================================================================================================

    private static JSONObject createParentJsonObject() {
        JSONObject obj = new JSONObject();
        obj.put("stations", getStations());
        obj.put("lines", getLines());
        obj.put("connections", getConnections());
        return obj;
    }

    private static Map<String, List<String>> getStations() {
        Map<String, List<String>> lineWithStations = new HashMap<>();
        for (Line line : metro.getLines()) {
            List<String> stations = new ArrayList<>();
            line.getStations().forEach(station -> stations.add(station.getName()));
            lineWithStations.put(line.getNumber(), stations);
        }
        return lineWithStations;
    }

    private static List<Map<String, String>> getLines() {
        List<Map<String, String>> array = new ArrayList<>();
        for (Line line : metro.getLines()) {
            Map<String, String> lineObj = new HashMap<>();
            lineObj.put("number", line.getNumber());
            lineObj.put("name", line.getName());
            array.add(lineObj);
        }
        return array;
    }

    private static JSONArray getConnections() {
        JSONArray array = new JSONArray();
        TreeMap<Station, TreeSet<Station>> map = metro.getConnections();
        for (Map.Entry<Station, TreeSet<Station>> pair : map.entrySet()) {
            JSONObject connection = new JSONObject();
            connection.put("lineFrom", pair.getKey().getLine().getNumber());
            connection.put("stationFrom", pair.getKey().getName());
            JSONArray transfer = new JSONArray();
            TreeSet<Station> connectedStations = map.get(pair.getKey());
            for (Station conStation : connectedStations) {
                JSONObject var = new JSONObject();
                var.put("lineTo", conStation.getLine().getNumber());
                var.put("stationTo", conStation.getName());
                transfer.add(var);
            }
            connection.put("transfer", transfer);
            array.add(connection);
        }
        return array;
    }

    private static String getJsonString(String fileName) {
        StringBuilder builder = new StringBuilder();
        try {
            List<String> lines = Files.readAllLines(Paths.get(fileName));
            lines.forEach(builder::append);
        } catch (IOException ex) {
            ex.printStackTrace();
            errorLogger.error(ex.getMessage(), ex);
        }
        return builder.toString();
    }

}
