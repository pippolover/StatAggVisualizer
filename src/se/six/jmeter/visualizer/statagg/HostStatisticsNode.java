package se.six.jmeter.visualizer.statagg;

import org.apache.jmeter.samplers.SampleResult;

/**
 * @author Lars Krog-Jensen
 *         Created: 2005-sep-30
 */
public class HostStatisticsNode
  extends StatisticsNode
{

  public HostStatisticsNode(String name)
  {
    super(name);
  }

  public boolean addSample(SampleResult result, String host)
  {
    calculator.addSample(result);

    return false;
  }

  public boolean getAllowsChildren()
  {
    return false;
  }

  public boolean isLeaf()
  {
    return true;
  }
}
