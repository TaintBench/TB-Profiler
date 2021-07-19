import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import soot.Body;
import soot.BodyTransformer;
import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.Transform;
import soot.options.Options;

/** @author Linghui Luo */
public class Util {

  public static void main(String... args) throws FileNotFoundException {
    runSootOnApk();
  }

  public static void runSootOnJar() throws FileNotFoundException {
    String jar = "E:\\Git\\androidPlatforms\\android-29\\android.jar";
    G.reset();
    Options.v().set_no_bodies_for_excluded(true);
    Options.v().set_allow_phantom_refs(true);
    Options.v().setPhaseOption("jb", "use-original-names:true");
    Options.v().set_process_dir(Collections.singletonList(jar));
    Options.v().set_prepend_classpath(true);
    Scene.v().loadNecessaryClasses();
    StringBuilder sb = new StringBuilder();
    for (SootClass cl : Scene.v().getClasses()) {
      sb.append(cl.toString().replace("$", ".") + "\n");
    }
    PrintWriter pw = new PrintWriter(new File("androidAPIs.csv"));
    pw.println(sb.toString());
    pw.close();
  }

  public static void runSootOnApk() {
    String apk =
        "E:\\Git\\Github\\taintbench\\taint-benchmark\\apps\\android\\backflash\\backflash.apk";
    String platformJars = "E:\\Git\\androidPlatforms";
    G.reset();
    Options.v().set_no_bodies_for_excluded(true);
    Options.v().set_allow_phantom_refs(true);
    Options.v().set_process_dir(Collections.singletonList(apk));
    Options.v().set_android_jars(platformJars);
    Options.v().set_src_prec(Options.src_prec_apk);
    // Options.v().set_keep_line_number(true);
    Options.v().set_process_multiple_dex(true);
    // Options.v().setPhaseOption("jb", "use-original-names:true");
    ArrayList<String> excluded = new ArrayList<>();
    excluded.add("android.support.*");
    Options.v().set_exclude(excluded);
    Options.v().set_soot_classpath(Scene.v().getAndroidJarPath(platformJars, apk));
    Options.v().set_force_overwrite(true);
    Options.v().set_output_format(Options.output_format_none);
    Scene.v().loadNecessaryClasses();

    BodyTransformer t =
        new BodyTransformer() {

          @Override
          protected void internalTransform(Body b, String phaseName, Map<String, String> options) {
            System.out.println(b.getMethod().getDeclaringClass().getPackageName());
          }
        };
    PackManager.v().getPack("jtp").add(new Transform("jtp.output", t));
    PackManager.v().runBodyPacks();
  }
}
