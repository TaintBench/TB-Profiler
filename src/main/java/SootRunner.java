import java.util.ArrayList;
import java.util.Collections;
import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.options.Options;

/** @author Linghui Luo */
public class SootRunner {

  public static void runExtractors(
      String apkPath,
      String androidJarPath,
      String sourcesPath,
      String sinksPath,
      AppInfo appInfo,
      String findings,
      String callbacksPath) {
    initSoot(apkPath, androidJarPath);
    SourcesSinksExtractor.analyze(sourcesPath, sinksPath, appInfo);
    AttributesExtractor.analyze(findings, callbacksPath, appInfo.fileName);
    run();
  }

  protected static void initSoot(String apk, String androidJarPath) {
    G.reset();
    Options.v().set_no_bodies_for_excluded(true);
    Options.v().set_allow_phantom_refs(true);
    Options.v().set_process_dir(Collections.singletonList(apk));
    Options.v().set_android_jars(androidJarPath);
    Options.v().set_src_prec(Options.src_prec_apk);
    Options.v().set_process_multiple_dex(true);
    ArrayList<String> excluded = new ArrayList<>();
    excluded.add("android.support.*");
    Options.v().set_exclude(excluded);
    Options.v().set_soot_classpath(Scene.v().getAndroidJarPath(androidJarPath, apk));
    Options.v().set_force_overwrite(true);
    Options.v().set_output_format(Options.output_format_none);
    Scene.v().loadNecessaryClasses();
  }

  protected static void run() {
    PackManager.v().runBodyPacks();
  }
}
