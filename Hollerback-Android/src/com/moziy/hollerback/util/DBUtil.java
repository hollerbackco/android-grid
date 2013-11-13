package com.moziy.hollerback.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import android.os.Environment;

import com.moziy.hollerback.HollerbackApplication;

public class DBUtil {

    public static void copyDbToSdcard() {
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String databaseName = "hollerback.db";
                String currentDBPath = "//data//" + HollerbackApplication.getInstance().getPackageName() + "//databases//" + databaseName;
                String backupDBPath = databaseName;
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            }
        } catch (Exception e) {
        }
    }
}
