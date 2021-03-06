package com.changhong.ttfileplore.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import com.changhong.ttfileplore.application.MyApp;
import com.changhong.ttfileplore.data.DownData;
import com.changhong.ttfileplore.data.PloreData;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.text.format.Formatter;
import android.util.Log;

@SuppressWarnings("unused")
public class Utils {
    static ArrayList<Content> results = new ArrayList<>();
    static final private Object lock = new Object();
    static ExecutorService pool;
    static DateFormat df = DateFormat.getDateTimeInstance();

    /**
     * @return 手机内部存储空间字符串数组（总空间，可用空间，可用百分比）
     */
    public static String[] getPhoneSpace(Context context) {
        try {
            StatFs fs = new StatFs("/storage/sdcard0");
            String totalSize = Formatter.formatFileSize(context, (fs.getTotalBytes()));
            String availableSize = Formatter.formatFileSize(context, (fs.getAvailableBytes()));
            return (new String[]{totalSize, availableSize,
                    (double) fs.getAvailableBytes() / (double) fs.getTotalBytes() * 100 + ""});
        }catch(IllegalArgumentException e){
            return (new String[]{"未装载", "未装载", "000"});
        }

    }

    /**
     * @return sd卡存储空间字符串数组（总空间，可用空间，可用百分比）
     */
    public static String[] getSdSpace(Context context) {
        File file = new File("/storage/sdcard1");
        if (file.exists() && null != file.list() && file.list().length != 0) {
            // if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            StatFs fs = new StatFs("/storage/sdcard1");
            // StatFs fs = new
            // StatFs(Environment.getExternalStorageDirectory().getPath());
            // Android API18之前：fs.getAvailableBlocks()*fs.getBlockSize()
            // Log.e("11", Environment.getExternalStorageDirectory().getPath());
            String totalSize = Formatter.formatFileSize(context, (fs.getTotalBytes()));
            String availableSize = Formatter.formatFileSize(context, (fs.getAvailableBytes()));
            // Log.e("11", fs.getAvailableBytes() + fs.getTotalBytes() + "");
            return (new String[]{totalSize, availableSize,
                    (double) fs.getAvailableBytes() / (double) fs.getTotalBytes() * 100 + ""});
        } else
            return (new String[]{"未装载", "未装载", "000"});

    }

    /**
     * @return Arraylist<content> content包含路径，标题，缩略图
     */
    public static ArrayList<Content> getVideo(Context context) {
        ArrayList<Content> results = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null, null,
                MediaStore.Video.Media.DEFAULT_SORT_ORDER);
        if (cursor != null) {
            cursor.moveToFirst();
            int counter = cursor.getCount();
            for (int j = 0; j < counter; j++) {
                int origId = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media._ID));

                Content video = new Content(cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA)),
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)),
                        MediaStore.Video.Thumbnails.getThumbnail(contentResolver, origId, Images.Thumbnails.MINI_KIND,
                                null));

                results.add(video);
                cursor.moveToNext();

            }
            cursor.close();
        }
        return results;
    }

    /**
     * @return Arraylist<content> content包含路径，标题，演唱者
     */
    public static ArrayList<Content> getMusic(Context context) {
        ArrayList<Content> results = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if (cursor != null) {
            cursor.moveToFirst();
            int counter = cursor.getCount();
            for (int j = 0; j < counter; j++) {
                String albumArt = cursor.getString(0);
                Bitmap bm;
                if (albumArt == null) {
                    Content content = new Content(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)),
                            cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)), null);

                    results.add(content);
                    cursor.moveToNext();
                } else {
                    bm = BitmapFactory.decodeFile(albumArt);
                    @SuppressWarnings("deprecation")
                    BitmapDrawable bmpDraw = new BitmapDrawable(bm);

                    Content video = new Content(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)),
                            cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)),
                            bmpDraw.getBitmap());

                    results.add(video);
                    cursor.moveToNext();

                }

            }
            cursor.close();
        }
        return results;
    }

    /**
     * 获取系统内图片相册
     *
     * @return 返回相册列表的Arraylist<content> content包含 dir:路径，(title:相册名)，缩略图
     */
    public static ArrayList<Content> getPhotoCata(Context context) {

        ArrayList<Content> results = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null,
                MediaStore.Images.Media.DEFAULT_SORT_ORDER);
        if (cursor != null) {
            cursor.moveToFirst();
            int counter = cursor.getCount();
            String tmp = "";
            for (int j = 0; j < counter; j++) {
                String dir = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));

                if (tmp.equalsIgnoreCase(dir.substring(0, dir.lastIndexOf("/")))) {
                    // Log.e("tmp","tmp"+tmp);
                    cursor.moveToNext();
                    continue;
                }

                tmp = dir.substring(0, dir.lastIndexOf("/"));
                // Log.e("dir", dir+"");
                int origId = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID));
                String[] path = dir.split("/");
                // Log.e("222",
                // cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)));
                Content photo = new Content(cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)),
                        // cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE)),
                        path[path.length - 2], MediaStore.Images.Thumbnails.getThumbnail(contentResolver, origId,
                        Images.Thumbnails.MINI_KIND, null),
                        origId);

                results.add(photo);
                cursor.moveToNext();

            }
            cursor.close();
        }
        return results;

    }

    public static ArrayList<Content> getDoc(Context context, Boolean reseach) {
        if (!reseach) {
            try {
                results = getObject("result_doc");
                if (results.size() == 0)
                    reSeach("doc");
            } catch (Exception e2) {
                reSeach("doc");
            }
            return results;

        } else {
            reSeach("doc");
            return results;
        }
    }

    private static void reSeach(String type) {

        pool = Executors.newFixedThreadPool(20);
        results.clear();
        File file = new File("/storage");
        if (type.equals("doc")) {
            GetResult gr1 = new Utils().new GetResult(file, "doc", 0);
            pool.submit(gr1);
        } else if (type.equals("zip")) {
            GetResult gr1 = new Utils().new GetResult(file, "zip", 0);
            pool.submit(gr1);
        }
        if (type.equals("apk")) {
            GetResult gr1 = new Utils().new GetResult(file, "apk", 0);
            pool.submit(gr1);
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e1) {

            e1.printStackTrace();
        }
        pool.shutdown();
        try {// 等待直到所有任务完成
            pool.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sortList(results);

        switch (type) {
            case "doc":
                saveObject("result_doc", results);
                break;
            case "zip":
                saveObject("result_zip", results);
                break;
            case "apk":
                saveObject("result_apk", results);
                break;
            default:
                break;
        }

    }

    public static ArrayList<Content> getApk(Context context, Boolean reseach) {
        if (!reseach) {
            try {
                results = getObject("result_apk");
                if (results.size() == 0)
                    reSeach("apk");
            } catch (Exception e) {
                reSeach("apk");
            }
            return results;
        } else
            reSeach("apk");
        return results;
    }

    public static ArrayList<Content> getZip(Context context, Boolean reseach) {
        if (!reseach) {
            try {
                results = getObject("result_zip");
                if (results.size() == 0)
                    reSeach("zip");

            } catch (Exception e2) {
                reSeach("zip");

            }
            return results;
        } else {
            reSeach("zip");
            return results;
        }
    }

    private static void sortList(List<Content> results) {
        int n = results.size();
        for (int i = 0; i < n - 1; i++)
            for (int j = i + 1; j < n; j++) {
                if (results.get(i).getTitle().charAt(0) > results.get(j).getTitle().charAt(0)) {
                    Content tmp = results.remove(i);
                    results.add(i, results.remove(j - 1));
                    results.add(j, tmp);
                }
            }
        for (int i = 0; i < results.size() - 1; i++)
            for (int j = i + 1; j < results.size(); j++) {
                if (results.get(i).getTitle().equals(results.get(j).getTitle())) {
                    results.remove(j);
                    j--;
                }
            }

    }

    @SuppressWarnings("unused")
    public static Bitmap getImageThumbnail(String imagePath, int width, int height) {
        Bitmap bitmap;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        // 获取这个图片的宽和高，注意此处的bitmap为null
        options.inJustDecodeBounds = false; // 设为 false
        // 计算缩放比
        int h = options.outHeight;
        int w = options.outWidth;
        int beWidth = w / width;
        int beHeight = h / height;
        int be;
        if (beWidth < beHeight) {
            be = beWidth;
        } else {
            be = beHeight;
        }
        if (be <= 0) {
            be = 1;
        }
        options.inSampleSize = be;
        // 重新读入图片，读取缩放后的bitmap，注意这次要把options.inJustDecodeBounds 设为 false
        bitmap = BitmapFactory.decodeFile(imagePath, options);
        // 利用ThumbnailUtils来创建缩略图，这里要指定要缩放哪个Bitmap对象
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        return bitmap;
    }

    /**
     * 使用系统工具打开文件
     *
     * @return 对应的Intent
     */
    public static Intent openFile(File file) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);

        String type = getMIMEType(file);
        intent.setDataAndType(Uri.fromFile(file), type);
        return intent;
    }

    public static String getMIMEType(File file) {
        String type;
        String name = file.getName();

        String end = name.substring(name.lastIndexOf(".") + 1, name.length()).toLowerCase();
        if (end.equals("m4a") || end.equals("mp3") || end.equals("wav")) {
            type = "audio";
        } else if (end.equals("mp4") || end.equals("3gp")) {
            type = "video";
        } else if (end.equals("jpg") || end.equals("png") || end.equals("jpeg") || end.equals("bmp")
                || end.equals("gif")) {
            type = "image";
        } else {
            type = "*";
        }
        type += "/*";
        return type;
    }

    class GetResult implements Runnable {
        File file;
        String type;
        int n = 0;

        public GetResult(File file, String type, int n) {
            this.file = file;
            this.type = type;
            this.n = n;
        }

        public void run() {
            if (file.isDirectory() && file.canRead()) {
                String[] files = file.list();
                if (null == files)
                    return;
                for (String tmpStr : files) {
                    if (n < 2) {
                        try {
                            pool.execute(new GetResult(new File(file.getPath() + "/" + tmpStr), type, n + 1));
                        } catch (RejectedExecutionException e) {
                            getresult(new File(file.getPath() + "/" + tmpStr), type);
                        }
                    } else
                        getresult(new File(file.getPath() + "/" + tmpStr), type);
                }

            } else if (file.canRead()) {
                String name = file.getName();
                String end = name.substring(name.lastIndexOf(".") + 1, name.length()).toLowerCase();
                if (type.equals("zip")) {
                    if (end.equals("zip") || end.equals("rar") || end.equals("7z")) {
                        synchronized (lock) {
                            Date date = new Date(file.lastModified());
                            results.add(new Content(file.getPath(), file.getName(), null,
                                    df.format(date), null));
                        }

                    }
                } else if (type.equals("doc")) {
                    if (end.equals("txt") || end.equals("doc") || end.equals("docx")) {
                        synchronized (lock) {
                            Date date = new Date(file.lastModified());

                            results.add(new Content(file.getPath(), file.getName(), null,
                                    df.format(date), null));
                        }
                    }
                } else if (type.equals("apk")) {
                    if (end.equals("apk")) {
                        synchronized (lock) {
                            Date date = new Date(file.lastModified());
                            results.add(new Content(file.getPath(), file.getName(), null,
                                    df.format(date), null));
                        }
                    }
                }
            }
        }
    }

    /**
     * 从指定文件递归获取文件列表
     *
     * @param type "zip","doc","apk"其中之一
     */
    public static void getresult(File file, String type) {

        if (type.equals("zip")) {
            if (file.isDirectory() && file.canRead()) {
                String[] files = file.list();
                if (null == files)
                    return;
                for (String tmpStr : files)
                    getresult(new File(file.getPath() + "/" + tmpStr), type);

            } else if (file.canRead()) {
                String name = file.getName();
                Date date = new Date(file.lastModified());
                String end = name.substring(name.lastIndexOf(".") + 1, name.length()).toLowerCase();
                if (end.equals("zip") || end.equals("rar") || end.equals("7z")) {
                    results.add(new Content(file.getPath(), file.getName(), null,
                            df.format(date), null));
                }
            }

        } else if (type.equals("doc")) {
            if (file.isDirectory() && file.canRead()) {
                String[] files = file.list();
                if (null == files)
                    return;
                for (String tmpStr : files) {
                    getresult(new File(file.getPath() + "/" + tmpStr), type);

                }

            } else if (file.canRead()) {
                Date date = new Date(file.lastModified());
                String name = file.getName();
                String end = name.substring(name.lastIndexOf(".") + 1, name.length()).toLowerCase();
                if (end.equals("doc") || end.equals("docx") || end.equals("txt")) {
                    results.add(new Content(file.getPath(), file.getName(), null,
                            df.format(date), null));
                }
            }

        } else if (type.equals("apk")) {
            if (file.isDirectory() && file.canRead()) {
                String[] files = file.list();
                if (null == files)
                    return;
                for (String tmpStr : files)
                    getresult(new File(file.getPath() + "/" + tmpStr), type);

            } else if (file.canRead()) {
                String name = file.getName();
                Date date = new Date(file.lastModified());
                String end = name.substring(name.lastIndexOf(".") + 1, name.length()).toLowerCase();
                if (end.equals("apk")) {
                    results.add(new Content(file.getPath(), file.getName(), null,
                            df.format(date), null));
                }
            }

        }

    }

    /**
     * 从指定文件获得对象
     *
     * @param name 文件名
     * @return content列表
     */
    @SuppressWarnings("unchecked")
    static ArrayList<Content> getObject(String name) {
        ArrayList<Content> savedArrayList = new ArrayList<>();
        File file = new File(getPath(MyApp.context.get(), name));
        if (file.exists()) {
            FileInputStream fileInputStream;
            ObjectInputStream objectInputStream;
            try {
                fileInputStream = new FileInputStream(file.toString());
                objectInputStream = new ObjectInputStream(fileInputStream);
                Object obj = objectInputStream.readObject();
                if (obj != null) {
                    savedArrayList = (ArrayList<Content>) obj;
                }
                objectInputStream.close();
                fileInputStream.close();
                return savedArrayList;
            } catch (IOException | ClassNotFoundException e) {
                Log.e("e", e.getMessage());
                return savedArrayList;
            }

        } else {
            savedArrayList = new ArrayList<>();
            return savedArrayList;
        }

    }


    public static ArrayList<DownData> getDownDataObject(String name) {
        ArrayList<DownData> savedArrayList = new ArrayList<>();
        File file = new File(getPath(MyApp.context.get(), name));
        FileInputStream fileInputStream;
        ObjectInputStream objectInputStream;
        try {
            fileInputStream = new FileInputStream(file.toString());
            objectInputStream = new ObjectInputStream(fileInputStream);
            Object obj = objectInputStream.readObject();
            if (obj != null) {
                savedArrayList = (ArrayList<DownData>) obj;
            }
            objectInputStream.close();
            fileInputStream.close();
        } catch (IOException | ClassNotFoundException e) {
            Log.e("e", e.getMessage());
            return savedArrayList;
        }
        return savedArrayList;

    }

    /**
     * @param name : file's name
     * @param obj  : Object
     */
    public static void saveObject(String name, Object obj) {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        File file = new File(getPath(MyApp.context.get(), name));
        if (!file.getParentFile().exists()) {
            if (!file.getParentFile().mkdirs())
                return;
        }

        if (!file.exists()) {
            try {
                if (!file.createNewFile())
                    return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            fos = new FileOutputStream(file);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(obj);
        } catch (Exception e) {
            e.printStackTrace();
            // 这里是保存文件产生异常
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    // fos流关闭异常
                    e.printStackTrace();
                }
            }
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    // oos流关闭异常
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * @param type    : one of
     *                "result_movie","result_music","result_photo","result_apk",
     *                "result_doc"，"result_zip"
     * @param context : context
     * @return counts of type
     */
    public static int getCount(String type, Context context) {
        ArrayList<Content> results;
        ArrayList<File> files = new ArrayList<>();
        int count = 0;
        if (type.equals("result_movie")) {

            ContentResolver contentResolver = context.getContentResolver();
            Cursor cursor = contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null, null,
                    MediaStore.Video.Media.DEFAULT_SORT_ORDER);
            if (cursor != null) {
                cursor.moveToFirst();
                count = cursor.getCount();
                cursor.close();
            }
        } else if (type.equals("result_music")) {

            ContentResolver contentResolver = context.getContentResolver();
            Cursor cursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
                    MediaStore.Video.Media.DEFAULT_SORT_ORDER);
            if (cursor != null) {
                cursor.moveToFirst();
                count = cursor.getCount();
                cursor.close();
            }
        } else if (type.equals("result_photo")) {

            ContentResolver contentResolver = context.getContentResolver();
            Cursor cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null,
                    MediaStore.Video.Media.DEFAULT_SORT_ORDER);
            if (cursor != null) {
                cursor.moveToFirst();
                count = cursor.getCount();
                cursor.close();
            }
        } else if (type.equals("result_qq")) {
            File file = new File("/storage/sdcard0/tencent/QQfile_recv");
            File sdfile = new File("/storage/sdcard1/tencent/QQfile_recv");


            if (file.exists() && file.isDirectory()) {
                PloreData mPloreData = new PloreData(file, true);
                files.addAll(mPloreData.getfiles());
            }
            if (sdfile.exists() && sdfile.isDirectory()) {
                PloreData mPloreData = new PloreData(sdfile, true);
                files.addAll(mPloreData.getfiles());
            }
            for (int i = 0; i < files.size(); i++) {
                if (files.get(i).getName().startsWith(".")) {
                    files.remove(i);
                    i--;
                }
            }
            return files.size();
        } else if (type.equals("result_wechat")) {
            ArrayList<File> wcfiles = new ArrayList<File>();

            File file = new File("/storage/sdcard0/tencent/MicroMsg");
            File sdfile = new File("/storage/sdcard1/tencent/MicroMsg");
            if (file.exists()) {
                File[] mfils1 = file.listFiles();
                for (File tmpFile : mfils1) {
                    if (tmpFile.getName().length() > 25 && tmpFile.isDirectory())
                        wcfiles.add(tmpFile);

                }
            }
            if (sdfile.exists()) {
                File[] mfils2 = sdfile.listFiles();
                for (File tmpFile : mfils2) {
                    if (tmpFile.getName().length() > 25 && tmpFile.isDirectory())
                        wcfiles.add(tmpFile);

                }
            }
            for (int i = 0; i < wcfiles.size(); i++) {

                file = new File(wcfiles.get(i).getPath() + "/video");

                files.addAll(new PloreData(file, true).getfiles());
            }
            for (int i = 0; i < files.size(); i++) {
                if (files.get(i).getName().startsWith(".") || !Utils.getMIMEType(files.get(i)).equals("video/*")) {
                    files.remove(i);
                    i--;
                }

            }
            return files.size();

        } else {
            try {
                results = getObject(type);
                count = results.size();

            } catch (Exception e) {
                count = 0;
            }
        }
        return count;
    }

    /**
     * @param name 文件名
     * @return 文件类型 one of {"audio","video","image","*","zip","doc","apk"}
     */
    public static String getMIMEType(String name) {
        String type;
        String end = name.substring(name.lastIndexOf(".") + 1, name.length()).toLowerCase();
        if (end.equals("m4a") || end.equals("mp3") || end.equals("wav")) {
            type = "audio";
        } else if (end.equals("mp4") || end.equals("3gp") || end.equals("avi")) {
            type = "video";
        } else if (end.equals("jpg") || end.equals("png") || end.equals("jpeg") || end.equals("bmp")
                || end.equals("gif")) {
            type = "image";
        } else if (end.equals("doc") || end.equals("docx") || end.equals("pdf") || end.equals("txt")) {
            type = "doc";
        } else if (end.equals("apk")) {
            type = "apk";
        } else if (end.equals("zip") || end.equals("rar") || end.equals("7z")) {
            type = "zip";
        } else {
            type = "*";
        }
        return type;

    }

    /**
     * 根据传入参数生成二维码
     *
     * @param text   待转化的字符串
     * @param width  二维码的宽度
     * @param height 二维码的高度
     * @return 二维码的bitmap
     */
    @SuppressWarnings("unused")
    static public Bitmap createImage(String text, int width, int height) {
        Bitmap bitmap = null;
        try {
            // 需要引入core包
            QRCodeWriter writer = new QRCodeWriter();

            if (text == null || "".equals(text) || text.length() < 1) {
                return null;
            }

            // 把输入的文本转为二维码
            BitMatrix martix = writer.encode(text, BarcodeFormat.QR_CODE, width, height);

            //	System.out.println("w:" + martix.getWidth() + "h:" + martix.getHeight());

            Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            BitMatrix bitMatrix = new QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, width, height, hints);
            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * width + x] = 0xff000000;
                    } else {
                        pixels[y * width + x] = 0xffffffff;
                    }

                }
            }

            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

        } catch (WriterException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    static public String getPath(Context context, String name) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir() != null ? context.getExternalCacheDir().getPath() :
                    context.getCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return cachePath + "/" + name + "/";

    }

    static public int dpTopx(int dp, Context context) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}
