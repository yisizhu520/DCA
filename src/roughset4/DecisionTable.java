package roughset4;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
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
        List<Integer> conditionList = new ArrayList<>(titleStr.length - 1);
        decisionStr = titleStr[titleStr.length - 1];
        for (int i = 0; i < titleStr.length - 1; i++) {
            conditionInt[i] = i;
            conditionList.add(i);
            conditionArray[i] = titleStr[i];
            conditionMap.put(titleStr[i], i);
        }
        int index = 0;
        while ((line = br.readLine()) != null) {
            String[] temp = line.split(",");
            List<Integer> condition = new ArrayList<>();
            for (int i = 0; i < temp.length - 1; i++) {
                condition.add(Integer.parseInt(temp[i].trim()));
            }
            Element e = new Element(condition, Integer.parseInt(temp[temp.length - 1].trim()), index);
            index++;
            data.add(e);
        }
        data = data.stream().distinct().collect(Collectors.toList());
        br.close();
        justDoIt(conditionList);

    }

    private void justDoIt(List<Integer> conditionList) {
        List<Integer> condtion = copyList(conditionList);
        List<Integer> resultConditions = new ArrayList<>();
        List<Integer> mostImportant;
//        List<List<Integer>> allPropertySetList = getSubSet(condtion);
//        for(List<Integer> attrs : allPropertySetList){
//
//        }
        do {
            mostImportant = getMostImportantConditions(condtion);
            condtion.removeAll(mostImportant);
            resultConditions.addAll(mostImportant);
        } while (!mostImportant.isEmpty() && !condtion.isEmpty());
        System.out.println("====约简后剩下的属性====");
        for (int i = 0; i < resultConditions.size(); i++) {
            System.out.print(conditionArray[resultConditions.get(i)]+", ");
        }
    }

    private List<Integer> getMostImportantConditions(List<Integer> originData) {
        List<Integer> resultList = new ArrayList<>();
        List<Integer> data = copyList(originData);
        List<Integer> importances = new ArrayList<>(data.size());
        List<Element> all = getPOS(data);
        for (int i = 0; i < data.size(); i++) {
            List<Integer> dataRemoveI = copyList(data);
            dataRemoveI.remove(i);
            List<Element> removeIPOS = getPOS(dataRemoveI);
            int importance = all.size() - removeIPOS.size();
            importances.add(importance);
        }
        // find the most important condition
        int max = importances.stream().max(Comparator.comparingInt(o -> o)).get();
        if (max == 0) {
            return resultList;
        }
        if (hasDuplicateData(importances, max)) {
            System.out.println("重要性相同的condition粗线了---");
            for (int i = 0; i < importances.size(); i++) {
                if (importances.get(i) == max) {
                    System.out.print(" "+conditionArray[data.get(i)]+"--");
                }
            }
        }
        for (int i = 0; i < importances.size(); i++) {
            if (importances.get(i) == max) {
                resultList.add(data.get(i));
            }
        }
        return resultList;
    }

    private boolean isAllSameData(List<Integer> data) {
        Set<Integer> set = new HashSet<>(data);
        return set.size() == 1;
    }

    private boolean hasDuplicateData(List<Integer> data, Integer value) {
        int count = 0;
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).equals(value)) {
                count++;
                if (count == 2) return true;
            }
        }
        return false;
    }

    private boolean isPosSame(List<Element> pos1, List<Element> pos2) {
        if (pos1.size() != pos2.size()) return false;
        for (int i = 0; i < pos1.size(); i++) {
            if (!pos1.get(i).equals(pos2.get(i))) {
                return false;
            }
        }
        return true;
    }

    private List<Integer> copyList(List<Integer> propertySet) {
        List<Integer> propertySetCopy = new ArrayList<>(propertySet.size());
        propertySetCopy.addAll(propertySet);
        return propertySetCopy;
    }

    private List<Element> getPOS(List<Integer> propertySet) {
        for (int i = 0; i < data.size(); i++) {
            // 对每一条数据计算相应属性集
            StringBuilder propertyValue = new StringBuilder();
            for (int j = 0; j < propertySet.size(); j++) {
                propertyValue.append(data.get(i).getValueOfCondition().get(propertySet.get(j))).append('-');
            }
            data.get(i).setTempPropertyValue(propertyValue.toString());
        }
        // 找出等价类
        Map<String, List<Element>> result =
                data.stream().collect(Collectors.groupingBy(Element::getTempPropertyValue, Collectors.toList()));
        // 求出这一组属性集的POS
        List<Element> posSet = new ArrayList<>();
        result.entrySet().stream()
                .filter(entry -> isPositiveSet(entry.getValue()))
                .map(Map.Entry::getValue)
                .forEach(posSet::addAll);
        Collections.sort(posSet);
        // 计算属性依赖度
        // float attrDepens = (float) posSet.size() / data.size();
        return posSet;
    }

    private boolean isPositiveSet(List<Element> elements) {
        for (int i = 0; i < elements.size(); i++) {
            if(elements.get(i).getValueOfDecision() == 0){
                return false;
            }
        }
        return true;
    }

    /**
     * 获取该集合去除一个元素的子集，并按元素由多到少排序，
     *
     * @param set
     * @return
     */
    private static List<List<Integer>> getSubSet(List<Integer> set) {
        List<List<Integer>> result = new ArrayList<>();    //用来存放子集的集合，如{{},{1},{2},{1,2}}
        int length = set.size();
//        int num = length == 0 ? 0 : 1 << (length);    //2的n次方，若集合set为空，num为0；若集合set有4个元素，那么num为16.
        //从0到2^n-1（[00...00]到[11...11]）
        for (int i = 0; i < length; i++) {
            List<Integer> subSet = new ArrayList<>();
//            int index = i;

            for (int j = 0; j < length; j++) {
                if (j != i) {        //每次判断index最低位是否为1，为1则把集合set的第j个元素放到子集中
                    subSet.add(set.get(j));
                }
//                index >>= 1;        //右移一位
            }
            if (!subSet.isEmpty() && subSet.size() >= 2) {
                result.add(subSet);        //把子集存储起来
            }

        }
        // 按元素个数从多到少排序
        result.sort((o1, o2) -> o2.size() - o1.size());
        return result;
    }

    public static void main(String[] args) {
        List<Integer> elements = Arrays.asList(0, 1, 2, 3);
        List<List<Integer>> result = getSubSet(elements);    //调用方法
        //输出结果
        for (List<Integer> subSet : result) {
            for (Integer num : subSet)
                System.out.print(num);

            System.out.println("");
        }

    }

}


