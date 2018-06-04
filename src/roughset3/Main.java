package roughset3;

import java.util.ArrayList;
import java.util.Iterator;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws CloneNotSupportedException {
		// �����е����־���Ԫ�أ�Ԫ�飩�����
		DecisionTable myTestDT = new DecisionTable("kddcup_1k_rs_test.dt");

		// ���������
		ArrayList<String> core = myTestDT.core();
		Iterator<String> itCore = core.iterator();
		System.out.println("�������ǣ�");
		while (itCore.hasNext()) {
			System.out.print(itCore.next() + "  ");
		}
		System.out.println();
//
//		// �������������Լ��
//		DecisionTable reductedDT = myTestDT.attributeReduct();
//		System.out.println("�������������Լ�����ǣ�");
//		reductedDT.print();

		// ���������ȵ�����Լ��
		DecisionTable reductedDTByRely = myTestDT.attributeReductByRely();
		System.out.println("���������ȵ�����Լ�����ǣ�");
		reductedDTByRely.print();
	}
}
