import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.io.IOUtils;
import pxb.android.axml.AxmlReader;
import pxb.android.axml.AxmlVisitor;
import pxb.android.axml.NodeVisitor;

/**
 * Extract information from AndroidManifest.xml
 *
 * @author Linghui Luo
 */
public class GeneralInformationExtractor {

  public static void analyze(String apkPath, AppInfo appInfo) {
    appInfo.fileName = new File(apkPath).getName();
    InputStream manifestIS = null;
    ZipFile apkFile = null;
    try {
      try {
        apkFile = new ZipFile(apkPath);
        for (Enumeration<? extends ZipEntry> entries = apkFile.entries();
            entries.hasMoreElements(); ) {
          ZipEntry entry = entries.nextElement();
          String entryName = entry.getName();
          if (entryName.equals("AndroidManifest.xml")) {
            manifestIS = apkFile.getInputStream(entry);
            break;
          }
        }
      } catch (Exception e) {
        System.err.println("Error when looking for manifest in apk: " + apkPath);
      }

      if (manifestIS == null) {
        System.err.println("Could not find sdk version in Android manifest!");
      }
      if (manifestIS != null)
        try {
          AxmlReader xmlReader = new AxmlReader(IOUtils.toByteArray(manifestIS));
          xmlReader.accept(
              new AxmlVisitor() {

                private String nodeName = null;
                private String parentNodeName = null;
                private String parentNodeValue = null;

                @Override
                public void attr(String ns, String name, int resourceId, int type, Object obj) {
                  super.attr(ns, name, resourceId, type, obj);
                  // System.out.print("nodeName " + nodeName + "\t");
                  // System.out.print("ns " + ns + "\t");
                  // System.out.print("name " + name + "\t");
                  // System.out.println("obj " + obj.toString());
                  if (nodeName != null && name != null) {
                    if (nodeName.equals("manifest")) {
                      if (name.equals("package")) appInfo.packageName = obj.toString();
                    }
                    if (nodeName.equals("uses-sdk")) {
                      // Obfuscated APKs often remove the attribute names and use the resourceId
                      // instead
                      // Therefore it is better to check for both variants
                      if (name.equals("targetSdkVersion")
                          || (name.equals("") && resourceId == 16843376)) {
                        appInfo.targetSdk = Integer.valueOf(obj.toString());
                      } else if (name.equals("minSdkVersion")
                          || (name.equals("") && resourceId == 16843276)) {
                        appInfo.minSdk = Integer.valueOf(obj.toString());
                      } else if (name.equals("maxSdkVersion")
                          || (name.equals("") && resourceId == 16843377)) {
                        appInfo.maxSdk = Integer.valueOf(obj.toString());
                      }
                    }
                    if (nodeName.equals("activity")) {
                      if (name.equals("name")) {
                        appInfo.activities.add(obj.toString());
                        parentNodeName = "activity";
                        parentNodeValue = obj.toString();
                      }
                    }
                    if (nodeName.equals("service")) {
                      if (name.equals("name")) appInfo.services.add(obj.toString());
                    }

                    if (nodeName.equals("receiver")) {
                      if (name.equals("name")) appInfo.receivers.add(obj.toString());
                    }

                    if (nodeName.equals("provider")) {
                      if (name.equals("provider")) appInfo.providers.add(obj.toString());
                    }

                    if (nodeName.equals("action")
                        && obj.toString().equals("android.intent.action.MAIN")) {
                      if (parentNodeName != null && parentNodeName.equals("activity"))
                        appInfo.mainActivity = parentNodeValue;
                    }

                    if (nodeName.equals("uses-permission")) {
                      if (name.equals("name")) {
                        appInfo.permissions.add(obj.toString());
                      }
                    }
                  }
                }

                @Override
                public NodeVisitor child(String ns, String name) {
                  // update the xml node name
                  nodeName = name;
                  return this;
                }
              });
        } catch (Exception e) {
          e.printStackTrace();
        }
    } finally {
      if (apkFile != null) {
        try {
          apkFile.close();
        } catch (IOException e) {
          throw new RuntimeException("Error when looking for manifest in apk: " + e);
        }
      }
    }
  }
}
