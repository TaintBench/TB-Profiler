import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import java.util.stream.Collectors;

/**
 * contains APIs considered for detecting attributes by {@link AttributesExtractor}:
 *
 * @author Linghui Luo
 */
public class APISignatures {

  public static String[] reflectionAPIs = {};

  public static String[] collectionAPIs = {};

  public static String[] payloadAPIs = {};

  public static String[] appendToStringAPIs = {
    "java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)"
  };

  public static String[] interComponentCommunicationAPIs = {"android.content.Intent"};

  public static String[] threadingAPIs = {};

  public static boolean contains(String[] listOfAPIs, String str) {
    for (String s : listOfAPIs) {
      if (str.contains(s)) return true;
    }
    return false;
  }

  public static void load(String configPath) {
    String[] APIs = readAPIs(new File(configPath + File.separator + "ReflectionAPIs.txt"));
    if (APIs != null) reflectionAPIs = APIs;
    APIs = readAPIs(new File(configPath + File.separator + "PayloadAPIs.txt"));
    if (APIs != null) payloadAPIs = APIs;
    APIs = readAPIs(new File(configPath + File.separator + "ThreadingAPIs.txt"));
    if (APIs != null) threadingAPIs = APIs;
    APIs = readAPIs(new File(configPath + File.separator + "CollectionAPIs.txt"));
    if (APIs != null) collectionAPIs = APIs;
  }

  private static String[] readAPIs(File file) {
    if (file.exists()) {
      BufferedReader reader;
      try {
        reader = new BufferedReader(new FileReader(file));
        List<String> APIs = reader.lines().collect(Collectors.toList());
        String[] arr = new String[APIs.size()];
        APIs.toArray(arr);
        return arr;
      } catch (FileNotFoundException e) {
        System.err.println(file + " doesn't exist, use default APIs");
        e.printStackTrace();
      }
    }
    return null;
  }
}
