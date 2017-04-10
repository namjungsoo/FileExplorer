package com.duongame.comicz.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.duongame.explorer.adapter.ExplorerItem;

import java.util.ArrayList;

/**
 * Created by namjungsoo on 2017-01-02.
 */

public class BookDB extends SQLiteOpenHelper {
    private final static String TAG = "BookDB";

    public BookDB(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public static class Book {
        // 변하지 않음
        public String path;// 패스
        public String name;// 파일명
        public ExplorerItem.FileType type = ExplorerItem.FileType.ZIP;
        public long size;// zip파일 사이즈
        public int total_file;// 최대 파일 수

        // 동적으로 변함
        public int current_page;// 현재 페이지 인덱스
        public int total_page;// 최대 페이지 수. 전체를 다 로딩하지 않으면 알수 없음

        public int current_file;// 현재 파일 인덱스
        public int extract_file;// 압축 풀린 파일수. zip에만 해당함. total_file == extract_file이면 로딩이 완료된것

        public ExplorerItem.Side side = ExplorerItem.Side.SIDE_ALL;// 책넘김 방법
        public String date;

        // DB에 저장되지 않음
        public int percent;

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

                    " percent=" + percent;
        }

        public void updatePercent() {
            if(total_page > 0) {
                percent = ((current_page + 1) * 100) / total_page;
            } else if (total_file > 0) {
                percent = ((current_file + 1) * 100) / total_file;
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

                "date TEXT);";
        Log.d(TAG, "onCreate sql=" + sql);
        db.execSQL(sql);
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
        book.type.setValue(cursor.getInt(2));
        book.size = cursor.getLong(3);
        book.total_file = cursor.getInt(4);

        book.current_page = cursor.getInt(5);
        book.total_page = cursor.getInt(6);
        book.current_file = cursor.getInt(7);
        book.extract_file = cursor.getInt(8);
        book.side.setValue(cursor.getInt(9));
        book.date = cursor.getString(10);

        book.updatePercent();
        return book;
    }

    public static void clearBooks(Context context) {
        final SQLiteDatabase db = getInstance(context).getWritableDatabase();
        final String sql = "DELETE FROM book";
        db.execSQL(sql);
        db.close();
    }

    public static Book getBook(Context context, String path) {
        final SQLiteDatabase db = getInstance(context).getReadableDatabase();
        final String sql = "SELECT * FROM book WHERE path='"+path+"'";
        final Cursor cursor = db.rawQuery(sql, null);
        Book book = null;
        while(cursor.moveToNext()) {
            book = newBook(cursor);
            break;
        }
        cursor.close();
        db.close();
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
        db.close();
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
        db.close();
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
                    + ",date=datetime('now','localtime') WHERE path='" + book.path + "'";
            Log.i(TAG, "setLastBook=" + sql2);
            db.execSQL(sql2);
        } else {// 없으면 추가
            final String sql2 = "INSERT INTO book VALUES('" + book.path
                    + "','" + book.name
                    + "'," + book.type.getValue()
                    + "," + book.size
                    + "," + book.total_file

                    + "," + book.current_page
                    + "," + book.total_page

                    + "," + book.current_file
                    + "," + book.extract_file

                    + "," + book.side.getValue()
                    + ",datetime('now','localtime'))";
            Log.i(TAG, "setLastBook=" + sql2);
            db.execSQL(sql2);
        }
        db.close();
    }
}
