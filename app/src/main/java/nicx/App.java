/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package nicx;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;


import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import nicx.Config.Arg;
import nicx.Config.Key;

public class App {
	
	public static void main(String[] args) {
		
        var helpOption = new Options().addOption(Arg.HELP.getOption());
		var options = buildOptions(); 

        var parser = new ExtendedParser();

        try {
        	var cl = parser.parse(helpOption, args,false);

			if(cl.hasOption(Arg.HELP.getOpt())){
				String header = "Provides financial and legal entity data to a running Neo4j instance.";
				String footer = "\nPlease report issues at github.com/austinleedavis/neo4j-financial-data-importer/issues";

				var formatter = new HelpFormatter();
				formatter.printHelp("java -jar n4jfindat.jar",header,options,footer,true);
				return;
			}

			cl = parser.parse(options, args);

			Config.INSTANCE.load(cl);
        	
        	if(cl.hasOption(Arg.RESET.getOpt())) {
				if(confirmReset())
        			clearDatabase();
        	}
        	
        	if(cl.hasOption(Arg.DOWNLOAD_ALL.getOpt())) {
        		downloadAllDataSources();
        	}
        	
        	if(cl.hasOption(Arg.ALL.getOpt())) {
        		importAllNodesDataSources();
        		importAllEdgeDataSources();
        		
        		return; // nothing more to import!
        	}
        	
        	if(cl.hasOption(Arg.NIC.getOpt())) {
        		importNodeDataSources(List.of(
        				NodeDataSource.NIC_ATTRIBUTES_ACTIVE,
        				NodeDataSource.NIC_ATTRIBUTES_BRANCHES,
        				NodeDataSource.NIC_ATTRIBUTES_CLOSED));
        				
        		if(cl.hasOption(Arg.NODES_ONLY.getOpt()) == false){
        			importEdgeDataSources(List.of(
        					EdgeDataSource.NIC_RELATIONSHIPS,
        					EdgeDataSource.NIC_TRANFORMATIONS));
        		}
        	}
        	
        	
        } catch (org.apache.commons.cli.ParseException e) {
        	System.err.print("Parse error: \t");
        	System.err.println(e.getMessage());
        	e.printStackTrace();
		}
		
    }
	

	private static boolean confirmReset(){
		var c = System.console();

		if(c == null){
			System.err.println("Unable to confirm database reset: No console");
			System.exit(-1);
		}

		String response = c.readLine("You are about to erase the database. This action cannot be undone. Do you wish to continue? (default: no) [yes/no] ");

		if(response == null) {
			System.out.println("No response given. Closing application.");
			System.exit(0);
		}

		response=response.toLowerCase();

		if(response.equals("yes")) {
			System.out.println("Reset confirmed.");
			return true;
		}

		System.out.println("Reset cancelled. Remove the -reset option and try again.");

		System.exit(0);

		return false;
			
	}
	
	private static Options buildOptions() {
		
		Options options = new Options();
		for(Arg a: Arg.values()) {
			options.addOption(a.getOption());
		}
		return options;
	}


	public static void importToNeo4j(NodeDataSource d) {
		FluentSession.runOnDefaultDatabase(csvToCypherNodeQuery(d));
	}

	
	public static void importToNeo4j(EdgeDataSource d) {
		csvToCypherEdgeQueryList(d).stream() //each query block should be in order
			.map(List::parallelStream) //commands in each block need not be in order
			.forEach(FluentSession::runQueryStreamOnDefaultDatabase); //each block stream should be run
	}
	
	public static void downloadAllDataSources() {
		List<DownloadableDataSource> allDataSources = new ArrayList<>();
		
		for(var e: EdgeDataSource.values()) allDataSources.add(e);
		for(var n: NodeDataSource.values()) allDataSources.add(n);
		
		allDataSources.parallelStream().forEach(d -> d.downloadLatestZippedDataForImport(Key.DATABASE.value()));
		
	}
	
	public static void download(DownloadableDataSource d) {
		d.downloadLatestZippedDataForImport(Key.DATABASE.value());
	}
	
	public static void clearDatabase() {
		System.out.println("Clearing Database.");
		var queries = List.of(
    			"CALL apoc.schema.assert({},{},true) YIELD label, key RETURN *;",
    			"MATCH (n) DETACH DELETE n;");
		FluentSession.runQueryStreamOnDefaultDatabase(queries.parallelStream());
	}  
	
	public static void importNodeDataSources(List<NodeDataSource> datasources) {
		FluentSession.runQueryStreamOnDefaultDatabase(datasources.parallelStream().map(App::csvToCypherNodeQuery));
	}

	public static void importAllNodesDataSources() {
		System.out.println("Importing Nodes to Neo4j");
		importNodeDataSources(List.of(NodeDataSource.values()));
    }
	
	public static void importEdgeDataSources(List<EdgeDataSource> dataSources) {
		dataSources.parallelStream() //stream of data sources
			.map(App::csvToCypherEdgeQueryList) //create list of queries for each data source
			.map(relType->relType.stream()) // each data source gets its own stream of query blocks
			.forEach(block -> block.map(List::parallelStream) // map each block to its own parallel stream
					.forEach(FluentSession::runQueryStreamOnDefaultDatabase)); // run the queries in the block in-parallel
	}
    
	public static void importAllEdgeDataSources() {
		System.out.println("Importing Edges to Neo4j");
    	Arrays.stream(EdgeDataSource.values())
				.parallel()
				.map(App::csvToCypherEdgeQueryList)
				.map(relType -> relType.stream())
				.forEach(block -> block.map(List::parallelStream)
										.forEach(FluentSession::runQueryStreamOnDefaultDatabase));
    }
    
	private static String csvToCypherNodeQuery(NodeDataSource ds) {
		var path = Path.of(ds.getDestinationFilename());
		var columnsPath = Neo4jUtility.getImportFolderPath(Key.DATABASE.value()).resolve(ds.getDestinationFilename());
    	var columns = getColumnsFromCsv(columnsPath);
    	
    	return "LOAD CSV WITH HEADERS FROM 'file:///"+path+"' AS line CREATE (:"+ds.getnodeTypeName()+" {"+columnsToProperties(columns,ds)+", DATA_SOURCE:\""+ds.getDestinationFilename()+"\"});";
	}
    
    private static String csvToCypherNodeQuery(Path csvPath, String nodeTypeName, EdgeDataSource ds) {
    	var columns = getColumnsFromCsv(csvPath);
    	
    	return "LOAD CSV WITH HEADERS FROM 'file:///"+csvPath.getFileName()+"' AS line CREATE (:"+nodeTypeName+" {"+columnsToProperties(columns,ds)+", DATA_SOURCE:\""+csvPath+"\"});";
	}
    
    private static List<List<String>> csvToCypherEdgeQueryList(EdgeDataSource e){
    	
    	var csvPath = e.getDestinationFullPath(Key.DATABASE.value());
	
		var qryList = new ArrayList<List<String>>();
	
		String hash = "%x".formatted(Math.abs(csvPath.hashCode()));
		
		qryList.add(List.of(csvToCypherNodeQuery(csvPath, "TEMP_ENTITY_"+hash,e))); // create temp entity for each relationship 
		
		
		//create indices on source and target keys
		qryList.add(List.of(
				"CREATE INDEX index_"+cleanse(e.getSourceNodeType())+"_"+cleanse(e.getSourceNodeKey())+" IF NOT EXISTS FOR (n:"+cleanse(e.getSourceNodeType())+") ON (n."+cleanse(e.getSourceNodeKey())+");",
				"CREATE INDEX index_TEMP_SRC_KEY"+hash+" IF NOT EXISTS FOR (n:TEMP_ENTITY_"+hash+") ON (n."+cleanse(e.getSourceKey())+");",
				"CREATE INDEX index_"+cleanse(e.getTargetNodeType())+"_"+cleanse(e.getTargetNodeKey())+" IF NOT EXISTS FOR (n:"+cleanse(e.getTargetNodeType())+") ON (n."+cleanse(e.getTargetNodeKey())+");",
				"CREATE INDEX index_TEMP_TGT_KEY"+hash+" IF NOT EXISTS FOR (n:TEMP_ENTITY_"+hash+") ON (n."+cleanse(e.getTargetKey())+");"
				));
		
		// convert TEMP_ENTITY to relationships
		qryList.add(List.of(
				"MATCH (rel:TEMP_ENTITY_"+hash+"), (src:"+cleanse(e.getSourceNodeType())+"), (tgt:"+cleanse(e.getTargetNodeType())+") "
						+ "WHERE src."+cleanse(e.getSourceNodeKey())+"=rel."+cleanse(e.getSourceKey())+" "
						+ "AND tgt."+cleanse(e.getTargetNodeKey())+" = rel."+cleanse(e.getTargetKey())+" "
						+ "CREATE (src)-[r:"+cleanse(e.getEdgeTypeName())+"]->(tgt) SET r=rel;"));
		
		// cleanup
		qryList.add(List.of(
			"DROP INDEX index_TEMP_SRC_KEY"+hash+" IF EXISTS;", 
			"DROP INDEX index_TEMP_TGT_KEY"+hash+" IF EXISTS;",
			"MATCH (r:TEMP_ENTITY_"+hash+") DELETE r;" //remove temp entities
			));   
		
		return qryList;
	}

	private static String columnsToProperties(List<String> columns, ColumnTypeHandler ds) {
    	
		String flattened = columns.stream()
				.map(colName -> "%s: %s, ".formatted(cleanse(colName), ds.getCypherTypeConversionString(colName)))
				.reduce("", (s1, s2) -> s1 + s2);
    	
    	return flattened.substring(0,flattened.length()-2);
    }
    
	private static List<String> getColumnsFromCsv(Path csvPath) {
		try (var br = Files.newBufferedReader(csvPath)){
			return Arrays.asList(br.readLine().split(","));
		} catch (IOException e) {
			System.err.println("Unable to read headers from csv file.");
			e.printStackTrace();
			return Collections.emptyList();
		}
	}

	private static String cleanse(String c) {return Neo4jUtility.cleanse(c);}
		
}