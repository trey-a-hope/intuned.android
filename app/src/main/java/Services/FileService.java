package Services;

import java.io.File;
import java.io.FileInputStream;

public class FileService {
    private static FileService _fileServiceInstance = new FileService();

    public static FileService getInstance() {
        return _fileServiceInstance;
    }

    private FileService() {
    }

    public byte[] readContentIntoByteArray(File file) {
        FileInputStream fileInputStream = null;
        byte[] bFile = new byte[(int) file.length()];
        try {
            //convert file into array of bytes
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(bFile);
            fileInputStream.close();
            for (int i = 0; i < bFile.length; i++) {
                System.out.print((char) bFile[i]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bFile;
    }
}
