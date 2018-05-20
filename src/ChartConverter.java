import java.awt.Font;
import java.io.FileOutputStream;
import java.math.BigDecimal;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class ChartConverter {
	
	XYSeries xyseries1;
	XYSeries xyseries2;
	XYSeries xyseries3;
	
	public ChartConverter() {
		 xyseries1 = new XYSeries("正常抗原");
		 xyseries2 = new XYSeries("异常抗原");
		 xyseries3 = new XYSeries("误判漏判抗原");
	}

	public  JFreeChart createScatterPlot(DCAImp.AntigenResult[] datas) {
		
		XYSeriesCollection xySeriesCollection = new XYSeriesCollection();
		xySeriesCollection.addSeries(xyseries1);
		xySeriesCollection.addSeries(xyseries2);
		xySeriesCollection.addSeries(xyseries3);
		
//		XYDataset xydataset = createXYDataset(datas);
		JFreeChart jfreechart = ChartFactory.createScatterPlot("", "抗原ID", "MCAV", xySeriesCollection, PlotOrientation.VERTICAL,
				true, false, false);
		// 使用CategoryPlot设置各种参数。以下设置可以省略。
		XYPlot plot = (XYPlot) jfreechart.getPlot();
		Font font = new Font("微软雅黑", Font.PLAIN, 14);//
		jfreechart.getTitle().setFont(new Font("楷体", Font.BOLD, 16));

		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setLabelFont(font);
		rangeAxis.setAutoRange(false);
		rangeAxis.setRange(-0.1, 1.1);
		ValueAxis domainAxis = plot.getDomainAxis();
		domainAxis.setLabelFont(font);
		// 背景色 透明度
		plot.setBackgroundAlpha(0.0f);
		plot.setOutlineVisible(false);
		// 隐藏xy轴
		// jfreechart.getXYPlot().getDomainAxis().setVisible(false);
		// jfreechart.getXYPlot().getRangeAxis().setVisible(false);
		// 前景色 透明度
		plot.setForegroundAlpha(1.0f);
		// 其它设置可以参考XYPlot类

		// 解决中文乱码问题,共要处理这三部分
		// １、对标题
		// Font font1 = new Font("SimSun", 10, 20); // 设定字体、类型、字号
		// jfreechart.getTitle().setFont(font1); // 标题

		// ２、对图里面的汉字设定,也就是Ｐlot的设定
		// Font font2 = new Font("SimSun", 10, 16); // 设定字体、类型、字号
		// xyplot.getDomainAxis().setLabelFont(font2);// 相当于横轴或理解为X轴
		// xyplot.getRangeAxis().setLabelFont(font2);// 相当于竖轴理解为Y轴

		// 3、下面的方块区域是 LegendTitle 对象
		Font font3 = new Font("SimSun", 10, 12); // 设定字体、类型、字号
		jfreechart.getLegend().setItemFont(font3);// 最下方
//		jfreechart.addLegend(new LegendTitle(new LegendItemSource() {
//			
//			@Override
//			public LegendItemCollection getLegendItems() {
//				return new LegendItemCollection();
//			}
//		}));
		return jfreechart;
	}
	
	int normal = 0, exp = 0, mistake = 0;
	public void addItem(DCAImp.AntigenResult ar) {
		switch (ar.status) {
		case 0:
			xyseries1.add(ar.ID, ar.MCAV);
			normal++;
			break;
		case 1:
			xyseries2.add(ar.ID, ar.MCAV);
			exp++;
			break;
		case 2:
			xyseries3.add(ar.ID, ar.MCAV);
			mistake++;
			break;
		}
		
		
		
		
	}

	/**
	 * 创建chart数据集
	 * 
	 * @param datas
	 * @return
	 */
	private  XYDataset createXYDataset(DCAImp.AntigenResult[] datas) {
		XYSeries xyseries1 = new XYSeries("正常抗原");
		XYSeries xyseries2 = new XYSeries("异常抗原");
		XYSeries xyseries3 = new XYSeries("误判漏判抗原");
		int normal = 0, exp = 0, mistake = 0;
		for (int i = 0; i < datas.length; i++) {
			DCAImp.AntigenResult ar = datas[i];
			switch (ar.status) {
			case 0:
				xyseries1.add(ar.ID, ar.MCAV);
				normal++;
				break;
			case 1:
				xyseries2.add(ar.ID, ar.MCAV);
				exp++;
				break;
			case 2:
				xyseries3.add(ar.ID, ar.MCAV);
				mistake++;
				break;
			}

		}
		xyseries1.setKey(String.format("正常抗原(%s)", getPercentString((float)normal/datas.length))+"%");
		xyseries2.setKey(String.format("异常抗原(%s)", getPercentString((float)exp/datas.length)+"%"));
		xyseries3.setKey(String.format("误判漏判抗原(%s)", getPercentString((float)mistake/datas.length)+"%"));
		XYSeriesCollection xySeriesCollection = new XYSeriesCollection();
		xySeriesCollection.addSeries(xyseries1);
		xySeriesCollection.addSeries(xyseries2);
		xySeriesCollection.addSeries(xyseries3);
		return xySeriesCollection;
	}

	private  String getPercentString(float number) {//0.7354
		BigDecimal bd = new BigDecimal(number*100);
		bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
		return bd.toString();
	}

	public static void saveChart(JFreeChart chart, String filePath) {
		// 保存为jpg
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(filePath);
			ChartUtilities.writeChartAsJPEG(fos, chart, 800, 600);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	

}
