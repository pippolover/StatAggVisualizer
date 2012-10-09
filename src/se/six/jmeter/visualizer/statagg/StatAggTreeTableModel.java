package se.six.jmeter.visualizer.statagg;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.jdesktop.swing.treetable.DefaultTreeTableModel;

/**
 * @author Lars Krog-Jensen
 *         Created: 2005-sep-30
 */
public class StatAggTreeTableModel
  extends DefaultTreeTableModel
{
  private final String[] COLUMNS = {JMeterUtils.getResString("URL"),
                                    JMeterUtils.getResString("aggregate_report_count"),
                                    JMeterUtils.getResString("average"),
                                    "Deviation",
                                    JMeterUtils.getResString("aggregate_report_min"),
                                    JMeterUtils.getResString("aggregate_report_max"),
                                    "Errors",
                                    JMeterUtils.getResString("aggregate_report_rate"),
                                    JMeterUtils.getResString("aggregate_report_bandwidth")};


  public StatAggTreeTableModel()
  {
    super(new RootStatisticsNode("Root"), false);
  }

  public void addSample(SampleResult result, String host)
  {
    boolean newNodes = ((RootStatisticsNode)root).addSample(result, host);

    if (newNodes) {
      nodeStructureChanged((RootStatisticsNode)root);
    } else {
      checkIfUpdateGUI();
    }
  }

  private int updates = 0;
  private long lastUpdate = 0;

  private synchronized void checkIfUpdateGUI()
  {
    long now = System.currentTimeMillis();

    if (updates > 100 || (now > (lastUpdate + 1000))) {
      fireTreeNodesChanged(root, ((StatisticsNode)root).getPath(), new int[]{0}, new Object[]{root});
      lastUpdate = System.currentTimeMillis();
      updates = 0;
    } else {
      updates++;
    }

  }

  public void testStopped()
  {
    fireTreeNodesChanged(root, ((StatisticsNode)root).getPath(), new int[]{0}, new Object[]{root});
  }

  public void testStarted()
  {
  }

  public boolean isLeaf(Object node)
  {
    //
    boolean isLeaf = super.isLeaf(node);
    //System.out.println("LoggerTreeTableModel.isLeaf: " + isLeaf + " Node: " + node);

    return isLeaf;
  }

  public Object getChild(Object parent, int index)
  {

    try {
      Object obj = ((StatisticsNode)parent).getChildren().get(index);
      //System.out.println("LoggerTreeTableModel.getChild, parent: " + parent + ", index: " + index + ": " + obj);
      return obj;
    } catch (Exception ex) {
      ex.printStackTrace();
      return super.getChild(parent, index);
    }
  }

  public int getChildCount(Object parent)
  {
    try {
      int size = ((StatisticsNode)parent).size();
      //System.out.println("LoggerTreeTableModel.getChildCount: " + parent + ", size: " + size);
      return size;
    } catch (Exception ex) {
      ex.printStackTrace();
      return super.getChildCount(parent);
    }
  }

  public boolean isCellEditable(Object node, int column)
  {
    return false;
  }

  public Object getValueAt(Object node, int column)
  {
    StatisticsNode statsNode = ((StatisticsNode)node);
    StatAggSampler calculator = statsNode.getCalculator();
    try {
      switch (column) {
        case 0:
          return calculator.getLabel();
        case 1:
          return String.valueOf(calculator.getCount());
        case 2:
          return calculator.getMeanAsNumber();
        case 3:
          return calculator.getStandardDeviationAsNumber();
        case 4:
          return calculator.getMin();
        case 5:
          return calculator.getMax();
        case 6:
          return String.valueOf(calculator.getErrorCount());
        case 7:
          return calculator.getRateString();
        case 8:
          return calculator.getPageSizeString();
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    return null;
  }

  public void setValueAt(Object value, Object node, int column)
  {


  }

  public int getColumnCount()
  {
    return COLUMNS.length;
  }

  public String getColumnName(int column)
  {
    return COLUMNS[column];
  }

  public Class getColumnClass(int columnIndex)
  {
    return columnIndex == 0 ? hierarchicalColumnClass : String.class;
  }

  public void clear()
  {

    if (root != null) {
      ((StatisticsNode)root).clear();
    }
    reload();
    //fireTableDataChanged();
  }


}
