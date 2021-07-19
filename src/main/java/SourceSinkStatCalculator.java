import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.util.Precision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.PackManager;

/**
 * Compute usage of sources and sinks.
 *
 * @author Linghui Luo
 */
public class SourceSinkStatCalculator {
  public static final Logger LOGGER = LoggerFactory.getLogger(SourceSinkStatCalculator.class);

  public static void run(String apksDir, String androidJarPath, String configPath)
      throws IOException {
    LOGGER.info("Running " + SourceSinkStatCalculator.class.toString());
    String sourcesPath = configPath + File.separator + "merged_sources.txt";
    String sinksPath = configPath + File.separator + "merged_sinks.txt";
    File dir = new File(apksDir);
    List<String> apks = new ArrayList<>();
    Files.walk(Paths.get(dir.toURI()))
        .filter(Files::isRegularFile)
        .forEach(
            f -> {
              if (f.toString().endsWith(".apk")) {
                apks.add(f.toFile().getAbsolutePath());
              }
            });
    calculate(dir.getName(), apks, androidJarPath, sourcesPath, sinksPath);
  }

  static boolean exists(String fileName) {
    String target = "output";
    for (File f : new File(target).listFiles())
      if (f.getName().endsWith(fileName.replace(".apk", "_profile.csv"))) return true;
    return false;
  }

  static void calculate(
      String benchName,
      List<String> apks,
      String androidJarPath,
      String sourcesPath,
      String sinksPath)
      throws IOException {
    HashMap<String, SourceSinkStat> stats = new HashMap<>();
    int id = 1;
    int failed = 1;
    for (String apkPath : apks) {
      try {
        AppInfo appInfo = new AppInfo();
        appInfo.id = benchName + "_" + id;
        appInfo.filePath = apkPath;
        LOGGER.info("Processing " + apkPath);
        GeneralInformationExtractor.analyze(apkPath, appInfo);
        if (!exists(appInfo.fileName)) {
          SootRunner.initSoot(apkPath, androidJarPath);
          SourcesSinksExtractor.analyze(sourcesPath, sinksPath, appInfo);
          PackManager.v().runBodyPacks();
          SourceSinkStat stat = appInfo.getSourceSinkStat();
          LOGGER.info(
              new File(apkPath).getName() + ";" + appInfo.packageName + ";" + stat.toString());
          stats.put(appInfo.id, stat);
          appInfo.output(benchName);
        }
        id++;
      } catch (Exception e) {
        LOGGER.info("Failed " + failed + " " + apkPath);
        failed++;
      }
    }
    double[] sourceAPIs = new double[stats.size()];
    double[] sourcesUsage = new double[stats.size()];
    double[] sinkAPIs = new double[stats.size()];
    double[] sinksUsage = new double[stats.size()];
    int i = 0;
    for (SourceSinkStat stat : stats.values()) {
      sourceAPIs[i] = stat.sourceAPIs;
      sourcesUsage[i] = stat.soucesUsage;
      sinkAPIs[i] = stat.sinkAPIs;
      sinksUsage[i] = stat.sinksUsage;
      i++;
    }
    File file = new File("output" + File.separator + benchName + "_usageOfSourcesSinks.csv");
    FileWriter outputWriter = new FileWriter(file, true);
    PrintWriter pw = new PrintWriter(outputWriter);
    StringBuilder str = new StringBuilder();
    str.append("metric;sourceAPIs;sourcesUsage;sinkAPIs;sinksUsage");
    str.append("\nmin");
    str.append(";" + StatUtils.min(sourceAPIs));
    str.append(";" + StatUtils.min(sourcesUsage));
    str.append(";" + StatUtils.min(sinkAPIs));
    str.append(";" + StatUtils.min(sinksUsage));
    str.append("\nmax");
    str.append(";" + StatUtils.max(sourceAPIs));
    str.append(";" + StatUtils.max(sourcesUsage));
    str.append(";" + StatUtils.max(sinkAPIs));
    str.append(";" + StatUtils.max(sinksUsage));
    str.append("\nmean");
    str.append(";" + Precision.round(StatUtils.geometricMean(sourceAPIs), 2));
    str.append(";" + Precision.round(StatUtils.geometricMean(sourcesUsage), 2));
    str.append(";" + Precision.round(StatUtils.geometricMean(sinkAPIs), 2));
    str.append(";" + Precision.round(StatUtils.geometricMean(sinksUsage), 2));
    str.append("\navg");
    str.append(";" + Precision.round(StatUtils.mean(sourceAPIs), 2));
    str.append(";" + Precision.round(StatUtils.mean(sourcesUsage), 2));
    str.append(";" + Precision.round(StatUtils.mean(sinkAPIs), 2));
    str.append(";" + Precision.round(StatUtils.mean(sinksUsage), 2));
    pw.println(str.toString());
    pw.close();
  }
}
