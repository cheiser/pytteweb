package fileManagement;

import java.io.*;
import java.net.URLConnection;

/**
 * A singleton filemanager that handles file IO
 * @author Mattis
 *
 */
public final class FileManager{
	private static FileManager instance = null;
	
	// the size of the buffer to use when coping large files
	private final int bufferSize = 1024;
	
	private FileManager(){
	}
	
	/**
	 * Singleton
	 * @return the instance of this singleton filemanager
	 */
	public synchronized static FileManager getInstance(){
		if(instance == null){
			instance = new FileManager();
		}
		return instance;
	}
	
	/**
	 * Reads a file as a byte array
	 * @param fileName the filename of the file to be read
	 * @return the file as an array of bytes
	 * @throws IOException could not read to the bytes array, possibly trouble closing the stream
	 */
	public byte[] readFileAsByte(String fileName) throws IOException{
		File file = new File(fileName);
		byte[] bytes = new byte[(int)file.length()];
		
		FileInputStream fileInputStream = new FileInputStream(file);
		fileInputStream.read(bytes);
		fileInputStream.close();
		
		return bytes;
	}
	
	
	/**
	 * Gets the size of the specified file in bytes as a long
	 * @param fileName
	 * @return
	 */
	public long getFileSize(String fileName){
		File file = new File(fileName);
		return file.length();
	}
	
	/**
	 * Checks if the specified file exists and if we got permission to read from it
	 * @param fileToBeRead the file to be checked
	 * @return a boolean representing whether or not we could read from the file and if it existed, true if we can read and it exists,
	 * false otherwise
	 */
	public boolean fileExistsAndIsReadable(String fileToBeRead){
		File file = new File(fileToBeRead);
		
		return file.exists() && file.canRead();
	}
	
	/**
	 * Gets the file type for this file, eg. html, image, plain etc.
	 * @param fileResource the file to check type for
	 * @return the file type as a string
	 */
	public String getFileMimeType(String fileResource){
		
		return URLConnection.guessContentTypeFromName(fileResource);
	}
	
	
	/**
	 * Copies the file to the specified outstream, this could also be used to send a large file over a socket if the out parameter
	 * is given a reference to the sockets OutputStream
	 * @param out the file/stream to copy the specified file to
	 * @param fileToCopy the file to be copied/sent
	 * @throws IOException (Includes FileNotFoundException if the specified file does not exist)
	 */
	public void copyFileToStream(OutputStream out, String fileToCopy) throws IOException{
		InputStream is = new FileInputStream(fileToCopy);
		int readBytes = 0;
		
		if(is != null){
			byte[] buffer = new byte[bufferSize];
			
			while((readBytes = is.read(buffer)) != -1){
				out.write(buffer, 0, readBytes);
			}
			is.close();
		}
	}
	
	
	
	
	/****************************************** NOT USED BELOW ***************************/
	
	/**
	 * Saves an object to a file
	 * @param object the object to be saved
	 * @param fileName the name of the file to save to, creates a new file if one does not exist
	 */
    public void saveFiles(Object object, String fileName) {

		try {
		    ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName));
		    out.writeObject(object);
	        out.close();
		} catch (FileNotFoundException e) {
		    System.out.println("File Not Found");
		} catch (IOException a) {
		    System.out.println("Ioexception in save");
	            a.printStackTrace();
		} 
    }
    
    /**
     * Reads the object from the file with the specified filename
     * @param fileName the filename of the file that contains the object
     * @return the object contained in the file, null if non exists
     */
    public Object readFile(String fileName) {
	
		Object object;
		try {
		    ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName));
		    object = in.readObject();
	        in.close();
		    return object;
		} catch (FileNotFoundException e) {
		    System.out.println("File Not Found");
		    return null;
		} catch (IOException a) {
		    System.out.println("empty file");
		} catch (ClassNotFoundException b) {
		    System.out.println("ClassNotFoundException");
		}
		return null;
    }
    
}


