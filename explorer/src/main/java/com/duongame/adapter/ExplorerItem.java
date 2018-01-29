package com.duongame.adapter;

import com.duongame.attacher.ImageViewAttacher;

/**
 * Created by namjungsoo on 2016-11-06.
 */

public class ExplorerItem implements Cloneable {

    public static final int FILETYPE_FOLDER = 0;
    public static final int FILETYPE_IMAGE = 1;
    public static final int FILETYPE_VIDEO = 2;
    public static final int FILETYPE_AUDIO = 3;
    public static final int FILETYPE_ZIP = 4;
    public static final int FILETYPE_RAR = 5;
    public static final int FILETYPE_PDF = 6;
    public static final int FILETYPE_TEXT = 7;
    public static final int FILETYPE_FILE = 8;
    public static final int FILETYPE_APK = 9;
    public static final int FILETYPE_ALL = 10;

    public static final int COMPRESSTYPE_ZIP = 0;
    public static final int COMPRESSTYPE_SEVENZIP = 1;
    public static final int COMPRESSTYPE_GZIP = 2;
    public static final int COMPRESSTYPE_BZIP2 = 3;
    public static final int COMPRESSTYPE_RAR = 4;
    public static final int COMPRESSTYPE_TAR = 5;
    public static final int COMPRESSTYPE_OTHER = 6;

    public static final int SIDE_ALL = 0;
    public static final int SIDE_LEFT = 1;
    public static final int SIDE_RIGHT = 2;
    public static final int SIDE_OTHER = 3;

    public String name;
    public String date;
    public long size;
    public String path;// 파일명+패스이다.
    public int type;// FileType

    // 이미지 ZIP 데이터
    public int side = SIDE_ALL;
    public int index;// 내자신의 인덱스
    public int orgIndex;// 원본의 인덱스(zip파일에 해당함)
    public int position;// adapter의 position

    // ZIP 추가 데이터
    public int width;
    public int height;

    // 로딩큐 우선순위
    public int priority;// 0이면 최우선, 1이면 낮음
    public boolean selected;// 선택되었는가 표시

    //    public WeakReference<ImageView> imageViewRef;
    public ImageViewAttacher attacher;

    public ExplorerItem(String path, String name, String date, long size, int type) {
        this.path = path;
        this.name = name;
        this.date = date;
        this.size = size;
        this.type = type;
    }

    public String toString() {
        return "path=" + path + " name=" + name + " date=" + date + " size=" + size + " type=" + type + " side=" + side + " index=" + index + " width=" + width + " height=" + height + " orgIndex=" + orgIndex;
    }

    public String getExt() {
        return name.substring(name.lastIndexOf('.') + 1);
    }

    @Override
    public Object clone() {
        Object item;
        try {
            item = super.clone();
            return item;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }
}
