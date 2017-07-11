package lu.uni.ibrahimtahirou.fitpro.database;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import lu.uni.ibrahimtahirou.fitpro.models.RouteModel;

import java.util.ArrayList;

public class Database {


    private static class DBHelper extends SQLiteOpenHelper {

        /**
         * Database name
         */
        private static final String DB_NAME = "db_fintess_trail_assistant";
        private static final int DB_VERSION = 1;

        /**
         * Table Name
         */
        private static final String TABLE_ROUTE = "tbl_route";


        /**
         * Table Route columns
         */
        private static final String ROUTE_ID_PK = "_id"; //Primary Key

        private static  final  String WALKING_LATLNG="walking_latlng";
        private static  final  String RUNNING_LATLNG="running_latlng";
        private static  final  String BIKE_LATLNG="bike_latlng";

        private static  final  String WALKING_DURATION="walking_duration";
        private static  final  String RUNNING_DURATION="running_duration";
        private static  final  String BIKE_DURATION="bike_duration";

        private static final String ROUTE_NAME = "route_name";
        private static final String ROUTE_DURATION = "route_time";
        private static final String ROUTE_STARTING_POINT = "route_starting_point";
        private static final String ROUTE_ENDING_POINT = "route_ending_point";
        private static final String ROUTE_DISTANCE = "route_distance";
        private static final String ROUTE_EXERCISE = "route_activity";
        private static final String ROUTE_EXERCISE_DURATION = "route_activity_time";


        /**
         * String to create route table
         */
        private final String CREATE_TABLE_ROUTE = "CREATE TABLE IF NOT EXISTS "
                + TABLE_ROUTE + " ( "
                + ROUTE_ID_PK + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ROUTE_NAME + " TEXT , "
                + WALKING_LATLNG + " TEXT , "
                + RUNNING_LATLNG + " TEXT , "
                + BIKE_LATLNG + " TEXT , "
                + ROUTE_DURATION + " TEXT , "
                + WALKING_DURATION + " TEXT , "
                + RUNNING_DURATION + " TEXT , "
                + BIKE_DURATION + " TEXT , "
                + ROUTE_EXERCISE + " TEXT , "
                + ROUTE_EXERCISE_DURATION + " TEXT , "
                + ROUTE_STARTING_POINT + " TEXT , "
                + ROUTE_ENDING_POINT + " TEXT , "
                + ROUTE_DISTANCE + " TEXT );";


        public DBHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);

        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE_ROUTE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
            db.execSQL("DROP TABLE IF EXISTS " + CREATE_TABLE_ROUTE);
            onCreate(db);
        }

    }


    /**
     * Table Route handler
     */
    public static class Route {


        /**
         * Check if specific route record is available or not.
         *
         * @param context
         * @param routeId
         * @return
         */

        /**
         * Insert values in route table
         *
         * @param context
         * @param routeModel
         * @return
         */
        public static boolean insert(Context context, RouteModel routeModel) {

            DBHelper dbHelper = new DBHelper(context);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DBHelper.ROUTE_NAME, routeModel.getRouteName());
            values.put(DBHelper.ROUTE_DURATION, routeModel.getRouteTimeDuration());
            values.put(DBHelper.WALKING_DURATION, routeModel.getWalkingDuration());
            values.put(DBHelper.RUNNING_DURATION, routeModel.getRunDuration());
            values.put(DBHelper.BIKE_DURATION, routeModel.getBikeDuration());
            values.put(DBHelper.WALKING_LATLNG, routeModel.getWalkingLatLng());
            values.put(DBHelper.RUNNING_LATLNG, routeModel.getRunLatLng());
            values.put(DBHelper.BIKE_LATLNG, routeModel.getBikeLatLng());
            values.put(DBHelper.ROUTE_DISTANCE, routeModel.getRouteDistance());
            values.put(DBHelper.ROUTE_EXERCISE, routeModel.getRouteExercise());
            values.put(DBHelper.ROUTE_EXERCISE_DURATION, routeModel.getRouteExerciseDuration());
            values.put(DBHelper.ROUTE_STARTING_POINT, routeModel.getRouteStartingPoint());
            values.put(DBHelper.ROUTE_ENDING_POINT, routeModel.getRouteEndingPoint());

            try {
                db.insert(DBHelper.TABLE_ROUTE, null, values);
                db.close();

                return true;
            } catch (SQLiteException e) {
                db.close();

                return false;
            }
        }

        /**
         * Update record in route table
         *
         * @param context
         * @param routeModel
         * @param routeId
         * @return
         */
        public static boolean update(Context context, RouteModel routeModel, String routeId) {


            DBHelper dbHelper = new DBHelper(context);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DBHelper.ROUTE_NAME, routeModel.getRouteName());
            values.put(DBHelper.ROUTE_DURATION, routeModel.getRouteTimeDuration());
            values.put(DBHelper.ROUTE_DISTANCE, routeModel.getRouteDistance());
            values.put(DBHelper.ROUTE_EXERCISE, routeModel.getRouteExercise());
            values.put(DBHelper.ROUTE_EXERCISE_DURATION, routeModel.getRouteExerciseDuration());
            values.put(DBHelper.ROUTE_STARTING_POINT, routeModel.getRouteStartingPoint());
            values.put(DBHelper.ROUTE_ENDING_POINT, routeModel.getRouteEndingPoint());

            try {

                String[] args = new String[]{routeId};
                db.update(dbHelper.TABLE_ROUTE, values, dbHelper.ROUTE_ID_PK + "=?", args);
                db.close();

                return true;
            } catch (SQLiteException e) {
                db.close();
                return false;
            }
        }


        /**
         * Get Route List
         *
         * @param context
         * @return
         */
        public static ArrayList<RouteModel> getRouteList(Context context) {

            DBHelper dbHelper = new DBHelper(context);
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            ArrayList<RouteModel> routeList = new ArrayList<>();

            try {
                String query = "SELECT * FROM " + dbHelper.TABLE_ROUTE + ";";


                Cursor cursor = db.rawQuery(query, null);

                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {

                    /**
                     * Getting values from table
                     */
                    String routeId = Integer.toString(cursor.getInt(cursor.getColumnIndex(DBHelper.ROUTE_ID_PK)));
                    String routeName = "" + cursor.getString(cursor.getColumnIndex(DBHelper.ROUTE_NAME));
                    String routeDistance = "" + cursor.getString(cursor.getColumnIndex(DBHelper.ROUTE_DISTANCE));
                    String routeTime = "" + cursor.getString(cursor.getColumnIndex(DBHelper.ROUTE_DURATION));
                    String routeExercise = "" + cursor.getString(cursor.getColumnIndex(DBHelper.ROUTE_EXERCISE));
                    String routeExerciseDuration = "" + cursor.getString(cursor.getColumnIndex(DBHelper.ROUTE_EXERCISE_DURATION));
                    String routeStartingPoint = "" + cursor.getString(cursor.getColumnIndex(DBHelper.ROUTE_STARTING_POINT));
                    String routeEndingPoint = "" + cursor.getString(cursor.getColumnIndex(DBHelper.ROUTE_ENDING_POINT));

                    /**
                     * Adding values in list
                     */
                    routeList.add(new RouteModel.RouteBuilder().
                            routeId(routeId)
                            .routeName(routeName)
                            .routeDistance(routeDistance)
                            .routeTime(routeTime)
                            .routeExercise(routeExercise)
                            .routeExerciseDuration(routeExerciseDuration)
                            .routeStartingPoint(routeStartingPoint)
                            .routeEndingPoint(routeEndingPoint)
                            .build());
                }

                db.close();

            } catch (SQLiteException e) {
                db.close();

            }


            return routeList;
        }

        /**
         * Delete route record by route id
         *
         * @param context
         */
        public static void deleteRouteRecordById(Context context, String routeId) {
            DBHelper dbHelper = new DBHelper(context);
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            try {

                String[] args = new String[]{routeId};
                db.delete(dbHelper.TABLE_ROUTE, DBHelper.ROUTE_ID_PK + "=?", args);

            } catch (SQLiteException e) {
                e.printStackTrace();
            }


        }


        /**
         * Check if routes are available or not
         *
         * @param context
         * @return
         */
        public static boolean isRecordsAvailable(Context context) {

            DBHelper dbHelper = new DBHelper(context);
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            boolean exists = false;

            String query = "SELECT * FROM " + dbHelper.TABLE_ROUTE;


            try {
                Cursor cursor = db.rawQuery(query, null);

                exists = (cursor.getCount() > 0);
                cursor.close();

            } catch (SQLiteException e) {

                e.printStackTrace();
                db.close();

            }

            return exists;
        }

        public static boolean isRecordAvailable(Context context, String routeId) {

            DBHelper dbHelper = new DBHelper(context);
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            boolean exists = false;

            String query = "SELECT * FROM " + dbHelper.TABLE_ROUTE + " WHERE "
                    + dbHelper.ROUTE_ID_PK + " = '" + String.valueOf(routeId) + "'";


            try {
                Cursor cursor = db.rawQuery(query, null);

                exists = (cursor.getCount() > 0);
                cursor.close();

            } catch (SQLiteException e) {

                e.printStackTrace();
                db.close();

            }

            return exists;
        }

        /**
         * Get Route record by Id
         *
         * @param context
         * @param routeId
         * @return
         */
        public static RouteModel getRecordById(Context context, String routeId) {

            RouteModel routeModel = null;

            DBHelper dbHelper = new DBHelper(context);
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            String query = "SELECT * FROM " + dbHelper.TABLE_ROUTE + " WHERE "
                    + dbHelper.ROUTE_ID_PK + " = '" + String.valueOf(routeId) + "'";


            try {
                Cursor cursor = db.rawQuery(query, null);
                cursor.moveToFirst();

                /**
                 * Getting values from table
                 */
                String routeName = "" + cursor.getString(cursor.getColumnIndex(DBHelper.ROUTE_NAME));
                String routeDistance = "" + cursor.getString(cursor.getColumnIndex(DBHelper.ROUTE_DISTANCE));
                String routeTime = "" + cursor.getString(cursor.getColumnIndex(DBHelper.ROUTE_DURATION));
                String routeExercise = "" + cursor.getString(cursor.getColumnIndex(DBHelper.ROUTE_EXERCISE));
                String routeExerciseDuration = "" + cursor.getString(cursor.getColumnIndex(DBHelper.ROUTE_EXERCISE_DURATION));
                String routeStartingPoint = "" + cursor.getString(cursor.getColumnIndex(DBHelper.ROUTE_STARTING_POINT));
                String routeEndingPoint = "" + cursor.getString(cursor.getColumnIndex(DBHelper.ROUTE_ENDING_POINT));

                /**
                 * Add values in model
                 */
                routeModel = new RouteModel.RouteBuilder().
                        routeId(routeId)
                        .routeName(routeName)
                        .routeDistance(routeDistance)
                        .routeTime(routeTime)
                        .routeExercise(routeExercise)
                        .routeExerciseDuration(routeExerciseDuration)
                        .routeStartingPoint(routeStartingPoint)
                        .routeEndingPoint(routeEndingPoint)
                        .build();

                cursor.close();

            } catch (SQLiteException e) {
                e.printStackTrace();
                db.close();
            }

            return routeModel;
        }


    }

    /**
     * Delete database tables
     *
     * @param context
     */
    public static void deleteAllTableRecords(Context context) {
        DBHelper dbHelper = new DBHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try {
            db.execSQL("DELETE FROM " + dbHelper.CREATE_TABLE_ROUTE);
        } catch (SQLiteException e) {
            e.printStackTrace();
        }

    }


}
