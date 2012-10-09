package se.six.jmeter.visualizer.statagg;

import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleResult;

import java.io.Serializable;

/**
 * @author Lars Krog-Jensen
 *         Created: 2005-sep-29
 */
public class StatAggResultCollector
  extends ResultCollector
  implements Serializable
{
  // transient private static Logger log = LoggingManager.getLoggerForClass();
  public void sampleOccurred(SampleEvent e)
  {
    //super.sampleOccurred(e);
    SampleResult sample = e.getResult();
    String host = e.getHostname();
    ((StatAggVisualizer)getVisualizer()).add(sample, host);

    //System.out.println("[StatAggResultCollector] sampleOccurred(): " + sample);
  }


  public void testEnded(String host)
  {
    System.out.println("[StatAggResultCollector] Test Ended, host: " + host);
    super.testEnded(host);
    ((StatAggVisualizer)getVisualizer()).testStopped();
  }

  public void testStarted(String host)
  {
    System.out.println("[StatAggResultCollector] Test Started, host: " + host);
    super.testStarted(host);
    ((StatAggVisualizer)getVisualizer()).testStarted();
  }
}
