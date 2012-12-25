package me.groix.android.picross;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class GamesDB {
	public static final String KEY_ID = "gameId";
	public static final String KEY_ERROR = "error";
	public static final String KEY_TIME = "time";

	private static final String TAG = "GamesDB";


	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;

	private static final String DATABASE_CREATE =
		"create table games (gameID text primary key, "
		+ "error integer, time integer);";
	private static final String DATABASE_NAME = "data";
	private static final String DATABASE_TABLE = "games";
	private static final int DATABASE_VERSION = 2;

	private final Context mCtx;

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS games");
			onCreate(db);

		}
	}

	/**
	 * Constructor
	 * @param ctx
	 */
	public GamesDB(Context ctx) {
		this.mCtx = ctx;
	}

	/**
	 * Open the Database
	 * @return
	 * @throws SQLException
	 */
	public GamesDB open() throws SQLException {
		mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}

	/**
	 * Close the database
	 */
	public void close() {
		mDbHelper.close();
	}

	/**
	 * enregistre ou met à jour un champs
	 * @param gameID
	 * @param error
	 * @param time
	 * @return
	 */
	public boolean newGame(String gameID,int error,int time) {
		Cursor cursor =
			mDb.query(true, DATABASE_TABLE, new String[] {KEY_ID,
					KEY_ERROR,KEY_TIME}, KEY_ID + "=\"" + gameID+"\"", null,
					null, null, null, null);
		if (cursor.getCount()==0) {
			ContentValues values = new ContentValues();
			values.put(KEY_ID, gameID);
			values.put(KEY_ERROR, error);
			values.put(KEY_TIME, time);
			mDb.insert(DATABASE_TABLE, null,values);
			System.out.println("new entry, gameID="+gameID);
			return true;
		} else {
			cursor.moveToFirst();
			if (cursor.getInt(1)>=error){
				if (cursor.getInt(1)>error || cursor.getInt(2)>time){
					ContentValues cvalues = new ContentValues();
					cvalues.put(KEY_ERROR, error);
					cvalues.put(KEY_TIME, time);
					mDb.update(DATABASE_TABLE, cvalues, KEY_ID + "=\"" + gameID+"\"", null);
					return true;
				}
			}
		}
		return false;
	}

	public boolean exist(String gameID) throws SQLException {
		Cursor cursor =
			mDb.query(true, DATABASE_TABLE, new String[] {KEY_ID,
					KEY_ERROR,KEY_TIME}, KEY_ID + "=\"" + gameID+"\"", null,
					null, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();
		}
		if (cursor.getCount()!=0) {
			return true;
		}
		return false;
	}

	public Cursor getCursor(String gameID) {
		return mDb.query(true, DATABASE_TABLE, new String[] {KEY_ID,
				KEY_ERROR,KEY_TIME}, KEY_ID + "=\"" + gameID+"\"", null,
				null, null, null, null);
	}

}
