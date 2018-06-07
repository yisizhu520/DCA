import java.io.*;
import java.util.*;

/**
 * DCA算法实现类
 *
 * @author WangJun
 */
public class ROCChart {

    public static boolean printLog = true;
    public static boolean needSaveResultFile = false;

    // TODO 随机取1W条数据集
    public static final int RANDOM_DATA_COUNT = 10000;

    // TODO 能够采集一组抗原（随机挑选n（没想好几行）行未标记的数据）
    public static final int CHOOSE_ANTIGEN_COUNT = 10;
    /**
     * 判定阈值
     */
    public static final int JUDGE_THRESHOLD = 10;
    /**
     * 异常阈值
     */
    public static float EXCEPTION_THRESHOLD;

    public static float MaxCSM = Float.NEGATIVE_INFINITY;
    public static float MinCSM = Float.POSITIVE_INFINITY;
    public static float MaxPAMP = Float.NEGATIVE_INFINITY;
    public static float MinPAMP = Float.POSITIVE_INFINITY;
    public static float MaxDS = Float.NEGATIVE_INFINITY;
    public static float MinDS = Float.POSITIVE_INFINITY;
    public static float MaxSS = Float.NEGATIVE_INFINITY;
    public static float MinSS = Float.POSITIVE_INFINITY;

    private void print(Object msg) {
        if (printLog) {
            System.out.println(msg);
        }
    }

    static float Max;

    private void initMigrationValue() {
        mMigrationThreshold = mTestData.getRandomMigrationThreshold();
    }

    private void initWeightMatrix() {
        mWeightMatrix =  mTestData.weightMatrix;
    }

    private float calMedian(String columnName) {
//		float sum=0;
        int count = titleMap.get(columnName.trim());
        float[] temp = new float[mAntigenArray.length];
        for (int i = 0; i < mAntigenArray.length; i++) {
            temp[i] = mAntigenArray[i][count];
        }
        Arrays.sort(temp);
        float result;
        if (temp.length % 2 == 0) {
            result = (float) (temp[temp.length / 2] + temp[temp.length / 2 - 1]) / 2;
        } else {
            result = (float) (Math.floor(temp[temp.length / 2]));
        }
        return result;
    }

    private float calAvg(String columnName) {
        float sum = 0;
        int count = titleMap.get(columnName.trim());
//		float[] temp=new float[mAntigenArray.length];
        for (int i = 0; i < mAntigenArray.length; i++) {
            sum += mAntigenArray[i][count];
        }
//		float result;
//		if(temp.length%2==0) {
//			result=(float)(temp[temp.length/2]+temp[temp.length/2-1])/2;
//		}else {
//			result=(float)(Math.floor(temp[temp.length/2]));
//		}
        return sum / mAntigenArray.length;
    }

    private void calculateDS() {
        // 计算方式为：属性与其均值差的平均（((c1-avg(c1)+(c2-avg(c2)+(c3-avg(c3))/3)），不局限于三个属性
        float[] avgArray = new float[mTestData.columns.length];
        for (int i = 0; i < avgArray.length; i++) {
            float sum = 0;
            int count = 0;
            for (int j = 0; j < mAntigenArray.length; j++) {
                int cloumn = titleMap.get(mTestData.columns[i].trim());
                if (mAntigenArray[j][titles.length - 1] == 10) {
                    sum += mAntigenArray[j][cloumn];
                    count++;
                }
            }
            avgArray[i] = sum / count;
        }
//		System.out.println(avgArray);

        for (int k = 0; k < mAntigenArray.length; k++) {
            float[] propertyArray = new float[avgArray.length];
            for (int m = 0; m < avgArray.length; m++) {
                int cloumn = titleMap.get(mTestData.columns[m].trim());
                float value = Math.abs(mAntigenArray[k][cloumn] - avgArray[m]);
//				if(value<0) {
//
//				}
                propertyArray[m] = value;
            }
            mDSArray[k] = getAvgValue(propertyArray);
            if (mDSArray[k] > MaxDS) {
                MaxDS = mDSArray[k];
            }
            if (mDSArray[k] < MinDS) {
                MinDS = mDSArray[k];
            }
        }
//		print(mDSArray.toString());
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

        void onMarkedFinished(ResultData data);
    }

    public static class ResultData {
        public float correct;
        public float falsePosi;
        public float trueNega;
    }

    TestData mTestData;

    public ROCChart(TestData data) {
        mTestData = data;
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


    public void start() {
        initWeightMatrix();
        calculatePAMPAndSS();
        calculateDS();
//		csmList = new ArrayList<Float>();
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
//				print("CSM="+cell.CSM);
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
                                ar.status = 10;
                            } else {
                                ar.status = 0;
                            }
                            // 判断是否是误判漏判的
//							 if (mAntigenArray[index][42] != ar.status) {
//							 ar.status = 2;
//							 }
                            mUnMarkIndexList.remove((Object) index);
//							print("第 " + index + " 条数据这一轮强行被标记了，(～￣▽￣)～ ，摸摸哒");
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
                if (cell.CSM > MaxCSM) {
                    MaxCSM = cell.CSM;
                }
                if (cell.CSM < MinCSM) {
                    MinCSM = cell.CSM;
                }
            }
            calCount = 0;
//			csmList.add(cell.CSM);
            // evalMigrationValue();
//			 开始迁移
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
                        ar.status = 10;
                    } else {
                        ar.status = 0;
                    }
                    // 判断是否是误判漏判的
                    // if (mAntigenArray[index][42] != ar.status) {
                    // ar.status = 2;
                    // }
                    mUnMarkIndexList.remove((Object) index);
//					print("抗原--" + index + "--被标记");
//					print("未标记抗原数量--" + mUnMarkIndexList.size());
                    if (mAntigenMarkListener != null) {
                        mAntigenMarkListener.onAntigenMarked(ar);
                    }
                }
            }
        }

        print("标记完成");
        String fileName = "kddcup_checked.csv";
        float correct = 0, falsePosi = 0, trueNega = 0;//falsePosi即不是有害，被标记为有害，trueNega即是有害，但是没有被标记出来
        for (int i = 0; i < mAntiResultArray.length; i++) {
            String[] temp = mAntiResultArray[i].originData.split(",");
            if (Float.parseFloat(temp[titles.length - 1]) == mAntiResultArray[i].status) {
                correct++;
            }
            if (Float.parseFloat(temp[titles.length - 1]) != mAntiResultArray[i].status && Float.parseFloat(temp[titles.length - 1]) == 10 && mAntiResultArray[i].status == 0) {
                trueNega++;
            }
            if (Float.parseFloat(temp[titles.length - 1]) != mAntiResultArray[i].status && Float.parseFloat(temp[titles.length - 1]) == 0 && mAntiResultArray[i].status == 10) {
                falsePosi++;
            }
        }
        if (needSaveResultFile) {
            saveResult(fileName);
        }
//		print("输出结果完成--" + fileName);
        print("MaxCSM=" + MaxCSM);
        print("MinCSM=" + MinCSM);
        print("correct=" + correct / mAntiResultArray.length);
        print("falsePosi=" + falsePosi / mAntiResultArray.length);
        print("trueNega=" + trueNega / mAntiResultArray.length);
        print("MCAV=" + EXCEPTION_THRESHOLD);
        print("MaxPAMP=" + MaxPAMP);
        print("MaxSS=" + MaxSS);
        print("MaxDS=" + MaxDS);
        ResultData resultData = new ResultData();
        resultData.correct = correct / mAntiResultArray.length;
        resultData.falsePosi = falsePosi / mAntiResultArray.length;
        resultData.trueNega = trueNega / mAntiResultArray.length;
        mAntigenMarkListener.onMarkedFinished(resultData);
//		print("Max=" + Max);
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
//		print(titleMap);
        dataList = new ArrayList<String>(10000);
        int count = 0;
        while ((line = br.readLine()) != null) {
            dataList.add(line);
            count++;
        }
        br.close();

        // 随机取10000条数据
//		dataList = getRandomData(dataList, RANDOM_DATA_COUNT);

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
        float posi = 0;
        for (int i = 0; i < mAntigenArray.length; i++) {
//			System.out.println(mAntigenArray[i][42]);
            if (mAntigenArray[i][titles.length - 1] == 10.0) {
                posi++;
            }
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


    //DS 粗糙集 1w条
    private static final String[] COLUMNS_RS_1W = {"dst_bytes", "dst_host_srv_count", "dst_host_diff_srv_rate", "dst_host_srv_diff_host_rate"};
    //DS xgboost 1w条
    private static final String[] COLUMNS_XG_1W = {"dst_host_srv_serror_rate", "dst_host_same_src_port_rate", "dst_host_srv_count", "hot", "dst_host_srv_diff_host_rate"};
    //DS 粗糙集 5k条
    private static final String[] COLUMNS_RS_5K = {"dst_host_srv_count", "dst_host_diff_srv_rate", "dst_host_srv_diff_host_rate", "dst_host_srv_rerror_rate"};
    //DS xgboost 5k条
    private static final String[] COLUMNS_XG_5K = {"dst_host_srv_serror_rate", "dst_host_same_src_port_rate", "src_bytes", "hot", "dst_host_srv_diff_host_rate"};
    //DS 粗糙集 1k条
    private static final String[] COLUMNS_RS_1K = {"dst_host_same_src_port_rate", "dst_host_srv_rerror_rate", "dst_host_srv_diff_host_rate"};
    //DS xgboost 1k条
    private static final String[] COLUMNS_XG_1K = {"dst_bytes", "hot", "dst_host_srv_count", "src_bytes", "dst_host_same_src_port_rate", "srv_serror_rate", "duration"};

    private static final String[] FILE_PATHS = {
            "kddcup_1w_test.csv",
            "kddcup_5k_rs_test_10.csv",
            "kddcup_0.5k_rs_test_10.csv"
    };



    /**
     * PAMP、SS：TODO 选择某些属性计算PAMP和SS。 TODO 满足某个条件（表明绝对异常），则PAMP为与均值的绝对值；
     * 则PAMP值为0，SS=10；
     */
    private void calculatePAMPAndSS() {
        //粗糙集 1w条
//		float count1=calAvg("dst_host_same_src_port_rate");//中位数
        //粗糙集 5k条
//		float count1=calAvg("srv_count");//中位数
        //粗糙集 1k条
        float count1 = calAvg("dst_host_srv_count");//中位数
        //粗糙集 0.5k条
//		float count1=calAvg("dst_host_same_src_port_rate");//中位数
//		//xgboost 1w条
//		float count1=calAvg("count");//中位数
//		float count2=calAvg("dst_bytes");//中位数
//		float count3=calAvg("src_bytes");//中位数
        //xgboost 5k条
//		float count1=calAvg("count");//中位数
//		float count2=calAvg("dst_bytes");//中位数
//		xgboost 1k条
//		float count1=calAvg("count");//中位数
//		xgboost 0.5k条
//		float count1=calAvg("count");//中位数
        for (int i = 0; i < mAntigenArray.length; i++) {
            //粗糙集 1w条
//			int num1=titleMap.get("dst_host_same_src_port_rate");
            //粗糙集 5k条
//			int num1=titleMap.get("srv_count");
            //粗糙集 1k条
            int num1 = titleMap.get("dst_host_srv_count");
            //粗糙集 0.5k条
//			int num1=titleMap.get("dst_host_same_src_port_rate");
            //xgboost 1w条
//			int num1=titleMap.get("count");
//			int num2=titleMap.get("dst_bytes");
//			int num3=titleMap.get("src_bytes");
//			//xgboost 5k条
//			int num1=titleMap.get("count");
//			int num2=titleMap.get("dst_bytes");
            //xgboost 1k条
//			int num1=titleMap.get("count");
            //xgboost 0.5k条
//			int num1=titleMap.get("count");
            float value1 = mAntigenArray[i][num1] - count1;
//			float value3=mAntigenArray[i][num3]-count3;
//			float value4=mAntigenArray[i][num4]-count4;
            // FIXME 修改PAMP和SS计算的判断条件
            if (value1 > 0) {
                float count5 = 0;
                if (value1 > 0) {
                    mPAMPArray[i] += value1;
                    count5++;
                }
//				if(value2>0) {
//					mPAMPArray[i]+=value2;
//					count5++;
//				}
//				if(value3>0) {
//					mPAMPArray[i]+=value3;
//					count5++;
//				}
//				if(value4>0) {
//					mPAMPArray[i]+=value4;
//					count5++;
//				}
                mPAMPArray[i] = (float) mPAMPArray[i] / count5;

                mSSArray[i] = 0;
            } else {
                mPAMPArray[i] = 0;
                mSSArray[i] = (float) Math.abs(value1);
//				mSSArray[i] = (float)Math.abs(value1+value2)/2;
//				mSSArray[i] = (float)Math.abs(value1+value2+value3)/3;
            }
            if (mPAMPArray[i] > MaxPAMP) {
                MaxPAMP = mPAMPArray[i];
            }
            if (mPAMPArray[i] < MinPAMP) {
                MinPAMP = mPAMPArray[i];
            }
            if (mSSArray[i] > MaxSS) {
                MaxSS = mSSArray[i];
            }
            if (mSSArray[i] < MinSS) {
                MinSS = mSSArray[i];
            }
        }
//		print(mPAMPArray.toString());
//		print(mSSArray.toString());
    }

    static class TestData {
        String name;
        String filePath;
        String[] columns;
        int[] migrationThresholdRange;
        float[][] weightMatrix;
        String[] PAMPAndSSColumns;

        float getRandomMigrationThreshold() {
            return (float) new Random().nextInt(migrationThresholdRange[1] - migrationThresholdRange[0] + 1)
                    + migrationThresholdRange[0];
        }

    }


    public static void main(String[] args) {

        List<TestData> testDataList = new ArrayList<>(6);
        //粗糙集权值矩阵 1w条
        TestData RS1W = new TestData();
        RS1W.name = "RS_1W";
        RS1W.filePath = FILE_PATHS[0];
        RS1W.columns = COLUMNS_RS_1W;
        RS1W.migrationThresholdRange = new int[]{0, 126};

        RS1W.weightMatrix = new float[][]{
                {2, 1, 2},
                {0, 0, 3.5f},
                {7, 3.5f, 0}
        };
        RS1W.PAMPAndSSColumns = new String[]{"dst_host_same_src_port_rate"};

        //粗糙集 5k条
        TestData RS5K = new TestData();
        RS5K.name = "RS_5K";
        RS5K.filePath = FILE_PATHS[1];
        RS5K.columns = COLUMNS_RS_5K;
        RS5K.migrationThresholdRange = new int[]{0, 132};
        RS5K.weightMatrix = new float[][]{
                {2, 1, 2},
                {0, 0, 1.9f},
                {4, 2f, 0}
        };
        RS5K.PAMPAndSSColumns = new String[]{"srv_count"};

        //粗糙集 1k条
        TestData RS1K = new TestData();
        RS1K.name = "RS_1K";
        RS1K.filePath = FILE_PATHS[2];
        RS1K.columns = COLUMNS_RS_1K;
        RS1K.migrationThresholdRange = new int[]{1, 135};
        RS1K.weightMatrix = new float[][]{
                {2, 1, 2},
                {0, 0, 0.8f},
                {2, 1, -0.3f}
        };
        RS1K.PAMPAndSSColumns = new String[]{"dst_host_same_src_port_rate"};

        //xgboost 1w条
        TestData XG1W = new TestData();
        XG1W.name = "XG_1W";
        XG1W.filePath = FILE_PATHS[0];
        XG1W.columns = COLUMNS_XG_1W;
        XG1W.migrationThresholdRange = new int[]{0, 62};
        XG1W.weightMatrix = new float[][]{
                {2, 0, 2},
                {0, 0, 3f},
                {3, 1.5f, -0.5f}
        };
        XG1W.PAMPAndSSColumns = new String[]{"count","dst_bytes","src_bytes"};

        //xgboost 5k条
        TestData XG5K = new TestData();
        XG5K.name = "XG_5K";
        XG5K.filePath = FILE_PATHS[1];
        XG5K.columns = COLUMNS_XG_5K;
        XG5K.migrationThresholdRange = new int[]{1, 92};
        XG5K.weightMatrix = new float[][]{
                {2, 1, 2},
                {0, 0, 3.6f},
                {3.6f, 1.8f, -1f}
        };
        XG5K.PAMPAndSSColumns = new String[]{"count","src_bytes"};

        //xgboost 1k条
        TestData XG1K = new TestData();
        XG1K.name = "XG_1K";
        XG1K.filePath = FILE_PATHS[2];
        XG1K.columns = COLUMNS_XG_1K;
        XG1K.migrationThresholdRange = new int[]{1, 93};
        XG1K.weightMatrix = new float[][]{
                {2, 1, 2},
                {0, 0, 1.3f},
                {2, 1, 0}
        };
        XG1K.PAMPAndSSColumns = new String[]{"count"};

        testDataList.add(RS1W);
        testDataList.add(RS5K);
        testDataList.add(RS1K);
        testDataList.add(XG1W);
        testDataList.add(XG5K);
        testDataList.add(XG1K);

        for(TestData data : testDataList){
            ROCChart imp = new ROCChart(data);
        }

        try {
            imp.parseDataTxt(filePath);
            imp.setOnAntigenMarkListener(new OnAntigenMarkListener() {
                @Override
                public void onAntigenMarked(AntigenResult result) {

                }

                @Override
                public void onMarkedFinished(ResultData data) {

                }
            });
            imp.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
