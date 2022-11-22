package nicx;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.stream.Stream;

import org.neo4j.driver.Query;
import org.neo4j.driver.Session;
import org.neo4j.driver.exceptions.ClientException;

import nicx.Config.Key;

class FluentSession implements AutoCloseable{
	
	private Session session;

	FluentSession(Session s){
		this.session = s;
	}
	
	FluentSession(String database){
		this.session = Neo4jUtility.getSession(database);
	}
	
	public FluentSession runWith(String queryString) {
		var br = new BufferedReader(new StringReader(queryString));
		try {
			br.lines().filter((txt) -> !txt.startsWith("//")).map(Query::new).forEach(q -> session.run(q));
		} catch (ClientException e) {
			System.err.println(e);
		}
		
		return this;
	}
	
	public static void runOnDefaultDatabase(String queryString) {
		run(queryString,Key.DATABASE.value());
	}
	
	public static void run(String queryString, String database) {
		var br = new BufferedReader(new StringReader(queryString));
		try {
			br.lines().filter((txt) -> !txt.startsWith("//")).map(Query::new).forEach(q -> Neo4jUtility.getSession(database).run(q));
		} catch (ClientException e) {
			System.err.println(e);
		}
	}
	
	@Override
	public void close() throws Exception {
		session.close();
	}
	
	public static void runQueryStreamOnDefaultDatabase(Stream<String> queryStream) {
		runQueryStream(queryStream,Key.DATABASE.value());
	}
	
	public static void runQueryStream(Stream<String> queryStream, String database) {
		queryStream.forEach(txt -> {
			var substr = txt;
			
			try (var fs = new FluentSession(database)) {
				fs.runWith(txt);
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("Completed:\t%s".formatted(substr));
		});
	}
	
}