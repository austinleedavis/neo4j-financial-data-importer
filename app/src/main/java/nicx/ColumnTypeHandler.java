package nicx;

/*
 * CSV Datasets typically use naming conventions in their columns to specify the type of data stored therein. 
 */
public interface ColumnTypeHandler {

	/**
	 * Return the appropriate Cypher code to convert elements in a column of data
	 * into typed data. When implementing this method, classes should use the naming
	 * convetions specific to each dataset. For instance, NIC uses the convention
	 * that all string dates start with "DT_". This allows a TypeHandler to look at
	 * the column name "DT_END" to know that the column should be converted into a
	 * date type in Neo4j.
	 * 
	 * @param columnName Name of the column
	 * @return A string of Cypher code that will convert elements from the column
	 *         into the appropriate format.
	 */
	String getCypherTypeConversionString(String columnName);

}
