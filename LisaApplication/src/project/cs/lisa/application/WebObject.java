package project.cs.lisa.application;

import java.io.File;

/**
 * A representation of a web object.
 * @author paolo
 *
 */
public class WebObject {

    private String mContentType;
    private File mFile;
    private String mHash;
   
    public WebObject(String contentType, File file, String hash) {
        mContentType = contentType;
        mFile = file;
        mHash = hash;
    }

    public String getContentType() {
        return mContentType;
    }

    public File getFile() {
        return mFile;
    }

    public String getHash() {
        return mHash;
    }
}
