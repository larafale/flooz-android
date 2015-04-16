package me.flooz.app.Utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Random;

import javax.security.auth.x500.X500Principal;

import me.flooz.app.BuildConfig;

/**
 * Created by Flooz on 1/8/15.
 */
public class FLHelper {

    public static boolean isDebuggable()
    {
        return BuildConfig.DEBUG_API || BuildConfig.LOCAL_API;
    }

    public static String generateRandomString() {
        return generateRandomString(16);
    }

    public static String generateRandomString(int lenght) {
        String letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        String randomString = "";
        Random r = new Random();

        for (int i = 0; i < lenght; i++) {
            randomString += String.format("%C", letters.charAt(r.nextInt(letters.length())));
        }

        return randomString;
    }

    public static String trimTrailingZeros(String number) {
        if(!number.contains(".")) {
            return number;
        }

        return number.replaceAll("\\.?0*$", "");
    }
}
