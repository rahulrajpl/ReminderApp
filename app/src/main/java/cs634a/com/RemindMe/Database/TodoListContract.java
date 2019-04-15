package cs634a.com.RemindMe.Database;

import android.provider.BaseColumns;


public final class TodoListContract {
    private TodoListContract() {}

    public static class TodoListEntries implements BaseColumns {
        public static final String TABLE_NAME = "TodolistEntries";
        public static final String COLUMN_NAME_CONTENT = "content";
        public static final String COLUMN_NAME_ADDRESS = "address";
        public static final String COLUMN_NAME_DONE = "done";
        public static final String COLUMN_NAME_REMINDERDATE = "reminderdate";
    }
}
