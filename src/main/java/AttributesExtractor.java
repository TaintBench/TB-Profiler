import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import soot.SootClass;
import soot.Unit;
import soot.Value;
import soot.jimple.StaticFieldRef;
import soot.jimple.internal.JArrayRef;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JIfStmt;
import soot.jimple.internal.JInstanceFieldRef;
import soot.jimple.internal.JInvokeStmt;
import soot.util.Chain;

/** @author Linghui Luo */
public class AttributesExtractor {

  private String[] reservedAttrs = {
    "nonStaticField",
    "partialFlow",
    "reflection",
    "callbacks",
    "interComponentCommunication",
    "staticField",
    "lifecycle",
    "array",
    "collections",
    "payload",
    "interAppCommunication",
    "threading",
    "implicitFlows",
    "pathConstraints",
    "appendToString"
  };

  static List<FindingInfo> findings = new ArrayList<>();
  static List<String> callbacks = new ArrayList<>();
  static Set<String> activityCallbacks = new HashSet<>();
  static Set<String> serviceCallbacks = new HashSet<>();
  static Set<String> broadcastreceiverCallbacks = new HashSet<>();
  static String apkFileName = null;

  public static void analyze(String file, String callbacksPath, String name) {
    try {
      apkFileName = name;
      findings = new ArrayList<>();
      callbacks = new ArrayList<>();
      setLifecycleCallbacks();
      parseCallbacks(callbacksPath);
      parseFindings(file);
      extractJimpleAndCheck();
    } catch (JsonIOException | JsonSyntaxException | FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  private static void setLifecycleCallbacks() {
    activityCallbacks.add("onCreate");
    activityCallbacks.add("onStart");
    activityCallbacks.add("onResume");
    activityCallbacks.add("onPause");
    activityCallbacks.add("onStop");
    activityCallbacks.add("onRestart");
    activityCallbacks.add("onDestroy");
    activityCallbacks.add("onSaveInstanceState");

    serviceCallbacks.add("onCreate");
    serviceCallbacks.add("onStartCommand");
    serviceCallbacks.add("onStart");
    serviceCallbacks.add("onDestroy");
    serviceCallbacks.add("onBind");
    serviceCallbacks.add("stopSelf");
    serviceCallbacks.add("onRebind");
    serviceCallbacks.add("onUnBind");

    broadcastreceiverCallbacks.add("onReceive");
  }

  private static void parseCallbacks(String callbacksPath) {
    BufferedReader bufferedReader;
    try {
      bufferedReader = new BufferedReader(new FileReader(callbacksPath));
      bufferedReader.lines().forEach(line -> callbacks.add(line));
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  private static void extractJimpleAndCheck() {
    StringBuilder output = new StringBuilder();
    for (FindingInfo finding : findings) {
      List<LocationInJimple> locations = finding.getLocationsInJimple();
      Set<String> attributesOfFinding = new HashSet<>();
      Witness witnesses = new Witness();
      HashMap<String, Set<String>> lifecycles = new HashMap<>();
      for (LocationInJimple l : locations) {
        if (containsCallback(witnesses, l)) attributesOfFinding.add("callbacks");
        if (containsThread(witnesses, l)) attributesOfFinding.add("threading");
        checkStatements(witnesses, finding, l, attributesOfFinding);
        checkLifeCycle(witnesses, l, lifecycles);
      }
      if (lifecycles.size() > 0) attributesOfFinding.add("lifecycle");
      StringBuilder s = new StringBuilder("Detected finding " + finding.ID + " has attributes: ");
      for (String a : attributesOfFinding) s.append(a + " | ");
      TaintProfiler.LOGGER.debug(s.toString());
      for (String a : attributesOfFinding) {
        if (!finding.attributes.contains(a)) {
          String print =
              "Detected attribute ["
                  + a
                  + "] was not logged for finding "
                  + finding.ID
                  + ". Please check witness: ";
          for (String w : witnesses.get(a)) {
            print += w + " ";
          }
          TaintProfiler.LOGGER.info(print);
          output.append(print + "\n");
        }
      }
      for (String a : finding.attributes) {
        if (!attributesOfFinding.contains(a)) {
          String print =
              "Logged attribute ["
                  + a
                  + "] was not detected. Please check finding "
                  + finding.ID
                  + ".";
          TaintProfiler.LOGGER.info(print);
          output.append(print + "\n");
        }
      }
      TaintProfiler.LOGGER.info(
          "---------------------------------------------------------------------");
      output.append("---------------------------------------------------------------------\n");
    }
    writeoutput(output);
  }

  private static void writeoutput(StringBuilder output) {
    try {
      String file =
          "output" + File.separator + apkFileName.replace(".apk", "_attributes_check.txt");
      FileWriter fileWriter = new FileWriter(file);
      PrintWriter printer = new PrintWriter(fileWriter);
      printer.print(output.toString());
      printer.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static void checkLifeCycle(
      Witness witnesses, LocationInJimple location, HashMap<String, Set<String>> lifecycles) {
    if (location.method == null) {
      TaintProfiler.LOGGER.warn("method location " + location.ID + " was not found");
      return;
    }
    String methodName = location.method.getName();
    String declaringClass = location.method.getDeclaringClass().getName();
    String superClassName = location.klass.getSuperclass().getName();
    if (superClassName.equals("android.app.Activity")) {
      if (activityCallbacks.contains(methodName)) {
        Set<String> ms = new HashSet<>();
        if (lifecycles.containsKey(declaringClass)) {
          ms = lifecycles.get(declaringClass);
        }
        ms.add(methodName);
        lifecycles.put(declaringClass, ms);
        witnesses.put("lifecycle", location.ID);
      }
    }
    if (superClassName.equals("android.app.Service")) {
      if (serviceCallbacks.contains(methodName)) {
        Set<String> ms = new HashSet<>();
        if (lifecycles.containsKey(declaringClass)) {
          ms = lifecycles.get(declaringClass);
        }
        ms.add(methodName);
        lifecycles.put(declaringClass, ms);
        witnesses.put("lifecycle", location.ID);
      }
    }
    if (superClassName.equals("android.content.BroadcastReceiver")) {
      if (broadcastreceiverCallbacks.contains(methodName)) {
        Set<String> ms = new HashSet<>();
        if (lifecycles.containsKey(declaringClass)) {
          ms = lifecycles.get(declaringClass);
        }
        ms.add(methodName);
        lifecycles.put(declaringClass, ms);
        witnesses.put("lifecycle", location.ID);
      }
    }
  }

  private static Set<String> checkStatements(
      Witness witnesses, FindingInfo finding, LocationInJimple location, Set<String> attributes) {
    for (Unit s : location.statements) {
      if (s instanceof JAssignStmt) {
        JAssignStmt assignStmt = (JAssignStmt) s;
        Value left = assignStmt.getLeftOp();
        Value right = assignStmt.getRightOp();
        if (left instanceof JInstanceFieldRef) {
          boolean statisfied = false;
          if (location.isSource()) {
            if (finding.isLeftSideOfSourceField()) statisfied = true;
          } else {
            statisfied = true;
          }
          if (statisfied) {
            attributes.add("nonStaticField");
            witnesses.put(
                "nonStaticField",
                location.ID
                    + "\n\t\tvar: "
                    + left.toString()
                    + "\n\t\tstmt: "
                    + finding.getStatement(location.ID));
          }
        }
        if (left instanceof StaticFieldRef) {
          boolean statisfied = false;
          if (location.isSource()) {
            if (finding.isLeftSideOfSourceField()) statisfied = true;
          } else {
            statisfied = true;
          }
          if (statisfied) {
            attributes.add("staticField");
            witnesses.put(
                "staticField",
                location.ID
                    + "\n\t\tvar: "
                    + left.toString()
                    + "\n\t\tstmt: "
                    + finding.getStatement(location.ID));
          }
        }
        if (left instanceof JArrayRef) {
          attributes.add("array");
          witnesses.put("array", location.ID);
        }
        if (right instanceof JInstanceFieldRef) {
          if (!location.isSource()) {
            attributes.add("nonStaticField");
            witnesses.put(
                "nonStaticField",
                location.ID
                    + "\n\t\tvar: "
                    + right.toString()
                    + "\n\t\tstmt: "
                    + finding.getStatement(location.ID));
          }
        }
        if (right instanceof StaticFieldRef) {
          if (!location.isSource()) {
            attributes.add("staticField");
            witnesses.put(
                "staticField",
                location.ID
                    + "\n\t\tvar: "
                    + right.toString()
                    + "\n\t\tstmt: "
                    + finding.getStatement(location.ID));
          }
        }
        if (right instanceof JArrayRef) {
          attributes.add("array");
          witnesses.put("array", location.ID);
        }
        if (APISignatures.contains(APISignatures.reflectionAPIs, s.toString())) {
          attributes.add("reflection");
          witnesses.put("reflection", location.ID);
        }
      }
      if (s instanceof JInvokeStmt) {
        if (APISignatures.contains(APISignatures.interComponentCommunicationAPIs, s.toString()))
          attributes.add("interComponentCommunication");
        if (APISignatures.contains(APISignatures.collectionAPIs, s.toString())) {
          attributes.add("collections");
          witnesses.put("collections", location.ID);
        }
        if (APISignatures.contains(APISignatures.payloadAPIs, s.toString())) {
          attributes.add("payload");
          witnesses.put("payload", location.ID);
        }
      }
      if (s instanceof JIfStmt) {
        attributes.add("pathConstraints");
        witnesses.put("pathConstraints", location.ID);
      }
      if (APISignatures.contains(APISignatures.appendToStringAPIs, s.toString())) {
        attributes.add("appendToString");
        witnesses.put("appendToString", location.ID);
      }
    }
    return attributes;
  }

  private static boolean containsCallback(Witness witnesses, LocationInJimple location) {
    if (location.method == null) {
      TaintProfiler.LOGGER.warn("method location " + location.ID + " was not found");
      return false;
    }
    Chain<SootClass> interfaces = location.method.getDeclaringClass().getInterfaces();
    for (SootClass i : interfaces) {
      String interfaceName = i.getName();
      if (callbacks.contains(interfaceName)) {
        witnesses.put("callbacks", location.ID);
        return true;
      }
    }
    return false;
  }

  private static boolean containsThread(Witness witnesses, LocationInJimple location) {
    if (location.method == null) {
      TaintProfiler.LOGGER.warn("method location " + location.ID + " was not found");
      return false;
    }
    String superClassName = location.method.getDeclaringClass().getSuperclass().getName();
    if (APISignatures.contains(APISignatures.threadingAPIs, superClassName)) {
      witnesses.put("threading", location.ID);
      return true;
    }
    Chain<SootClass> interfaces = location.method.getDeclaringClass().getInterfaces();
    for (SootClass i : interfaces) {
      String interfaceName = i.getName();
      if (APISignatures.contains(APISignatures.threadingAPIs, interfaceName)) {
        witnesses.put("threading", location.ID);
        return true;
      }
    }
    return false;
  }

  private static void parseFindings(String file)
      throws JsonIOException, JsonSyntaxException, FileNotFoundException {
    JsonParser parser = new JsonParser();
    JsonObject obj = parser.parse(new FileReader(file)).getAsJsonObject();
    Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    JsonArray findings = obj.getAsJsonArray("findings");
    for (int i = 0; i < findings.size(); i++) {
      JsonObject finding = findings.get(i).getAsJsonObject();
      FindingInfo fInfo = new FindingInfo();
      if (finding.has("ID")) {
        fInfo.ID = finding.get("ID").getAsInt();
      }
      if (finding.has("source")) {
        JsonObject source = finding.get("source").getAsJsonObject();
        if (source.has("statement")) {
          fInfo.sourceInfo.statement = source.get("statement").getAsString();
        }
        if (source.has("methodName")) {
          fInfo.sourceInfo.methodName = source.get("methodName").getAsString();
        }
        if (source.has("className")) {
          fInfo.sourceInfo.className = source.get("className").getAsString();
        }
        if (source.has("lineNo")) {
          fInfo.sourceInfo.lineNo = source.get("lineNo").getAsInt();
        }
        if (source.has("targetName")) {
          fInfo.sourceInfo.targetName = source.get("targetName").getAsString();
        }
        fInfo.sourceInfo.ID = fInfo.ID + "_source";
      }
      if (finding.has("sink")) {
        JsonObject sink = finding.get("sink").getAsJsonObject();
        if (sink.has("statement")) {
          fInfo.sinkInfo.statement = sink.get("statement").getAsString();
        }
        if (sink.has("methodName")) {
          fInfo.sinkInfo.methodName = sink.get("methodName").getAsString();
        }
        if (sink.has("className")) {
          fInfo.sinkInfo.className = sink.get("className").getAsString();
        }
        if (sink.has("lineNo")) {
          fInfo.sinkInfo.lineNo = sink.get("lineNo").getAsInt();
        }
        if (sink.has("targetName")) {
          fInfo.sinkInfo.targetName = sink.get("targetName").getAsString();
        }
        fInfo.sinkInfo.ID = fInfo.ID + "_sink";
      }
      fInfo.interFlows = new ArrayList<>();
      if (finding.has("intermediateFlows")) {
        JsonArray flows = finding.getAsJsonArray("intermediateFlows");
        for (int j = 0; j < flows.size(); j++) {
          JsonObject flow = flows.get(j).getAsJsonObject();
          IntermediateFlowInfo inter = new IntermediateFlowInfo();
          if (flow.has("statement")) {
            inter.statement = flow.get("statement").getAsString();
          }
          if (flow.has("methodName")) {
            inter.methodName = flow.get("methodName").getAsString();
          }
          if (flow.has("className")) {
            inter.className = flow.get("className").getAsString();
          }
          if (flow.has("lineNo")) {
            inter.lineNo = flow.get("lineNo").getAsInt();
          }
          if (flow.has("ID")) inter.ID = fInfo.ID + "_inter_" + flow.get("ID").getAsInt();
          fInfo.interFlows.add(inter);
        }
      }

      if (finding.has("attributes")) {
        JsonObject attrs = finding.getAsJsonObject("attributes");
        fInfo.attributes = new HashSet<>();
        for (Entry<String, JsonElement> attr : attrs.entrySet()) {
          if (attr.getValue().getAsBoolean()) fInfo.attributes.add(attr.getKey());
        }
        AttributesExtractor.findings.add(fInfo);
      }
    }
  }
}
