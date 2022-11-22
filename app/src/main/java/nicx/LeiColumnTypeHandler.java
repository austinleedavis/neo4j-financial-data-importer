package nicx;

public enum LeiColumnTypeHandler implements ColumnTypeHandler {

	INSTANCE;
	
	@Override
	public String getCypherTypeConversionString(String columnName) {
		
		if(Neo4jUtility.cleanse(columnName).contains("Date")) {
			return "date(datetime(line[\"%s\"]))".formatted(columnName); //LEI is in ISO8601 datetime standard
		}
		
		return "trim(line[\"%s\"])".formatted(columnName); //all other LEI fields are alphanumeric (Zip codes should not be parsed as integers)
	}

}