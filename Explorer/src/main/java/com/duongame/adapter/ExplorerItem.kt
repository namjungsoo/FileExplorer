package com.duongame.adapter

import androidx.room.Entity
import androidx.room.Ignore
import com.duongame.helper.DateHelper.getSimpleDateStringFromExplorerDateString
import androidx.room.PrimaryKey
import com.duongame.adapter.ExplorerItem
import com.duongame.attacher.ImageViewAttacher
import com.duongame.helper.DateHelper
import java.io.Serializable

/**
 * Created by namjungsoo on 2016-11-06.
 */
@Entity
class ExplorerItem(// 파일명+패스이다.
    @field:PrimaryKey var path: String,
    var name: String,
    var date: String?,
    var size: Long, // FileType
    var type: Int
) : Cloneable, Serializable {
    // 이미지 ZIP 데이터
    @JvmField
    @Ignore
    var side = SIDE_ALL

    @Ignore
    var index // 내자신의 인덱스
            = 0

    @Ignore
    var orgIndex // 원본의 인덱스(zip파일에 해당함)
            = 0

    @JvmField
    @Ignore
    var position // adapter의 position
            = 0

    // ZIP 추가 데이터
    @JvmField
    @Ignore
    var width = 0

    @JvmField
    @Ignore
    var height = 0

    // 로딩큐 우선순위
    @Ignore
    var priority // 0이면 최우선, 1이면 낮음
            = 0

    @JvmField
    @Ignore
    var selected // 선택되었는가 표시
            = false

    //    public WeakReference<ImageView> imageViewRef;
    @JvmField
    @Ignore
    var attacher: ImageViewAttacher? = null

    @JvmField
    @Ignore
    var metadata: Any? = null

    @Ignore
    var simpleDate: String? = null

    override fun toString(): String {
        return "path=$path name=$name date=$date size=$size type=$type side=$side index=$index width=$width height=$height orgIndex=$orgIndex"
    }

    val ext: String
        get() = name.substring(name.lastIndexOf('.') + 1)

    public override fun clone(): Any {
        val item: Any
        return try {
            item = super.clone()
            item
        } catch (e: CloneNotSupportedException) {
            e.printStackTrace()
        }
    }

    companion object {
        //FILETYPE_IMAGE
        const val EXTTYPE_JPG = 0 // JPG, GIF
        const val EXTTYPE_PNG = 1

        //FILETYPE_VIDEO
        const val EXTTYPE_AVI = 2 // MKV, MOV, WMV, 3GP, K3G, ASF
        const val EXTTYPE_MP4 = 3
        const val EXTTYPE_FLV = 4

        //FILETYPE_TEXT
        const val EXTTYPE_JSON = 4
        const val EXTTYPE_TEXT = 5
        const val FILETYPE_FOLDER = 0
        const val FILETYPE_IMAGE = 1 // JPG, PNG, GIF
        const val FILETYPE_VIDEO = 2 // MP4, FLV, (AVI, MKV, MOV, WMV, 3GP, K3G, ASF)
        const val FILETYPE_AUDIO = 3 // MP3
        const val FILETYPE_ZIP =
            4 // ZIP, RAR, 7Z, CBZ, CBR, CB7, TAR, TAR.GZ, (TAR.TB2), TGZ, (TB2)
        const val FILETYPE_PDF = 6
        const val FILETYPE_TEXT = 7 // TXT, LOG, JSON
        const val FILETYPE_FILE = 8
        const val FILETYPE_APK = 9 // APK <- FILE
        const val FILETYPE_DOC = 10
        const val FILETYPE_RTF = 11
        const val FILETYPE_CSV = 12
        const val FILETYPE_XLS = 13
        const val FILETYPE_PPT = 14
        const val FILETYPE_HTML = 15
        const val COMPRESSTYPE_ZIP = 0
        const val COMPRESSTYPE_SEVENZIP = 1
        const val COMPRESSTYPE_GZIP = 2
        const val COMPRESSTYPE_BZIP2 = 3
        const val COMPRESSTYPE_RAR = 4
        const val COMPRESSTYPE_TAR = 5
        const val COMPRESSTYPE_XZ = 6
        const val COMPRESSTYPE_OTHER = 7
        const val SIDE_ALL = 0
        const val SIDE_LEFT = 1
        const val SIDE_RIGHT = 2
    }

    init {
        if (date != null) {
            simpleDate = getSimpleDateStringFromExplorerDateString(date)
        }
    }
}