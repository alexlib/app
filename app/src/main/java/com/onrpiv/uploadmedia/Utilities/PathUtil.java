package com.onrpiv.uploadmedia.Utilities;

/**
 * author: sarbajit mukherjee
 * Created by sarbajit mukherjee on 09/07/2020.
 */

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.onrpiv.uploadmedia.BuildConfig;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

public class PathUtil {

    public static String getRealPath(Context context, Uri fileUri) {
        String realPath;
        // SDK < API11
        if (Build.VERSION.SDK_INT < 11) {
            realPath = PathUtil.getRealPathFromURI_BelowAPI11(context, fileUri);
        }
        // SDK >= 11 && SDK < 19
        else if (Build.VERSION.SDK_INT < 19) {
            realPath = PathUtil.getRealPathFromURI_API11to18(context, fileUri);
        }
        // SDK > 19 (Android 4.4) and up
        else {
            realPath = PathUtil.getRealPathFromURI_API19(context, fileUri);
        }
        return realPath;
    }


    @SuppressLint("NewApi")
    public static String getRealPathFromURI_API11to18(Context context, Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        String result = null;

        CursorLoader cursorLoader = new CursorLoader(context, contentUri, proj, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();

        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            result = cursor.getString(column_index);
            cursor.close();
        }
        return result;
    }

    public static String getRealPathFromURI_BelowAPI11(Context context, Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        int column_index = 0;
        String result = "";
        if (cursor != null) {
            column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            result = cursor.getString(column_index);
            cursor.close();
            return result;
        }
        return result;
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @author paulburke
     */
    @SuppressLint("NewApi")
    public static String getRealPathFromURI_API19(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.parseLong(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return getPathFromInputStreamUri(context, uri);

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return uri.toString();
    }

    /**
     * Get path of a stream Uri (e.g. Google Photos) by creating a temporary file in app's data directory.
     * see https://stackoverflow.com/questions/35909008/pick-image-from-gallery-or-google-photos-failing/50253933#50253933
     * @param context context of the activity; Needed for app's data directory
     * @param uri stream uri
     * @return path of newly created temp file created from uri data stream
     */
    public static String getPathFromInputStreamUri(Context context, Uri uri) {
        InputStream inputStream = null;
        String filePath = null;

        String dataDir = context.getApplicationInfo().dataDir;

        try {
            inputStream = context.getContentResolver().openInputStream(uri);
            File file = createTemporalFileFrom(inputStream, dataDir);
            filePath = file.getPath();
        } catch (FileNotFoundException e) {
            Log.getStackTraceString(e);
        } catch (IOException e) {
            Log.getStackTraceString(e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return filePath;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static File getUserDirectory(Context context, String userName) {
        File userDir = new File(context.getExternalFilesDir(null), "miPIV_" + userName);
        return checkDir(userDir);
    }

    public static File getFramesDirectory(Context context, String userName) {
        File result = new File(getUserDirectory(context, userName), "Extracted_Frames");
        return checkDir(result);
    }

    public static File getFramesNumberedDirectory(Context context, String userName, int framesDirNum) {
        File result = new File(getFramesDirectory(context, userName), "Frames_"+framesDirNum);
        return checkDir(result);
    }

    public static File getExperimentsDirectory(Context context, String userName) {
        File result = new File(getUserDirectory(context, userName), "Experiments");
        return checkDir(result);
    }

    public static File getExperimentNumberedDirectory(Context context, String userName, int expDirNum) {
        File result = new File(getExperimentsDirectory(context, userName), "Experiment_"+expDirNum);
        return checkDir(result);
    }

    public static String getExperimentImageFileSuffix(int currentExperiment) {
        return "_Experiment_" + currentExperiment + ".png";
    }

    public static String getExperimentTextFileSuffix(int currentExperiment) {
        return "_Experiment_" + currentExperiment + ".txt";
    }

    public static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : Objects.requireNonNull(fileOrDirectory.listFiles())) {
                deleteRecursive(child);
            }
        }
        fileOrDirectory.delete();
    }

    private static File checkDir(File dir) {
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                Log.d("MAKEDIRS", "Directory created successfully.");
            } else {
                Log.d("MAKEDIRS", "Failed to create directory.");
            }
        }
        return dir;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.contentprovider".equals(uri.getAuthority());
    }

    /**
     * @param context VideoActivity context. Needed for data directory.
     * @param path Path of the video.
     * @return True if temp file and deleted successfully, otherwise False.
     */
    public static boolean deleteIfTempFile(Context context, String path) {
        File tempFile = new File(context.getExternalFilesDir(null), "tempFile");
        boolean result = false;

        if (tempFile.getAbsolutePath().equals(path)) {
            result = tempFile.delete();
        }

        return result;
    }

    public static Uri getTempFileUri(Context context) {
        return FileProvider.getUriForFile(context,
                BuildConfig.APPLICATION_ID + ".provider",
                new File(context.getExternalFilesDir(null), "tempFile"));
    }

    public static String getTempFilePath(Context context) {
        return new File(context.getExternalFilesDir(null), "tempFile").getAbsolutePath();
    }

    /**
     * Create a temporary file from a data stream in the app's data directory.
     * @param inputStream input data stream
     * @param dataDir app's data directory
     * @return File object pointing to the newly created temp file
     * @throws IOException if data stream is unable to close.
     */
    private static File createTemporalFileFrom(InputStream inputStream, String dataDir) throws IOException {
        File targetFile = null;

        if (inputStream != null) {
            int read;
            byte[] buffer = new byte[8 * 1024];

            targetFile = new File(dataDir, "tempFile");
            OutputStream outputStream = new FileOutputStream(targetFile);

            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();

            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return targetFile;
    }
}