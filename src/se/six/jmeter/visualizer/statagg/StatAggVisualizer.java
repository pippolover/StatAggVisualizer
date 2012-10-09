// $Header: /home/cvs/jakarta-jmeter/src/components/org/apache/jmeter/visualizers/StatVisualizer.java,v 1.21 2005/07/12 20:50:29 mstover1 Exp $
/*
 * Copyright 2002-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *  
 */

package se.six.jmeter.visualizer.statagg;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.apache.jmeter.samplers.Clearable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.visualizers.gui.AbstractVisualizer;
import org.jdesktop.swing.JXTreeTable;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;
import org.jfree.ui.RectangleInsets;

/**
 * Aggregrate Table-Based Reporting Visualizer for JMeter. Props to the people
 * who've done the other visualizers ahead of me (Stefano Mazzocchi), who I
 * borrowed code from to start me off (and much code may still exist). Thank
 * you!
 *
 * @version $Revision: 1.21 $ on $Date: 2005/07/12 20:50:29 $
 */
public class StatAggVisualizer
  extends AbstractVisualizer
  implements Clearable
{
  protected JXTreeTable _table;


  transient private StatAggTreeTableModel _model;
  private transient JFreeChart _chart;
  private transient ChartPanel _chartPanel;

  private TimeSeriesCollection _dataSet1;
  private TimeSeriesCollection _dataSet2;
  private TimeSeries _throughPutSeries;
  private TimeSeries _averageSeries;
  private StatAggSampler _calculator;
  // How long  sampleinterval for each chart sample update
  private int _chartSampleInterval = 5000;
  // Maximus number of samples i chart, to prevent it from growing
  // to much. When the max item count is reached the series is resampled
  // by a factor _resampleFactor.
  private int _maxChartItemCount = 1000;
  private int _resampleFactor = 5;
  private long _lastChartUpdate = 0;
  private int _resampleIdx = 0;

  Map tableRows = Collections.synchronizedMap(new HashMap());

  public StatAggVisualizer()
  {
    super();
    setModel(new StatAggResultCollector());

    clear();
    init();
  }

  public String getStaticLabel()
  {
    return "Statistical Aggregate Report";
  }

  public String getLabelResource()
  {
    return "Statistical Aggregate Report";
  }

  public void add(SampleResult res)
  {
	  add(res,"");
  }

  public synchronized void add(SampleResult res, String host)
  {
    try {
      _model.addSample(res, host);
      addToChart(res);
    } catch (Exception e) {
      System.out.println("add(...): Error at: " + new Date());
      e.printStackTrace();
    }
  }

  private void addToChart(SampleResult res)
  {
    if (_calculator == null) {
      _calculator = new StatAggSampler(null);
    }
    _calculator.addSample(res);

    long tm = System.currentTimeMillis();
    if (tm > (_lastChartUpdate + _chartSampleInterval)) {
      updateChart();
    }
  }

  private synchronized void updateChart()
  {
    if (_lastChartUpdate >= 0) {
      Second now = new Second();
      addOrUpdateValue(_throughPutSeries, now, _calculator.getMaxThroughput());
//      System.out.println("_calculator.getMaxThroughput(): "+ _calculator.getMaxThroughput());
      addOrUpdateValue(_averageSeries, now, _calculator.getMean());
      if (_throughPutSeries.getItemCount() >= _maxChartItemCount) {
        resampleChartSeries();
      }
    }

    _lastChartUpdate = System.currentTimeMillis();
    _calculator = new StatAggSampler(null);
  }


  private void resampleChartSeries()
  {
    try {
      System.out.println("Resampling, at: " + new Date() + ", Cursor: " + _resampleIdx);
      // Disable dataset notification during resampling
      setDataSetNotify(false);
      // make a backup COPY of current timeseries
      ArrayList tp = new ArrayList(_throughPutSeries.getItems());
      ArrayList av = new ArrayList(_averageSeries.getItems());

      // Clear timeseries.
      _throughPutSeries.clear();
      _averageSeries.clear();


      // Resample
      resample(av, _averageSeries);
      resample(tp, _throughPutSeries);

      // Save current index, from where next resample wil occur
      _resampleIdx = _averageSeries.getItemCount();
    } finally {
      // Finally enable dataset notification during resampling
      setDataSetNotify(true);
      System.out.println("Resampling DONE, at: " + new Date() + ", Cursor: " + _resampleIdx);
    }
  }


  private void resample(ArrayList items, TimeSeries ts)
  {
    int cursor = _resampleIdx;

    // To prevent the timeseries history to be resamped
    // each time, we restart the resampling from where the last
    // ended.
    System.out.println("Resample, items size: " + items.size() + ", Cursor: " + cursor);
    if (cursor >= _maxChartItemCount) {
      // we have now reach the end of the timeseries,
      // now restart the sampling from the root.
      cursor = 0;
      System.out.println("Resetting cursor!");
    }

    // Just copy those items less than cursor index to timeseries
    for (int i = 0; i < cursor; i++) {
      TimeSeriesDataItem item = (TimeSeriesDataItem)items.get(i);
      addValue(ts, item);
    }

    double sampleValue = 0;
    int sampleCount = 0;
    for (int i = cursor; i < items.size(); i++) {
      TimeSeriesDataItem item = (TimeSeriesDataItem)items.get(i);
      RegularTimePeriod period = item.getPeriod();
      Number value = item.getValue();
      sampleValue += value.doubleValue();
      sampleCount++;

      if (((i + 1) % _resampleFactor) == 0) {
        sampleValue /= sampleCount;
        addValue(ts, period, sampleValue);
        sampleValue = sampleCount = 0;
      }
    }

  }

  private void addValue(TimeSeries ts, RegularTimePeriod period, double value)
  {
    TimeSeriesDataItem item = new TimeSeriesDataItem(period, value);
    addValue(ts, item);
  }

  private void addValue(TimeSeries ts, TimeSeriesDataItem item)
  {
    if (item != null) {
      try {
        ts.add(item);
      } catch (Exception e) {
        System.out.println("addValue(..): Error at: " + new Date());
        e.printStackTrace();
      }
    }
  }
  private void addOrUpdateValue(TimeSeries ts, Second period, double value)
  {
    try {
      ts.addOrUpdate(period, value);
    } catch (Exception e) {
      System.out.println("addOrUpdateValue(..): Error at: " + new Date());
      e.printStackTrace();
    }
  }


  private void setDataSetNotify(boolean notify)
  {
    try {
      _throughPutSeries.setNotify(notify);
    } catch (Exception e) {
      System.out.println("setDataSetNotiy(..): Error at: " + new Date());
      e.printStackTrace();
    }
    try {
      _averageSeries.setNotify(notify);
    } catch (Exception e) {
      System.out.println("setDataSetNotiy(..): Error at: " + new Date());
      e.printStackTrace();
    }
  }

  public void testStarted()
  {
    //clear();
  }

  public void testStopped()
  {
    try {
      _model.testStopped();
      updateChart();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Clears this visualizer and its model, and forces a repaint of the table.
   */
  public void clear()
  {
    if (_model != null) {
      _model.clear();
    }
    if (_throughPutSeries != null) {
      _throughPutSeries.clear();
    }
    if (_averageSeries != null) {
      _averageSeries.clear();
    }

    _resampleIdx = 0;
  }
  
  public void clearData()
  {
	  this.clear();
  }

  // overrides AbstractVisualizer
  // forces GUI update after sample file has been read
  public TestElement createTestElement()
  {
    TestElement t = super.createTestElement();

    // sleepTill = 0;
    return t;
  }

  /**
   * Main visualizer setup.
   */
  private void init()
  {
    this.setLayout(new BorderLayout());

    // MAIN PANEL
    JPanel mainPanel = new JPanel();
    Border margin = new EmptyBorder(10, 10, 5, 10);

    mainPanel.setBorder(margin);
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

    //mainPanel.add(makeTitlePanel());
    _chart = createChart();
    _chartPanel = new ChartPanel(_chart);
    mainPanel.add(_chartPanel);

    _model = new StatAggTreeTableModel();
    _table = new JXTreeTable(_model);
    _table.setRootVisible(false);
    //table.setShowHorizontalLines(true);
    //table.setShowVerticalLines(true);
    _table.setLeafIcon(new ImageIcon(getClass().getResource("task.png")));
    _table.setRowSelectionAllowed(true);
    _table.setExpandsSelectedPaths(true);
    _table.setPreferredScrollableViewportSize(new Dimension(500, 70));

    JScrollPane scrollPane = new JScrollPane(_table);
    this.add(mainPanel, BorderLayout.NORTH);
    this.add(scrollPane, BorderLayout.CENTER);
  }

  private JFreeChart createChart()
  {
    setupDatasets();

    final JFreeChart chart = ChartFactory.createTimeSeriesChart(null,
                                                                "Time",
                                                                "ThroughPut",
                                                                _dataSet1,
                                                                true,
                                                                true,
                                                                false);
    final XYPlot plot = chart.getXYPlot();
    plot.setBackgroundPaint(Color.lightGray);
    plot.setDomainGridlinePaint(Color.white);
    plot.setRangeGridlinePaint(Color.white);
    plot.setAxisOffset(new RectangleInsets(5D, 5D, 5D, 5D));

    ValueAxis axis = plot.getDomainAxis();
    axis.setAutoRange(true);
    //axis.setFixedAutoRange(12 * 3600 * 1000);  // 12 Hours

    XYItemRenderer renderer1 = plot.getRenderer();
    renderer1.setSeriesPaint(0, Color.BLACK);


    final NumberAxis axis2 = new NumberAxis("Response Time");
    axis2.setAutoRangeIncludesZero(false);
    plot.setRangeAxis(1, axis2);
    plot.setDataset(1, _dataSet2);
    plot.mapDatasetToRangeAxis(1, 1);


    StandardXYItemRenderer renderer2 = new StandardXYItemRenderer();
    renderer2.setSeriesPaint(0, new Color(0, 153, 255));
    plot.setRenderer(1, renderer2);

    chart.setBackgroundPaint(Color.white);

    return chart;
  }

  private void setupDatasets()
  {
    setupTimeSeries();

    _dataSet1 = new TimeSeriesCollection();
    _dataSet1.addSeries(_throughPutSeries);
    _dataSet2 = new TimeSeriesCollection();
    _dataSet2.addSeries(_averageSeries);
  }

  private void setupTimeSeries()
  {
    _throughPutSeries = new TimeSeries("Throughput, hits/sec", Second.class);
    _throughPutSeries.setMaximumItemCount(_maxChartItemCount);
    _averageSeries = new TimeSeries("Average Response Time, msec", Second.class);
    _averageSeries.setMaximumItemCount(_maxChartItemCount);
  }

}
