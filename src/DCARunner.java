import java.io.IOException;

public class DCARunner {

    static float correctSum = 0;
    static float falsePosiSum = 0;
    static float trueNegaSum = 0;

    public static void main(String[] args) throws Exception {
        int N = 10;
        DCAImp.printLog = false;
        DCAImp.needSaveResultFile = false;
        long now = System.currentTimeMillis();

        for (int i = 0; i < N; i++) {
            DCAImp imp = new DCAImp();
            String filePath = "kddcup_1w_test.csv";
            imp.parseDataTxt(filePath);
            final int index = i+1;
            imp.setOnAntigenMarkListener(new DCAImp.OnAntigenMarkListener() {
                @Override
                public void onAntigenMarked(DCAImp.AntigenResult result) {

                }

                @Override
                public void onMarkedFinished(DCAImp.ResultData data) {
                    System.out.println("=======第 "+index+" 次运行结果========");
                    System.out.println("correct="+data.correct);
                    System.out.println("falsePosi="+data.falsePosi);
                    System.out.println("trueNega="+data.trueNega);
                    correctSum += data.correct;
                    falsePosiSum += data.falsePosi;
                    trueNegaSum += data.trueNega;
                }
            });
            imp.start();
        }
        System.out.println("==========================");
        System.out.println("correct平均值="+correctSum/N);
        System.out.println("falsePosi平均值="+falsePosiSum/N);
        System.out.println("trueNega平均值="+trueNegaSum/N);
        System.out.println("耗时秒=="+(System.currentTimeMillis() - now) / 1000);
    }


}
