import java.io.File;
import java.io.IOException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Main {
  public static void main(String[] args) throws ParseException, IOException {
    Options options = new Options();
    options.addOption("h", "help", false, "Print this message");
    options.addOption(
        "statSS", "statSourcesSinks", true, "Run SourceSinkStatCalculator with given path");
    options.addOption(
        "statAttr", "statAttributes", true, "Run AttributeStatCalculator with given path");
    options.addOption("apk", "apkPath", true, "The path to apk file");
    options.addOption(
        "f", "findings", true, "The path to the findings of the apk file in TAF-format");
    options.addOption("p", "androidPlatform", true, "The path to android platform jars");
    options.addOption("c", "config", true, "The path to configuration files");
    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = parser.parse(options, args);
    HelpFormatter helper = new HelpFormatter();
    String cmdLineSyntax =
        "\n1. profile an apk:\n -apk <apk> -f <TAF-file>  -p <android platform jars> -c <path to configuration files>\n"
            + "2. calculate statistics of sources and sinks:\n -statSS <apks> -p <android platform jars> -c <path to configuration files>\n"
            + "3. calculate statistics of attributes:\n -statAttr <TAF-files>\n\n";
    if (cmd.hasOption('h') && cmd.getOptions().length == 0) {
      helper.printHelp(cmdLineSyntax, options);
      return;
    }
    if (cmd.hasOption("statSS") && cmd.hasOption("statAttr")) {
      helper.printHelp(cmdLineSyntax, options);
      return;
    }
    String outputDirName = "output";
    File output = new File(outputDirName);
    if (!output.exists()) output.mkdir();
    String androidJarPath = null;
    String configPath = null;
    if (cmd.hasOption("statSS") && cmd.hasOption("p") && cmd.hasOption("c")) {
      String apksDir = cmd.getOptionValue("statSS");
      androidJarPath = cmd.getOptionValue("p");
      configPath = cmd.getOptionValue("c");
      SourceSinkStatCalculator.run(apksDir, androidJarPath, configPath);
      return;
    }
    if (cmd.hasOption("statAttr")) {
      String findingsDir = cmd.getOptionValue("statAttr");
      AttributeStatCalculator.run(findingsDir);
      return;
    }
    if (!cmd.hasOption("c")
        || !cmd.hasOption("apk")
        || !cmd.hasOption("f")
        || !cmd.hasOption("p")) {
      helper.printHelp(cmdLineSyntax, options);
      return;
    }
    String apkPath = null;
    String findingsPath = null;
    if (cmd.hasOption("apk") && cmd.hasOption("f") && cmd.hasOption("p") && cmd.hasOption("c")) {
      apkPath = cmd.getOptionValue("apk");
      findingsPath = cmd.getOptionValue("f");
      androidJarPath = cmd.getOptionValue("p");
      configPath = cmd.getOptionValue("c");
      APISignatures.load(configPath);
    }
    TaintProfiler.run(apkPath, findingsPath, configPath, androidJarPath, outputDirName);
  }
}
