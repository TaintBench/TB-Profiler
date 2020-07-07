public class SourceSinkStat {

  protected double sourceAPIs;
  protected double soucesUsage;
  protected double sinkAPIs;
  protected double sinksUsage;

  @Override
  public String toString() {
    return sourceAPIs + ";" + soucesUsage + ";" + sinkAPIs + ";" + sinksUsage;
  }
}
