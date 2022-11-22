package nicx;

public enum NicColumnTypeHandler implements ColumnTypeHandler {

	INSTANCE;
	
	@Override
	public String getCypherTypeConversionString(String columnName) {
		
			if(Neo4jUtility.cleanse(columnName).startsWith("D_DT_")) {
				return "date(datetime(apoc.date.toISO8601(apoc.date.parse(line[\"%s\"],'ms','MM/dd/yyyy hh:mm'))))".formatted(columnName);
			}
			
			if(Neo4jUtility.cleanse(columnName).endsWith("_CD") || columnName.contains("RSSD")) {
				return  "toInteger(line[\"%s\"])".formatted(columnName);
			}
			return "trim(line[\"%s\"])".formatted(columnName);

	}

}
