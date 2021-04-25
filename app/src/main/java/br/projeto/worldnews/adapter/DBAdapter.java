package br.projeto.worldnews.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import br.projeto.worldnews.model.Article;
import br.projeto.worldnews.model.Topic;


public class DBAdapter {

    static final String KEY_ID = "id";
    static final String KEY_TOPIC = "topic";
    static final String KEY_ARGS = "args";
    static final String KEY_TITLES = "title";
    static final String KEY_TRANSLATE = "translate";
    static final String KEY_TRANSLATED = "translated";
    static final String KEY_MENSAGEM = "mensagem";
    static final String DATABASE_NAME = "newstopics";
    static final String DATABASE_TABLENAME_TITLES = "titles";
    static final String DATABASE_TABLENAME_TOPIC = "topics";
    static final String DATABASE_TABLENAME_MENSAGEM = "mensagem";
    static final String DATABASE_TABLENAME_MY_TOPIC = "mytopics";
    static final int DATABASE_VERSION = 1;
    static final String DATABASE_CREATE_TOPICS = "CREATE TABLE IF NOT EXISTS " + DATABASE_TABLENAME_TOPIC + "(" +
            KEY_ID + " integer primary key autoincrement," +
            KEY_TOPIC + " text not null unique ON CONFLICT ABORT," +
            KEY_TRANSLATE + " text," +
            KEY_TRANSLATED + " boolean DEFAULT 0);";
    static final String DATABASE_CREATE_MY_TOPICS = "CREATE TABLE IF NOT EXISTS " + DATABASE_TABLENAME_MY_TOPIC + "(" +
            KEY_ID + " integer primary key autoincrement," +
            KEY_TOPIC + " text not null unique ON CONFLICT ABORT," +
            KEY_ARGS + " text);";
    static final String DATABASE_CREATE_MENSAGEM = "CREATE TABLE IF NOT EXISTS " + DATABASE_TABLENAME_MENSAGEM + "(" +
            KEY_ID + " integer primary key autoincrement," +
            KEY_MENSAGEM + " text not null unique ON CONFLICT ABORT," +
            KEY_TRANSLATE + " text," +
            KEY_TRANSLATED + " boolean DEFAULT 0);";
    static final String DATABASE_CREATE_TITLES = "CREATE TABLE IF NOT EXISTS " + DATABASE_TABLENAME_TITLES + "(" +
            KEY_ID + " integer primary key autoincrement," +
            KEY_TITLES + " text not null unique ON CONFLICT ABORT);";

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
        initialValues.put(KEY_TRANSLATE, translate);
        return db.insert(DATABASE_TABLENAME_TOPIC, null, initialValues);
    }

    public long insertMyTopics(String topics, String args) throws SQLException {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TOPIC, topics);
        initialValues.put(KEY_ARGS, args);
        return db.insert(DATABASE_TABLENAME_MY_TOPIC, null, initialValues);
    }

    public boolean deleteAllTitles() throws SQLException {

        return db.delete(DATABASE_TABLENAME_TITLES, null, null) > 0;

    }

    public String getMensagemTranslated(int id) {
        String value = "ok";
        Cursor cursor = db.query(DATABASE_TABLENAME_MENSAGEM,
                new String[]{KEY_TRANSLATE}, KEY_ID + "=?", new String[]{id + ""}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            value = cursor.getString(cursor.getColumnIndex(KEY_TRANSLATE));
            cursor.close();
        }

        return value;

    }

    public long insertMensagem(String mensagem, String translate) throws SQLException {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_MENSAGEM, mensagem);
        initialValues.put(KEY_TRANSLATE, translate);
        return db.insert(DATABASE_TABLENAME_MENSAGEM, null, initialValues);
    }

    public long insertTitle(String titles) throws SQLException {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TITLES, titles);
        return db.insert(DATABASE_TABLENAME_TITLES, null, initialValues);
    }

    public int existTitleInTable(String titles) {

        Cursor cursor = db.query(DATABASE_TABLENAME_TITLES,
                new String[]{KEY_TITLES}, KEY_TITLES + "=?", new String[]{titles}, null, null, "id ASC");
        cursor.moveToFirst();

        if (cursor.getCount() == 1)
            return 1;
        else
            return 0;

    }

    public List<Article> getAllTitles() throws SQLException {

        List<Article> list = new LinkedList<Article>();

        Cursor cursor = db.query(DATABASE_TABLENAME_TITLES,
                new String[]{"id", KEY_TITLES}, null, null, null, null, "id ASC");

        Article article;
        if (cursor.moveToFirst()) {
            do {
                article = new Article();
                article.setId(cursor.getInt(0));
                article.setTitle(cursor.getString(1));
                list.add(article);

            } while (cursor.moveToNext());
        }
        return list;
    }

    public int getCountTitles() {
        Cursor cursor;
        int count = 0;

        cursor = db.rawQuery("select count(" + KEY_TITLES + ") from " + DATABASE_TABLENAME_TITLES, null);
        if (cursor.moveToFirst())
            count = cursor.getInt(0);
        cursor.close();

        return count;


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

    public String getCountryName(String country) {

        Cursor cursor = db.query(DATABASE_TABLENAME_TOPIC,
                new String[]{KEY_TRANSLATE}, KEY_TOPIC + "=?", new String[]{country}, null, null, "id ASC");
        cursor.moveToFirst();
        return cursor.getString(0);

    }

    public List<Mensagem> getAllMensagem() throws SQLException {

        List<Mensagem> mensagemList = new ArrayList<>();

        Cursor cursor = db.query(DATABASE_TABLENAME_MENSAGEM,
                new String[]{"id", KEY_MENSAGEM, KEY_TRANSLATE, KEY_TRANSLATED}, null, null, null, null, "id ASC");

        Mensagem mensagem;
        if (cursor.moveToFirst()) {
            do {
                mensagem = new Mensagem();
                mensagem.setId(cursor.getInt(0));
                mensagem.setMensagem(cursor.getString(1));
                mensagem.setTranslated(cursor.getString(2));
                if (cursor.getInt(3) == 0)
                    mensagem.setIsTranlated(false);
                else
                    mensagem.setIsTranlated(true);
                mensagemList.add(mensagem);
            } while (cursor.moveToNext());
        }
        return mensagemList;

    }


    //retriever all values from database
    public List<Topic> getAllTopics() throws SQLException {

        List<Topic> topicList = new LinkedList<Topic>();

        Cursor cursor = db.query(DATABASE_TABLENAME_TOPIC,
                new String[]{"id", KEY_TOPIC, KEY_TRANSLATE, KEY_TRANSLATED}, null, null, null, null, "id ASC");

        Topic topic;
        if (cursor.moveToFirst()) {
            do {
                topic = new Topic();
                topic.setId(cursor.getInt(0));
                topic.setTopic(cursor.getString(1));
                topic.setTopicTranslate(cursor.getString(2));
                if (cursor.getInt(3) == 0)
                    topic.setTranslated(false);
                else
                    topic.setTranslated(true);
                topicList.add(topic);
            } while (cursor.moveToNext());
        }
        return topicList;

    }

    public boolean updateMensagem(int id, String translate) throws SQLException {

        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_TRANSLATE, translate);
        contentValues.put(KEY_TRANSLATED, 1);

        return db.update(DATABASE_TABLENAME_MENSAGEM, contentValues, "id=" + id, null) > 0;

    }

    public boolean updateTopicsToFalseTranslate() throws SQLException {

        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_TRANSLATED, 0);
        return db.update(DATABASE_TABLENAME_TOPIC, contentValues, null, null) > 0;

    }

    public boolean updateMensagemToFalseTranslate() throws SQLException {

        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_TRANSLATED, 0);
        return db.update(DATABASE_TABLENAME_MENSAGEM, contentValues, null, null) > 0;

    }

    public boolean updateTopics(int id, String translate) throws SQLException {

        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_TRANSLATE, translate);
        contentValues.put(KEY_TRANSLATED, 1);

        return db.update(DATABASE_TABLENAME_TOPIC, contentValues, "id=" + id, null) > 0;

    }

    public int getCountMensagemTranslated() {
        Cursor cursor;
        int count = 0;

        cursor = db.rawQuery("select count(" + KEY_TRANSLATED + ") from " + DATABASE_TABLENAME_MENSAGEM
                + " where " + KEY_TRANSLATED + " =1", null);
        if (cursor.moveToFirst())
            count = cursor.getInt(0);
        cursor.close();
        return count;

    }

    public int getCountTopicsTranslated() {
        Cursor cursor;
        int count = 0;

        cursor = db.rawQuery("select count(" + KEY_TRANSLATED + ") from " + DATABASE_TABLENAME_TOPIC
                + " where " + KEY_TRANSLATED + " =1", null);
        if (cursor.moveToFirst())
            count = cursor.getInt(0);
        cursor.close();

        return count;


    }
    public int getCountTopics(){
        Cursor cursor;
        int count = 0;

        cursor = db.rawQuery("select count(" + KEY_TOPIC + ") from " + DATABASE_TABLENAME_TOPIC, null);
        if (cursor.moveToFirst())
            count = cursor.getInt(0);
        cursor.close();

        return count;


    }

    public int getCountMensagem() {
        Cursor cursor;
        int count = 0;

        cursor = db.rawQuery("select count(" + KEY_MENSAGEM + ") from " + DATABASE_TABLENAME_MENSAGEM, null);
        if (cursor.moveToFirst())
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
                sqLiteDatabase.execSQL(DATABASE_CREATE_TITLES);
                sqLiteDatabase.execSQL(DATABASE_CREATE_MENSAGEM);
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

    public class Mensagem {
        int id;
        String mensagem;
        String translated;
        boolean isTranlated;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getMensagem() {
            return mensagem;
        }

        public void setMensagem(String mensagem) {
            this.mensagem = mensagem;
        }

        public String getTranslated() {
            return translated;
        }

        public void setTranslated(String translated) {
            this.translated = translated;
        }

        public boolean getIsTranlated() {
            return isTranlated;
        }

        public void setIsTranlated(boolean isTranlated) {
            this.isTranlated = isTranlated;
        }


    }


}
