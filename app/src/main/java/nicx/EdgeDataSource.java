package nicx;

enum EdgeDataSource implements DownloadableDataSource,ColumnTypeHandler{
	
	NIC_TRANFORMATIONS(
			"Company", "#ID_RSSD","#ID_RSSD_PREDECESSOR",
			"Company", "#ID_RSSD", "ID_RSSD_SUCCESSOR",
			"Transformation",
			"https://www.ffiec.gov/npw/FinancialReport/ReturnTransformationZipFileCSV", 
			"CSV_TRANSFORMATIONS.CSV", NicColumnTypeHandler.INSTANCE),
	
	NIC_RELATIONSHIPS(
			"Company", "#ID_RSSD","#ID_RSSD_PARENT",
			"Company", "#ID_RSSD","ID_RSSD_OFFSPRING",
			"Ownership",
			"https://www.ffiec.gov/npw/FinancialReport/ReturnRelationshipsZipFileCSV", 
			"CSV_RELATIONSHIPS.CSV", NicColumnTypeHandler.INSTANCE);
	
	EdgeDataSource(
			String sourceNodeType, String sourceNodeKey, String sourceKey, 
			String targetNodeType, String targetNodekey, String targetKey, 
			String edgeTypeName,
			String dataFileURL, 
			String csvFilename,
			ColumnTypeHandler handler){
		this.sourceNodeType=sourceNodeType; 
		this.sourceNodeKey=sourceNodeKey;
		this.sourceKey=sourceKey;
		this.targetNodeType=targetNodeType; 
		this.targetNodeKey = targetNodekey; 
		this.targetKey = targetKey;
		this.edgeTypeName = edgeTypeName;
		this.dataFileURL = dataFileURL; 
		this.csvFilename = csvFilename;
		this.columnTypeHandler = handler;
	}
	
	private final String sourceNodeType;
	private final String sourceNodeKey;
	private final String sourceKey;
	private final String targetNodeType;
	private final String targetNodeKey;
	private final String targetKey;
	private final String edgeTypeName;
	private final String dataFileURL;
	private final String csvFilename;
	private final ColumnTypeHandler columnTypeHandler;


	String getSourceNodeType() {return sourceNodeType;}

	String getSourceNodeKey() {return sourceNodeKey;}

	String getSourceKey() {return sourceKey;}

	String getTargetNodeType() {return targetNodeType;}

	String getTargetNodeKey() {return targetNodeKey;}

	String getTargetKey() {return targetKey;}

	String getEdgeTypeName() {return edgeTypeName;}

	public String getDataFileUrl() {return dataFileURL;}

	String getCsvFilename() {return csvFilename;}
	
	public String getDestinationFilename() {return csvFilename;}

	@Override
	public String getCypherTypeConversionString(String columnName) {
		return this.columnTypeHandler.getCypherTypeConversionString(columnName);
	}
	
}