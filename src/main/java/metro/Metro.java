package metro;

import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

@Getter
public class Metro {
    private final static Logger rootLogger = LogManager.getRootLogger();
    private final static Logger errorLogger = LogManager.getLogger("errorLogger");
    private final static Logger infoLogger = LogManager.getLogger("infoLogger");

    private static Metro metro;
    private TreeSet<Line> lines;
    private TreeSet<Station> stations;
    private TreeMap<Station, TreeSet<Station>> connections;

    private Metro() {
        lines = new TreeSet<>();
        stations = new TreeSet<>();
        connections = new TreeMap<>();
        rootLogger.info("Метро готово к заполнению");
    }

    public static Metro getInstance() {
        if (metro == null) {
            metro = new Metro();
        }
        return metro;
    }

    public void addStation(Station station) {
        stations.add(station);
        infoLogger.info("\tДобавлена станция: {}", station.getName());
    }

    public void addLine(Line line) {
        lines.add(line);
        infoLogger.info("В метро добавлена линия: {} ({})",
                line.getName(),
                line.getNumber());
    }

    public void addConnections(Station station, TreeSet<Station> connectedStations) {
        connections.put(station, connectedStations);
        StringBuilder sb = new StringBuilder("{");
        connectedStations.forEach(con ->
                sb.append(con.getName())
                        .append("(")
                        .append(con.getLine().getNumber())
                        .append("); ")
        );
        sb.append("}");
        infoLogger.info("Добавлено соединение станции {} ({}) со станциями: \t{}",
                station.getName(),
                station.getLine().getName(),
                sb.toString());
    }

    public Line getLineByName(String lineName) {
        for (Line line : lines) {
            if (lineName.equalsIgnoreCase(line.getName())) {
                return line;
            }
        }
        errorLogger.error("Линия с именем {} не найдена", lineName);
        return null;
    }

    public Line getLineByNumber(String lineNumber) {
        for (Line line : lines) {
            if (lineNumber.equals(line.getNumber()))
                return line;
        }
        errorLogger.error("Линия с номером {} не найдена", lineNumber);
        return null;
    }

    public Station getStationByNameAndLine(String stationName, Line line) {
        List<Station> stations = line.getStations();
        for (Station station : stations) {
            if (stationName.equalsIgnoreCase(station.getName())) {
                return station;
            }
        }
        errorLogger.error("Станция с именем {} не найдена", stationName);
        return null;
    }
}
