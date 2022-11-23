package nicx;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;

public interface DownloadableDataSource {

	String getDataFileUrl();
	String getDestinationFilename();
	
	default Path getDestinationFullPath(String database) {
		return Neo4jUtility.getImportFolderPath(database).resolve(getDestinationFilename());
	}
	
	default void downloadLatestZippedDataForImport(String database) {
    	System.out.println("Downloading: "+this);
    	
    	var targetDirectory = Neo4jUtility.getImportFolderPath(database).toString();
    	var zipFilePath = Path.of(targetDirectory,this+".ZIP");
		
    	try (ReadableByteChannel rbc = Channels.newChannel(new URL(this.getDataFileUrl()).openStream());
    			FileOutputStream fos = new FileOutputStream(zipFilePath.toString());){
    		
	    	fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
	    	
    	} catch (MalformedURLException e) {
    		e.printStackTrace();
			System.exit(-1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		} 
    	
    	try {
			UnzipUtility.unzip(zipFilePath.toString(), targetDirectory,this.getDestinationFilename());
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
    	
		
    	try { 
			Files.delete(zipFilePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	System.out.println("Downlaoded: "+this);
    }
	
	
}
