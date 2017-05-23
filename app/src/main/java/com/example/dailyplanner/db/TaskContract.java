package com.example.dailyplanner.db;

/**
 * Created by Jared Cole on 3/5/2017.
 */
import android.provider.BaseColumns;

//Basic info for Database
public class TaskContract {
    public static final String DB_NAME = "com.example.dailyplanner.db";
    public static final int DB_VERSION = 1;

    public class TaskEntry implements BaseColumns {
        public static final String TABLE = "tasks";

        public static final String COL_TASK_TITLE = "title";
    }
}
