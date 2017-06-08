//package com.ggl.security;
//
//
//
//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.InputStream;
//import java.io.OutputStream;
//
//import it.sauronsoftware.base64.Base64;
//
///**
// * <p>
// * BASE64蝻�圾��極����
// * </p>
// * <p>
// * 靘�avabase64-1.3.1.jar
// * </p>
// * 
// * @author IceWee
// * @date 2012-5-19
// * @version 1.0
// */
//public class Base64Utils {
//
//    /**
//     * ��辣霂餃���憭批��
//     */
//    private static final int CACHE_SIZE = 1024;
//    
//    /**
//     * <p>
//     * BASE64摮泵銝脰圾��蛹鈭���
//     * </p>
//     * 
//     * @param base64
//     * @return
//     * @throws Exception
//     */
//    public static byte[] decode(String base64) throws Exception {
//        return Base64.decode(base64.getBytes());
//    }
//    
//    /**
//     * <p>
//     * 鈭���蝻�蛹BASE64摮泵銝�
//     * </p>
//     * 
//     * @param bytes
//     * @return
//     * @throws Exception
//     */
//    public static String encode(byte[] bytes) throws Exception {
//        return new String(Base64.encode(bytes));
//    }
//    
//    /**
//     * <p>
//     * 撠�辣蝻�蛹BASE64摮泵銝�
//     * </p>
//     * <p>
//     * 憭扳�辣��嚗�隡紡����滯�
//     * </p>
//     * 
//     * @param filePath ��辣蝏笆頝臬��
//     * @return
//     * @throws Exception
//     */
//    public static String encodeFile(String filePath) throws Exception {
//        byte[] bytes = fileToByte(filePath);
//        return encode(bytes);
//    }
//    
//    /**
//     * <p>
//     * BASE64摮泵銝脰蓮���辣
//     * </p>
//     * 
//     * @param filePath ��辣蝏笆頝臬��
//     * @param base64 蝻��泵銝�
//     * @throws Exception
//     */
//    public static void decodeToFile(String filePath, String base64) throws Exception {
//        byte[] bytes = decode(base64);
//        byteArrayToFile(bytes, filePath);
//    }
//    
//    /**
//     * <p>
//     * ��辣頧祆銝箔���蝏�
//     * </p>
//     * 
//     * @param filePath ��辣頝臬��
//     * @return
//     * @throws Exception
//     */
//    public static byte[] fileToByte(String filePath) throws Exception {
//        byte[] data = new byte[0];
//        File file = new File(filePath);
//        if (file.exists()) {
//            FileInputStream in = new FileInputStream(file);
//            ByteArrayOutputStream out = new ByteArrayOutputStream(2048);
//            byte[] cache = new byte[CACHE_SIZE];
//            int nRead = 0;
//            while ((nRead = in.read(cache)) != -1) {
//                out.write(cache, 0, nRead);
//                out.flush();
//            }
//            out.close();
//            in.close();
//            data = out.toByteArray();
//         }
//        return data;
//    }
//    
//    /**
//     * <p>
//     * 鈭������辣
//     * </p>
//     * 
//     * @param bytes 鈭���
//     * @param filePath ��辣���敶�
//     */
//    public static void byteArrayToFile(byte[] bytes, String filePath) throws Exception {
//        InputStream in = new ByteArrayInputStream(bytes);   
//        File destFile = new File(filePath);
//        if (!destFile.getParentFile().exists()) {
//            destFile.getParentFile().mkdirs();
//        }
//        destFile.createNewFile();
//        OutputStream out = new FileOutputStream(destFile);
//        byte[] cache = new byte[CACHE_SIZE];
//        int nRead = 0;
//        while ((nRead = in.read(cache)) != -1) {   
//            out.write(cache, 0, nRead);
//            out.flush();
//        }
//        out.close();
//        in.close();
//    }
//    
//    
//}