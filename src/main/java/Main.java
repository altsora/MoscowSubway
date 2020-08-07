import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.JsonUtil;
import utils.ParsingUtil;

@Getter
public class Main {
    private static final Logger rootLogger = LogManager.getRootLogger();
    private final static String JSON_FILE = "result/metro.json";

    public static void main(String[] args) {
        ParsingUtil.parseWikiPage();
        JsonUtil.createJsonFile(JSON_FILE);
        JsonUtil.showMetroInfo(JSON_FILE);
        rootLogger.info("Завершение программы");
    }
}
