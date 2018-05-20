import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jvnet.substance.SubstanceLookAndFeel;
import org.jvnet.substance.skin.SubstanceCremeLookAndFeel;
import org.jvnet.substance.watermark.SubstanceCopperplateEngravingWatermark;

public class ChartFrame extends JFrame {

	private JPanel contentPane;
	private JTextField fileTextField;
	private JPanel chartPanel;
	private JButton startBtn;
	private JButton chooseBtn;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					// UIManager.setLookAndFeel(new NimbusLookAndFeel());
					UIManager.setLookAndFeel(new SubstanceCremeLookAndFeel());
					SubstanceLookAndFeel.setCurrentWatermark(new SubstanceCopperplateEngravingWatermark());
					ChartFrame frame = new ChartFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public ChartFrame() {
		setTitle("DCA算法");
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds(0, 0, (int) dim.getWidth() * 7 / 8, (int) dim.getHeight() * 7 / 8);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.NORTH);

		chooseBtn = new JButton("选择数据文件");
		panel.add(chooseBtn);

		fileTextField = new JTextField();
		fileTextField.setEditable(false);
		fileTextField.setText("");
		panel.add(fileTextField);
		fileTextField.setColumns(40);

		startBtn = new JButton("开始运行算法");
		panel.add(startBtn);

		chartPanel = new JPanel();
		contentPane.add(chartPanel, BorderLayout.CENTER);
		chartPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		setLocationRelativeTo(null);

		chooseBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				openFileDialog();
			}
		});

		startBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				startDCA();
			}
		});

		imp = new DCAImp();
		imp.setOnAntigenMarkListener(new DCAImp.OnAntigenMarkListener() {
			
			@Override
			public void onAntigenMarked(DCAImp.AntigenResult result) {
				ChartFrame.this.onAntigenMarked(result);
			}
			
			@Override
			public void onMarkedFinished() {
				validate();
			}
			
		});
	}

	String filePath;
	DCAImp imp;
	ChartConverter cc;
	
	private void onAntigenMarked(DCAImp.AntigenResult result){
		cc.addItem(result);
//		new SwingWorker<Void, Void>() {
//
//			@Override
//			protected Void doInBackground() throws Exception {
//				
//				Thread.sleep(300);
//				return null;
//			}
//
//
//			@Override
//			protected void done() {
//				cc.addItem(result);
//				validate();
//			}
//		}.execute();
	}

	protected void startDCA() {
		if (filePath == null) {
			JOptionPane.showMessageDialog(this, "请先导入数据");
		} else {
			
			imp.start();
//			JFreeChart chart = ChartConverter.createScatterPlot(imp.getResultArray());
//			ChartPanel cp = new ChartPanel(chart);
//			cp.setPreferredSize(new Dimension(getWidth()-60, getHeight()-100));
//			chartPanel.removeAll();
//			chartPanel.add(cp);
//			validate();
//			ChartConverter.saveChart(chart, "test.jpg");
		}
	}

	protected void openFileDialog() {
		JFileChooser jfc = new JFileChooser(System.getProperty("user.dir"));
		jfc.setDialogTitle("请选择TXT格式的数据文件");
		jfc.showDialog(this, "确定");
		File file = jfc.getSelectedFile();
		if (file == null)
			return;
		filePath = file.getPath();
		fileTextField.setText(filePath);
		try {
			imp.parseDataTxt(filePath);
			cc = new ChartConverter();
			JFreeChart chart = cc.createScatterPlot(imp.getResultArray());
			ChartPanel cp = new ChartPanel(chart);
			cp.setPreferredSize(new Dimension(getWidth()-60, getHeight()-100));
			chartPanel.removeAll();
			chartPanel.add(cp);
			validate();
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "该数据文件内容格式有误，请检查后再试");
		}

	}

}
