import java.util.Properties;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;

public interface Propertied {

	String directoryName = "config";

	Properties getProperties();
	
	Properties seedProperties();

	default Properties loadPropertiesFromDisk(String propertiesName) {
		String path = directoryName + "/" + propertiesName + ".properties";
		FileInputStream fis = null;
		Properties props = null;
		try {
			fis = new FileInputStream(path);
			props = new Properties();
			props.load(fis);
		} catch (IOException ioe) {
			System.out.println("Attempted to load file input stream from file "+path+" - ioexception");
			System.out.println(ioe);
			System.out.println("Attempting to seed properties for " + propertiesName);
			props = seedProperties();
			savePropertiesToDisk(propertiesName, props);
			System.out.println("Default properties for " + propertiesName + "seeded.  Halting.  Re-run main class.");
			System.exit(0);
		}
		return props;
	}

	default void savePropertiesToDisk(String propertiesName, Properties props) {
		String path = directoryName + "/" + propertiesName + ".properties";
		try {
            if (new File(directoryName).mkdir()) {
				System.out.println("No config directory - creating.");
			}
			props.store(new FileOutputStream(path), null);
		} catch (IOException ioe) {
    		System.out.println("There was an error writing to the filesystem at "+path);
			System.out.println(ioe);
			System.out.println("Halting");
			System.exit(1);
        }
	}

}





