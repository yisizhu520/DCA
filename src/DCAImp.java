import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import org.jfree.util.StringUtils;

/**
 * DCA算法实现类
 * 
 * @author WangJun
 *
 */
public class DCAImp {

	private static final boolean printLog = true;

	// FIXME 选取特定的一些列来计算PAMP，SS，DS
	private static final String[] COLUMNS_TO_CAL = { "same_srv_rate",
			"dst_host_srv_count", "same_srv_rate" };

	// TODO 随机取1W条数据集
	public static final int RANDOM_DATA_COUNT = 10000;

	// TODO 能够采集一组抗原（随机挑选n（没想好几行）行未标记的数据）
	public static final int CHOOSE_ANTIGEN_COUNT = 100;
	/**
	 * 判定阈值
	 */
	public static final int JUDGE_THRESHOLD = 10;
	/**
	 * 异常阈值
	 */
	public static final float EXCEPTION_THRESHOLD = 0.6f;

	private void print(String msg) {
		if (printLog) {
			System.out.println(msg);
		}
	}

	private void initMigrationValue() {
		// mMigrationThreshold = new Random().nextInt(11) + 30;
		mMigrationThreshold = 1;
	}

	private void initWeightMatrix() {
		// CSM权值列
		mWeightMatrix[0][0] = 2;
		mWeightMatrix[0][1] = 1;
		mWeightMatrix[0][2] = 2;
		// SEMI权值列
		mWeightMatrix[1][0] = 0;
		mWeightMatrix[1][1] = 0;
		mWeightMatrix[1][2] = 3;
		// SS权值列
		mWeightMatrix[2][0] = 2;
		mWeightMatrix[2][1] = 1;
		mWeightMatrix[2][2] = -2;
	}

	/**
	 * PAMP、SS：TODO 选择某些属性计算PAMP和SS。 TODO 满足某个条件（表明绝对异常），则PAMP值为10，SS=0；
	 * 则PAMP值为0，SS=10；
	 */
	private void calculatePAMPAndSS() {
		for (int i = 0; i < mAntigenArray.length; i++) {
			// FIXME 修改PAMP和SS计算的判断条件
			if (new Random().nextBoolean()) {
				mPAMPArray[i] = 10;
				mSSArray[i] = 0;
			} else {
				mPAMPArray[i] = 0;
				mSSArray[i] = 10;
			}
		}
	}

	private void calculateDS() {
		// 计算方式为：属性与其均值差的平均（((c1-avg(c1)+(c2-avg(c2)+(c3-avg(c3))/3)），不局限于三个属性
		float[] avgArray = new float[COLUMNS_TO_CAL.length];
		for (int i = 0; i < avgArray.length; i++) {
			float sum = 0;
			for (int j = 0; j < mAntigenArray.length; j++) {
				int cloumn = titleMap.get(COLUMNS_TO_CAL[i].trim());
				sum += mAntigenArray[j][cloumn];
			}
			avgArray[i] = sum / mAntigenArray.length;
		}

		// 归一化处理
		for (int k = 0; k < mAntigenArray.length; k++) {
			float[] propertyArray = new float[avgArray.length];// 存放每条抗原指定的属性的归一化结果
			for (int m = 0; m < avgArray.length; m++) {
				int cloumn = titleMap.get(COLUMNS_TO_CAL[m].trim());
				float value = mAntigenArray[k][cloumn] - avgArray[m];
				propertyArray[m] = value;
			}
			mDSArray[k] = getAvgValue(propertyArray);
		}
	}

	private float[][] mAntigenArray;
	private float[] mPAMPArray;
	private float[] mSSArray;
	private float[] mDSArray;
	/**
	 * 权值矩阵
	 */
	private float[][] mWeightMatrix = new float[3][3];

	/**
	 * 迁移阈值
	 */
	private float mMigrationThreshold;

	class AntigenResult {
		public String originData;
		public float MCAV;
		public int ID;
		// 0:正常 1：异常 2：误判漏判
		public int status;
	}

	class DCCell {
		public float CSM;
		public float SEMI;
		public float MAT;
		public int status;
	}

	public static final int STATUS_SEMI = 1;
	public static final int STATUS_MAT = 2;
	public static final int STATUS_ERROR = -1;

	private int[] mJudgeSumCountArray;
	private int[] mJudgeExceptionCountArray;
	private List<Integer> mUnMarkIndexList;
	// private List<AntigenResult> mAntiResultList;
	private AntigenResult[] mAntiResultArray;

	private OnAntigenMarkListener mAntigenMarkListener;

	public void setOnAntigenMarkListener(OnAntigenMarkListener listener) {
		mAntigenMarkListener = listener;
	}

	public interface OnAntigenMarkListener {
		void onAntigenMarked(AntigenResult result);

		void onMarkedFinished();
	}

	public DCAImp() {

	}

	public AntigenResult[] getResultArray() {
		return mAntiResultArray;
	}

	/**
	 * 选择一个抗原
	 */
	private int chooseOneAntigen() {
		// TODO 选择抗原的方式，随机还是按一定规律，自己修改
		Random random = new Random();
		int selectedIndex = random.nextInt(mUnMarkIndexList.size());
		int antigenIndex = mUnMarkIndexList.get(selectedIndex);
		return antigenIndex;
	}

	List<Float> csmList;

	private void evalMigrationValue() {
		float sum = 0;
		for (int i = 0; i < csmList.size(); i++) {
			sum += csmList.get(i);
		}
		float value = sum / csmList.size();
		print("CSM 迁移值--" + value);
	}

	public void start() {
		initWeightMatrix();
		calculatePAMPAndSS();
		calculateDS();
		csmList = new ArrayList<Float>();
		boolean stopFlag = false;
		while (!mUnMarkIndexList.isEmpty() && !stopFlag) {

			// 初始化DC细胞，设置阈值
			DCCell cell = new DCCell();
			initMigrationValue();
			List<Integer> antigenIndexs = new ArrayList<Integer>(
					CHOOSE_ANTIGEN_COUNT);
			// 判断CSM与阈值
			int calCount = 0;
			while (cell.CSM <= mMigrationThreshold) {
				calCount++;

				// 处理最后几个未标记的抗原，或者运行了很多很多次几乎死循环的情况
				if (mUnMarkIndexList.size() <= CHOOSE_ANTIGEN_COUNT || calCount == Integer.MAX_VALUE) {
					cell = new DCCell();
					for (int index = 0; index < mUnMarkIndexList.size(); index++) {
						// 计算CSM,SEMI,MAT
						cell.CSM += mPAMPArray[index] * mWeightMatrix[0][0]
								+ mDSArray[index] * mWeightMatrix[0][1]
								+ mSSArray[index] * mWeightMatrix[0][2];
						cell.SEMI += mPAMPArray[index] * mWeightMatrix[1][0]
								+ mDSArray[index] * mWeightMatrix[1][1]
								+ mSSArray[index] * mWeightMatrix[1][2];
						cell.MAT += mPAMPArray[index] * mWeightMatrix[2][0]
								+ mDSArray[index] * mWeightMatrix[2][1]
								+ mSSArray[index] * mWeightMatrix[2][2];
					}
					// 如果剩下的抗原加起来也无法超过阈值，则一起打包处理
					if (cell.CSM <= mMigrationThreshold) {
						// 开始迁移
						if (cell.SEMI >= cell.MAT) {
							cell.status = STATUS_SEMI;
						} else {
							cell.status = STATUS_MAT;
						}
						for (int i = 0; i < mUnMarkIndexList.size(); i++) {
							// 记录判定状态
							int index = mUnMarkIndexList.get(i);
							mJudgeSumCountArray[index]++;
							if (cell.status == STATUS_MAT) {
								mJudgeExceptionCountArray[index]++;
							}

							// 标记抗原
							float ratio = (float) mJudgeExceptionCountArray[index]
									/ mJudgeSumCountArray[index];
							AntigenResult ar = mAntiResultArray[index];
							ar.ID = (int) mAntigenArray[index][0];
							ar.MCAV = ratio;
							if (ratio > EXCEPTION_THRESHOLD) {// 超过阈值，计算MCAV
								ar.status = 1;
							} else {
								ar.status = 0;
							}
							// 判断是否是误判漏判的
							// if (mAntigenArray[index][42] != ar.status) {
							// ar.status = 2;
							// }
							mUnMarkIndexList.remove((Object) index);
							print("第 " + index + " 条数据这一轮强行被标记了，(～￣▽￣)～ ，摸摸哒");
							if (mAntigenMarkListener != null) {
								mAntigenMarkListener.onAntigenMarked(ar);
							}
						}
						stopFlag = true;
						break;
					}

				}

				if (antigenIndexs.size() >= CHOOSE_ANTIGEN_COUNT) {
					cell = new DCCell();
					initMigrationValue();
					antigenIndexs.clear();
				}
				// 采集不同的抗原
				int index = chooseOneAntigen();
				antigenIndexs.add(index);
				// 计算CSM,SEMI,MAT
				cell.CSM += mPAMPArray[index] * mWeightMatrix[0][0]
						+ mDSArray[index] * mWeightMatrix[0][1]
						+ mSSArray[index] * mWeightMatrix[0][2];
				cell.SEMI += mPAMPArray[index] * mWeightMatrix[1][0]
						+ mDSArray[index] * mWeightMatrix[1][1]
						+ mSSArray[index] * mWeightMatrix[1][2];
				cell.MAT += mPAMPArray[index] * mWeightMatrix[2][0]
						+ mDSArray[index] * mWeightMatrix[2][1]
						+ mSSArray[index] * mWeightMatrix[2][2];
			}
			calCount = 0;
			csmList.add(cell.CSM);
			// evalMigrationValue();
			// 开始迁移
			if (cell.SEMI >= cell.MAT) {
				cell.status = STATUS_SEMI;
			} else {
				cell.status = STATUS_MAT;
			}

			for (int i = 0; i < antigenIndexs.size(); i++) {
				// 记录判定状态
				int index = antigenIndexs.get(i);
				mJudgeSumCountArray[index]++;
				if (cell.status == STATUS_MAT) {
					mJudgeExceptionCountArray[index]++;
				}

				// 标记抗原
				if (mJudgeSumCountArray[index] >= JUDGE_THRESHOLD) {
					float ratio = (float) mJudgeExceptionCountArray[index]
							/ mJudgeSumCountArray[index];
					AntigenResult ar = mAntiResultArray[index];
					ar.ID = (int) mAntigenArray[index][0];
					ar.MCAV = ratio;
					if (ratio > EXCEPTION_THRESHOLD) {// 超过阈值，计算MCAV
						ar.status = 1;
					} else {
						ar.status = 0;
					}
					// 判断是否是误判漏判的
					// if (mAntigenArray[index][42] != ar.status) {
					// ar.status = 2;
					// }
					mUnMarkIndexList.remove((Object) index);
					print("抗原--" + index + "--被标记");
					print("未标记抗原数量--" + mUnMarkIndexList.size());
					if (mAntigenMarkListener != null) {
						mAntigenMarkListener.onAntigenMarked(ar);
					}
				}
			}
		}
		mAntigenMarkListener.onMarkedFinished();
		print("标记完成");
		String fileName = "kddcup_checked.csv";
		saveResult(fileName);
		print("输出结果完成--" + fileName);

	}

	private void saveResult(String fileName) {
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(fileName));
			for (int i = 0; i < titles.length; i++) {

			}
			String title = titleStr + "," + "status";
			bw.write(title);
			for (int i = 0; i < mAntiResultArray.length; i++) {
				bw.newLine();
				bw.write(mAntiResultArray[i].originData);
				bw.write("," + mAntiResultArray[i].status);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private String titleStr = null;
	private String[] titles = null;
	private Map<String, Integer> titleMap = null;
	private List<String> dataList = null;

	public void parseDataTxt(String txtPath) throws IOException {
		BufferedReader br = null;
		br = new BufferedReader(new FileReader(txtPath));
		String line = null;
		// 读取标题行
		titleStr = br.readLine();
		titles = titleStr.split(",");
		titleMap = new HashMap<String, Integer>();
		for (int i = 0; i < titles.length; i++) {
			titles[i] = titles[i].trim();
			titleMap.put(titles[i], i);
		}
		dataList = new ArrayList<String>(10000);
		int count = 0;
		while ((line = br.readLine()) != null) {
			dataList.add(line);
			count++;
		}
		br.close();

		// 随机取10000条数据
		dataList = getRandomData(dataList, RANDOM_DATA_COUNT);

		mAntigenArray = new float[dataList.size()][titles.length];
		mPAMPArray = new float[dataList.size()];
		mSSArray = new float[dataList.size()];
		mDSArray = new float[dataList.size()];
		mJudgeSumCountArray = new int[dataList.size()];
		mJudgeExceptionCountArray = new int[dataList.size()];
		mAntiResultArray = new AntigenResult[dataList.size()];
		mUnMarkIndexList = new ArrayList<Integer>(dataList.size());
		// mAntiResultList = new ArrayList<AntigenResult>(dataList.size());

		for (int i = 0; i < dataList.size(); i++) {
			mAntigenArray[i] = parseLine2Array(dataList.get(i));
			mAntiResultArray[i] = new AntigenResult();
			mAntiResultArray[i].originData = dataList.get(i);
		}

		for (int i = 0; i < mAntigenArray.length; i++) {
			mUnMarkIndexList.add(i);
		}

	}

	private List<String> getRandomData(List<String> data, int randomDataCount) {
		Random r = new Random();
		int size = data.size();
		List<String> result = new ArrayList<>(randomDataCount);
		for (int i = 0; i < randomDataCount; i++) {
			result.add(data.get(r.nextInt(size)));
			size--;
		}
		return result;
	}

	/**
	 * 将每一行数据变成float数组
	 * 
	 * @param line
	 * @return
	 */
	private float[] parseLine2Array(String line) {
		Scanner scanner = new Scanner(line);
		scanner.useDelimiter(",");
		float[] data = new float[titles.length];// 定死了数据的数量和位置
		for (int i = 0; i < data.length; i++) {
			data[i] = scanner.nextFloat();
		}
		scanner.close();
		return data;
	}

	/**
	 * 计算一个float数组的平均值
	 * 
	 * @param array
	 * @return
	 */
	private float getAvgValue(float[] array) {
		float sum = 0;
		for (int i = 0; i < array.length; i++) {
			sum += array[i];
		}
		return sum / array.length;
	}

}
