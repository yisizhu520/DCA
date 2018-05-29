package roughset;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DecisionTable {

    private String[] conditionArray;
    private int[] conditionInt;
    private Map<String, Integer> conditionMap = new HashMap<String, Integer>();
    private String decisionStr;
    private int serialNumberOfElement;
    private List<Element> data = new ArrayList<Element>(0);

    public DecisionTable(String fileName) throws Exception {
        // 读取数据
        BufferedReader br = null;
        br = new BufferedReader(new FileReader(fileName));
        String line = null;
        String[] titleStr = br.readLine().split(",");
        conditionArray = new String[titleStr.length - 1];
        conditionInt = new int[titleStr.length - 1];
        decisionStr = titleStr[titleStr.length - 1];
        for (int i = 0; i < titleStr.length - 1; i++) {
            conditionInt[i] = i;
            conditionArray[i] = titleStr[i];
            conditionMap.put(titleStr[i], i);
        }
        int index = 0;
        while ((line = br.readLine()) != null) {
            String[] temp = line.split(",");
            List<Integer> condition = new ArrayList<>();
            for (int i = 0; i < temp.length - 1; i++) {
                condition.add(Integer.parseInt(temp[i]));
            }
            Element e = new Element(condition, Integer.parseInt(temp[temp.length - 1]), index);
            index++;
            data.add(e);
        }
        br.close();

        // 获取属性集合
        List<List<Integer>> allPropertySetList = getSubSet(conditionInt);

        // 计算pos
        for (List<Integer> propertySet : allPropertySetList) {
            List<String> propertyValueList = new ArrayList<>(data.size());
            for (int i = 0; i < data.size(); i++) {
                // 对每一条数据计算相应属性集
                StringBuilder propertyValue = new StringBuilder();
                for (int j = 0; j < propertySet.size(); j++) {
                    propertyValue.append(data.get(i).getValueOfCondition().get(propertySet.get(j)));
                }
                data.get(i).setTempPropertyValue(propertyValue.toString());
                propertyValueList.add(propertyValue.toString());
            }

            Map<String, List<Element>> result =
                    data.stream().collect(Collectors.groupingBy(Element::getTempPropertyValue, Collectors.toList()));
        }

    }


    static List<List<Integer>> getSubSet(int[] set) {
        List<List<Integer>> result = new ArrayList<>();    //用来存放子集的集合，如{{},{1},{2},{1,2}}
        int length = set.length;
        int num = length == 0 ? 0 : 1 << (length);    //2的n次方，若集合set为空，num为0；若集合set有4个元素，那么num为16.
        //从0到2^n-1（[00...00]到[11...11]）
        for (int i = 0; i < num; i++) {
            List<Integer> subSet = new ArrayList<>();
            int index = i;
            for (int j = 0; j < length; j++) {
                if ((index & 1) == 1) {        //每次判断index最低位是否为1，为1则把集合set的第j个元素放到子集中
                    subSet.add(set[j]);
                }
                index >>= 1;        //右移一位
            }
            if (!subSet.isEmpty()) {
                result.add(subSet);        //把子集存储起来
            }

        }
        return result;
    }

    public static void main(String[] args) {
        int[] set = new int[]{0, 1, 2};
        List<List<Integer>> result = getSubSet(set);    //调用方法

        //输出结果
        for (List<Integer> subSet : result) {
            for (Integer num : subSet)
                System.out.print(num);

            System.out.println("");
        }

    }

}


