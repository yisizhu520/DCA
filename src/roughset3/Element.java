package roughset3;

import java.util.ArrayList;
import java.util.Iterator;
public class Element implements Cloneable
{
	public Element(){}
	
	//ͨ���������ԡ��������Լ�ֵ�������������ı�ų�ʼ������
	public Element(ArrayList<Integer> condition, ArrayList<Integer> decision, int iNo)
	{
		this.valueOfCondition = condition;
		this.valueOfDecision = decision;
		this.u = iNo;
	}
	
	//ͨ���������ԡ��������Լ�ֵ�������������ı�ų�ʼ������
	public Element(MySet condition, MySet decision, int iNo)
	{
		this.valueOfCondition = condition.toIntArrayList();
		this.valueOfDecision = condition.toIntArrayList();
		this.u = iNo;
	}
	
	//ʵ�ֿ�¡����
	public Element clone() throws CloneNotSupportedException
	{
		Element cloned = new Element();
		cloned.u = this.getU();
		Iterator<Integer> itCondition = this.getCondition().iterator();
		while(itCondition.hasNext())
		{
			cloned.valueOfCondition.add(itCondition.next());
		}
		Iterator<Integer> itDecision = this.getDecision().iterator();
		while(itDecision.hasNext())
		{
			cloned.valueOfDecision.add(itDecision.next());
		}
		return cloned;
	}
	
	//��ȡ�������������Լ�ֵ
	public ArrayList<Integer> getCondition()
	{
		return this.valueOfCondition;
	}
	
	//��ȡ�����ľ������Լ�ֵ
	public ArrayList<Integer> getDecision()
	{
		return this.valueOfDecision;
	}
	
	//��ȡ�����������еı��
	public int getU()
	{
		return this.u;
	}
	
	//�����������������Լ�ֵ
	void setCondition(ArrayList<Integer> condition)
	{
		this.valueOfCondition = condition;
	}
	
	//���������ľ������Լ�ֵ
	void setDecision(ArrayList<Integer> decision)
	{
		this.valueOfDecision = decision;
	}
	
	//���������������еı��
	void setU(int u)
	{
		this.u = u;
	}
	
	//�ж������������������Լ�ֵ�Ƿ����
	boolean isConditionEqual(Element another)
	{
		Iterator<Integer> it1 = this.valueOfCondition.iterator();
		Iterator<Integer> it2 = another.valueOfCondition.iterator();
		
		while(it1.hasNext() && it2.hasNext())
		{
			if(it1.next() != it2.next())
				return false;
		}
		return true;
	}
	
	//�ж����������ľ������Լ�ֵ�Ƿ����
	boolean isDecisionEqual(Element another)
	{
		Iterator<Integer> it1 = this.getDecision().iterator();
		Iterator<Integer> it2 = another.getDecision().iterator();
		
		while(it1.hasNext() && it2.hasNext())
		{
			if(it1.next() != it2.next())
				return false;
		}
		return true;
	}
	
	//�ж����������������еı���Ƿ����
	boolean isUEqual(Element another)
	{
		if(this.getU() != another.getU())
			return false;
		else 
			return true;
	}
	
	//ֻ�ж�����ֵ�����ֵ��ȣ�����������U
	boolean isEqual(Element another)
	{
		if(this.isConditionEqual(another) && this.isDecisionEqual(another))
			return true;
		else 
			return false;
	}
	
	//ɾ��itλ�õ���������ֵ
	void removeConditionAttribute(int it)
	{
		this.valueOfCondition.remove(it);
	}
	
	//�����������ֵ
	void addConditionAttribute(int a)
	{
		this.valueOfCondition.add(a);
	}
	
	//��ӡ����
	void print()
	{
		if(!this.valueOfCondition.isEmpty() && !this.valueOfDecision.isEmpty())
		{
			System.out.print("(");
			Iterator<Integer> it = this.valueOfCondition.iterator();
			System.out.print(it.next());
			while(it.hasNext())
			{
				System.out.print(",\t" + it.next());
			}
			System.out.print(")\t->\t(");
			it = this.valueOfDecision.iterator();
			System.out.print(it.next());
			while(it.hasNext())
			{
				System.out.print(",\t" + it.next());
			}
			System.out.println(")\tu" + u);
		}
		else
			System.out.println("������������������������");
	}
	
	private ArrayList<Integer> valueOfCondition = new ArrayList<Integer>(0);
	private ArrayList<Integer> valueOfDecision = new ArrayList<Integer>(0);
	private int u;
}