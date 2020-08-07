package utils;

import metro.Line;
import metro.Metro;
import metro.Station;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;
import java.util.TreeSet;

public class ParsingUtil {
    private static final Logger rootLogger = LogManager.getRootLogger();
    private static final Logger errorLogger = LogManager.getLogger("errorLogger");

    public static Metro metro = Metro.getInstance();
    private static final String WIKI_PAGE = "https://ru.wikipedia.org/wiki/Список_станций_Московского_метрополитена";
    private static Document wikiPageDoc = getNewDocument();

    private static Document getNewDocument() {
        Document doc = null;
        try {
            doc = Jsoup.connect(WIKI_PAGE).maxBodySize(0).get();
        } catch (IOException ex) {
            ex.printStackTrace();
            errorLogger.error(ex.getMessage(), ex);
        }
        return doc;
    }

    public static void parseWikiPage() {
        long start = System.currentTimeMillis();
        rootLogger.info("Начало парсинга");
        parseLinesAndStations();
        parseConnections();
        long end = System.currentTimeMillis() - start;
        rootLogger.info("Метро построено. Время построения: {} мс", end);
        rootLogger.info("Всего линий - {}, всего станций - {}, всего пересадок: {}",
                metro.getLines().size(),
                metro.getStations().size(),
                metro.getConnections().size());
    }

    private static void parseLinesAndStations() {
        // Три таблицы: подземное метро, монорельс и МЦК
        Elements tables = wikiPageDoc.select("div.mw-parser-output > table.standard");
        for (Element rowsTable : tables) {
            // Все строки текущей таблицы (заголовок, шапка, обычные строки)
            Elements rows = rowsTable.select("tbody > tr");
            for (Element correctRow : rows) {
                // Если строка - заголовок или шапка таблицы
                if (correctRow.getElementsByTag("th").size() > 0) continue;
                parseWikiLine(correctRow);
                parseWikiStation(correctRow);
            }
        }
    }

    private static void parseConnections() {
        // Три таблицы со станциями-строками подземки, монорельса и МЦК
        Elements tables = wikiPageDoc.select("div.mw-parser-output > table.standard");
        for (Element rowsTable : tables) {
            // Все строки текущей таблицы (заголовок, шапка, обычные строки)
            Elements rows = rowsTable.select("tbody > tr");
            for (Element correctRow : rows) {
                // Если строка - заголовок или шапка таблицы
                if (correctRow.getElementsByTag("th").size() > 0) continue;
                parseWikiConnections(correctRow);
            }
        }
    }

    //==================================================================================================================

    private static void parseWikiLine(Element row) {
        Element cellWithLine = row.select("td").get(0);                   // Ячейка таблицы с линией
        String lineName = cellWithLine.selectFirst("a").attr("title");  // Имя линии
        String lineNumber = cellWithLine.selectFirst("span").text();               // Номер линии
        Line newLine = new Line(lineNumber, lineName);                             // Новая линия

        if (!metro.getLines().contains(newLine)) {                                 // Добавляем линию в метро
            metro.addLine(newLine);
        }
    }

    private static void parseWikiStation(Element row) {
        Elements cellWithLine = row.select("td").get(0).select("a");        // Ячейка таблицы с линией/линиями
        for (Element element : cellWithLine) {
            String lineName = element.attr("title");                              // Имя линии
            Line currentLine = metro.getLineByName(lineName);                               // Берём линию из метро по её имени

            Element cellWithStation = row.select("td").get(1).selectFirst("a");     // Ячейка таблицы со станцией
            String stationName = cellWithStation.text();                                    // Имя станции
            Station newStation = new Station(stationName, currentLine);                     // Новая станция

            currentLine.addStation(newStation);                                             // Добавляем новую станцию в линию
            if (!metro.getStations().contains(newStation)) {                                // Добавляем станцию в метро
                metro.addStation(newStation);
            }
        }
    }

    // Получаем все пересадки с каждой станции, где это возможно
    private static void parseWikiConnections(Element row) {
        Element cellConnects = row.select("td").get(3)                           // Ячейка с пересадкой
                .selectFirst("td[data-sort-value~=(\\d+.*)]");                            // Ячейка, где пересадка есть (атрибут не равен Infinity)
        if (cellConnects == null)
            return;                                                 // Элемент равен null, если пересадки нет
        TreeSet<Station> connectedStations = new TreeSet<>();                             // Список станций, доступных для перехода
        //===============================================================================================================================
        Element cellWithLine = row.select("td").get(0);                               // Ячейка с линией отправления
        String lineNameFrom = cellWithLine.selectFirst("a").attr("title");          // Имя линии
        Line lineFrom = metro.getLineByName(lineNameFrom);                                    // Линия (начало пересадки)

        Element cellWithStation = row.select("td").get(1).selectFirst("a");           // Ячейка со станцией отправления
        String nameStationFrom = cellWithStation.text();                                      // Имя станции
        Station stationFrom = metro.getStationByNameAndLine(nameStationFrom, lineFrom);       // Станция (начало пересадки)
        //===============================================================================================================================
        Elements cellWithConnections = cellConnects.select("a");                                  // Все станции, с которыми есть контакт
        for (Element stationConnect : cellWithConnections) {
            String transferInfo = stationConnect.attr("title");                                // Строка, содержащая информацию о пересадке
            String nameLineTo =
                    transferInfo.matches("Переход на станцию Деловой центр Калининской линии") ?    // Имя линии, на станцию которой осуществляется пересадка
                            "Солнцевская линия" : getNameLine(transferInfo);                              // Пересадка на Деловой центр Солнцевской линии прописана неверно - задаём линию явно.
            Line lineTo = metro.getLineByName(nameLineTo);                                                // Линия станции (конец пересадки)

            String nameStationTo = getNameStation(transferInfo, lineTo);                                  // Имя станции, на которую осуществляется пересадка
            Station stationTo = metro.getStationByNameAndLine(nameStationTo, lineTo);                            // Станция (конец пересадки)
            connectedStations.add(stationTo);                                                             // Добавили в список станций пересадки
        }
        metro.addConnections(stationFrom, connectedStations);                                            // Добавили соединение в метро
    }

    // Возвращает имя линии из сообщения о пересадки
    private static String getNameLine(String info) {
        TreeSet<Line> lines = metro.getLines();
        String result = null;
        for (Line line : lines) {
            String nameLine = line.getName();
            String subNameLine;
            if (nameLine.contains("Московск")) {      // Если линия - Московский монорельс или Московское центральное кольцо
                subNameLine = nameLine.split(" ")[1].substring(0, 4);   // Первые 4 буквы второго слова ("цент" или "моно")
            } else {
                subNameLine = nameLine.substring(0, 5);        // Берём первые 5 букв имени линии
            }
            if (info.contains(subNameLine)) {                  // Если такое имя линии содержится в инфо
                result = line.getName();
                break;
            }
        }
        return result;
    }

    // Возвращает имя станции из сообщения о пересадки
    private static String getNameStation(String info, Line line) {
        List<Station> stations = line.getStations();
        for (Station station : stations) {
            if (info.contains(station.getName()))
                return station.getName();
        }
        errorLogger.error("Сообщение о пересадке \"{}\" не содержит имя станции назначения", info);
        return null;
    }
}
