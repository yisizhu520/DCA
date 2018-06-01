package roughset3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import java.io.*;

//���߱�DT=(U,CUD,V,f)������
public class DecisionTable implements Cloneable {
	public static int iNo;

	// ȱʡ���췽��
	public DecisionTable() {

	}

	// ���޶���ʽ�ļ�FileName�������߱�
	public DecisionTable(String fileName) {
		parseByDTFormat(fileName);
	}

	private void parseByDTFormat(String fileName) {
		try {
			File file = new File(fileName);
			Scanner in = new Scanner(file);
			String[] temp;
			while (in.hasNextLine()) {
				temp = in.nextLine().split(" |,");
				if (temp[0].equals("@conditionAttribute")) {
					for (int i = 1; i < temp.length; i++) {
						this.conditionSet.add(temp[i]);
					}
				} else if (temp[0].equals("@decisionAttribute")) {
					for (int i = 1; i < temp.length; i++) {
						this.decisionSet.add(temp[i]);
					}
				} else if (temp[0].equals("%") || temp[0].equals("@data")) {
					continue;
				} else if (temp[0].equals("@end")) {
					break;
				} else if (Integer.parseInt(temp[0]) < Integer.MAX_VALUE) {
					Element e = new Element();
					int i = 0;
					ArrayList<Integer> C = new ArrayList<Integer>(0);
					ArrayList<Integer> D = new ArrayList<Integer>(0);
					for (; i < this.conditionSet.size(); i++) {
						C.add(Integer.parseInt(temp[i]));
					}
					for (int j = 0; j < this.decisionSet.size(); j++) {
						D.add(Integer.parseInt(temp[i + j]));
					}
					e.setCondition(C);
					e.setDecision(D);
					this.addElement(e);
				} else {
					System.out.println("������Ŀ��������");
					break;
				}
			}
		}

		catch (IOException e) {
			System.out.print("���ܴ��ļ�" + fileName + " " + e.getMessage());
		}
	}

	// ʵ�ֿ�¡����
	public DecisionTable clone() throws CloneNotSupportedException {
		DecisionTable cloned = new DecisionTable();

		cloned.conditionSet = (ArrayList<String>) this.conditionSet.clone();
		cloned.decisionSet = (ArrayList<String>) this.decisionSet.clone();
		ArrayList<Element> arrayElements = new ArrayList<Element>(0);
		Element tempElement = new Element();
		Iterator<Element> itElements = this.DT.iterator();
		while (itElements.hasNext()) {
			tempElement = itElements.next().clone();
			arrayElements.add(tempElement);
		}
		cloned.DT = arrayElements;

		return cloned;
	}

	// �����������Լ�
	void setConditionSet(ArrayList<String> condition) {
		this.conditionSet = condition;
	}

	// ���þ������Լ�
	void setDecisionSet(ArrayList<String> decision) {
		this.decisionSet = decision;
	}

	// �Ѿ��߱�ǰλ��Ϊindex�������������ΪnewU
	void setU(int index, int newU) {
		Element temp = DT.get(index);
		temp.setU(newU);
		DT.set(index, temp);
	}

	// �����������Լ�
	ArrayList<String> getConditionSet() {
		return this.conditionSet;
	}

	// ���ؾ������Լ�
	ArrayList<String> getDecisionSet() {
		return this.decisionSet;
	}

	// ���ؾ��߱�Ԫ��
	ArrayList<Element> getDT() {
		return DT;
	}

	// ���ر��Ϊu������
	Element getElement(int u) {
		Element temp = new Element();
		Iterator<Element> it = this.DT.iterator();
		while (it.hasNext()) {
			temp = it.next();
			if (temp.getU() == u) {
				return temp;
			}
		}
		System.out.println("����������ָ��������");
		return null;
	}

	// ���ؾ��߱�DT�е�������
	int numberOfElements() {
		return DT.size();
	}

	// �ھ��߱���������e
	void addElement(Element e) {
		this.serialNumberOfElement++;
		if (DecisionTable.iNo < Integer.MAX_VALUE) {
			e.setU(this.serialNumberOfElement);
			DT.add(e);
		} else {
			System.out.println("���ñ���Ѻľ����������ʧ��");
		}
	}

	// ��flag=trueͬAddElement(Element &e)������������������Զ���ţ�ʹ�������������ı��
	void addElement(Element e, boolean flag) {
		if (flag) {
			this.addElement(e);
		} else {
			DT.add(e);
		}
	}

	// �ھ��߱��в�������e�������򷵻������ı�ţ������ڷ���-1
	int findElement(Element e) {
		Iterator<Element> it = this.DT.iterator();
		while (it.hasNext()) {
			Element temp = it.next();
			if (temp.equals(e)) {
				return temp.getU();
			}
		}
		return -1;
	}

	// ɾ�����߱�DT�е�������ΪiNo�ĵ�һ������
	void removeElement(int iNo) {
		if (iNo >= 1 && iNo <= this.serialNumberOfElement) {
			Iterator<Element> it = this.DT.iterator();
			while (it.hasNext()) {
				if (it.next().getU() == iNo) {
					this.DT.remove(iNo);
					break;
				}
			}
		} else {
			System.out.println("Խ�磬����������������");
		}
	}

	// �ж��Ƿ����ĳ��������
	boolean containsConditionAttribute(String c) {
		Iterator<String> itConditionAttr = this.getConditionSet().iterator();
		while (itConditionAttr.hasNext()) {
			if (itConditionAttr.next().equals(c))
				return true;
		}
		return false;
	}

	// ɾ���������Լ��е�����c�����µľ��߱�
	DecisionTable removeConditionAttribute(String c) {
		int index = 0;
		Iterator<String> it = this.conditionSet.iterator();
		String temp;
		while (it.hasNext()) {
			index++;
			temp = it.next();
			if (temp.equals(c)) {// �п���λ�������⣻
				it.remove();
				break;
			}
		}
		index--;
		Iterator<Element> itEle = this.DT.iterator();
		while (itEle.hasNext()) {
			itEle.next().removeConditionAttribute(index);
		}
		return this;
	}

	// ����߱�DT=(U, CUD, V, f)��D��C������
	MySet POS() {
		boolean flag;
		MySet pos = new MySet();
		Iterator<Element> it1 = this.DT.iterator();
		Iterator<Element> it2;
		Element temp1 = new Element(), temp2 = new Element();
		while (it1.hasNext()) {
			temp1 = it1.next();
			flag = true;
			it2 = this.DT.iterator();
			while (it2.hasNext()) {
				temp2 = it2.next();
				// TODO
				if (temp1.isConditionEqual(temp2) && !temp1.isDecisionEqual(temp2)) {
					flag = false;
					break;
				}
			}
			if (flag) {
				pos.add(temp1.getU());
			}
		}
		return pos;
	}

	// ������߱���D��C��������
	double relyDegreeOfDToC() {
		double result = 0.0d;
		MySet pos = this.POS();

		result = (double) pos.card() / this.numberOfElements();

		return result;
	}

	// ������߱���D��C�е�����c��������
	double relyDegreeOfDToAttribute(String c) throws CloneNotSupportedException {
		DecisionTable temp = (DecisionTable) this.clone();
		Iterator<String> it = temp.conditionSet.iterator();
		double result = 0.0d;
		String nowCondition;
		while (it.hasNext()) {
			nowCondition = it.next();
			if (!c.equals(nowCondition)) {
				temp.removeConditionAttribute(nowCondition);
			}
		}
		MySet pos = temp.POS();
		result = (double) pos.card() / this.numberOfElements();
		return result;
	}

	// �����������Լ�������c����Ҫ��
	double sigOfAttribute(String c) throws CloneNotSupportedException {
		double sig = 0.0d;
		double x, y;
		DecisionTable temp = (DecisionTable) this.clone();
		temp.removeConditionAttribute(c);

		// ����D��C��������
		MySet posx = this.POS();
		x = (double) posx.card() / this.numberOfElements();

		// ����D��C-{c}��������
		MySet posy = temp.POS();
		y = (double) posy.card() / temp.numberOfElements();

		// ��������c����Ҫ��
		sig = x - y;
		return sig;
	}

	// ����߱�ĺ˼�
	ArrayList<String> core() throws CloneNotSupportedException {
		ArrayList<String> temp = new ArrayList<String>(0);
		Iterator<String> it = this.getConditionSet().iterator();
		String tmpChar;
		while (it.hasNext()) {
			tmpChar = it.next();
			if (this.sigOfAttribute(tmpChar) != 0) {
				temp.add(tmpChar);
			}
		}
		return temp;
	}

	// ��ȼ���
	ArrayList<MySet> equivalenceClass(ArrayList<String> alChar) throws CloneNotSupportedException {
		boolean availablePara = true;
		Iterator<String> itPara = alChar.iterator();
		while (itPara.hasNext()) {
			if (!this.containsConditionAttribute(itPara.next()))
				availablePara = false;
		}
		if (availablePara) {
			ArrayList<String> conditionsToBeDeleted = new ArrayList<String>(0);
			Iterator<String> itAllConditionAttr = this.getConditionSet().iterator();
			String tempChar;
			while (itAllConditionAttr.hasNext()) {
				tempChar = itAllConditionAttr.next();
				if (!alChar.contains(tempChar)) {
					conditionsToBeDeleted.add(tempChar);
				}
			}

			Iterator<String> itconditionsToBeDeleted = conditionsToBeDeleted.iterator();
			DecisionTable tempDT = (DecisionTable) this.clone();
			while (itconditionsToBeDeleted.hasNext()) {
				tempDT.removeConditionAttribute(itconditionsToBeDeleted.next());
			}

			ArrayList<MySet> equClass = new ArrayList<MySet>(0); // ���յĵȼ���
			ArrayList<Boolean> flag = new ArrayList<Boolean>(0); // ��Ǹ���Ԫ���ǲ����Ѿ������������ĳ���ȼ�����
			for (int i = 0; i <= tempDT.numberOfElements(); i++) {// flag[i]=false(i>=1)��ʾ����������֪�ĵȼ����У�true��ʾ�ѱ�����
				flag.add(false);
			}

			for (int i = 1; i <= tempDT.numberOfElements(); i++) {
				if (flag.get(i) != true) {
					MySet temp = new MySet();
					temp.add(i);
					flag.set(i, true);
					for (int j = i + 1; j <= tempDT.numberOfElements(); j++) {
						if (flag.get(j) != true) {
							if (tempDT.getElement(i).getCondition().equals(tempDT.getElement(j).getCondition())) {
								temp.add(j);
								flag.set(j, true);
							}
						}
					}
					equClass.add(temp);
				}
			}
			return equClass;
		} else {
			System.out.println("����������Բ����ڣ�����Ϊ������Ӧ�ȼ���");
			return null;
		}
	}

	// ��������������Լ��
	DecisionTable attributeReduct() throws CloneNotSupportedException {
		DecisionTable reduction = (DecisionTable) this.clone();
		DecisionTable temp = (DecisionTable) this.clone();
		ArrayList<String> C = reduction.getConditionSet();
		Iterator<String> it = C.iterator();
		MySet pos = this.POS();// ԭ���߱���D��C����

		while (it.hasNext()) {
			if (temp.removeConditionAttribute(it.next()).POS().isEqual(pos)) {
				reduction = temp.clone();
			} else {
				temp = reduction.clone();
			}
		}
		return reduction;
	}

	// ���������ȵ�Լ��
	DecisionTable attributeReductByRely() throws CloneNotSupportedException {
		DecisionTable reduction = this.clone();
		DecisionTable tempDT;
		double r = this.relyDegreeOfDToC();

		while (reduction.relyDegreeOfDToC() == r) {
			double sig = 1.0d;
			int index = 0;
			tempDT = reduction.clone();
			ArrayList<String> condition = reduction.getConditionSet();
			for (int i = 0; i < condition.size(); i++) {
				if (reduction.sigOfAttribute(condition.get(i)) < sig) {
					sig = reduction.sigOfAttribute(condition.get(i));
					index = i;
				}
			}
			if (reduction.removeConditionAttribute(condition.get(index)).relyDegreeOfDToC() == r) {
//				System.out.println("�Ƴ�����--" + condition.get(index));
				continue;
			} else {
				reduction = tempDT.clone();
				break;
			}
		}
		return reduction;
	}

	// ��ӡ���߱���������Ժ;�������
	void print() {
		Iterator<String> itAttribute;
		itAttribute = this.getConditionSet().iterator();
		while (itAttribute.hasNext()) {
			System.out.print(itAttribute.next() + "  ");
		}
		System.out.print("\t->");
		itAttribute = this.getDecisionSet().iterator();
		while (itAttribute.hasNext()) {
			System.out.print("  " + itAttribute.next());
		}
		System.out.println();
	}

	private ArrayList<String> conditionSet = new ArrayList<String>(0);
	private ArrayList<String> decisionSet = new ArrayList<String>(0);
	private ArrayList<Element> DT = new ArrayList<Element>(0);
	int serialNumberOfElement;

}
