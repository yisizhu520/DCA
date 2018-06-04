package roughset2;

import java.io.IOException;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
    	FileAttributeValueReducer reducer = new FileAttributeValueReducer();
    	try {
    		reducer.generateGroupsForHeaders("C:\\Projects\\attribute-value-reduction\\attributeValueReduction\\src\\main\\kddcup.data_1w_raw.csv", ",", true, 0, "p", "e", "0");
    		reducer.groupValues("C:\\Projects\\attribute-value-reduction\\attributeValueReduction\\src\\main\\kddcup.data_1w_raw.csv", "C:\\Projects\\attribute-value-reduction\\attributeValueReduction\\src\\main\\kddcup.data_1w_raw-grouped.csv", ",", true);
//    		reducer.groupValues("E:\\Mushroom\\agaricus-lepiota-test.csv", "E:\\Mushroom\\agaricus-lepiota-test-grouped.csv", ",", true);
    	}
    	catch(IOException e) {
    		e.printStackTrace();
    	}
    }
}
