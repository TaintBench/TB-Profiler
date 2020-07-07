import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Profiling an apk
 *
 * @author Linghui Luo
 */
public class TaintProfiler {
  private static boolean writeToFile = true;
  public static final Logger LOGGER = LoggerFactory.getLogger(TaintProfiler.class);

  public static void run(
      String apkPath,
      String findingsPath,
      String configPath,
      String androidJarPath,
      String outputDirName) {
    LOGGER.info("Running " + TaintProfiler.class.toString());
    String sourcesPath = configPath + File.separator + "merged_sources.txt";
    String sinksPath = configPath + File.separator + "merged_sinks.txt";
    String callbacksPath = configPath + File.separator + "AndroidCallbacks.txt";
    if (androidJarPath != null & apkPath != null && findingsPath != null && configPath != null)
      if (new File(androidJarPath).exists()
          && new File(apkPath).exists()
          && new File(findingsPath).exists()
          && new File(configPath).exists()) {
        AppInfo appInfo = new AppInfo();
        File apk = new File(apkPath);
        TaintProfiler.LOGGER.info("Processing " + apk.getName());
        TaintProfiler.LOGGER.info(
            "---------------------------------------------------------------------");
        GeneralInformationExtractor.analyze(apkPath, appInfo);
        SootRunner.runExtractors(
            apkPath, androidJarPath, sourcesPath, sinksPath, appInfo, findingsPath, callbacksPath);
        TaintProfiler.LOGGER.info(
            "---------------------------------------------------------------------");
        if (writeToFile) {
          String outputMdFile =
              new File(outputDirName).getAbsolutePath()
                  + File.separator
                  + apk.getName().replace(".apk", "_summary.md");
          TaintProfiler.LOGGER.info("Writing summary into " + outputMdFile);
          writeAppInfoToFile(outputMdFile, appInfo.toMarkdownString());
        }
      }
  }

  private static void writeAppInfoToFile(String fileName, String profile) {
    try {
      FileWriter fileWriter = new FileWriter(fileName);
      PrintWriter printer = new PrintWriter(fileWriter);
      printer.print(profile);
      printer.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
