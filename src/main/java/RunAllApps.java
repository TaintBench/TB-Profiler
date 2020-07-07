import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import java.io.File;
import java.io.IOException;
import org.apache.commons.cli.ParseException;

public class RunAllApps {
  public static void main(String[] args)
      throws JsonIOException, JsonSyntaxException, IOException, ParseException {
    String dir = "E:\\Git\\Github\\taintbench\\taint-benchmark\\apps\\android";
    String androidPlatform = "E:\\Git\\androidPlatforms";
    String config = "E:\\Git\\Github\\taintbench\\TaintProfiler\\config";
    File dirFile = new File(dir);
    for (File appDir : dirFile.listFiles()) {
      if (appDir.isDirectory() && !appDir.getAbsolutePath().contains("beita_com_beita_contact")) {
        String[] a = {
          "-dir", appDir.getAbsolutePath(), "-p", androidPlatform, "-c", config, "-o", "output"
        };
        // TaintProfiler.main(a);
      }
    }
  }
}
