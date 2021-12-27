package com.duongame.db

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.database.sqlite.SQLiteOpenHelper
import java.util.*

/**
 * Created by namjungsoo on 2017-01-02.
 */
class BookDB(context: Context?, name: String?, factory: CursorFactory?, version: Int) :
    SQLiteOpenHelper(context, name, factory, version) {
    override fun onCreate(db: SQLiteDatabase) {
        val sql = "CREATE TABLE book " +  // 변하지 않음 5개
                "(path TEXT PRIMARY KEY, " +
                "name TEXT, " +
                "type INTEGER, " +
                "size INTEGER, " +
                "total_file INTEGER, " +  // 동적으로 변함 6개
                "current_page INTEGER, " +
                "total_page INTEGER, " +
                "current_file INTEGER, " +
                "extract_file INTEGER, " +
                "side INTEGER, " +
                "date TEXT," +
                "last_file TEXT" +
                ");"
        db.execSQL(sql)
        //        db.close();
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    companion object {
        private const val TAG = "BookDB"
        private var mBookDB: BookDB? = null
        fun getInstance(context: Context?): BookDB? {
            if (mBookDB == null) {
                mBookDB = BookDB(context, "book.db", null, 1)
            }
            return mBookDB
        }

        private fun newBook(cursor: Cursor): Book {
            val book = Book()
            book.path = cursor.getString(0)
            book.name = cursor.getString(1)
            book.type = cursor.getInt(2)
            book.size = cursor.getLong(3)
            book.total_file = cursor.getInt(4)
            book.current_page = cursor.getInt(5)
            book.total_page = cursor.getInt(6)
            book.current_file = cursor.getInt(7)
            book.extract_file = cursor.getInt(8)
            book.side = cursor.getInt(9)
            book.date = cursor.getString(10)
            book.last_file = cursor.getString(11)
            book.updatePercent()
            return book
        }

        fun clearBook(context: Context?, path: String?) {
            var path = path
            if (path == null) return
            // 문자열중 작은 따옴표 '변환
            path = path.replace("'", "''")
            val db = getInstance(context)!!.writableDatabase
            val sql = "DELETE FROM book WHERE path='$path'"
            db.execSQL(sql)
            //        db.close();
        }

        fun clearBooks(context: Context?) {
            val db = getInstance(context)!!.writableDatabase
            val sql = "DELETE FROM book"
            db.execSQL(sql)
            //        db.close();
        }

        // path에 해당하는 책이 있는지 확인하고 있으면 돌려줌
        fun getBook(context: Context?, path: String?): Book? {
            var path = path
            if (path == null) return null
            // 문자열중 작은 따옴표 '변환
            path = path.replace("'", "''")
            val db = getInstance(context)!!.readableDatabase
            val sql = "SELECT * FROM book WHERE path='$path'"
            val cursor = db.rawQuery(sql, null)
            var book: Book? = null
            while (cursor.moveToNext()) {
                book = newBook(cursor)
                break
            }
            cursor.close()
            //        db.close();
            return book
        }

        // 최근 50개 읽은 책 리스트를 반환
        fun getBooks(context: Context?): ArrayList<Book> {
            val db = getInstance(context)!!.readableDatabase
            val sql = "SELECT * FROM book ORDER BY date DESC LIMIT 50"
            var book: Book? = null
            val bookList = ArrayList<Book>()
            val cursor = db.rawQuery(sql, null)
            while (cursor.moveToNext()) {
                book = newBook(cursor)
                bookList.add(book)
            }
            cursor.close()
            //        db.close();
            return bookList
        }

        // 마지막 책을 반환
        fun getLastBook(context: Context?): Book? {
            val db = getInstance(context)!!.readableDatabase
            var book: Book? = null
            val sql = "SELECT * FROM book ORDER BY date DESC LIMIT 1"
            val cursor = db.rawQuery(sql, null)
            while (cursor.moveToNext()) {
                book = newBook(cursor)
            }
            cursor.close()
            //        db.close();
            return book
        }

        // 마지막 책을 셋팅
        fun setLastBook(context: Context?, book: Book?) {
            if (book == null) return

            //FIX: NPE 수정
            if (book.path == null) return
            if (book.name == null) return
            val db = getInstance(context)!!.readableDatabase
            book.path = book.path!!.replace("'", "''")
            book.name = book.name!!.replace("'", "''")
            if (book.last_file != null) book.last_file = book.last_file!!.replace("'", "''")

            // 기존에 저장된게 있는지 없는지 찾아본다음에 INSERT, UPDATE를 구분해서 처리함
            val sql = "SELECT current_page FROM book WHERE path='" + book.path + "' LIMIT 1"
            val cursor = db.rawQuery(sql, null)
            var exist = false
            if (cursor.moveToNext()) {
                exist = true
            }
            cursor.close()

            // 있으면 수정
            if (exist) {
                // 변경되는 내용: current_page, extract_file, side, datetime
                // 동적으로 변경되는 내용과 날짜만 변경해서 넣자
                val sql2 = ("UPDATE book SET current_page=" + book.current_page
                        + ",total_page=" + book.total_page
                        + ",current_file=" + book.current_file
                        + ",extract_file=" + book.extract_file
                        + ",side=" + book.side
                        + ",date=datetime('now','localtime')"
                        + ",last_file='" + book.last_file + "'"
                        + " WHERE path='" + book.path + "'")
                db.execSQL(sql2)
            } else { // 없으면 추가
                val sql2 = ("INSERT INTO book VALUES('" + book.path
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
                        + ")")
                db.execSQL(sql2)
            }
            //        db.close();
        }
    }
}