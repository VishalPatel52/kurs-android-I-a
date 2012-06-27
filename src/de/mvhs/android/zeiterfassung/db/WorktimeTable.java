package de.mvhs.android.zeiterfassung.db;

import java.text.ParseException;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;

public class WorktimeTable {
	// Variablen
	/**
	 * Tabellenname
	 */
	public static final String TABLE_NAME = "worktime";
	/**
	 * ID Spalte
	 */
	public static final String COLUMN_ID = "_id";
	/**
	 * Startzeit Spalte
	 */
	public static final String COLUMN_START_TIME = "start_time";
	/**
	 * Endzeit Spalte
	 */
	public static final String COLUMN_END_TIME = "end_time";
	
	/**
	 * Script zum erstellen der Tabelle
	 */
	public static final String CREATE_TABLE = "CREATE TABLE "
		+ TABLE_NAME
		+ " ("
		+ COLUMN_ID
		+ " INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL , "
		+ COLUMN_START_TIME
		+ " TEXT NOT NULL , "
		+ COLUMN_END_TIME
		+ " TEXT)";
	/**
	 * Script zum L�schen der Tabelle
	 */
	public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
	
	private final Context _CONTEXT;
	
	/**
	 * Standard-Constructor
	 * @param context
	 * App Context
	 */
	public WorktimeTable(Context context){
		_CONTEXT = context;
	}
	
	// Public Methoden
	/**
	 * Anlegen eines neuen Datensatzes
	 * @param startTime Startzeit
	 * @return
	 * ID des neuen Datensatzes
	 */
	public long saveWorktime(Date startTime){
		long returnValue = -1;
		
		ContentValues values = new ContentValues();
		values.put(WorktimeTable.COLUMN_START_TIME,
			DBHelper.DB_DATE_FORMAT.format(startTime));
		
		DBHelper helper = new DBHelper(_CONTEXT);
		SQLiteDatabase db = helper.getWritableDatabase();
		returnValue = db.insert(WorktimeTable.TABLE_NAME, null, values);
		
		// Schlie�en der Datenbankverbindung
		db.close();
		helper.close();
		
		return returnValue;
	}
	
	/**
	 * Aktualisieren eines Datensatzes mit Endzeit
	 * @param endTime
	 * Endzeit
	 * @param id
	 * ID des zu aktualisierenden Datensatzes
	 * @return
	 * Anzahl der aktualiserten Datens�tze
	 */
	public int updateWorktime(long id, Date endTime){
		int returnValue = -1;
		
		ContentValues values = new ContentValues();
		values.put(WorktimeTable.COLUMN_END_TIME,
			DBHelper.DB_DATE_FORMAT.format(endTime));
		
		DBHelper helper = new DBHelper(_CONTEXT);
		SQLiteDatabase db = helper.getWritableDatabase();
		returnValue = db.update(
			WorktimeTable.TABLE_NAME, // Tabellenname
			values, // Werte, die aktualisiert werden sollen
			WorktimeTable.COLUMN_ID + " =?", // Bedingung, welche Datens�tze aktualisiert werden sollten
			new String[]{String.valueOf(id)}); // Parameter f�r die Bedingung
		
		// Schlie�en der Datenbank
		db.close();
		helper.close();
		
		// Ergebnis zur�ckliefern
		return returnValue;
	}
	
	/**
	 * Aktualisieren des gesamten Datensatzes
	 * @param id
	 * ID des zu aktualisiernden Datensatzes
	 * @param startTime
	 * Startzeit
	 * @param endTime
	 * Endzeit
	 * @return
	 * Anzhal der aktualisierten Datens�tze
	 */
	public int updateWorktime(long id, Date startTime, Date endTime){
		int returnValue = -1;
		
		DBHelper helper = new DBHelper(_CONTEXT);
		SQLiteDatabase db = helper.getWritableDatabase();
		
		// Kompiliertes Statement hinzuf�gen
		SQLiteStatement update = db.compileStatement("UPDATE "
			+ WorktimeTable.TABLE_NAME
			+ " SET "
			+ WorktimeTable.COLUMN_START_TIME + " = ?1, "
			+ WorktimeTable.COLUMN_END_TIME + " = ?2 "
			+ " WHERE "
			+ WorktimeTable.COLUMN_ID + " = ?3");
		
		// Paremeter an das Kompilat binden
		update.bindString(1, DBHelper.DB_DATE_FORMAT.format(startTime));
		update.bindString(2, DBHelper.DB_DATE_FORMAT.format(endTime));
		update.bindLong(3, id);
		
		// Update ausf�hren
		returnValue = update.executeUpdateDelete();
		
		// Datenbank schlie�en
		update.close();
		db.close();
		helper.close();
		
		return returnValue;
	}
	
	/**
	 * L�schen eines Datensatzes
	 * @param id
	 * ID des zu l�schenden Datensatzes
	 * @return
	 * Anzahl der gel�schten Datens�tze
	 */
	public int deleteWorktime(long id){
		int returnValue = -1;
		
		DBHelper helper = new DBHelper(_CONTEXT);
		SQLiteDatabase db = helper.getWritableDatabase();
		
		returnValue = db.delete(
			WorktimeTable.TABLE_NAME, // Tabellenname
			WorktimeTable.COLUMN_ID + " =?", // Bedingung / Filter
			new String[]{String.valueOf(id)}); // Paramter f�r die Bedingung
		
		// Schlie�en der Datenbank
		db.close();
		helper.close();
		
		return returnValue;
	}
	
	/**
	 * ID eines offenen Datensatzes
	 * @return
	 * ID des gefundenen Datensatzes (-1, wenn keins gefunden wurde)
	 */
	public long getOpenWorktime(){
		long returnValue = -1;
		
		DBHelper helper = new DBHelper(_CONTEXT);
		SQLiteDatabase db = helper.getReadableDatabase();
		
		// Builder initialisieren
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		// Tabelle binden
		builder.setTables(WorktimeTable.TABLE_NAME);
		// Bedingung binden
		builder.appendWhere(WorktimeTable.COLUMN_END_TIME + " IS NULL OR " + WorktimeTable.COLUMN_END_TIME + " = ''");
		// Holen der Daten
		Cursor data = builder.query(
			db, // Datenbnak
			new String[]{WorktimeTable.COLUMN_ID}, // Spalten (null -> alle)
			null, // Bedingung
			null, // Parameter f�r die Bedingung
			null, // Group Bedingung
			null, // Having Bedingung
			null); // Sortierung
		
		if (data != null && data.moveToFirst()) {
			returnValue = data.getLong(data.getColumnIndex(WorktimeTable.COLUMN_ID));
		}
		
		// Datenbank Schlie�en
		data.close();
		db.close();
		helper.close();
		
		return returnValue;
	}
	
	/**
	 * Startzeit des Datensatzes holen
	 * @param id
	 * ID des Datensatzes
	 * @return
	 * Startdatum, null wenn keins gefunden wurde
	 */
	public Date getStartDate(long id){
		Date returnValue = null;
		
		DBHelper helper = new DBHelper(_CONTEXT);
		SQLiteDatabase db = helper.getReadableDatabase();
		
		// Builder initialisieren
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		// Tabelle binden
		builder.setTables(WorktimeTable.TABLE_NAME);
		// Bedingung binden
		builder.appendWhere(WorktimeTable.COLUMN_ID + " =? ");
		// Holen der Daten
		Cursor data = builder.query(
			db, // Datenbank
			new String[]{WorktimeTable.COLUMN_START_TIME}, // Spalten
			null, // Bedingung
			new String[]{String.valueOf(id)}, // Parameter
			null, // Group Bedingung
			null, // Having Bedingung
			null); // Sortierung
		
		if (data != null && data.moveToFirst()) {
			String dbValue = data.getString(data.getColumnIndex(WorktimeTable.COLUMN_START_TIME));
			try {
				returnValue = DBHelper.DB_DATE_FORMAT.parse(dbValue);
			} catch (ParseException e) {
				// Keine Behandlung der Ausnahme
				returnValue = null;
			}
		}
		
		// Datenbank Schlie�en
		data.close();
		db.close();
		helper.close();
		
		return returnValue;
	}
}