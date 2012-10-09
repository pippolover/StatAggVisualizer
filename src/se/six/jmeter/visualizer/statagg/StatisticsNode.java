package se.six.jmeter.visualizer.statagg;

import org.apache.jmeter.samplers.SampleResult;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * @author Lars Krog-Jensen
 *         Created: 2005-sep-30
 */
public abstract class StatisticsNode
  extends DefaultMutableTreeNode
{
  protected Map childMap;
  protected StatAggSampler calculator;

  public StatisticsNode(String name)
  {
    setUserObject(name);
    calculator = new StatAggSampler(name);
    childMap = new HashMap();
    children = new Vector();
  }


  public boolean getAllowsChildren()
  {
    return true;
  }

  public abstract boolean addSample(SampleResult result, String host);

  public StatAggSampler getCalculator()
  {
    return calculator;
  }

  public Vector getChildren()
  {
    return children;
  }

  public boolean isLeaf()
  {
    //System.out.println("IsLeaf: " + this + ": " + (children == null));
    return children == null;
  }

  public int size()
  {
    if (children != null)
      return children.size();

    return 0;
  }

  public void clear()
  {
    if (children != null) {
      children.clear();
      childMap.clear();
    }
  }
}

