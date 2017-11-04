package com.duongame.comicz.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

import com.duongame.explorer.adapter.ExplorerItem;

import java.util.ArrayList;

import static com.duongame.comicz.db.BookDB.TextBook.LINES_PER_PAGE;

/**
 * Created by namjungsoo on 2017-01-02.
 */

public class BookDB extends SQLiteOpenHelper {
    private final static String TAG = "BookDB";

    public BookDB(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public static class TextBook {
        public static int LINES_PER_PAGE = 1000;
        public int lineCount;// 전체 라인 갯수. 1000라인별로 페이지가 분할되어 슬라이더 이동됨
        public int currentPage;// 1000으로 나눈 몫
        public int currentPercent;// 현재 페이지의 퍼센트
        public int currentLine;// 전체중에 몇번째 라인인지
        public int totalPercent;

        // 현재라인(추정)=lineCount*totalPercent / lineCount
        public String getPageText() {
            return "" + (lineCount * totalPercent / LINES_PER_PAGE) + "/" + lineCount;
        }
    }

    public static TextBook getTextBook(Book book) {
        TextBook text = new TextBook();
        text.lineCount = book.total_page;
        text.currentPage = book.current_page / LINES_PER_PAGE;
        text.currentPercent = book.current_page - text.currentPage * LINES_PER_PAGE;

        if(book.current_file == 0) {
            // 마지막 페이지이면, 1000라인 이하일수 있다. 전체가 1000라인 이하인 것도 마찬가지다.
            if (text.currentPage == (text.lineCount / LINES_PER_PAGE)) {// 읽기 완료된 파일이다.
                text.currentLine = text.lineCount;
                text.totalPercent = LINES_PER_PAGE;
            } else {
                int currentPageLine = 0;
                int prevTotalPageLineCount = 0;
                int currentTotalPageLineCount = 0;
                prevTotalPageLineCount = text.currentPage * LINES_PER_PAGE;

                currentPageLine = currentTotalPageLineCount * text.currentPercent / LINES_PER_PAGE;
                if (text.currentPage == ((text.lineCount / LINES_PER_PAGE) - 1)) {// 마지막 페이지이다.
                    // 남은 라인을 먼저 계산
                    currentTotalPageLineCount = text.lineCount - prevTotalPageLineCount;
                } else {// 가운데 페이지이다.
                    currentTotalPageLineCount = LINES_PER_PAGE;
                }

                text.currentLine = prevTotalPageLineCount + currentPageLine;
                text.totalPercent = text.currentLine * LINES_PER_PAGE / text.lineCount;
            }
        } else {
            // 마지막 페이지이면, 1000라인 이하일수 있다. 전체가 1000라인 이하인 것도 마찬가지다.
            if (text.currentPage == (text.lineCount / LINES_PER_PAGE)) {// 읽기 완료된 파일이다.
                text.currentLine = text.lineCount;
                text.totalPercent = LINES_PER_PAGE;
            } else {
                int currentPageLine = 0;
                int prevTotalPageLineCount = 0;
                int currentTotalPageLineCount = 0;
                prevTotalPageLineCount = text.currentPage * LINES_PER_PAGE;

                currentPageLine = currentTotalPageLineCount * text.currentPercent / LINES_PER_PAGE;
                if (text.currentPage == ((text.lineCount / LINES_PER_PAGE) - 1)) {// 마지막 페이지이다.
                    // 남은 라인을 먼저 계산
                    currentTotalPageLineCount = text.lineCount - prevTotalPageLineCount;
                } else {// 가운데 페이지이다.
                    currentTotalPageLineCount = LINES_PER_PAGE;
                }

                text.currentLine = prevTotalPageLineCount + currentPageLine;
                text.totalPercent = text.currentLine * LINES_PER_PAGE / text.lineCount;
            }
        }

        return text;
    }

    // setLastBook에 저장될 Book을 만듬
    public static Book buildTextBook(String path, String name, long size, int percent, int page, int lineCount) {
        // 현재 라인을 book에 저장하자.
        final BookDB.Book book = new BookDB.Book();
        // 고정적인 내용 5개
        book.path = path;
        book.name = name;
        book.type = ExplorerItem.FileType.TEXT;
        book.size = size;
        book.total_file = 0;// 파일의 갯수이다.

        // 동적인 내용 6개
        book.current_page = page * LINES_PER_PAGE + percent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            book.total_page = lineCount;
        }

        // zip아 아니면 사용하지 않는 부분
        book.current_file = 0;
        book.extract_file = 0;
        book.side = ExplorerItem.Side.SIDE_ALL;

        return book;
    }

    public static Book buildTextBook2(String path, String name, long size, int percent, int page, int lineCount) {
        // 현재 라인을 book에 저장하자.
        final BookDB.Book book = new BookDB.Book();
        // 고정적인 내용 5개
        book.path = path;
        book.name = name;
        book.type = ExplorerItem.FileType.TEXT;
        book.size = size;
        book.total_file = 0;// 파일의 갯수이다.

        // 동적인 내용 6개
        book.current_page = page * LINES_PER_PAGE + percent/10;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            book.total_page = lineCount;
        }

        // zip아 아니면 사용하지 않는 부분
        book.current_file = percent;
        book.extract_file = 0;
        book.side = ExplorerItem.Side.SIDE_ALL;

        return book;
    }

    public static class Book {
        // 변하지 않음
        public String path;// 패스
        public String name;// 파일명
        public ExplorerItem.FileType type = ExplorerItem.FileType.ZIP;
        public long size;// zip파일 사이즈
        public int total_file;// 최대 파일 수

        // 동적으로 변함
        public int current_page;// 현재 페이지 인덱스. 텍스트의 경우 현재 페이지*1000 + 0~99.9%
        //TODO: 텍스트의 경우 1000 -> 10000으로 변경하여야 함
        public int total_page;// 최대 페이지 수. 전체를 다 로딩하지 않으면 알수 없음. 텍스트의 경우 전체 라인수

        public int current_file;// 현재 파일 인덱스
        public int extract_file;// 압축 풀린 파일수. zip에만 해당함. total_file == extract_file이면 로딩이 완료된것

        public ExplorerItem.Side side = ExplorerItem.Side.SIDE_ALL;// 책넘김 방법
        public String date;

        public String last_file;// 마지막 이미지 파일의 파일명

        // DB에 저장되지 않음
        public int percent;// 0~100을 가진다.

        public Book() {

        }

        @Override
        public String toString() {
            return "path=" + path +
                    " name=" + name +
                    " type=" + type +
                    " size=" + size +
                    " total_file=" + total_file +

                    " current_page=" + current_page +
                    " total_page=" + total_page +
                    " current_file=" + current_file +
                    " extract_file=" + extract_file +
                    " side=" + side +
                    " date=" + date +
                    " last_file=" + last_file +

                    " currentPercent=" + percent;
        }

        public void updatePercent() {
            if(name.toLowerCase().endsWith(".txt")) {
                if(current_file == 0) {
                    percent = current_page % 1000 / 10;
                } else {
                    percent = current_file % 10000 / 100;
                }
            } else {
                if (total_page > 0) {
                    percent = ((current_page + 1) * 100) / total_page;
                } else if (total_file > 0) {
                    percent = ((current_file + 1) * 100) / total_file;
                }
            }
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String sql = "CREATE TABLE book " +
                // 변하지 않음 5개
                "(path TEXT PRIMARY KEY, " +
                "name TEXT, " +

                "type INTEGER, " +
                "size INTEGER, " +
                "total_file INTEGER, " +

                // 동적으로 변함 6개
                "current_page INTEGER, " +
                "total_page INTEGER, " +

                "current_file INTEGER, " +
                "extract_file INTEGER, " +

                "side INTEGER, " +
                "date TEXT," +
                "last_file TEXT" +

                ");";
        Log.d(TAG, "onCreate sql=" + sql);
        db.execSQL(sql);
//        db.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private static BookDB mBookDB = null;

    public static BookDB getInstance(Context context) {
        if (mBookDB == null) {
            mBookDB = new BookDB(context, "book.db", null, 1);
        }
        return mBookDB;
    }

    private static Book newBook(Cursor cursor) {
        Book book = new Book();

        book.path = cursor.getString(0);
        book.name = cursor.getString(1);
        int type = cursor.getInt(2);
        book.type = ExplorerItem.FileType.values()[type];
        book.size = cursor.getLong(3);
        book.total_file = cursor.getInt(4);

        book.current_page = cursor.getInt(5);
        book.total_page = cursor.getInt(6);
        book.current_file = cursor.getInt(7);
        book.extract_file = cursor.getInt(8);
        book.side.setValue(cursor.getInt(9));
        book.date = cursor.getString(10);
        book.last_file = cursor.getString(11);

        book.updatePercent();
        return book;
    }

    public static void clearBook(Context context, String path) {
        final SQLiteDatabase db = getInstance(context).getWritableDatabase();
        final String sql = "DELETE FROM book WHERE path='" + path + "'";
        db.execSQL(sql);
//        db.close();
    }

    public static void clearBooks(Context context) {
        final SQLiteDatabase db = getInstance(context).getWritableDatabase();
        final String sql = "DELETE FROM book";
        db.execSQL(sql);
//        db.close();
    }

    public static Book getBook(Context context, String path) {
        final SQLiteDatabase db = getInstance(context).getReadableDatabase();
        final String sql = "SELECT * FROM book WHERE path='" + path + "'";
        final Cursor cursor = db.rawQuery(sql, null);
        Book book = null;
        while (cursor.moveToNext()) {
            book = newBook(cursor);
            break;
        }
        cursor.close();
//        db.close();
        return book;
    }

    // 최근 50개 읽은 책 리스트를 반환
    public static ArrayList<Book> getBooks(Context context) {
        final SQLiteDatabase db = getInstance(context).getReadableDatabase();

        final String sql = "SELECT * FROM book ORDER BY date DESC LIMIT 50";
        Book book = null;
        final ArrayList<Book> bookList = new ArrayList<Book>();

        final Cursor cursor = db.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            book = newBook(cursor);
            bookList.add(book);

            Log.i(TAG, "getBooks " + book.toString());
        }
        cursor.close();
//        db.close();
        return bookList;
    }

    // 마지막 책을 반환
    public static Book getLastBook(Context context) {
        final SQLiteDatabase db = getInstance(context).getReadableDatabase();

        Book book = null;
        final String sql = "SELECT * FROM book ORDER BY date DESC LIMIT 1";
        Log.d(TAG, "getLastBookmark sql=" + sql);

        final Cursor cursor = db.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            book = newBook(cursor);
            Log.d(TAG, "getLastBook " + book.toString());
        }
        cursor.close();
//        db.close();
        return book;
    }

    // 마지막 책을 셋팅
    public static void setLastBook(Context context, Book book) {
        final SQLiteDatabase db = getInstance(context).getReadableDatabase();

        // 기존에 저장된게 있는지 없는지 찾아본다음에 INSERT, UPDATE를 구분해서 처리함
        final String sql = "SELECT current_page FROM book WHERE path='" + book.path + "' LIMIT 1";

        final Cursor cursor = db.rawQuery(sql, null);
        boolean exist = false;
        if (cursor.moveToNext()) {
            exist = true;
        }
        cursor.close();

        // 있으면 수정
        if (exist) {
            // 변경되는 내용: current_page, extract_file, side, datetime
            // 동적으로 변경되는 내용과 날짜만 변경해서 넣자
            final String sql2 = "UPDATE book SET current_page=" + book.current_page
                    + ",total_page=" + book.total_page
                    + ",current_file=" + book.current_file
                    + ",extract_file=" + book.extract_file
                    + ",side=" + book.side.getValue()
                    + ",date=datetime('now','localtime')"
                    + ",last_file='" + book.last_file + "'"
                    + " WHERE path='" + book.path + "'";
            Log.i(TAG, "setLastBook=" + sql2);
            db.execSQL(sql2);
        } else {// 없으면 추가
            int type = book.type.getValue();
            final String sql2 = "INSERT INTO book VALUES('" + book.path
                    + "','" + book.name
                    + "'," + type
                    + "," + book.size
                    + "," + book.total_file

                    + "," + book.current_page
                    + "," + book.total_page

                    + "," + book.current_file
                    + "," + book.extract_file

                    + "," + book.side.getValue()

                    + ",datetime('now','localtime')"
                    + ",'" + book.last_file + "'"
                    + ")";
            Log.i(TAG, "setLastBook=" + sql2);
            db.execSQL(sql2);
        }
//        db.close();
    }
}
