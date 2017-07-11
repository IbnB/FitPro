package lu.uni.ibrahimtahirou.fitpro.utils;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Toast;

public class Utils {

    public static class Msg {

        /**
         * Toast with Long Length
         *
         * @param context
         * @param message
         */
        public static void showToastLengthLong(Context context, String message) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }

        /**
         * Toast with Short Length
         *
         * @param context
         * @param message
         */
        public static void showToastLengthShort(Context context, String message) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }

        /**
         * SnackBar with Short Length
         *
         * @param view
         * @param message
         */
        public static void showSnackBarLengthShort(View view, String message) {
            Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
        }

        /**
         * SnackBar with Long Length
         *
         * @param view
         * @param message
         */
        public static void showSnackBarLengthLong(View view, String message) {
            Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
        }

        /**
         * SnackBar with Indefinite Length
         *
         * @param view
         * @param message
         */
        public static void showSnackBarLengthIndefinite(View view, String message) {
            Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE).show();
        }
    }


}
