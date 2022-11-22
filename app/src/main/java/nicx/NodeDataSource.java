package nicx;

enum NodeDataSource implements DownloadableDataSource,ColumnTypeHandler{
	
	NIC_ATTRIBUTES_ACTIVE("Company:Active","https://www.ffiec.gov/npw/FinancialReport/ReturnAttributesActiveZipFileCSV", "CSV_ATTRIBUTES_ACTIVE.CSV", NicColumnTypeHandler.INSTANCE),
	NIC_ATTRIBUTES_BRANCHES("Company:Branch","https://www.ffiec.gov/npw/FinancialReport/ReturnAttributesBranchesZipFileCSV", "CSV_ATTRIBUTES_BRANCHES.CSV", NicColumnTypeHandler.INSTANCE),
	NIC_ATTRIBUTES_CLOSED("Company:Closed","https://www.ffiec.gov/npw/FinancialReport/ReturnAttributesClosedZipFileCSV", "CSV_ATTRIBUTES_CLOSED.CSV", NicColumnTypeHandler.INSTANCE);//,
//	LEI_NODES("LEI","https://leidata-preview.gleif.org/storage/golden-copy-files/2021/09/15/537887/20210915-1600-gleif-goldencopy-lei2-golden-copy.csv.zip","gleif-goldencopy-lei2-golden.csv", LeiColumnTypeHandler.INSTANCE);
	
	final private String dataFileURL;
	final private String csvFilename;
	final private String nodeTypeName;
	final private ColumnTypeHandler columnTypeHandler;

	NodeDataSource(String nodeTypeName, String uriString, String csvFilename, ColumnTypeHandler handler) {
		this.nodeTypeName = nodeTypeName;
		this.csvFilename = csvFilename;
		this.dataFileURL = uriString;
		this.columnTypeHandler = handler;
	}
	public String getDataFileUrl() {
		return dataFileURL;
	}
	
	public String getDestinationFilename() {
		return csvFilename;
	}
	
	String getnodeTypeName() {
		return this.nodeTypeName;
	}
	@Override
	public String getCypherTypeConversionString(String columnName) {
		return columnTypeHandler.getCypherTypeConversionString(columnName);
	}
	
	
	
}