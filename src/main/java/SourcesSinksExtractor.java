import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import soot.Body;
import soot.BodyTransformer;
import soot.PackManager;
import soot.Transform;
import soot.Unit;

/**
 * Extract usages of sources and sinks based on the provided lists of sources and sinks
 *
 * @author Linghui Luo
 */
public class SourcesSinksExtractor {

  protected static Set<String> sources = new HashSet<>();
  protected static Set<String> sinks = new HashSet<>();

  public static void analyze(String sourcesPath, String sinksPath, AppInfo appInfo) {
    sources = new HashSet<>();
    sinks = new HashSet<>();
    parse(sourcesPath, sources);
    parse(sinksPath, sinks);
    searchSourcesAndSinks(appInfo);
  }

  private static void searchSourcesAndSinks(AppInfo appInfo) {
    BodyTransformer t =
        new BodyTransformer() {

          @Override
          protected void internalTransform(Body b, String phaseName, Map<String, String> options) {
            if (b.getMethod().getDeclaringClass().isApplicationClass()) {
              for (Unit u : b.getUnits()) {
                String code = u.toString();
                String matchedSource = match(code, sources);
                if (matchedSource != null) {
                  if (!appInfo.sources.containsKey(matchedSource)) {
                    appInfo.sources.put(matchedSource, new HashSet<>());
                  }
                  appInfo.sources.get(matchedSource).add(new Location(b.getMethod(), u));
                }
                String matchedSink = match(code, sinks);

                if (matchedSink != null) {
                  if (!appInfo.sinks.containsKey(matchedSink)) {
                    appInfo.sinks.put(matchedSink, new HashSet<>());
                  }
                  appInfo.sinks.get(matchedSink).add(new Location(b.getMethod(), u));
                }
              }
            }
          }
        };
    PackManager.v().getPack("jtp").add(new Transform("jtp.searchSourcesSinks", t));
  }

  private static String match(String code, Set<String> apis) {
    for (String api : apis) {
      if (code.contains(api)) {
        return api;
      }
    }
    return null;
  }

  public static void parse(String path, Set<String> apis) {
    File file = new File(path);
    BufferedReader rd;
    try {
      rd = new BufferedReader(new FileReader(file));
      String line = null;

      while ((line = rd.readLine()) != null) {
        if (line.startsWith("<")) {
          String signature = line.split(">")[0] + ">";
          apis.add(signature);
        }
      }

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
