package roughset;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class Element implements Comparable<Element> {

    private List<Integer> valueOfCondition = new ArrayList<Integer>(0);
    private int valueOfDecision;
    private int u;
    private String tempPropertyValue;

    //通过条件属性、决策属性集值与论域中样本的编号初始化样本
    public Element(List<Integer> condition, int decision, int iNo) {
        this.valueOfCondition = condition;
        this.valueOfDecision = decision;
        this.u = iNo;
    }

    public List<Integer> getValueOfCondition() {
        return valueOfCondition;
    }

    public void setValueOfCondition(List<Integer> valueOfCondition) {
        this.valueOfCondition = valueOfCondition;
    }

    public int getValueOfDecision() {
        return valueOfDecision;
    }

    public void setValueOfDecision(int valueOfDecision) {
        this.valueOfDecision = valueOfDecision;
    }

    public int getU() {
        return u;
    }

    public void setU(int u) {
        this.u = u;
    }

    public String getTempPropertyValue() {
        return tempPropertyValue;
    }

    public void setTempPropertyValue(String tempPropertyValue) {
        this.tempPropertyValue = tempPropertyValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Element element = (Element) o;
        return u == element.u;
    }

    @Override
    public int hashCode() {
        return Objects.hash(u);
    }

    @Override
    public int compareTo(Element o) {
        return this.u - o.u;
    }
}