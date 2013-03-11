package com.lastorder.pushnotifications.data;

import android.os.Environment;

public class DataConstants {

   private static final String APP_PACKAGE_NAME = "com.lastorder.pushnotifications";
   public static final String DATABASE_NAME = "promotions.db";
   public static final String APP_PATH = Environment.getDataDirectory() + "/data/" + DataConstants.APP_PACKAGE_NAME;
   public static final String DATABASE_PATH =
            Environment.getDataDirectory() + "/data/" + DataConstants.APP_PACKAGE_NAME + "/databases/"
                     + DataConstants.DATABASE_NAME;


   public static final String TABLE_PROMOTIONS = "promotions";

   public static final String ID = "id";
   public static final String NAME = "name";
   public static final String VENUE = "venue";
   public static final String DESCRIPTION = "description";
   
   
   public static final String DISCOUNT = "discount";
   public static final String PRICE = "price";
   public static final String EXPIRATION = "expiration";
   public static final String ADDRESS = "address";
   public static final String LAT = "lat";
   public static final String LON = "lon";
   public static final String URL_IMAGE = "image";
    
   private DataConstants() {
   }
}
