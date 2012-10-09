package se.six.jmeter.visualizer.statagg;

import org.apache.jmeter.samplers.SampleResult;

/**
 * @author Lars Krog-Jensen
 *         Created: 2005-sep-30
 */
public class SampleStatisticsNode
  extends StatisticsNode
{

  public SampleStatisticsNode(String name)
  {
    super(name);
  }

  public boolean addSample(SampleResult result, String host)
  {
    boolean newNodes = false;
    synchronized (childMap) {
      HostStatisticsNode hostNode = (HostStatisticsNode)childMap.get(host);
      if (hostNode == null) {
        hostNode = new HostStatisticsNode(host);
        childMap.put(host, hostNode);
        children.add(0, hostNode);
        hostNode.addSample(result, host);
        newNodes = true;
      } else {
        newNodes = hostNode.addSample(result, host);
      }
      calculator.addSample(result);
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

  }
}
