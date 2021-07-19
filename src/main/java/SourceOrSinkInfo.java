/** @author Linghui Luo */
public class SourceOrSinkInfo extends IntermediateFlowInfo {
  protected String targetName;

  @Override
  public String toString() {
    StringBuilder str = new StringBuilder();
    str.append("ID:\t" + ID + "\n");
    str.append("statement:\t" + statement + "\n");
    str.append("methodName:\t" + methodName + "\n");
    str.append("className:\t" + className + "\n");
    str.append("lineNo:\t" + lineNo + "\n");
    str.append("targetName:\t" + targetName + "\n");
    return str.toString();
  }
}
