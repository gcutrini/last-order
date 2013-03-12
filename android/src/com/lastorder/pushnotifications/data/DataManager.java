package com.lastorder.pushnotifications.data;

import java.util.ArrayList;

import com.lastorder.pushnotifications.Promotion;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.SystemClock;
import android.util.Log;

/**
 * Android DataManager to encapsulate SQL and DB details.
 * Includes SQLiteOpenHelper, and uses DAO objects (in specified order)
 * to create/update and clear tables, and manipulate data.
 *
 */
public class DataManager {

   public static final int DATABASE_VERSION = 2;

   private static Context context;

   private SQLiteDatabase db;

   private static PromotionDAO promotionDAO;

   private static boolean itUpgraded;
   public DataManager(final Context context) {

      this.context = context; 

      OpenHelper openHelper = new OpenHelper(this.context); 
      
      db = openHelper.getWritableDatabase();
     
      // DAOs are all needed here for onCreate/onUpgrade/deleteAll, etc.
      // in some cases though they are not used to manipulate data directly
      // (rather they are nested, see bookDAO, which includes authorDAO, for now)
      // (future they probably should be more separated)
     if(!itUpgraded) {
      promotionDAO = new PromotionDAO(db);
      itUpgraded = true;
     }
      if (openHelper.isDbCreated()) {
         // insert default data here if needed
      }
   }

   public SQLiteDatabase getDb() {
      return db;
   }

   public void openDb() {
      if (!db.isOpen()) {
         db = SQLiteDatabase.openDatabase(DataConstants.DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
         // since we pass db into DAO, have to recreate DAO if db is re-opened
         
         promotionDAO.renewDB(db);
      }

   }

   public void closeDb() {
      if (db.isOpen()) {
         db.close();
      }
   }

   public void resetDb() {
      closeDb();
      SystemClock.sleep(500);
      openDb();
   }
   
   
   public Promotion selectPromotion(long id) {
	   return promotionDAO.select(id);
	   
   }
   public ArrayList<Promotion> selectAllPromotion() {
	   return promotionDAO.selectAll();
	   
   }
   public void updatePromotion(final Promotion entity) {
	   promotionDAO.update(entity);
   }
   public void deletePromotion(final long id) {
	   promotionDAO.delete(id);
   }
   public long insertPromotion(Promotion entity) {
	   return promotionDAO.insert(entity);
   }

   private static class OpenHelper extends SQLiteOpenHelper {

      private boolean dbCreated;

      OpenHelper(final Context context) {
         super(context, DataConstants.DATABASE_NAME, null, DataManager.DATABASE_VERSION);
      }
      OpenHelper(final Context context, String name) {
    	  super(context, name, null, DataManager.DATABASE_VERSION);
      }

      @Override
      public void onCreate(final SQLiteDatabase db) {
         PromotionDAO.onCreate(db);
         dbCreated = true;
      }

      @Override
      public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
         
         if(!itUpgraded) {
        	 promotionDAO = new PromotionDAO(db);
         } else {
        	 promotionDAO.renewDB(db);
         }
         promotionDAO.onUpgrade(db, oldVersion, newVersion);
         
         itUpgraded = true;
        
      }

      public boolean isDbCreated() {
         return dbCreated;
      }
   }
}