package com.topwise.topos.appstore.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;

public class Signature {

    public static SignatureInfo getSignatureInfo(Context context) {
        SignatureInfo info = new SignatureInfo();
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            android.content.pm.Signature[] signs = packageInfo.signatures;
            android.content.pm.Signature sign = signs[0];
            byte[] signature = sign.toByteArray();
            X509Certificate cert = parseSignature(signature);
            info.signName = cert.getSigAlgName();
            info.pubKey = cert.getPublicKey().toString();
            info.serialNumber = cert.getSerialNumber().toString();
            info.sigAlgOID = cert.getSigAlgOID();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            info.startTime = sdf.format(cert.getNotBefore());
            info.endTime = sdf.format(cert.getNotAfter());
            info.subjectDN = cert.getSubjectDN().toString();
            info.MD5 = getMessageDigest("MD5", signature);
            info.SHA1 = getMessageDigest("SHA1", signature);
            info.SHA256 = getMessageDigest("SHA256", signature);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return info;
    }

    private static String getMessageDigest(String instance, byte[] signature) {
        String sinfo = null;
        try {
            MessageDigest md = MessageDigest.getInstance(instance);
            md.update(signature);
            byte[] digest = md.digest();
            sinfo = toHexString(digest);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sinfo;
    }

    public static X509Certificate parseSignature(byte[] signature) {
        X509Certificate cert = null;
        try {
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            cert = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(signature));
        } catch (CertificateException e) {
            e.printStackTrace();
        }
        return cert;
    }

    private static void byte2hex(byte b, StringBuffer buf) {
        char[] hexChars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        int high = ((b & 0xf0) >> 4);
        int low = (b & 0x0f);
        buf.append(hexChars[high]);
        buf.append(hexChars[low]);
    }

    /**
     * Converts a byte array to hex string
     */
    private static String toHexString(byte[] block) {
        StringBuffer buf = new StringBuffer();
        int len = block.length;
        for (int i = 0; i < len; i++) {
            byte2hex(block[i], buf);
            if (i < len - 1) {
                buf.append(":");
            }
        }
        return buf.toString();
    }

    public static class SignatureInfo {
        public String signName = "";
        public String pubKey = "";
        public String serialNumber = "";
        public String sigAlgOID = "";
        public String startTime = "";
        public String endTime = "";
        public String subjectDN = "";
        public String MD5 = "";
        public String SHA1 = "";
        public String SHA256 = "";

        @Override
        public String toString() {
            try {
                JSONObject json = new JSONObject();
                json.put("signName", signName);
                json.put("pubKey", pubKey);
                json.put("serialNumber", serialNumber);
                json.put("sigAlgOID", sigAlgOID);
                json.put("startTime", startTime);
                json.put("endTime", endTime);
                json.put("subjectDN", subjectDN);
                json.put("MD5", MD5);
                json.put("SHA1", SHA1);
                json.put("SHA256", SHA256);
                return json.toString();
            } catch (JSONException e) {
                return "";
            }
        }
    }
}
