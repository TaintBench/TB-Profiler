import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

public class HelpMethods {

  public static void main(String[] args) throws FileNotFoundException {
    // sources
    File swanDir = new File("config/swan");
    File susiSouceFile = new File("config/Susi_Sources.txt");
    File susiSinkFile = new File("config/Susi_Sinks.txt");
    Set<String> sources = new HashSet<>();
    SourcesSinksExtractor.parse(susiSouceFile.getAbsolutePath(), sources);
    Set<String> sinks = new HashSet<>();
    System.out.println("#sources: " + sources.size());
    SourcesSinksExtractor.parse(susiSinkFile.getAbsolutePath(), sinks);
    System.out.println("#sinks: " + sinks.size());
    for (File dir : swanDir.listFiles()) {
      if (dir.isDirectory()) {
        {
          File targetSourceFile =
              new File(
                  dir.getAbsolutePath()
                      + File.separator
                      + "txt"
                      + File.separator
                      + "output_source.txt");
          System.err.println("\nprocessing " + targetSourceFile.getAbsolutePath());
          SourcesSinksExtractor.parse(targetSourceFile.getAbsolutePath(), sources);
          System.out.println("#sources: " + sources.size());
          PrintWriter pw = new PrintWriter(new File("config/merged_sources.txt"));
          for (String source : sources) pw.append(source + "\n");
          pw.close();
        }
        {
          File targetSinkFile =
              new File(
                  dir.getAbsolutePath()
                      + File.separator
                      + "txt"
                      + File.separator
                      + "output_sink.txt");
          System.err.println("\nprocessing " + targetSinkFile.getAbsolutePath());
          SourcesSinksExtractor.parse(targetSinkFile.getAbsolutePath(), sinks);
          System.out.println("#sinks: " + sinks.size());
          PrintWriter pw = new PrintWriter(new File("config/merged_sinks.txt"));
          for (String sink : sinks) pw.append(sink + "\n");
          pw.close();
        }
      }
    }
  }
}
