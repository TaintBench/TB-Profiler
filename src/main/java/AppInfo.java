import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/** @author Linghui Luo */
public class AppInfo {
  protected String id;
  protected String filePath;
  protected String fileName = "UNKNOWN";
  protected String packageName = "UNKNOWN";
  protected int targetSdk = -1;
  protected int minSdk = -1;
  protected int maxSdk = -1;
  protected String mainActivity = "UNKNOWN";

  protected Set<String> activities = new HashSet<>();
  protected Set<String> services = new HashSet<>();
  protected Set<String> receivers = new HashSet<>();
  protected Set<String> providers = new HashSet<>();
  protected Set<String> permissions = new HashSet<>();

  protected HashMap<String, Set<Location>> sources = new HashMap<>();
  protected HashMap<String, Set<Location>> sinks = new HashMap<>();
  protected SourceSinkStat stat;

  public SourceSinkStat getSourceSinkStat() {
    if (stat != null) return stat;
    {
      stat = new SourceSinkStat();
      stat.sourceAPIs = sources.keySet().size();
      stat.soucesUsage = 0;
      for (Set<Location> ls : sources.values()) stat.soucesUsage += ls.size();
      stat.sinkAPIs = sinks.keySet().size();
      stat.sinksUsage = 0;
      for (Set<Location> ls : sinks.values()) stat.sinksUsage += ls.size();
      return stat;
    }
  }

  public String toMarkdownString() {
    StringBuilder str = new StringBuilder();
    str.append("# Installation:\n");
    str.append("![ICON](icon.png)\n");

    str.append("# General Information:\n");
    str.append("- **fileName**: " + fileName + "\n");
    str.append("- **packageName**: " + packageName + "\n");
    str.append("- **targetSdk**: " + targetSdk + "\n");
    str.append("- **minSdk**: " + minSdk + "\n");
    str.append("- **maxSdk**: " + maxSdk + "\n");
    str.append("- **mainActivity**: " + mainActivity + "\n");

    str.append("# Behavior Information:\n");
    str.append("## Activities:\n");
    str.append("TODO\n");
    str.append("## BroadcastReceivers:\n");
    str.append("TODO\n");
    str.append("## Services:\n");
    str.append("TODO\n");

    str.append("# Detail Information:\n");
    if (activities.size() > 0) {
      str.append("## Activities: " + activities.size() + "\n");
      for (String a : activities) {
        str.append("\t" + a + "\n");
      }
    }
    if (services.size() > 0) {
      str.append("## Services: " + services.size() + "\n");
      for (String a : services) {
        str.append("\t" + a + "\n");
      }
    }
    if (receivers.size() > 0) {
      str.append("## Receivers: " + receivers.size() + "\n");
      for (String a : receivers) {
        str.append("\t" + a + "\n");
      }
    }
    if (providers.size() > 0) {
      str.append("## Providers: " + providers.size() + "\n");
      for (String a : providers) {
        str.append("\t-" + a + "\n");
      }
    }

    if (permissions.size() > 0) {
      str.append("## Permissions: " + permissions.size() + "\n");
      for (String a : permissions) {
        str.append("\t" + a + "\n");
      }
    }

    if (sources.size() > 0) {
      str.append("## Sources: " + sources.size() + "\n");
      for (String a : sources.keySet()) {
        str.append("\t" + a + ": " + sources.get(a).size() + "\n");
      }
    }

    if (sinks.size() > 0) {
      str.append("## Sinks: " + sinks.size() + "\n");
      for (String a : sinks.keySet()) {
        str.append("\t" + a + ": " + sinks.get(a).size() + "\n");
      }
    }
    return str.toString();
  }

  public String toString() {
    StringBuilder str = new StringBuilder();
    str.append("fileName;" + fileName + "\n");
    str.append("packageName;" + packageName + "\n");
    str.append("targetSdk;" + targetSdk + "\n");
    str.append("minSdk;" + minSdk + "\n");
    str.append("maxSdk;" + maxSdk + "\n");
    str.append("mainActivity;" + mainActivity + "\n");
    if (activities.size() > 0) {
      str.append("Activities;" + activities.size() + "\n");
      for (String a : activities) {
        str.append(";" + a + "\n");
      }
    }
    if (services.size() > 0) {
      str.append("Services;" + services.size() + "\n");
      for (String a : services) {
        str.append(";" + a + "\n");
      }
    }
    if (receivers.size() > 0) {
      str.append("Receivers;" + receivers.size() + "\n");
      for (String a : receivers) {
        str.append(";" + a + "\n");
      }
    }
    if (providers.size() > 0) {
      str.append("Providers;" + providers.size() + "\n");
      for (String a : providers) {
        str.append(";" + a + "\n");
      }
    }

    if (permissions.size() > 0) {
      str.append("Permissions;" + permissions.size() + "\n");
      for (String a : permissions) {
        str.append(";" + a + "\n");
      }
    }

    if (sources.size() > 0) {
      str.append("Sources;" + sources.size() + "\n");
      for (String a : sources.keySet()) {
        str.append(";;" + a + ";" + sources.get(a).size() + "\n");
        for (Location l : sources.get(a)) {
          str.append(";;;;" + l.toString() + "\n");
        }
      }
    }

    if (sinks.size() > 0) {
      str.append("Sinks;" + sinks.size() + "\n");
      for (String a : sinks.keySet()) {
        str.append(";;" + a + ";" + sinks.get(a).size() + "\n");
        for (Location l : sinks.get(a)) {
          str.append(";;;;" + l.toString() + "\n");
        }
      }
    }
    return str.toString();
  }

  public void output(String benchName) {
    File file = new File("output");
    if (!file.exists()) {
      file.mkdir();
    }
    outputReport();
    outputCSV(benchName);
  }

  protected void outputReport() {
    File outputFile =
        new File("output" + File.separator + id + "_" + fileName.replace(".apk", "_profile.csv"));
    try (PrintWriter pw = new PrintWriter(outputFile)) {
      pw.println(this.toString());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected void outputCSV(String benchName) {
    try {
      File file = new File("output" + File.separator + benchName + "_profiles.csv");
      StringBuilder str = new StringBuilder();
      String sep = ";";
      if (!file.exists()) {
        str.append("id");
        str.append(sep);
        str.append("filePath");
        str.append(sep);
        str.append("fileName");
        str.append(sep);
        str.append("packageName");
        str.append(sep);
        str.append("targetSdk");
        str.append(sep);
        str.append("minSdk");
        str.append(sep);
        str.append("maxSdk");
        str.append(sep);
        str.append("mainActivity");
        str.append(sep);
        str.append("#activities");
        str.append(sep);
        str.append("#services");
        str.append(sep);
        str.append("#receivers");
        str.append(sep);
        str.append("#providers");
        str.append(sep);
        str.append("#permissions");
        str.append(sep);
        str.append("#sources");
        str.append(sep);
        str.append("#sourceUsage");
        str.append(sep);
        str.append("#sinks");
        str.append(sep);
        str.append("#sinkUsage");
        str.append("\n");
      }
      FileWriter outputWriter = new FileWriter(file, true);
      PrintWriter pw = new PrintWriter(outputWriter);
      str.append(id);
      str.append(sep);
      str.append(filePath);
      str.append(sep);
      str.append(fileName);
      str.append(sep);
      str.append(packageName);
      str.append(sep);
      str.append(targetSdk);
      str.append(sep);
      str.append(minSdk);
      str.append(sep);
      str.append(maxSdk);
      str.append(sep);
      str.append(mainActivity);
      str.append(sep);
      str.append(activities.size());
      str.append(sep);
      str.append(services.size());
      str.append(sep);
      str.append(receivers.size());
      str.append(sep);
      str.append(providers.size());
      str.append(sep);
      str.append(permissions.size());
      str.append(sep);
      str.append(sources.size());
      str.append(sep);
      str.append(getSourceSinkStat().soucesUsage);
      str.append(sep);
      str.append(sinks.size());
      str.append(sep);
      str.append(getSourceSinkStat().sinksUsage);
      pw.println(str.toString());
      pw.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
