package nicx;

import java.util.HashMap;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

public enum Config {
	INSTANCE;
	
	public enum Key{
		USERNAME,
		PASSWORD,
		DATABASE,
		URI;
		
		public String value() {
			return Config.INSTANCE.get(this);
		}
	}
	
	public enum Arg {

		HELP(Option.builder("h")
		.longOpt("help")
		.build()),

		DB(Option.builder("db")
		.required()
		.hasArg()
		.longOpt("database")
		.desc("The database the session binds to. (e.g., \"mininic\"")
		.build()),
	
		URI(Option.builder("uri")
		.required()
		.hasArg()
		.desc("The URL to a Neo4j instance. For instance: \"bolt://localhost:7687\"")
		.build()),
		
		USERNAME(Option.builder("username")
		.required()
		.hasArg()
		.desc("The username on the Neo4j instance")
		.build()),
		
		PASSWORD(Option.builder("pass")
		.required()
		.hasArg()
		.longOpt("password")
		.desc("The user's password on the Neo4j instance")
		.build()),
		
		RESET(Option.builder("reset")
		.desc("DANGER! Completely clears the database. This occurs prior to running any other command.")
		.build()),
		
		DOWNLOAD_ALL(Option.builder("d")
		.desc("Downloads latest data from online sources")
		.longOpt("downloadAll")
		.build()),
		
		NIC(Option.builder("nic")
		.desc("Imports the NIC data sources to Neo4j.")
		.build()),
		
		LEI(Option.builder("lei")
		.desc("Imports the LEI data sources into Neo4j. Currently only node data is supported.")
		.build()),
		
		ALL(Option.builder("A")
		.desc("Imports all data available from the library.")
		.longOpt("all")
		.build()),
		
		NODES_ONLY(Option.builder("nodesOnly")
		.desc("Restrict import to nodes only; relationships will not be imported.")
		.build());

		Arg(Option option) {
			this.option = option;
		}
		
		private final Option option;
		
		public String getOpt() {
			return this.option.getOpt();
		}

		Option getOption() {
			return this.option;
		}
	}
	
	private HashMap<Key,String> store = new HashMap<>();
	
	public void set(Key k, String value) {
		if(value == null) throw new IllegalArgumentException("Tried to initialize value of "+k+" to null.");
		this.store.put(k, value);
	}

	public Config with(Key k, String value) {
		set(k, value);
		return this;
	}

	public String get(Key k) {
		var value = store.get(k);
		if (value == null)
			throw new IllegalStateException("Tried to use " + k + " without first setting its value.");
		return value;

	}

	void load(CommandLine cl) {
		set(Key.USERNAME,cl.getOptionValue(Arg.USERNAME.getOpt()));
		set(Key.PASSWORD,cl.getOptionValue(Arg.PASSWORD.getOpt()));
		set(Key.DATABASE,cl.getOptionValue(Arg.DB.getOpt()));
		set(Key.URI,cl.getOptionValue(Arg.URI.getOpt()));
	}

}
