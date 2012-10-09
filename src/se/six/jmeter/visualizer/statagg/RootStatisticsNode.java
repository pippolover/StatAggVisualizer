package se.six.jmeter.visualizer.statagg;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;

/**
 * @author Lars Krog-Jensen
 *         Created: 2005-sep-30
 */
public class RootStatisticsNode
  extends StatisticsNode
{
  private final String TOTAL_ROW_LABEL = JMeterUtils.getResString("aggregate_report_total_label");

  private SampleStatisticsNode totalNode;

  public RootStatisticsNode(String name)
  {
    super(name);
    init();
  }

  public boolean addSample(SampleResult result, String host)
  {
    boolean newNodes = false;

    String name = result.getSampleLabel();
    synchronized (childMap) {
      SampleStatisticsNode sampleNode = (SampleStatisticsNode)childMap.get(name);
      if (sampleNode == null) {
        sampleNode = new SampleStatisticsNode(name);
        childMap.put(name, sampleNode);
        children.add(0, sampleNode);
        newNodes = true;
        sampleNode.addSample(result, host);
      } else {
        newNodes = sampleNode.addSample(result, host);
      }

      // Always add to the total
      totalNode.addSample(result, host);
    }
    return newNodes;
  }

  public void clear()
  {
    super.clear();
    init();
  }

  private void init()
  {
    totalNode = new SampleStatisticsNode(TOTAL_ROW_LABEL);
    children.add(totalNode);
  }
}
