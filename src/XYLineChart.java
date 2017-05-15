import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Locale;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTitleAnnotation;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import static org.jfree.data.general.DatasetUtilities.findMaximumDomainValue;
import static org.jfree.data.general.DatasetUtilities.findMinimumDomainValue;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.LengthAdjustmentType;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;
import org.jfree.ui.RectangleEdge;

public class XYLineChart extends JFrame {
    public double range_max,  range_min;
    public double domain_max, domain_min; 
    public int width = 800;
    public int height = 600;
    public static long Batch_Size_init, Batch_Size, Batch_Number, Increase_Factor;
    
    public XYLineChart() {
        super("Monte Carlo Simulation of Random Numbers Mean Value");
        JPanel chartPanel = createChartPanel();
        add(chartPanel, BorderLayout.CENTER);
        setSize(width, height);
        setLocationRelativeTo(null);
    }
 
    @SuppressWarnings("empty-statement")
    private JPanel createChartPanel() {
    String chartTitle = "";
    String xAxisLabel = "Sample size";
    String yAxisLabel = "Mean value";
    XYDataset dataset = (XYDataset) createDataset();    
    JFreeChart chart = ChartFactory.createXYLineChart(chartTitle, xAxisLabel, yAxisLabel, dataset,PlotOrientation.VERTICAL, false, true, false);
    XYPlot plot = chart.getXYPlot();
    plot.setBackgroundPaint(new Color(220, 220, 220));

//  Legend settings
    Font font1 = new Font("SansSerif", Font.PLAIN, 11); 
    LegendTitle lt = new LegendTitle(plot);
    lt.setItemFont(font1);
    lt.setBackgroundPaint(new Color(236, 236, 236));
    lt.setFrame(new BlockBorder(Color.white));
    lt.setItemPaint(Color.blue);
    lt.setPosition(RectangleEdge.BOTTOM);
    XYTitleAnnotation ta = new XYTitleAnnotation(0.95, 0.95, lt,RectangleAnchor.TOP_RIGHT);
    ta.setMaxWidth(0.80);
    plot.addAnnotation(ta);

// define domain and range boundaries 
    domain_max = (double) findMaximumDomainValue(dataset); 
    domain_min = (double) findMinimumDomainValue(dataset); 
    range_max = MonteCarlo.Upper_Bound * 1.01; 
    range_min = MonteCarlo.Lower_Bound * 0.99; 
        
    if(MonteCarlo.Batch_Size / MonteCarlo.Batch_Size_init > 400 )   { 
        LogAxis xAxis = new LogAxis();  
        xAxis.setBase(10);
        xAxis.setLabel("Sample size");
        plot.setDomainAxis(xAxis);
        plot.setDomainAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
    } else{    
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Sample size");
        plot.setDomainAxis(xAxis);
        plot.setDomainAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
    }
        NumberAxis yAxis = new NumberAxis();
        yAxis.setRange(range_min, range_max);
        DecimalFormat format = (DecimalFormat)DecimalFormat.getNumberInstance(Locale.ENGLISH);
        yAxis.setNumberFormatOverride(format);
        yAxis.setLabel("Mean value");
        plot.setRangeAxis(yAxis);
        
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        plot.setRenderer(renderer);
        renderer.setSeriesPaint(0, Color.BLUE);
        renderer.setSeriesStroke(0, new BasicStroke(1.0f));
        double size = 5.0;
        double delta = size / 2.0;
        Shape shape1 = new Ellipse2D.Double(-delta, -delta, size, size);
        renderer.setSeriesShape(0, shape1);
        Font font2 = new Font("SansSerif", Font.PLAIN, 16); 
        plot.getDomainAxis().setLabelFont(font2);
        plot.getRangeAxis().setLabelFont(font2);        

// add a labelled marker for true Mean Value
        Marker True_Mean_Value = new ValueMarker(MonteCarlo.TrueMeanValue);
        True_Mean_Value.setLabelOffsetType(LengthAdjustmentType.EXPAND);
        True_Mean_Value.setPaint(Color.red);
        True_Mean_Value.setStroke(new BasicStroke(1.0f));
        True_Mean_Value.setLabel("True Mean Value");
        True_Mean_Value.setLabelFont(new Font("SansSerif", Font.PLAIN, 11));
        True_Mean_Value.setLabelPaint(Color.red);
        True_Mean_Value.setLabelAnchor(RectangleAnchor.TOP_LEFT);
        True_Mean_Value.setLabelTextAnchor(TextAnchor.BOTTOM_LEFT);
        plot.addRangeMarker(True_Mean_Value);

// Saving the chart as an image
    File imageFile = new File("MonteCarloRNMV.png");
    try {
        ChartUtilities.saveChartAsPNG(imageFile, chart, width, height);
    } catch (IOException ex) {
        System.err.println(ex);
    }    
    return new ChartPanel(chart);    
}
 
    private XYSeriesCollection createDataset() {
        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries series1 = new XYSeries("    Monte Carlo Mean Value        ");
        int maxDataPoints = MonteCarlo.MeanValueTable.getRowCount();
        for (int i = 0; i < maxDataPoints; i++) {
            long x = (long) MonteCarlo.MeanValueTable.getValueAt(i, 0);
            double y = (double) MonteCarlo.MeanValueTable.getValueAt(i, 1);
// define 1st serie to plot
            series1.add(x,y); 
        }
// export series        
        dataset.addSeries(series1);
        return dataset;
    }

}