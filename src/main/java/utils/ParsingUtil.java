package utils;

import metro.Line;
import metro.Metro;
import metro.Station;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class ParsingUtil {
    private static final Logger rootLogger = LogManager.getRootLogger();
    private static final Logger errorLogger = LogManager.getLogger("errorLogger");

    public static Metro metro = Metro.getInstance();
    private static final String WIKI_PAGE = "https://ru.wikipedia.org/wiki/Список_станций_Московского_метрополитена";

    public static Document getNewDocument() {
        Document doc = null;
        try {
            doc = Jsoup.connect(WIKI_PAGE).maxBodySize(0).get();
//            throw new IOException();
        } catch (IOException ex) {
            ex.printStackTrace();
            errorLogger.error(ex.getMessage(), ex);
        }
        return doc;
    }
}
