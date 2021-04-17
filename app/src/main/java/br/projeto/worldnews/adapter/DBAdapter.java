package br.projeto.worldnews.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.LinkedList;
import java.util.List;

import br.projeto.worldnews.model.Topic;


public class DBAdapter {

    static final String KEY_ID = "id";
    static final String KEY_TOPIC = "topic";
    static final String KEY_ARGS = "args";
    static final String KEY_TRANSLATED = "translated";
    static final String DATABASE_NAME = "newstopics";
    static final String DATABASE_TABLENAME_TOPIC = "topics";
    static final String DATABASE_TABLENAME_MY_TOPIC = "mytopics";
    static final int DATABASE_VERSION = 1;
    static final String DATABASE_CREATE_TOPICS = "CREATE TABLE IF NOT EXISTS " + DATABASE_TABLENAME_TOPIC + "(" +
            KEY_ID + " integer primary key autoincrement," +
            KEY_TOPIC + " text not null unique ON CONFLICT ABORT," +
            KEY_TRANSLATED + " text);";
    static final String DATABASE_CREATE_MY_TOPICS = "CREATE TABLE IF NOT EXISTS " + DATABASE_TABLENAME_MY_TOPIC + "(" +
            KEY_ID + " integer primary key autoincrement," +
            KEY_TOPIC + " text not null unique ON CONFLICT ABORT," +
            KEY_ARGS + " text);";


    final Context context;
    DataBaseHelper dataBaseHelper;
    SQLiteDatabase db;

    public DBAdapter(Context cont) {
        context = cont;
        dataBaseHelper = new DataBaseHelper(context);
        db = dataBaseHelper.getWritableDatabase();
    }

    public void close() {
        dataBaseHelper.close();
        db.close();
    }

    //insert
    public long insertTopics(String topics, String translate) throws SQLException {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TOPIC, topics);
        initialValues.put(KEY_TRANSLATED, translate);
        return db.insert(DATABASE_TABLENAME_TOPIC, null, initialValues);
    }

    public long insertMyTopics(String topics, String args) throws SQLException {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TOPIC, topics);
        initialValues.put(KEY_ARGS, args);
        return db.insert(DATABASE_TABLENAME_MY_TOPIC, null, initialValues);
    }

    //delete
    public boolean deleteTopic(long id) throws SQLException {

        return db.delete(DATABASE_TABLENAME_TOPIC, "id=" + id, null) > 0;

    }

    public boolean deleteMyTopic(long id) throws SQLException {

        return db.delete(DATABASE_TABLENAME_MY_TOPIC, "id=" + id, null) > 0;

    }

    //delete
    public boolean deleteAllTopics() throws SQLException {

        return db.delete(DATABASE_TABLENAME_TOPIC, null, null) > 0;

    }

    public boolean deleteAllMyTopics() throws SQLException {

        return db.delete(DATABASE_TABLENAME_MY_TOPIC, null, null) > 0;

    }

    public List<Topic> getAllMyTopics() throws SQLException {

        List<Topic> topicList = new LinkedList<Topic>();

        Cursor cursor = db.query(DATABASE_TABLENAME_MY_TOPIC,
                new String[]{"id", KEY_TOPIC, KEY_ARGS}, null, null, null, null, "id ASC");

        Topic topic;
        if (cursor.moveToFirst()) {
            do {
                topic = new Topic();
                topic.setId(cursor.getInt(0));
                topic.setTopic(cursor.getString(1));
                topic.setArgs(cursor.getString(2));
                topicList.add(topic);
            } while (cursor.moveToNext());
        }
        return topicList;
    }

    //retriever all values from database
    public List<Topic> getAllTopics() throws SQLException {

        List<Topic> topicList = new LinkedList<Topic>();

        Cursor cursor = db.query(DATABASE_TABLENAME_TOPIC,
                new String[]{"id", KEY_TOPIC, KEY_TRANSLATED}, null, null, null, null, "id ASC");

        Topic topic;
        if (cursor.moveToFirst()) {
            do {
                topic = new Topic();
                topic.setId(cursor.getInt(0));
                topic.setTopic(cursor.getString(1));
                topic.setTopicTranslated(cursor.getString(2));
                topicList.add(topic);
            } while (cursor.moveToNext());
        }
        return topicList;

    }

    public boolean updateTopics(int id, String translated) throws SQLException {

        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_TRANSLATED, translated);

        return db.update(DATABASE_TABLENAME_TOPIC, contentValues, "id=" + id, null) > 0;

    }

    public int getCountTopics(){
        Cursor cursor;
        int count = 0;

        cursor = db.rawQuery("select count("+ KEY_TOPIC +") from "+ DATABASE_TABLENAME_TOPIC,null);
        if(cursor.moveToFirst())
            count = cursor.getInt(0);
        cursor.close();

        return count;


    }

    private static class DataBaseHelper extends SQLiteOpenHelper {


        DataBaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {

            try {
                sqLiteDatabase.execSQL(DATABASE_CREATE_TOPICS);
                sqLiteDatabase.execSQL(DATABASE_CREATE_MY_TOPICS);
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLENAME_TOPIC);
            onCreate(sqLiteDatabase);
        }

    }


}
