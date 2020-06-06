package com.duongame.adapter;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.duongame.attacher.ImageViewAttacher;
import com.duongame.helper.DateHelper;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * Created by namjungsoo on 2016-11-06.
 */
@Entity
public class ExplorerItem implements Cloneable, Serializable {
    //FILETYPE_IMAGE
    public static final int EXTTYPE_JPG = 0;// JPG, GIF
    public static final int EXTTYPE_PNG = 1;
    //FILETYPE_VIDEO
    public static final int EXTTYPE_AVI = 2;// MKV, MOV, WMV, 3GP, K3G, ASF
    public static final int EXTTYPE_MP4 = 3;
    public static final int EXTTYPE_FLV = 4;
    //FILETYPE_TEXT
    public static final int EXTTYPE_JSON = 4;
    public static final int EXTTYPE_TEXT = 5;

    public static final int FILETYPE_FOLDER = 0;
    public static final int FILETYPE_IMAGE = 1;// JPG, PNG, GIF
    public static final int FILETYPE_VIDEO = 2;// MP4, FLV, (AVI, MKV, MOV, WMV, 3GP, K3G, ASF)
    public static final int FILETYPE_AUDIO = 3;// MP3
    public static final int FILETYPE_ZIP = 4;// ZIP, RAR, 7Z, CBZ, CBR, CB7, TAR, TAR.GZ, (TAR.TB2), TGZ, (TB2)
    public static final int FILETYPE_PDF = 6;
    public static final int FILETYPE_TEXT = 7;// TXT, LOG, JSON
    public static final int FILETYPE_FILE = 8;
    public static final int FILETYPE_APK = 9;// APK <- FILE
    public static final int FILETYPE_DOC = 10;
    public static final int FILETYPE_RTF = 11;
    public static final int FILETYPE_CSV = 12;
    public static final int FILETYPE_XLS = 13;
    public static final int FILETYPE_PPT = 14;
    public static final int FILETYPE_HTML = 15;

    public static final int COMPRESSTYPE_ZIP = 0;
    public static final int COMPRESSTYPE_SEVENZIP = 1;
    public static final int COMPRESSTYPE_GZIP = 2;
    public static final int COMPRESSTYPE_BZIP2 = 3;
    public static final int COMPRESSTYPE_RAR = 4;
    public static final int COMPRESSTYPE_TAR = 5;
    public static final int COMPRESSTYPE_XZ = 6;
    public static final int COMPRESSTYPE_OTHER = 7;

    public static final int SIDE_ALL = 0;
    public static final int SIDE_LEFT = 1;
    public static final int SIDE_RIGHT = 2;

    @PrimaryKey
    @NotNull
    public String path;// 파일명+패스이다.
    public String name;
    public String date;
    public long size;
    public int type;// FileType

    // 이미지 ZIP 데이터
    @Ignore
    public int side = SIDE_ALL;
    @Ignore
    public int index;// 내자신의 인덱스
    @Ignore
    public int orgIndex;// 원본의 인덱스(zip파일에 해당함)
    @Ignore
    public int position;// adapter의 position

    // ZIP 추가 데이터
    @Ignore
    public int width;
    @Ignore
    public int height;

    // 로딩큐 우선순위
    @Ignore
    public int priority;// 0이면 최우선, 1이면 낮음
    @Ignore
    public boolean selected;// 선택되었는가 표시

    //    public WeakReference<ImageView> imageViewRef;
    @Ignore
    public ImageViewAttacher attacher;
    @Ignore
    public Object metadata;
    @Ignore
    public String simpleDate;

    public ExplorerItem(String path, String name, String date, long size, int type) {
        this.path = path;
        this.name = name;
        this.date = date;
        this.size = size;
        this.type = type;

        if(date != null) {
            simpleDate = DateHelper.getSimpleDateStringFromExplorerDateString(date);
        }
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
