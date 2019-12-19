package de.bfw.cbo.myquizapp;

// mit der Superklasse SQLiteDATABASE

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

// Klasse einbinden, in der das Layout festgelegt wurde
import de.bfw.cbo.myquizapp.QuizContract.*;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class QuizDbHelper extends SQLiteOpenHelper {

    //https://stackoverflow.com/questions/513084/ship-an-application-with-a-database
    private static final String TAG = "SQLiteOpenHelper";

    private final Context context;
    private boolean createDb = false, upgradeDb = false;

    private static final String DATABASE_NAME = "MyQuizApp.db";
    private static final int DATABASE_VERSION = 2; // Trigger onUpgrade

    // aus der klasse ein Singleton machen, damit nur eine Instanz auf die DB zugreift
    private static QuizDbHelper instance;

    private SQLiteDatabase db;

    public QuizDbHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        this.context = context;
    }

    // zugriff von anderer Klasse möglich, ohne neue Instanz
    public static synchronized QuizDbHelper getInstance(Context context) {
        if (instance == null) {
            instance = new QuizDbHelper(context.getApplicationContext());
        }
        return instance;
    }

    // nur beim ersten Mal
    @Override
    public void onCreate(SQLiteDatabase db) {
        this.db = db;
        Log.i(TAG, "<-- onCreate() called -->" + db.getPath());

        createDb = true;

        final String SQL_CREATE_CATEGORIES_TABLE = "CREATE TABLE " +
                CategoriesTables.TABLE_NAME + "( " +
                CategoriesTables._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                CategoriesTables.COLUMN_NAME + " TEXT" +
                ")";

        /*
         *  hier wird das SQL zusammengesetzt, kein AUTOINCREMENT,
         *  auf Leerzeichen achten
         */
        final String SQL_CREATE_QUESTIONS_TABLE ="CREATE TABLE " +
                QuestionsTable.TABLE_NAME + " ( " +
                QuestionsTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                QuestionsTable.COLUMN_QUESTION + " TEXT, " +
                QuestionsTable.COLUMN_OPTION1 + " TEXT," +
                QuestionsTable.COLUMN_OPTION2 + " TEXT," +
                QuestionsTable.COLUMN_OPTION3 + " TEXT," +
                QuestionsTable.COLUMN_ANSWER_NR + " INTEGER," +
                QuestionsTable.COLUMN_DIFFICULTY + " TEXT," +
                QuestionsTable.COLUMN_CATEGORY_ID + " INTEGER," +
                "FOREIGN KEY(" + QuestionsTable.COLUMN_CATEGORY_ID + ") REFERENCES " +
                CategoriesTables.TABLE_NAME + "(" + CategoriesTables._ID + ")" + "ON DELETE CASCADE" +
                ")";

        // SQL ausführen
        db.execSQL(SQL_CREATE_CATEGORIES_TABLE);
        db.execSQL(SQL_CREATE_QUESTIONS_TABLE);
        /**
         * Für Änderungen App deinstallieren, Version ändern, oder DB löschen
         */
        fillCategoriesTable();
        fillQuestionsTable();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        upgradeDb = true;
        db.execSQL("DROP TABLE IF EXISTS " + CategoriesTables.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + QuestionsTable.TABLE_NAME);
        onCreate(db);
    }

    // bei Änderung der DAtenbank
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    public void addCategory(Category category) {
        db = getWritableDatabase();
        insertCategory(category);
    }

    public void addCategories(List<Category> categories) {
        db = getWritableDatabase();

        for (Category category : categories) {
            insertCategory(category);
        }
    }

    private void insertCategory(Category category) {
        ContentValues cv = new ContentValues();
        cv.put(CategoriesTables.COLUMN_NAME, category.getName());
        db.insert(CategoriesTables.TABLE_NAME, null, cv);
    }

    private void fillCategoriesTable() {
        Category c1 = new Category("Programmierung");
        insertCategory(c1);
        Category c2 = new Category("Geographie");
        insertCategory(c2);
        Category c3 = new Category("Mathe");
        insertCategory(c3);
        Category c4 = new Category("Kunst");
        insertCategory(c4);
        Category c5 = new Category("Geschichte");
        insertCategory(c5);
        Category c6 = new Category("Mischen");
        insertCategory(c6);


    }

    private void fillQuestionsTable() {
        Question q1 = new Question("Einfach A ist korrekt",
                "A", "B", "C", 1,
                Question.DIFFICULTY_EASY, Category.PROGRAMMING);
        insertQuestion(q1);
        Question q2 = new Question("Geographie, Mittel, B ist korrekt",
                "A", "B", "C", 2,
                Question.DIFFICULTY_MEDIUM, Category.GEOGRAPHY);
        insertQuestion(q2);
        Question q3 = new Question("Mathe, Schwer C ist korrekt",
                "A", "B", "C", 3,
                Question.DIFFICULTY_HARD, Category.MATH);
        insertQuestion(q3);
        Question q4 = new Question("Mathe, Einfach A ist korrekt",
                "A", "B", "C", 1,
                Question.DIFFICULTY_EASY, Category.MATH);
        insertQuestion(q4);
        // android.database.sqlite.SQLiteConstraintException: FOREIGN KEY constraint failed (code 787)
        Question q5 = new Question(" Kunst, Einfach A ist korrekt",
                "A", "B", "C", 1,
                Question.DIFFICULTY_EASY, Category.ART);
        insertQuestion(q5);
        Question q6 = new Question("Geschichte, Mittel B ist korrekt",
                "A", "B", "C", 2,
                Question.DIFFICULTY_MEDIUM, Category.HISTORY);
        insertQuestion(q6);

    }

    public void addQuestion(Question question) {
        db = getWritableDatabase();
        insertQuestion(question);
    }

    public void addQuestions(List<Question> questions) {
        db = getWritableDatabase();

        for (Question question : questions) {
            insertQuestion(question);
        }
    }


    private void insertQuestion(Question question) {
        ContentValues cv = new ContentValues();
        cv.put(QuestionsTable.COLUMN_QUESTION, question.getQuestion());
        cv.put(QuestionsTable.COLUMN_OPTION1, question.getOption1());
        cv.put(QuestionsTable.COLUMN_OPTION2, question.getOption2());
        cv.put(QuestionsTable.COLUMN_OPTION3, question.getOption3());
        cv.put(QuestionsTable.COLUMN_ANSWER_NR,question.getAnswerNr());
        cv.put(QuestionsTable.COLUMN_DIFFICULTY, question.getDifficulty());
        cv.put(QuestionsTable.COLUMN_CATEGORY_ID, question.getCategoryID());
        db.insert(QuestionsTable.TABLE_NAME, null, cv);
    }

    public List<Category> getAllCategories() {
        List<Category> categoryList = new ArrayList<>();
        db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + CategoriesTables.TABLE_NAME, null);

        if (c.moveToFirst()) {
            do {
                Category category = new Category();
                category.setId(c.getInt(c.getColumnIndex(CategoriesTables._ID)));
                category.setName(c.getString(c.getColumnIndex(CategoriesTables.COLUMN_NAME)));
                categoryList.add(category);
            } while (c.moveToNext());
        }
        c.close();
        return categoryList;
    }

    // Fragen laden
    public ArrayList<Question> getAllQuestions(String difficulty) {
        ArrayList<Question> questionList = new ArrayList<>();
        db = getReadableDatabase();

        String selection = QuestionsTable.COLUMN_DIFFICULTY + " = ? ";
        String[] selectionArgs = new String[]{difficulty}; // zahl in String

        //Cursor c = db.rawQuery("SELECT * FROM " + QuestionsTable.TABLE_NAME, null);
        Cursor c = db.query(
                QuestionsTable.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        if (c.moveToFirst()) {
            do {
                Question question = new Question();
                question.setId(c.getInt(c.getColumnIndex(QuestionsTable._ID)));
                question.setQuestion(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_QUESTION)));
                question.setOption1(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_OPTION1)));
                question.setOption2(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_OPTION2)));
                question.setOption3(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_OPTION3)));
                question.setAnswerNr(c.getInt(c.getColumnIndex(QuestionsTable.COLUMN_ANSWER_NR)));
                question.setDifficulty(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_DIFFICULTY)));
                question.setCategoryID(c.getInt(c.getColumnIndex(QuestionsTable.COLUMN_CATEGORY_ID)));
                questionList.add(question);
            } while (c.moveToNext());
        }

        c.close();
        return questionList;
    } // getAllQuestions

    public ArrayList<Question> getQuestions(int categoryID, String difficulty) {
        ArrayList<Question> questionList = new ArrayList<>();
        db = getReadableDatabase();

        String selection = QuestionsTable.COLUMN_CATEGORY_ID + " = ? " +
                " AND " + QuestionsTable.COLUMN_DIFFICULTY + " = ? ";
        String[] selectionArgs = new String[]{String.valueOf(categoryID), difficulty}; // zahl in String

        Cursor c = db.query(
                QuestionsTable.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
        if (c.moveToFirst()) {
            do {
                Question question = new Question();
                question.setId(c.getInt(c.getColumnIndex(QuestionsTable._ID)));
                question.setQuestion(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_QUESTION)));
                question.setOption1(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_OPTION1)));
                question.setOption2(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_OPTION2)));
                question.setOption3(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_OPTION3)));
                question.setAnswerNr(c.getInt(c.getColumnIndex(QuestionsTable.COLUMN_ANSWER_NR)));
                question.setDifficulty(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_DIFFICULTY)));
                question.setCategoryID(c.getInt(c.getColumnIndex(QuestionsTable.COLUMN_CATEGORY_ID)));
                questionList.add(question);
            } while (c.moveToNext());
        }

        c.close();
        return questionList;
    } // getAllQuestions


    private void copyDatabaseFromAssets(SQLiteDatabase db) {
        Log.i(TAG, "copyDatabase");
        InputStream myInput = null;
        OutputStream myOutput = null;
        try {
            // Open db packaged as asset as the input stream
            myInput = context.getAssets().open("databases/" + DATABASE_NAME);

            // Open the db in the application package context:
            myOutput = new FileOutputStream(db.getPath());

            // Transfer db file contents:
            byte[] buffer = new byte[1024];
            int length;
            while ((length = myInput.read(buffer)) > 0) {
                myOutput.write(buffer, 0, length);
            }
            myOutput.flush();

            // Set the version of the copied database to the current
            // version:
            SQLiteDatabase copiedDb = context.openOrCreateDatabase(
                    DATABASE_NAME, 0, null);
            copiedDb.execSQL("PRAGMA user_version = " + DATABASE_VERSION);
            copiedDb.close();

        } catch (IOException e) {
            e.printStackTrace();
            throw new Error(TAG + " Error copying database");
        } finally {
            // Close the streams
            try {
                if (myOutput != null) {
                    myOutput.close();
                }
                if (myInput != null) {
                    myInput.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw new Error(TAG + " Error closing streams");
            }
        }
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        Log.i(TAG, "onOpen db");
        if (createDb) {// The db in the application package
            // context is being created.
            // So copy the contents from the db
            // file packaged in the assets
            // folder:
            createDb = false;
            copyDatabaseFromAssets(db);

        }
        if (upgradeDb) {// The db in the application package
            // context is being upgraded from a lower to a higher version.
            upgradeDb = false;
            // Your db upgrade logic here:
        }
    }

}
