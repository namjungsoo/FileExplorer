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
        public String path;// 패스
        public String name;// 파일명
        public ExplorerItem.FileType type = ExplorerItem.FileType.ZIP;
        public ExplorerItem.Side side = ExplorerItem.Side.SIDE_ALL;// 책넘김 방법

        // ExplorerItem에 없는거. zip파일의 이미지 기준
        public int page;// 현재 페이지

        public int count;// 최대 파일수.
        public int extract;// 압축 풀린 파일수. zip에만 해당함
        public long size;// zip파일 사이즈

        public String date;
        public int percent;

        public Book() {

        }

        public Book(String path, String name, ExplorerItem.FileType type, ExplorerItem.Side side, int page, int count, int extract, long size) {
            this.path = path;
            this.name = name;
            this.type = type;
            this.side = side;

            this.page = page;
            this.count = count;
            this.extract = extract;
            this.size = size;
        }

        @Override
        public String toString() {
            return "path=" + path +
                    " name=" + name +
                    " type=" + type +
                    " side=" + side +

                    " page=" + page +
                    " count=" + count +
                    " extract=" + extract +

                    " size=" + size +
                    " date=" + date;
        }

        public void updatePercent() {
            if (count > 0) {
                percent = ((page + 1) * 100) / count;
            }
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String sql = "CREATE TABLE book " +
                "(path TEXT PRIMARY KEY, " +
                "name TEXT, " +

                "type INTEGER, " +
                "side INTEGER, " +

                "page INTEGER, " +
                "count INTEGER, " +
                "extract INTEGER, " +

                "size INTEGER, " +
                "datetime TEXT);";
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
        book.side.setValue(cursor.getInt(3));

        book.page = cursor.getInt(4);
        book.count = cursor.getInt(5);
        book.extract = cursor.getInt(6);

        book.size = cursor.getLong(7);
        book.date = cursor.getString(8);

        book.updatePercent();
        return book;
    }

    public static void clearBooks(Context context) {
        final SQLiteDatabase db = getInstance(context).getWritableDatabase();
        final String sql = "DELETE FROM book";
        db.execSQL(sql);
        db.close();
    }

    // 최근 50개 읽은 책 리스트를 반환
    public static ArrayList<Book> getBooks(Context context) {
        final SQLiteDatabase db = getInstance(context).getReadableDatabase();

        final String sql = "SELECT * FROM book ORDER BY datetime DESC LIMIT 50";
        Book book = null;
        final ArrayList<Book> bookList = new ArrayList<Book>();

        final Cursor cursor = db.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            book = newBook(cursor);
            bookList.add(book);

            Log.d(TAG, "getBooks " + book.toString());
        }
        cursor.close();
        db.close();
        return bookList;
    }

    // 마지막 책을 반환
    public static Book getLastBook(Context context) {
        final SQLiteDatabase db = getInstance(context).getReadableDatabase();

        Book book = null;
        final String sql = "SELECT * FROM book ORDER BY datetime DESC LIMIT 1";
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
        final String sql = "SELECT page FROM book WHERE path='" + book.path + "' LIMIT 1";

        final Cursor cursor = db.rawQuery(sql, null);
        boolean exist = false;
        if (cursor.moveToNext()) {
            exist = true;
        }
        cursor.close();

        // 있으면 수정
        if (exist) {
            // 변경되는 내용: page, extract, side, datetime
            final String sql2 = "UPDATE book SET page=" + book.page + ",extract=" + book.extract + ",side=" + book.side.getValue() + ",datetime=datetime('now','localtime') WHERE path='" + book.path + "'";
            Log.d(TAG, "setLastBook=" + sql2);
            db.execSQL(sql2);
        } else {// 없으면 추가
            final String sql2 = "INSERT INTO book VALUES('" + book.path + "','" + book.name + "'," + book.type.getValue() + "," + book.side.getValue() + "," + book.page + "," + book.count + "," + book.extract + "," + book.size + ",datetime('now','localtime'))";
            Log.d(TAG, "setLastBook=" + sql2);
            db.execSQL(sql2);
        }
        db.close();
    }
}
