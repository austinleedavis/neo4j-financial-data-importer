package nicx;

import java.io.BufferedReader;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Query;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;

import nicx.Config.Key;

public class Neo4jUtility {
	
		
	/**
	 * Cleans the column text of all special characters
	 * @param text 
	 * @return
	 */
	public static String cleanse(String text) {
		var allowedRegex = "[^a-zA-Z0-9/_]";
		var result = text.replaceAll(allowedRegex,"");
		return result;
	}
	

	static Path getImportFolderPath(String database) {
    	var importQuery = "Call dbms.listConfig() YIELD name, value WHERE name='dbms.directories.import' RETURN value";
    	var homeQuery = "Call dbms.listConfig() YIELD name, value WHERE name='dbms.directories.neo4j_home' RETURN value";
    	
		try (var transaction = getDriver().session(SessionConfig.forDatabase(database)).beginTransaction()) {
			String importFolderName = transaction.run(importQuery).next().values().get(0).asString();
			String homeFolderName = transaction.run(homeQuery).next().values().get(0).asString();
			return Path.of(homeFolderName,importFolderName);
		}
    }
	
	public static List<Result> sendQueryToNeo4j(String queryText) {
        return sendQueryToNeo4j(queryText,Key.URI.value(),Key.USERNAME.value(),Key.PASSWORD.value());
    }
    
    static Driver getDriver() {
    	return GraphDatabase.driver(Key.URI.value(), AuthTokens.basic(Key.USERNAME.value(),Key.PASSWORD.value()));
    }
    
    static Session getSession(String database) {
    	return getDriver().session(SessionConfig.forDatabase(database));
    }    
    
    public static List<Result> sendQueryToNeo4j(String queryText, String uri, String user, String password) {
        final Driver driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
        
        var br = new BufferedReader(new StringReader(queryText));
		try (var transaction = driver.session(SessionConfig.forDatabase(Key.DATABASE.value())).beginTransaction()) {
			var results =  br.lines()
					.filter((txt) -> !txt.startsWith("//"))
					.map(Query::new)
					.map(qry -> transaction.run(qry))
					.collect(Collectors.toList());
			return results;
		}

    }
    
}
