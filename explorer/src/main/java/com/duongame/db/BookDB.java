package com.duongame.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by namjungsoo on 2017-01-02.
 */

public class BookDB extends SQLiteOpenHelper {
    private final static String TAG = "BookDB";

    public BookDB(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
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
        book.type = cursor.getInt(2);
        book.size = cursor.getLong(3);
        book.total_file = cursor.getInt(4);

        book.current_page = cursor.getInt(5);
        book.total_page = cursor.getInt(6);
        book.current_file = cursor.getInt(7);
        book.extract_file = cursor.getInt(8);
        book.side = cursor.getInt(9);
        book.date = cursor.getString(10);
        book.last_file = cursor.getString(11);

        book.updatePercent();
        return book;
    }

    public static void clearBook(Context context, String path) {
        if (path == null)
            return;
        // 문자열중 작은 따옴표 '변환
        path = path.replace("'", "''");
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

    // path에 해당하는 책이 있는지 확인하고 있으면 돌려줌
    public static Book getBook(Context context, String path) {
        if (path == null)
            return null;
        // 문자열중 작은 따옴표 '변환
        path = path.replace("'", "''");
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

        final Cursor cursor = db.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            book = newBook(cursor);
        }
        cursor.close();
//        db.close();
        return book;
    }

    // 마지막 책을 셋팅
    public static void setLastBook(Context context, Book book) {
        if(book == null)
            return;

        //FIX: NPE 수정
        if(book.path == null)
            return;
        if(book.name == null)
            return;

        final SQLiteDatabase db = getInstance(context).getReadableDatabase();

        book.path = book.path.replace("'", "''");
        book.name = book.name.replace("'", "''");
        if(book.last_file != null)
            book.last_file = book.last_file.replace("'", "''");

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
                    + ",side=" + book.side
                    + ",date=datetime('now','localtime')"
                    + ",last_file='" + book.last_file + "'"
                    + " WHERE path='" + book.path + "'";
            db.execSQL(sql2);
        } else {// 없으면 추가
            final String sql2 = "INSERT INTO book VALUES('" + book.path
                    + "','" + book.name
                    + "'," + book.type
                    + "," + book.size
                    + "," + book.total_file

                    + "," + book.current_page
                    + "," + book.total_page

                    + "," + book.current_file
                    + "," + book.extract_file

                    + "," + book.side

                    + ",datetime('now','localtime')"
                    + ",'" + book.last_file + "'"
                    + ")";
            db.execSQL(sql2);
        }
//        db.close();
    }
}
