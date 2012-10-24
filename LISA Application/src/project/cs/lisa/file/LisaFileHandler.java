/**
 * 
 * Uppsala University
 *
 * Project CS course, Fall 2012
 *
 * Projekt DV/Project CS, is a course in which the students develop software for
 * distributed systems. The aim of the course is to give insights into how a big
 * project is run (from planning to realization), how to construct a complex
 * distributed system and to give hands-on experience on modern construction
 * principles and programming methods.
 *
 * All rights reserved.
 *
 * Copyright (C) 2012 LISA team
 */

package project.cs.lisa.file;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

/**
 * Class used to display files to the user.
 * Raises intent and lets user select what he will use
 * to visualize the files. If no application is available,
 * We return a toast to the user stating that.
 * @author Thiago Costa Porto
 */

public class LisaFileHandler {
    private static final int ERR_NULL_PATH_RECEIVED = -1;
    private static final int ERR_NULL_TYPE_RECEIVED = -2;
    private static final int ERR_EMPTY_PATH_RECEIVED = -3;
    private static final int ERR_EMPTY_TYPE_RECEIVED = -4;
    private static final int ERR_FAILED_TO_OPEN_FILE = -5;
    private static final int ERR_FAILED_TO_FIND_APP = -6;
    private static final int OK = 1;
    
    private static final String TAG = "DisplayFile";
    
    private Intent mIntent;
    private File mFile;
    
    public int displayContent(Context context, String path, String mimetype) {
        Log.d(TAG, "Received file " + path);
        Log.d(TAG, "File type: " + mimetype);
        
        // Error handling
        if (path == null) {
            Log.d(TAG, "Received a null file. Stop.");
            return ERR_NULL_PATH_RECEIVED;
        }
        
        if (path == "") {
            Log.d(TAG, "Empty path. Stop.");
            return ERR_EMPTY_PATH_RECEIVED;
        }
        
        if (mimetype == null) {
            Log.d(TAG, "Received a null MIMEType. Stop.");
            return ERR_NULL_TYPE_RECEIVED;
        }
        
        if (mimetype == "") {
            Log.d(TAG, "Empty path. Stop.");
            return ERR_EMPTY_TYPE_RECEIVED;
        }
        
        mIntent = new Intent();
        mFile = new File(path);
        
        if (mFile == null) {
            Log.d(TAG, "Failed to open file. Stop.");
            return ERR_FAILED_TO_OPEN_FILE;
        }

        // Get list of apps who can open the file
        PackageManager pkgManager = context.getPackageManager();
        List<ResolveInfo> listAppsViewFile = pkgManager.queryIntentActivities(new Intent().setType(mimetype),
                PackageManager.MATCH_DEFAULT_ONLY);
        
        // List of applications that can open the file must have at least one element.
        // Otherwise, we cannot open the file.
        if (listAppsViewFile.size() > 0) {
            // Flag for starting new activity. This flag is needed and
            // makes the new activity -- the one that visualizes the file --
            // independent of our main activity.
            mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Set data fields
            mIntent.setDataAndType(Uri.fromFile(mFile), mimetype);
            
            // Start intent
            context.startActivity(mIntent);        
        }
        else {
            Log.d(TAG, "Could not visualize content!");
            Toast toast = Toast.makeText(context, "Failed to find an app to open " + path +
                    ". We can not handle the type of the file (" + mimetype + "). Maybe you" +
            		" should look at Google?" , Toast.LENGTH_LONG);
            toast.show();
            return ERR_FAILED_TO_FIND_APP;
        }
        
        return OK;
    }
    
    // TODO: Needs testing
    /**
     * Function that returns a file content-type. Remember that this obtained by the file
     * extension, so it is insecure.
     * @param path Path to the file
     * @return String with the MimeType
     */
    
    public static String getFileContentType(String path) {
        // Gets file extension
        String extension = MimeTypeMap.getFileExtensionFromUrl(path);
        // Returns MimeType
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }
}
