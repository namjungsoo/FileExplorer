package com.duongame.explorer.bitmap;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfRenderer;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

import com.duongame.R;
import com.duongame.explorer.adapter.ExplorerItem;
import com.duongame.explorer.helper.FileHelper;

import net.lingala.zip4j.exception.ZipException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import static android.graphics.Bitmap.createBitmap;
import static com.duongame.explorer.adapter.ExplorerItem.Side.LEFT;
import static com.duongame.explorer.adapter.ExplorerItem.Side.RIGHT;

/**
 * Created by namjungsoo on 2016. 11. 17..
 */

public class BitmapLoader {
    public static final String TAG = "BitmapLoader";
    public static final int MICRO_KIND_SIZE = 96;

    public static class BitmapOrDrawable {
        public Bitmap bitmap;
        public Drawable drawable;
    }

    public static BitmapOrDrawable loadThumbnail(Context context, ExplorerItem.FileType type, String path) {
        BitmapOrDrawable bod = new BitmapOrDrawable();

        switch (type) {
            case APK:
                bod.drawable = BitmapLoader.loadApkThumbnailDrawable(context, path);
                break;
            case PDF:
                bod.bitmap = BitmapLoader.loadPdfThumbnailBitmap(context, path);
                break;
            case IMAGE:
                bod.bitmap = BitmapLoader.loadImageThumbnailBitmap(context, path);
                break;
            case VIDEO:
                bod.bitmap = BitmapLoader.loadVideoThumbnailBitmap(context, path);
                break;
            case ZIP:
                bod.bitmap = BitmapLoader.loadZipThumbnailBitmap(context, path);
                break;
        }

        return bod;
    }

    public static Drawable loadApkThumbnailDrawable(Context context, String path) {
        Drawable drawable = BitmapCacheManager.getDrawable(path);
        if (drawable != null)
            return drawable;

        final PackageManager pm = context.getPackageManager();
        final PackageInfo pi = pm.getPackageArchiveInfo(path, 0);

        if (pi != null) {
            pi.applicationInfo.sourceDir = path;
            pi.applicationInfo.publicSourceDir = path;
            drawable = pi.applicationInfo.loadIcon(pm);

            if (drawable != null) {
                BitmapCacheManager.setDrawable(path, drawable);
                return drawable;
            }
        }

        return drawable;
    }

    public static Bitmap loadImageThumbnailBitmap(Context context, String path) {
        Bitmap bitmap = BitmapCacheManager.getThumbnail(path);
        if (bitmap != null)
            return bitmap;

        // 시스템에서 찾은거
        bitmap = BitmapLoader.getThumbnail(context, path, true);
        if (bitmap != null) {
            BitmapCacheManager.setThumbnail(path, bitmap);
            return bitmap;
        }

        // 직접 생성
        bitmap = BitmapLoader.decodeSquareThumbnailFromFile(path, MICRO_KIND_SIZE, true);
        if (bitmap != null) {
            BitmapCacheManager.setThumbnail(path, bitmap);
            return bitmap;
        }

        return bitmap;
    }

    public static Bitmap loadVideoThumbnailBitmap(Context context, String path) {
        Bitmap bitmap = BitmapCacheManager.getThumbnail(path);
        if (bitmap != null)
            return bitmap;

        // 시스템에서 찾은거
        bitmap = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Images.Thumbnails.MICRO_KIND);
        if (bitmap != null) {
            BitmapCacheManager.setThumbnail(path, bitmap);
            return bitmap;
        }

        //TODO: 직접 만드는 것도 넣어야 한다.
        return bitmap;
    }

    public static Bitmap loadPdfThumbnailBitmap(Context context, String path) {
        Bitmap bitmap = BitmapCacheManager.getThumbnail(path);
        if (bitmap != null)
            return bitmap;

        // 직접 생성
        bitmap = BitmapLoader.decodeSquareThumbnailFromPdfFile(path, MICRO_KIND_SIZE);
        if (bitmap != null) {
            BitmapCacheManager.setThumbnail(path, bitmap);
            return bitmap;
        }

        return bitmap;
    }

    public static String getZipThumbnailFileName(Context context, String path) {
        // ZIP파일 안에 있는 이미지 파일을 찾자.
        String image = null;
        try {
            //image = ZipLoader.getFirstImage(context, path);
            final ZipLoader loader = new ZipLoader();
            final ArrayList<ExplorerItem> imageList = loader.load(context, path, null, 0, ExplorerItem.Side.LEFT, true);

            if (imageList != null && imageList.size() > 0) {
                image = imageList.get(0).path;
            }
        } catch (ZipException e) {
            return null;
        }
        return image;
    }

    public static Bitmap loadZipThumbnailBitmap(Context context, String path) {
        Bitmap bitmap = BitmapCacheManager.getThumbnail(path);
        if (bitmap != null)
            return bitmap;

        final String image = getZipThumbnailFileName(context, path);

        // 못찾았을 경우에는 기본 ZIP 아이콘이 뜨게 한다.
        if (image == null) {
            bitmap = BitmapCacheManager.getResourceBitmap(context.getResources(), R.drawable.zip);
            if (bitmap != null) {
                BitmapCacheManager.setThumbnail(path, bitmap);
            }
        } else {
            bitmap = BitmapLoader.decodeSquareThumbnailFromFile(image, MICRO_KIND_SIZE, false);
            if (bitmap != null) {
                BitmapCacheManager.setThumbnail(path, bitmap);
            }
        }

        return bitmap;
    }


    public static void writeDebugBitmap(String path, Bitmap bitmap) {
        File file = new File(path);
        String name = file.getName();
        name = name.replace(".zip", ".png");
        String extPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + name;
        try {
            OutputStream os = new FileOutputStream(extPath);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
            os.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Bitmap getThumbnail(Context context, String path, boolean exifRotation) {
        final Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.MediaColumns._ID},
                MediaStore.MediaColumns.DATA + "=?",
                new String[]{path}, null);
        if (cursor != null && cursor.moveToFirst()) {
            final int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));

            cursor.close();
//            cursor = context.getContentResolver().query(
//                    MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
//                    new String[]{MediaStore.Images.Thumbnails.DATA},
//                    MediaStore.Images.Thumbnails.IMAGE_ID + "=?",
//                    new String[]{String.valueOf(id)}, null);
//            if (cursor != null && cursor.moveToFirst()) {
//                String fullPath = cursor.getString(0);
//                cursor.close();
//                return BitmapFactory.decodeFile(fullPath);
//            }
            Bitmap bitmap = MediaStore.Images.Thumbnails.getThumbnail(context.getContentResolver(), id, MediaStore.Images.Thumbnails.MICRO_KIND, null);
            if (exifRotation) {
                bitmap = rotateBitmapOnExif(bitmap, null, path);
            }
            return bitmap;
        }
        cursor.close();
        return null;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        // 실제 크기가 더 클경우에
        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    public static Bitmap rotateBitmapOnExif(Bitmap bitmap, BitmapFactory.Options options, String path) {
        if (!FileHelper.isJpegImage(path))
            return bitmap;

        try {
            int degree = 0;
            ExifInterface exif = new ExifInterface(path);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }

            if (bitmap != null && degree != 0) {
                Matrix m = new Matrix();
                m.setRotate(degree, (float) bitmap.getWidth() * 0.5f, (float) bitmap.getHeight() * 0.5f);
                Bitmap rotated = createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, false);
                if (rotated != null) {
                    bitmap.recycle();
                    bitmap = rotated;
                }
            }
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return bitmap;
        }
    }

    public static BitmapFactory.Options decodeBounds(String path) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        return options;
    }

    public static Bitmap decodeSampleBitmapFromFile(String path, int reqWidth, int reqHeight, boolean exifRotation) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        //FIX: 565로 메모리를 아낌
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        // 로드하기 위해서는 위에서 true 로 설정했던 inJustDecodeBounds 의 값을 false 로 설정합니다.
        options.inJustDecodeBounds = false;

        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        if (exifRotation) {
            bitmap = rotateBitmapOnExif(bitmap, options, path);
        }
        return bitmap;
    }

    private static Bitmap cropSquareBitmap(Bitmap decoder, float ratio) {
        final Bitmap bitmap;
        if (ratio > 1) {
            int top = (decoder.getHeight() - decoder.getWidth()) >> 1;
            bitmap = Bitmap.createBitmap(decoder, 0, top, decoder.getWidth(), decoder.getWidth());
            decoder.recycle();
        } else {
            int left = (decoder.getWidth() - decoder.getHeight()) >> 1;
            bitmap = Bitmap.createBitmap(decoder, left, 0, decoder.getHeight(), decoder.getHeight());
            decoder.recycle();
        }
        return bitmap;
    }

    private static Bitmap cropSquareBitmap(String path, float ratio, BitmapFactory.Options options) {
        final Bitmap decoder = BitmapFactory.decodeFile(path, options);
        return cropSquareBitmap(decoder, ratio);
    }

    private static Bitmap cropBitmapUsingDecoder(String path, float ratio, BitmapFactory.Options options) throws IOException {
        final Bitmap bitmap;
        final BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(path, false);

        if (ratio > 1) {// 어떤 크기가 들어오더라도 가운데 크롭을 하기 위해서이다.
            int top = (decoder.getHeight() - decoder.getWidth()) >> 1;
            bitmap = decoder.decodeRegion(new Rect(0, top, decoder.getWidth(), top + decoder.getWidth()), options);
            decoder.recycle();
        } else {
            int left = (decoder.getWidth() - decoder.getHeight()) >> 1;
            bitmap = decoder.decodeRegion(new Rect(left, 0, left + decoder.getHeight(), decoder.getHeight()), options);
            decoder.recycle();
        }
        return bitmap;
    }

    public static Bitmap decodeSquareThumbnailFromPdfFile(String path, int size) {
        Bitmap bitmap = null;
        try {
            final ParcelFileDescriptor parcel = ParcelFileDescriptor.open(new File(path), ParcelFileDescriptor.MODE_READ_ONLY);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                PdfRenderer renderer = new PdfRenderer(parcel);
                final PdfRenderer.Page page = renderer.openPage(0);

                // 종횡비를 계산하자.
                float ratio = (float) page.getHeight() / (float) page.getWidth();
                final Rect rectClip = new Rect();

                if (ratio > 1) {// 세로
                    final int newSize = (int) (size * ratio);
                    bitmap = Bitmap.createBitmap(size, newSize, Bitmap.Config.ARGB_8888);
                } else {// 가로
                    final int newSize = (int) (size / ratio);
                    bitmap = Bitmap.createBitmap(newSize, size, Bitmap.Config.ARGB_8888);
                }

                // 이미지를 렌더링후 close한다.
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                page.close();
                renderer.close();

                bitmap = cropSquareBitmap(bitmap, ratio);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public static Bitmap decodeSquareThumbnailFromFile(String path, int size, boolean exifRotation) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        // 이미지의 크기만 읽는다.
        BitmapFactory.decodeFile(path, options);

        // size는 일반적으로 96이다.
        int width = size;
        int height = size;

        // 종횡비 계산해야 해서 sample size를 결정한다.
        float ratio = (float) options.outHeight / (float) options.outWidth;
        if (ratio > 1) {
            height = (int) (size * ratio);
        } else {
            width = (int) (size / ratio);
        }

        options.inSampleSize = calculateInSampleSize(options, width, height);

        // 로드하기 위해서는 위에서 true 로 설정했던 inJustDecodeBounds 의 값을 false 로 설정합니다.
        options.inJustDecodeBounds = false;
        try {
            //Bitmap bitmap = cropBitmapUsingDecoder(path, ratio, options);

            // 이미지를 로딩하고서 크롭한다.
            Bitmap bitmap = cropSquareBitmap(path, ratio, options);

            if (exifRotation) {
                bitmap = rotateBitmapOnExif(bitmap, options, path);
            }
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 왼쪽 오른쪽을 자른 비트맵을 리턴한다
    public static Bitmap splitBitmapSide(Bitmap bitmap, ExplorerItem item) {
        // 이미 캐시된 페이지가 있으면
        final String key;
        final String keyOther;
        final ExplorerItem itemOther = (ExplorerItem) item.clone();
        itemOther.side = item.side == LEFT ? RIGHT : LEFT;
        key = BitmapCacheManager.changePathToPage(item);
        keyOther = BitmapCacheManager.changePathToPage(itemOther);

        Bitmap page = BitmapCacheManager.getPage(key);
        if (page != null) {
            return page;
        }

        Bitmap pageOther = null;
        switch (item.side) {
            case LEFT:
                page = createBitmap(bitmap, 0, 0, bitmap.getWidth() >> 1, bitmap.getHeight());
                pageOther = createBitmap(bitmap, bitmap.getWidth() >> 1, 0, bitmap.getWidth() >> 1, bitmap.getHeight());
                break;
            case RIGHT:
                page = createBitmap(bitmap, bitmap.getWidth() >> 1, 0, bitmap.getWidth() >> 1, bitmap.getHeight());
                pageOther = createBitmap(bitmap, 0, 0, bitmap.getWidth() >> 1, bitmap.getHeight());
                break;
        }

        if (page != null && pageOther != null) {
            BitmapCacheManager.setPage(key, page);
            BitmapCacheManager.setPage(keyOther, pageOther);
        } else {
        }

        // 잘리는 비트맵은 더이상 사용하지 않으므로 삭제한다.
        // 이거 때문에 recycled 에러가 발생한다.
        // remove를 하지 않으면 oom이 발생한다.
        BitmapCacheManager.removeBitmap(item.path);
        return page;
    }
}
