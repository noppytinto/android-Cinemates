package mirror42.dev.cinemates.utilities;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

import androidx.security.crypto.EncryptedFile;
import androidx.security.crypto.MasterKey;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import mirror42.dev.cinemates.model.User;


public class MyUtilities {

    /**
     * Return date in specified format.
     * @param milliSeconds Date in milliseconds
     * @return String representing date in specified format
     */
    public static String convertMillisInDate(long milliSeconds)
    {
        // date format examples
        // System.out.println(getDate(82233213123L, "dd/MM/yyyy hh:mm:ss.SSS"));

        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    public static long convertStringDateInMillis(String myDate) {
        // NOTE: from epoch
        // f.e. :
        // String myDate = "1990/01/01 00:00:00";
        // SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        // String myDate = "1990/01/01";
        // SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");

        Date date = null;
        long result = (long) 0.0;
        try {
            date = sdf.parse(myDate);
            result = date.getTime();

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static void encryptFile(String filename, String rawData, Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            // Creates a file with this name, or replaces an existing file
            // that has the same name. Note that the file name cannot contain
            // path separators.
            File fileToWrite = new File(context.getFilesDir(), filename);
            EncryptedFile encryptedFile = new EncryptedFile.Builder(context,
                    fileToWrite,
                    masterKey,
                    EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build();

            // File cannot exist before using openFileOutput
            if (fileToWrite.exists()) {
                fileToWrite.delete();
            }

            byte[] fileContent = rawData.getBytes(StandardCharsets.UTF_8);
            OutputStream outputStream = encryptedFile.openFileOutput();
            outputStream.write(fileContent);
            outputStream.flush();
            outputStream.close();

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String decryptFile(String filename, Context context) {
        String result=null;

        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            EncryptedFile encryptedFile = new EncryptedFile.Builder(context,
                    new File(context.getFilesDir(), filename),
                    masterKey,
                    EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build();

            InputStream inputStream = encryptedFile.openFileInput();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            int nextByte = inputStream.read();
            while (nextByte != -1) {
                byteArrayOutputStream.write(nextByte);
                nextByte = inputStream.read();
            }

            result = byteArrayOutputStream.toString();

        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
        }


        return result;
    }

    public static boolean checkFileExists(String filename, Context context) {
        File file = new File(context.getFilesDir(), filename);
        return file.exists();
    }

    public static boolean deletFile(String filename, Context context) {
        boolean result = false;
        File file = new File(context.getFilesDir(), filename);
        if(file.exists()) {
            file.delete();
            result  = true;
        }

        return result;
    }

    public static byte[] getSHA(String input) throws NoSuchAlgorithmException {
        // Static getInstance method is called with hashing SHA
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        // digest() method called
        // to calculate message digest of an input
        // and return array of byte
        return md.digest(input.getBytes(StandardCharsets.UTF_8));
    }

    public static String toHexString(byte[] hash) {
        // Convert byte array into signum representation
        BigInteger number = new BigInteger(1, hash);

        // Convert message digest into hex value
        StringBuilder hexString = new StringBuilder(number.toString(16));

        // Pad with leading zeros
        while (hexString.length() < 32)
        {
            hexString.insert(0, '0');
        }

        return hexString.toString();
    }

    public static String SHA256encrypt(String message) {
        if(message== null || message.isEmpty()) return null;

        String hex = null;

        try {
            hex = toHexString(getSHA(message));
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return hex;
    }

    public static void showToast(String message, Context context) {
        // print response
        final Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.show();
    }// end showToastOnUiThread()

    public static void showCenteredToast(String message, Context context) {
        // print response
        final Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }// end showToastOnUiThread()

    public static void showCenteredToastLong(String message, Context context) {
        // print response
        final Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }// end showToastOnUiThread()

    public static void showCenteredToastOnUiThread(String message, Activity activity) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // print response
                final Toast toast = Toast.makeText(activity.getApplicationContext(), message, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        });
    }// end showToastOnUiThread()

    public static String convertUserInJSonString(User user) {
        String string = null;

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("Username", user.getUsername());
            jsonObject.put("Email", user.getEmail());
            jsonObject.put("Name", user.getFirstName());
            jsonObject.put("LastName", user.getLastName());
            jsonObject.put("BirthDate", user.getBirthDate());
            jsonObject.put("ProfileImage", user.getProfilePicturePath());
            jsonObject.put("AccessToken", user.getAccessToken());
            jsonObject.put("Analytics", user.getAnalytics());

            string = jsonObject.toString();

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return string;
    }

    public static void showToastOnUiThread(String message, Activity activity) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // print response
                final Toast toast = Toast.makeText(activity, message, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }// end showToastOnUiThread()

    public static String convertStringDateToStringSqlDate(String date) {
        if(date==null || date.isEmpty()) return null;

        Date parsed = null;
        SimpleDateFormat format = null;
        String sqldate = null;
        try {
            format = new SimpleDateFormat("dd-MM-yyyy");
            parsed = format.parse(date);
        } catch (Exception e) {
            e.printStackTrace();
            format = new SimpleDateFormat("dd/MM/yyyy");
            try {
                parsed = format.parse(date);
            } catch (ParseException parseException) {
                parseException.printStackTrace();

            }
        }
        java.sql.Date sqlStartDate = new java.sql.Date(parsed.getTime());
        sqldate = String.valueOf(sqlStartDate);

        return sqldate;
    }

    public static java.sql.Date convertStringDateToSqlDate(String date) {
        if(date==null || date.isEmpty()) return null;

        Date parsed = null;
        SimpleDateFormat format = null;
        java.sql.Date sqldate = null;
        try {
            format = new SimpleDateFormat("dd-MM-yyyy");
            parsed = format.parse(date);
        } catch (Exception e) {
            e.printStackTrace();
            format = new SimpleDateFormat("dd/MM/yyyy");
            try {
                parsed = format.parse(date);
            } catch (ParseException parseException) {
                parseException.printStackTrace();
            }
        }
        sqldate = new java.sql.Date(parsed.getTime());

        return sqldate;
    }

}// end MyUtilities class
