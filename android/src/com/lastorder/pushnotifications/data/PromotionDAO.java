package com.lastorder.pushnotifications.data;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.lastorder.pushnotifications.Promotion;


public class PromotionDAO implements DAO<Promotion> {
	
	private static final String dateFormat = "yyyy MM dd HH:mm:ss Z";
	DateFormat df = new SimpleDateFormat(dateFormat);
	private SQLiteStatement promotionInsertStmt;
	private static final String promotion_INSERT = 
		"insert into " + DataConstants.TABLE_PROMOTIONS + "(" + DataConstants.VENUE + "," + DataConstants.NAME + "," + DataConstants.DESCRIPTION + 
		"," + DataConstants.DISCOUNT + "," + DataConstants.PRICE + "," + DataConstants.EXPIRATION + "," + DataConstants.ADDRESS + "," + DataConstants.LAT + "," + DataConstants.LON + "," +DataConstants.URL_IMAGE + ") values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

	private SQLiteDatabase db;
	
	public PromotionDAO(SQLiteDatabase db) {
		this.db = db;
		promotionInsertStmt = db.compileStatement(promotion_INSERT);
	}
	public void renewDB(SQLiteDatabase db) {
		this.db = db;
		promotionInsertStmt = db.compileStatement(promotion_INSERT);
	}
	
	public static void onCreate(SQLiteDatabase db) {
		StringBuilder sb = new StringBuilder();
		// plants table
		
		sb.setLength(0);
		sb.append("CREATE TABLE " + DataConstants.TABLE_PROMOTIONS + " (");
		sb.append(DataConstants.ID + " INTEGER PRIMARY KEY, ");
		sb.append(DataConstants.VENUE + " TEXT, ");
		sb.append(DataConstants.NAME + " TEXT, ");
		sb.append(DataConstants.DESCRIPTION + " TEXT, ");
		sb.append(DataConstants.DISCOUNT + " INTEGER,");
		sb.append(DataConstants.PRICE + " TEXT,");
		sb.append(DataConstants.EXPIRATION + " DATE,");
		sb.append(DataConstants.ADDRESS + " TEXT,");
		sb.append(DataConstants.LAT + " TEXT,");
		sb.append(DataConstants.LON + " TEXT,");
		sb.append(DataConstants.URL_IMAGE + " TEXT");
		sb.append(");");
		db.execSQL(sb.toString());
	}
	
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		ArrayList<Promotion> set = new ArrayList<Promotion>();
		Cursor c =
			db.query(DataConstants.TABLE_PROMOTIONS, null, null,
	                        null, null, null, DataConstants.ID, null);
		
		while (c.moveToNext())
			{
				Promotion a = new Promotion();
				a.id = (c.getLong(0));
				a.venue = (c.getString(1));
				a.name = (c.getString(2));
				a.description = (c.getString(3));
				a.discount = (c.getInt(4));
				a.price = (Long.parseLong(c.getString(5)));
				try {
					a.expiration.getInstance().setTime(df.parse(c.getString(6)));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				a.address = (c.getString(7));
				a.lat = (Double.parseDouble(c.getString(8)));
				a.lon = (Double.parseDouble(c.getString(9)));
				a.url_image = (c.getString(10));

	            set.add(a);
	        } 
		
		if (!c.isClosed()) {
			c.close();
		}
		db.execSQL("DROP TABLE IF EXISTS " + DataConstants.TABLE_PROMOTIONS);
		PromotionDAO.onCreate(db);
		int z = set.size();
		for(int x = 0; x < z; x ++) {
			this.insert(set.get(x));
		}
	}
	
	@Override
	public void deleteAll() {
		// TODO Auto-generated method stub
		db.delete(DataConstants.TABLE_PROMOTIONS, null, null);
	}

	@Override
	public Cursor getCursor(String orderBy, String whereClauseLimit) {
		// TODO Auto-generated method stub

	      throw new UnsupportedOperationException("Not yet implemented");
	}

	
	
	@Override
	public Promotion select(long id) {
		// TODO Auto
		Promotion a = null;
		Cursor c =
			db.query(DataConstants.TABLE_PROMOTIONS , null, DataConstants.ID
	                        + " = ?", new String[] { String.valueOf(id) }, null, null, null, "1");
		if (c.moveToFirst()) {
			a = new Promotion();
			a.id = (c.getLong(0));
			a.venue = (c.getString(1));
			a.name = (c.getString(2));
			a.description = (c.getString(3));
			a.discount = (c.getInt(4));
			a.price = (Long.parseLong(c.getString(5)));
			try {
				a.expiration.getInstance().setTime(df.parse(c.getString(6)));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			a.address = (c.getString(7));
			a.lat = (Double.parseDouble(c.getString(8)));
			a.lon = (Double.parseDouble(c.getString(9)));
			a.url_image = (c.getString(10));

			}
		if (!c.isClosed()) {
			c.close();
		}
		return a;
	}

	@Override
	public ArrayList<Promotion> selectAll() {
		// TODO Auto-generated method stub
		ArrayList<Promotion> set = new ArrayList<Promotion>();
		Cursor c =
			db.query(DataConstants.TABLE_PROMOTIONS, null, null,
	                        null, null, null, DataConstants.ID, null);
		
		while (c.moveToNext())
			{
				Promotion a = new Promotion();
				a.id = (c.getLong(0));
				a.venue = (c.getString(1));
				a.name = (c.getString(2));
				a.description = (c.getString(3));
				a.discount = (c.getInt(4));
				try {
				a.price = (Long.parseLong(c.getString(5)));
				} catch(NumberFormatException e) { }
				try {
					a.expiration.getInstance().setTime(df.parse(c.getString(6)));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				a.address = (c.getString(7));
				a.lat = (Double.parseDouble(c.getString(8)));
				a.lon = (Double.parseDouble(c.getString(9)));
				a.url_image = (c.getString(10));

	            set.add(a);
	        } 
		
		if (!c.isClosed()) {
			c.close();
		}
		int size = set.size();
		return set;
	}

	@Override
	public long insert(Promotion entity) {
		// TODO Auto-generated method stub
		promotionInsertStmt.clearBindings();
		promotionInsertStmt.bindString(1, entity.venue);
		promotionInsertStmt.bindString(2, entity.name);
		promotionInsertStmt.bindString(3, entity.description);
		promotionInsertStmt.bindLong(4, entity.discount);
		promotionInsertStmt.bindString(5, ((Double)entity.price).toString());
		promotionInsertStmt.bindString(6, entity.expiration.toString());
		promotionInsertStmt.bindString(7, entity.address);
		promotionInsertStmt.bindString(8, ((Double)entity.lat).toString());
		promotionInsertStmt.bindString(9, ((Double)entity.lon).toString());
		promotionInsertStmt.bindString(10, entity.url_image);
		
		return promotionInsertStmt.executeInsert();
	}

	@Override
	public void update(Promotion entity) {
		// TODO Auto-generated method stub
		if (entity != null && entity.id != 0) {
			Promotion OldPlant = select(entity.id);
			
			if (OldPlant == null) {
				throw new IllegalArgumentException("Cannot update login that does not already exist.");
			}
			db.beginTransaction();
			
			try {
			final ContentValues values = new ContentValues();
			values.put(DataConstants.VENUE, entity.venue);
			values.put(DataConstants.NAME, entity.name);
			values.put(DataConstants.DESCRIPTION, entity.description);
			values.put(DataConstants.DISCOUNT, entity.discount);
			values.put(DataConstants.PRICE, ((Double)entity.price).toString());
			values.put(DataConstants.EXPIRATION, entity.expiration.toString());
			values.put(DataConstants.ADDRESS, entity.address);
			values.put(DataConstants.LAT, ((Double)entity.lat).toString());
			values.put(DataConstants.LON, ((Double)entity.lon).toString());
			values.put(DataConstants.URL_IMAGE, entity.url_image);
			db.update(DataConstants.TABLE_PROMOTIONS, values, DataConstants.ID + " = ?", new String[] { String .valueOf(entity.id) });
			db.setTransactionSuccessful();
			} catch (SQLException e) {
				//Log.d("PDAO", "Error updating plant");
			} finally {
				db.endTransaction();
			}
		} else {
			throw new IllegalArgumentException("Error, login cannot be null, and must have a title");
		}
		
	}

	@Override
	public void delete(long id) {
		// TODO Auto-generated method stub
		Promotion a = select(id);
		if (a != null) {
			db.delete(DataConstants.TABLE_PROMOTIONS, DataConstants.ID + " = ?", new String[] { String.valueOf(id) });
		}
		
	}
	}
