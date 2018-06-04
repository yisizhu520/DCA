package roughset3;

import java.util.ArrayList;
import java.util.Iterator;

public class Main {

    /**
     * @param args
     */
    public static void main(String[] args) throws CloneNotSupportedException {
        // 集合中的数字就是元素（元组）的序号
        DecisionTable myTestDT = new DecisionTable("kddcup_1k_rs_test.dt");

        // 计算核属性
        ArrayList<String> core = myTestDT.core();
        Iterator<String> itCore = core.iterator();
        System.out.println("核属性是：");
        while (itCore.hasNext()) {
            System.out.print(itCore.next() + "  ");
        }
        System.out.println();
//
//		// 基于正域的属性约简
//		DecisionTable reductedDT = myTestDT.attributeReduct();
//		System.out.println("基于正域的属性约简结果是：");
//		reductedDT.print();

        // 基于依赖度的属性约简
        DecisionTable reductedDTByRely = myTestDT.attributeReductByRely();
        System.out.println("基于依赖度的属性约简结果是：");
        reductedDTByRely.print();
    }
}
