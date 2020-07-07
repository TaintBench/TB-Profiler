import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Compute statistics of documented attributes for all findings.
 *
 * @author Linghui Luo
 */
public class AttributeStatCalculator {

  private static final String[] HEADERS = {
    "apkName",
    "findingID",
    "nonStaticField",
    "partialFlow",
    "reflection",
    "pathConstraints",
    "callbacks",
    "interComponentCommunication",
    "appendToString",
    "staticField",
    "lifecycle",
    "array",
    "collections",
    "payload",
    "interAppCommunication",
    "threading",
    "implicitFlows"
  };
  static Set<String> attrSet = new HashSet<>();
  public static final Logger LOGGER = LoggerFactory.getLogger(AttributeStatCalculator.class);

  public static void run(String findingsDir) throws IOException {
    LOGGER.info("Running " + AttributeStatCalculator.class.toString());
    File outFile = new File("output/attributes_stat.csv");
    outFile.getParentFile().mkdirs();
    outFile.createNewFile();
    FileWriter out = new FileWriter(outFile);
    CSVPrinter printer =
        new CSVPrinter(out, CSVFormat.DEFAULT.withHeader(HEADERS).withDelimiter(';'));
    for (File f : new File(findingsDir).listFiles()) {
      if (f.toString().endsWith("findings.json")) {
        LOGGER.info("Processing " + f.getAbsolutePath());
        calculate(f.getAbsolutePath(), printer);
      }
    }
    printer.flush();
    printer.close();
    LOGGER.info("Writing attributes statistics to "+outFile.getAbsolutePath());
  }

  static int j = 1;

  private static void calculate(String findingsFile, CSVPrinter printer) throws IOException {
    JsonParser parser = new JsonParser();
    JsonObject obj = parser.parse(new FileReader(findingsFile)).getAsJsonObject();
    JsonArray findings = obj.getAsJsonArray("findings");
    String apkName = obj.get("fileName").getAsString();
    for (int i = 0; i < findings.size(); i++) {
      JsonObject finding = findings.get(i).getAsJsonObject();
      int id = finding.get("ID").getAsInt();
      int nonStaticField = 0;
      int partialFlow = 0;
      int reflection = 0;
      int pathConstraints = 0;
      int callbacks = 0;
      int interComponentCommunication = 0;
      int appendToString = 0;
      int staticField = 0;
      int lifecycle = 0;
      int array = 0;
      int collections = 0;
      int payload = 0;
      int interAppCommunication = 0;
      int threading = 0;
      int implicitFlows = 0;
      boolean isNegative = finding.getAsJsonPrimitive("isNegative").getAsBoolean();
      if (!isNegative) {
        if (finding.has("attributes")) {
          JsonObject attributes = finding.getAsJsonObject("attributes");

          for (Entry<String, JsonElement> attribute : attributes.entrySet()) {
            if (attribute.getValue().getAsBoolean()) {
              String attr = attribute.getKey();
              if (attr.equals("nonStaticField")) nonStaticField = 1;
              if (attr.equals("partialFlow")) partialFlow = 1;
              if (attr.equals("reflection")) {
                reflection = 1;
              }
              if (attr.equals("pathConstraints")) pathConstraints = 1;
              if (attr.equals("callbacks")) callbacks = 1;
              if (attr.equals("interComponentCommunication")) interComponentCommunication = 1;
              if (attr.equals("appendToString")) appendToString = 1;
              if (attr.equals("staticField")) staticField = 1;
              if (attr.equals("lifecycle")) lifecycle = 1;
              if (attr.equals("array")) array = 1;
              if (attr.equals("collections")) collections = 1;
              if (attr.equals("payload")) payload = 1;
              if (attr.equals("interAppCommunication")) interAppCommunication = 1;
              if (attr.equals("threading")) threading = 1;
              if (attr.equals("implicitFlows")) implicitFlows = 1;
            }
          }
        }
        printer.printRecord(
            apkName,
            id,
            nonStaticField,
            partialFlow,
            reflection,
            pathConstraints,
            callbacks,
            interComponentCommunication,
            appendToString,
            staticField,
            lifecycle,
            array,
            collections,
            payload,
            interAppCommunication,
            threading,
            implicitFlows);
      }
    }
  }
}
