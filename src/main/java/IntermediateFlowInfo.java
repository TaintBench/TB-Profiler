import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;

/** @author Linghui Luo */
public class IntermediateFlowInfo {
  protected String statement;
  protected String methodName;
  protected String className;
  protected int lineNo;
  protected String ID;

  /**
   * @return method name from a declaration e.g return "onCreate" from "public void onCreate(Bundle
   *     savedInstanceState)".
   */
  protected String getName() {
    if (methodName != null) {
      String[] strs = methodName.split("\\(");
      if (strs.length > 0) {
        String s = strs[0];
        String[] tokens = s.split(" ");
        if (tokens.length > 0) return tokens[tokens.length - 1];
      }
    }
    return null;
  }

  protected String newClassNameForInnerClass(String className, int i) {
    String[] names = className.split("\\.");
    if (i > names.length - 1) return null;
    StringBuilder str = new StringBuilder();
    for (int j = 0; j < names.length; j++) {
      if (j + i + 1 < names.length) {
        str.append(names[j]);
        if (j != names.length - 1) str.append(".");
      } else {
        str.append(names[j]);
        if (j != names.length - 1) str.append("$");
      }
    }
    return str.toString();
  }

  @Override
  public String toString() {
    StringBuilder str = new StringBuilder();
    str.append("ID:\t" + ID + "\n");
    str.append("statement:\t" + statement + "\n");
    str.append("methodName:\t" + methodName + "\n");
    str.append("className:\t" + className + "\n");
    str.append("lineNo:\t" + lineNo + "\n");
    return str.toString();
  }

  protected boolean conditionForUnitSatisfied(Unit u) {
    return lineNo != -1 && u.getJavaSourceStartLineNumber() == lineNo;
  }

  protected LocationInJimple getLocationInJimple() {
    LocationInJimple l = new LocationInJimple();
    l.ID = this.ID;
    if (className == null) {
      System.out.println(ID + " className is null");
    }
    if (!Scene.v().containsClass(className)) {
      // the class can an inner class.
      if (className.contains(".AnonymousClass"))
        className = className.replace(".AnonymousClass", "$");
      int i = 1;
      while (!Scene.v().containsClass(className)) {
        String newClassName = newClassNameForInnerClass(className, i);
        if (newClassName != null) className = newClassName;
        else break;
        i++;
      }
    }

    SootClass klass = Scene.v().getSootClass(className);
    l.klass = klass;
    boolean found = false;
    for (SootMethod m : klass.getMethods()) {
      if (getName().equals(m.getName())) {
        l.method = m;
        found = true;
        for (Unit u : m.retrieveActiveBody().getUnits()) {
          if (conditionForUnitSatisfied(u)) l.addStatement(u);
        }
      }
    }
    if (!found) {
      System.err.println(
          IntermediateFlowInfo.class
              + ": Couldn't find method "
              + methodName
              + " in class "
              + className);
    }
    return l;
  }
}
