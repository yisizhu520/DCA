package roughset3;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

//决策表DT=(U,CUD,V,f)类声明
public class DecisionTable implements Cloneable {
    public static int iNo;

    // 缺省构造方法
    public DecisionTable() {

    }

    // 由限定格式文件FileName创建决策表
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
                    System.out.println("数据数目有误，请检查");
                    break;
                }
            }
        } catch (IOException e) {
            System.out.print("不能打开文件" + fileName + " " + e.getMessage());
        }
    }

    // 实现克隆功能
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

    // 设置条件属性集
    void setConditionSet(ArrayList<String> condition) {
        this.conditionSet = condition;
    }

    // 设置决策属性集
    void setDecisionSet(ArrayList<String> decision) {
        this.decisionSet = decision;
    }

    // 把决策表当前位置为index的样本编号设置为newU
    void setU(int index, int newU) {
        Element temp = DT.get(index);
        temp.setU(newU);
        DT.set(index, temp);
    }

    // 返回条件属性集
    ArrayList<String> getConditionSet() {
        return this.conditionSet;
    }

    // 返回决策属性集
    ArrayList<String> getDecisionSet() {
        return this.decisionSet;
    }

    // 返回决策表元素
    ArrayList<Element> getDT() {
        return DT;
    }

    // 返回编号为u的样本
    Element getElement(int u) {
        Element temp = new Element();
        Iterator<Element> it = this.DT.iterator();
        while (it.hasNext()) {
            temp = it.next();
            if (temp.getU() == u) {
                return temp;
            }
        }
        System.out.println("不存在你所指定的样本");
        return null;
    }

    // 返回决策表DT中的样本数
    int numberOfElements() {
        return DT.size();
    }

    // 在决策表后添加样本e
    void addElement(Element e) {
        this.serialNumberOfElement++;
        if (DecisionTable.iNo < Integer.MAX_VALUE) {
            e.setU(this.serialNumberOfElement);
            DT.add(e);
        } else {
            System.out.println("可用编号已耗尽，添加样本失败");
        }
    }

    // 当flag=true同AddElement(Element &e)，否则添加样本，不自动编号，使用添加样本本身的编号
    void addElement(Element e, boolean flag) {
        if (flag) {
            this.addElement(e);
        } else {
            DT.add(e);
        }
    }

    // 在决策表中查找样本e，存在则返回样本的编号，不存在返回-1
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

    // 删除决策表DT中的样本号为iNo的第一个样本
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
            System.out.println("越界，不存在这样的样本");
        }
    }

    // 判断是否包含某条件属性
    boolean containsConditionAttribute(String c) {
        Iterator<String> itConditionAttr = this.getConditionSet().iterator();
        while (itConditionAttr.hasNext()) {
            if (itConditionAttr.next().equals(c))
                return true;
        }
        return false;
    }

    // 删除条件属性集中的属性c构成新的决策表
    DecisionTable removeConditionAttribute(String c) {
        int index = 0;
        Iterator<String> it = this.conditionSet.iterator();
        String temp;
        while (it.hasNext()) {
            index++;
            temp = it.next();
            if (temp.equals(c)) {// 有可能位置有问题；
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

    // 求决策表DT=(U, CUD, V, f)的D的C正区域
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

    // 计算决策表中D对C的依赖度
    double relyDegreeOfDToC() {
        double result = 0.0d;
        MySet pos = this.POS();

        result = (double) pos.card() / this.numberOfElements();

        return result;
    }

    // 计算决策表中D对C中的属性c的依赖度
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

    // 计算条件属性集中属性c的重要度
    double sigOfAttribute(String c) throws CloneNotSupportedException {
        double sig = 0.0d;
        double x, y;
        DecisionTable temp = (DecisionTable) this.clone();
        temp.removeConditionAttribute(c);

        // 计算D对C的依赖度
        MySet posx = this.POS();
        x = (double) posx.card() / this.numberOfElements();

        // 计算D对C-{c}的依赖度
        MySet posy = temp.POS();
        y = (double) posy.card() / temp.numberOfElements();

        // 计算属性c的重要度
        sig = x - y;
        return sig;
    }

    // 求决策表的核集
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

    // 求等价类
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

            ArrayList<MySet> equClass = new ArrayList<MySet>(0); // 最终的等价类
            ArrayList<Boolean> flag = new ArrayList<Boolean>(0); // 标记各个元素是不是已经包含在求出的某个等价类中
            for (int i = 0; i <= tempDT.numberOfElements(); i++) {// flag[i]=false(i>=1)表示不包含在已知的等价类中，true表示已被包含
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
            System.out.println("您输入的属性不存在，不能为您求相应等价类");
            return null;
        }
    }

    // 基于正域求属性约简
    DecisionTable attributeReduct() throws CloneNotSupportedException {
        DecisionTable reduction = (DecisionTable) this.clone();
        DecisionTable temp = (DecisionTable) this.clone();
        ArrayList<String> C = reduction.getConditionSet();
        Iterator<String> it = C.iterator();
        MySet pos = this.POS();// 原决策表中D的C正域

        while (it.hasNext()) {
            if (temp.removeConditionAttribute(it.next()).POS().isEqual(pos)) {
                reduction = temp.clone();
            } else {
                temp = reduction.clone();
            }
        }
        return reduction;
    }

    // 基于依赖度的约简
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
//				System.out.println("移除属性--" + condition.get(index));
                continue;
            } else {
                reduction = tempDT.clone();
                break;
            }
        }
        return reduction;
    }

    // 打印决策表的条件属性和决策属性
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
