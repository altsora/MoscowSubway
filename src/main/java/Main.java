import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import utils.ParsingUtil;

@Getter
public class Main {
//    private static final Marker errorMarker = MarkerManager.getMarker("ERRORS");
    public static Logger rootLogger = LogManager.getRootLogger();

    public static void main(String[] args) {
        Document doc = ParsingUtil.getNewDocument();
//        System.out.println("Hello");
//        rootLogger.info("Start");
//        rootLogger.error(errorMarker, "Ошибка");
    }
}
