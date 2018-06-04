package roughset3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
public class MySet implements Cloneable
{
	public MySet(){}
	//�������ʼ������
	public MySet(int A[])
	{
		for(int e : A)
		{
			arrayList.add(e);
		}
	}
	
	//�ü��ϳ�ʼ������
	public MySet(MySet s)
	{
		arrayList = s.arrayList;
	}
	
	//��ArrayList<Integer>��ʼ������
	public MySet(ArrayList<Integer> al)
	{
		arrayList = al;
	}
	
	//ʵ�ֿ�¡����
	public MySet clone() throws CloneNotSupportedException
	{
		MySet cloned = new MySet();
				
		Iterator<Integer> itArrayList = this.arrayList.iterator();
		while(itArrayList.hasNext())
		{
			cloned.arrayList.add(itArrayList.next());
		}
		
		return cloned;
	}
	
	//�жϼ����Ƿ�Ϊ�ռ�
	boolean isEmpty()
	{
		return arrayList.isEmpty();
	}
	
	//���㼯��A��ģ
	int card()
	{
		return arrayList.size();
	}
	
	//�������
	void clear()
	{
		arrayList.clear();
	}
	
	//�ڼ����в���Ԫ��,�����򷵻�λ�ã������ڷ���-1
	int find(int a)
	{
		for(int i=0; i<this.card();i++)
		{
			if(a == this.keyAt(i))
			{
				return i;
			}
		}
		return -1;
	}
	
	//���ؼ�����locλ�õ�ֵ
	int keyAt(int loc)
	{
		return arrayList.get(loc);
	}
	
	//����locλ�õ�ֵΪval
	void setKey(int loc,int val)
	{
		arrayList.set(loc, val);
	}
	
	//�Լ����е�Ԫ�ذ���С��������
	void sort()
	{
		Collections.sort(arrayList);
	}
	
	//�ڼ�����ɾ��Ԫ�صõ��µļ���
	void remove(int a)
	{
		if(-1 != find(a))
		{
			ArrayList<Integer> temp = new ArrayList<Integer>(0);
			for(int i=0;i<arrayList.size();i++)
			{
				if(arrayList.get(i) != a)
				{
					temp.add(arrayList.get(i));
				}
			}
			arrayList = temp;
		}
	}
	
	//�ڼ��������Ԫ�صõ��µļ���
	void add(int a)
	{
		arrayList.add(a);
	}
	
	//�жϵ�ǰ�����ǲ���s���ϵ��Ӽ�
	boolean belongTo(MySet s)
	{
		for(int e : arrayList)
		{
			if(-1 == s.find(e))
				return false;
		}
		return true;
	}
	
	//�ж����������Ƿ����
	boolean isEqual(MySet s)
	{
		if(this.belongTo(s) && s.belongTo(this))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	//��ǰ������ȥ���Ӽ�s,s�����ǵ�ǰ���ϵ��Ӽ�
	MySet sub(MySet s)
	{
		if(s.belongTo(this))
		{
			MySet temp = new MySet(arrayList);
			for(int i=0; i<s.card(); i++)
			{
				temp.remove(s.keyAt(i));
			}
			return temp;
		}
		else
		{
			System.out.println("�����м��ϲ��ǵ�ǰ�����Ӽ������ܽ��и�����");
			return null;
		}
	}
	
	//��ǰ��������һ������
	MySet union(MySet s)
	{
		MySet temp = new MySet(arrayList);
		for(int i=0;i<s.card();i++)
			if(-1 == temp.find(s.keyAt(i)))
			{
				temp.add(s.keyAt(i));
			}
		return temp;
	}
	
	//��ǰ������һ�����ϵĽ���
	MySet interSect(MySet s)
	{
		MySet temp = new MySet(arrayList);
		for(int i=0; i<s.card();i++)
			if(-1 == s.find(s.keyAt(i)))
			{
				temp.remove(s.keyAt(i));
			}
		MySet VOfA = temp.valueOfAttribute();
		return VOfA;
	}
	
	//��ĳ����ֵ����ֵ��
	MySet valueOfAttribute()
	{
		MySet temp = new MySet();
		for(int i=0; i<this.card();i++)
		{
			if(this.keyAt(i) != Integer.MAX_VALUE)
			{
				temp.add(this.keyAt(i));
				for(int j=i+1; j<this.card();j++)
				{
					if(this.keyAt(i) == this.keyAt(j))
					{
						this.setKey(j, Integer.MAX_VALUE);
					}
				}
			}
			else
				continue;
		}
		return temp;
	}
	
	//�Ѹü���ת������������
	ArrayList<Integer> toIntArrayList()
	{
		ArrayList<Integer> temp = new ArrayList<Integer>(0);
		for(int i=0; i<this.card(); i++)
		{
			temp.add(this.keyAt(i));
		}
		return temp;
	}
	
	//����Ļ��ӡ����A
	void print()
	{
		int i=0;
		System.out.print("{");
		if(!arrayList.isEmpty())
		{
			System.out.print(arrayList.get(i));
		}
		i++;
		for(; i<arrayList.size(); ++i)
		{
			System.out.print(","+arrayList.get(i));
		}
		System.out.print("}");
	}
	
	private ArrayList<Integer> arrayList = new ArrayList<Integer>(0);
}
