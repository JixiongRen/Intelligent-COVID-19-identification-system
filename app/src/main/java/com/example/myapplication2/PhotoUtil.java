package com.example.myapplication2;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class PhotoUtil {
    private static final String TAD = PhotoUtil.class.getName();

    /*
    public static void use_photo(Activity activity, int requestCode){
        Intent intent = new Intent(Intent.ACTION_PICK);//对弹出的列表选择一张图片，然后进行下一步的处理。打开相册
        intent.setType("image/*");
        activity.startActivityForResult(intent,requestCode);
    }
    */
    /*
    public static String get_path_from_URI(Context context, Uri uri){
        String result;
        Cursor cursor = context.getContentResolver().query(uri,null,null,null,null);
        if (cursor == null){
            result = uri.getPath();
        }else{
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;

        List imag = getFilesAllName(sdCardDir);
                    Log.d(TAH,imag + " imag");

    }*/
    public static ByteBuffer getScaledMatrix(Bitmap bitmap, int[] ddims){
        ByteBuffer imgData = ByteBuffer.allocateDirect(ddims[0]*ddims[1]*ddims[2]*ddims[3]*4);
        imgData.order(ByteOrder.nativeOrder());
        int[] pixels = new int[ddims[2]*ddims[3]];
        Bitmap bm = Bitmap.createScaledBitmap(bitmap,ddims[2],ddims[3],false);
        bm.getPixels(pixels,0,bm.getWidth(),0,0,ddims[2],ddims[3]);
        int pixel = 0;
        for (int i=0;i<ddims[2];++i){
            for(int j=0;j<ddims[3];++j){
                final int val = pixels[pixel++];
                imgData.putFloat(((((val >> 16) & 0xFF) - 128f) / 128f));
                imgData.putFloat(((((val >> 8) & 0xFF) - 128f) / 128f));
                imgData.putFloat((((val & 0xFF) - 128f) / 128f));
            }
        }
        if (bm.isRecycled()){
            bm.recycle();
        }
        return imgData;
    }

    public static Bitmap getScaleBitmap(String filePath){
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath,opt);

        int bmpWidth = opt.outWidth;
        int bmpHeight = opt.outHeight;
        int maxSize = 500;
        opt.inSampleSize = 1;
        while(true){
            if(bmpWidth / opt.inSampleSize < maxSize || bmpHeight / opt.inSampleSize < maxSize){
                break;
            }
            opt.inSampleSize = 2;

        }
        opt.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath,opt);
    }
}
