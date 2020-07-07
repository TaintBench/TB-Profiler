import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ReadSourcesAndSinks {
  static Set<String> sources = new HashSet<>();
  static Set<String> sinks = new HashSet<>();

  static void readMergedSourcesSinks() {
    String sourcePath = "E:\\Git\\Github\\taintbench\\TaintProfiler\\config\\merged_sources.txt";
    String sinkPath = "E:\\Git\\Github\\taintbench\\TaintProfiler\\config\\merged_sinks.txt";

    File sourceFile = new File(sourcePath);
    BufferedReader rd;
    try {
      rd = new BufferedReader(new FileReader(sourceFile));
      String line = null;
      while ((line = rd.readLine()) != null) {
        sources.add(line);
      }

    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    File sinkFile = new File(sinkPath);
    BufferedReader rd2;
    try {
      rd2 = new BufferedReader(new FileReader(sinkFile));
      String line = null;
      while ((line = rd2.readLine()) != null) {
        sinks.add(line);
      }

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  static void readDroidBenchSourcesSinks()
      throws IOException, ParserConfigurationException, SAXException {
    String xmlsDir = "E:\\Git\\Github\\taintbench\\DroidBench30\\benchmark\\groundtruth";
    File dirFile = new File(xmlsDir);
    List<String> xmls = new ArrayList<>();
    Files.walk(Paths.get(dirFile.toURI()))
        .filter(Files::isRegularFile)
        .forEach(
            f -> {
              if (f.toString().endsWith(".xml")) {
                xmls.add(f.toFile().getAbsolutePath());
              }
            });

    for (String xmlFile : xmls) {
      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      Document doc = dBuilder.parse(xmlFile);
      NodeList nList = doc.getElementsByTagName("reference");
      for (int temp = 0; temp < nList.getLength(); temp++) {
        Node nNode = nList.item(temp);
        Node type = nNode.getAttributes().getNamedItem("type");
        if (type.toString().contains("from")) {
          NodeList children = nNode.getChildNodes();
          for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeName().equals("statement")) {
              NodeList stmtChildren = child.getChildNodes();
              for (int j = 0; j < stmtChildren.getLength(); j++) {
                Node stmt = stmtChildren.item(j);
                if (stmt.getNodeName().equals("statementgeneric")) {
                  String source = "<" + stmt.getFirstChild().getTextContent() + ">";
                  sources.add(source);
                }
              }
            }
          }
        }
        if (type.toString().contains("to")) {
          NodeList children = nNode.getChildNodes();
          for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeName().equals("statement")) {
              NodeList stmtChildren = child.getChildNodes();
              for (int j = 0; j < stmtChildren.getLength(); j++) {
                Node stmt = stmtChildren.item(j);
                if (stmt.getNodeName().equals("statementgeneric")) {
                  String sink = "<" + stmt.getFirstChild().getTextContent() + ">";
                  sinks.add(sink);
                }
              }
            }
          }
        }
      }
    }
  }

  static void readTaintBenchSourcesSinks()
      throws JsonIOException, JsonSyntaxException, FileNotFoundException {

    String vscodeFindingsDir =
        "E:\\Git\\Github\\taintbench\\taint-benchmark\\taintviewer\\vscode\\findings";
    File dir = new File(vscodeFindingsDir);
    for (File file : dir.listFiles()) {
      if (file.isFile()) {
        JsonParser parser = new JsonParser();
        JsonObject obj = parser.parse(new FileReader(file)).getAsJsonObject();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonArray findings = obj.getAsJsonArray("findings");
        for (int i = 0; i < findings.size(); i++) {
          JsonObject finding = findings.get(i).getAsJsonObject();
          if (finding.has("source")) {
            JsonObject source = finding.get("source").getAsJsonObject();
            if (source.has("IRs")) {
              JsonObject jimpleIR = source.get("IRs").getAsJsonArray().get(0).getAsJsonObject();
              if (jimpleIR.has("IRstatement")) {
                String ir = jimpleIR.get("IRstatement").getAsString();
                Pattern pattern = Pattern.compile("<.*:\\s.*>");
                Matcher matcher = pattern.matcher(ir);
                if (matcher.find()) {
                  sources.add(matcher.group(0));
                }
              }
            }
          }
          if (finding.has("sink")) {
            JsonObject sink = finding.get("sink").getAsJsonObject();
            if (sink.has("IRs")) {
              JsonObject jimpleIR = sink.get("IRs").getAsJsonArray().get(0).getAsJsonObject();
              if (jimpleIR.has("IRstatement")) {
                String ir = jimpleIR.get("IRstatement").getAsString();
                Pattern pattern = Pattern.compile("<.*:\\s.*>");
                Matcher matcher = pattern.matcher(ir);
                if (matcher.find()) {
                  sinks.add(matcher.group(0));
                }
              }
            }
          }
        }
      }
    }
  }

  public static void main(String[] args)
      throws IOException, ParserConfigurationException, SAXException {
    readMergedSourcesSinks();
    readDroidBenchSourcesSinks();
    readTaintBenchSourcesSinks();
    File outputFile = new File("output" + File.separator + "sources.txt");
    try (PrintWriter pw = new PrintWriter(outputFile)) {
      for (String source : sources) {
        pw.println(source);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    File outputFile2 = new File("output" + File.separator + "sinks.txt");
    try (PrintWriter pw = new PrintWriter(outputFile2)) {
      for (String sink : sinks) {
        pw.println(sink);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
