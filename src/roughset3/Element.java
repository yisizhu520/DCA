package roughset3;

import java.util.ArrayList;
import java.util.Iterator;

public class Element implements Cloneable {
    public Element() {
    }

    //通过条件属性、决策属性集值与论域中样本的编号初始化样本
    public Element(ArrayList<Integer> condition, ArrayList<Integer> decision, int iNo) {
        this.valueOfCondition = condition;
        this.valueOfDecision = decision;
        this.u = iNo;
    }

    //通过条件属性、决策属性集值与论域中样本的编号初始化样本
    public Element(MySet condition, MySet decision, int iNo) {
        this.valueOfCondition = condition.toIntArrayList();
        this.valueOfDecision = condition.toIntArrayList();
        this.u = iNo;
    }

    //实现克隆功能
    public Element clone() throws CloneNotSupportedException {
        Element cloned = new Element();
        cloned.u = this.getU();
        Iterator<Integer> itCondition = this.getCondition().iterator();
        while (itCondition.hasNext()) {
            cloned.valueOfCondition.add(itCondition.next());
        }
        Iterator<Integer> itDecision = this.getDecision().iterator();
        while (itDecision.hasNext()) {
            cloned.valueOfDecision.add(itDecision.next());
        }
        return cloned;
    }

    //获取样本的条件属性集值
    public ArrayList<Integer> getCondition() {
        return this.valueOfCondition;
    }

    //获取样本的决策属性集值
    public ArrayList<Integer> getDecision() {
        return this.valueOfDecision;
    }

    //获取样本在论域中的编号
    public int getU() {
        return this.u;
    }

    //设置样本的条件属性集值
    void setCondition(ArrayList<Integer> condition) {
        this.valueOfCondition = condition;
    }

    //设置样本的决策属性集值
    void setDecision(ArrayList<Integer> decision) {
        this.valueOfDecision = decision;
    }

    //设置样本在论域中的编号
    void setU(int u) {
        this.u = u;
    }

    //判断两个样本的条件属性集值是否相等
    boolean isConditionEqual(Element another) {
        Iterator<Integer> it1 = this.valueOfCondition.iterator();
        Iterator<Integer> it2 = another.valueOfCondition.iterator();

        while (it1.hasNext() && it2.hasNext()) {
            if (it1.next() != it2.next())
                return false;
        }
        return true;
    }

    //判断两个样本的决策属性集值是否相等
    boolean isDecisionEqual(Element another) {
        Iterator<Integer> it1 = this.getDecision().iterator();
        Iterator<Integer> it2 = another.getDecision().iterator();

        while (it1.hasNext() && it2.hasNext()) {
            if (it1.next() != it2.next())
                return false;
        }
        return true;
    }

    //判断两个样本在论域中的编号是否相等
    boolean isUEqual(Element another) {
        if (this.getU() != another.getU())
            return false;
        else
            return true;
    }

    //只判断条件值与决策值相等，不包括论域U
    boolean isEqual(Element another) {
        if (this.isConditionEqual(another) && this.isDecisionEqual(another))
            return true;
        else
            return false;
    }

    //删除it位置的条件属性值
    void removeConditionAttribute(int it) {
        this.valueOfCondition.remove(it);
    }

    //添加条件属性值
    void addConditionAttribute(int a) {
        this.valueOfCondition.add(a);
    }

    //打印样本
    void print() {
        if (!this.valueOfCondition.isEmpty() && !this.valueOfDecision.isEmpty()) {
            System.out.print("(");
            Iterator<Integer> it = this.valueOfCondition.iterator();
            System.out.print(it.next());
            while (it.hasNext()) {
                System.out.print(",\t" + it.next());
            }
            System.out.print(")\t->\t(");
            it = this.valueOfDecision.iterator();
            System.out.print(it.next());
            while (it.hasNext()) {
                System.out.print(",\t" + it.next());
            }
            System.out.println(")\tu" + u);
        } else
            System.out.println("该样本不符合条件，无意义");
    }

    private ArrayList<Integer> valueOfCondition = new ArrayList<Integer>(0);
    private ArrayList<Integer> valueOfDecision = new ArrayList<Integer>(0);
    private int u;
}