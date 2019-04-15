package cs634a.com.RemindMe;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.astuetz.PagerSlidingTabStrip;

import java.util.ArrayList;

import cs634a.com.RemindMe.Adapters.MyFragmentPagerAdapter;
import cs634a.com.RemindMe.Database.TodoListContract;
import butterknife.BindView;
import butterknife.ButterKnife;
import cs634a.com.RemindMe.Database.TodoListDbHelper;
import cs634a.com.RemindMe.Utils.UpdateDatabase;

import static cs634a.com.RemindMe.PageFragment.toDoItems;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.view_pager) ViewPager pager;
    @BindView(R.id.tabs) PagerSlidingTabStrip tabStrip;
    @BindView(R.id.descriptImage) ImageSwitcher descriptImage;
    @BindView(R.id.actionButton) FloatingActionButton actionButton;

    UpdateDatabase updateDatabase;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        openAndQueryDb(0);
        initializeComponents();
    }

    private void initializeComponents() {
        updateDatabase = new UpdateDatabase();
        ButterKnife.bind(this);
        pager.setAdapter(new MyFragmentPagerAdapter(getSupportFragmentManager()));
        tabStrip.setViewPager(pager);
        tabStrip.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                changeColor(position);
                openAndQueryDb(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), AddTodoItem.class);
                startActivity(intent);
            }
        });

        descriptImage.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                ImageView imgview = new ImageView(getApplicationContext());
                imgview.setScaleType(ImageView.ScaleType.CENTER_CROP);
                return imgview;
            }
        });
        Animation in = AnimationUtils.loadAnimation(this,android.R.anim.slide_in_left);
        Animation out = AnimationUtils.loadAnimation(this,android.R.anim.slide_out_right);
        descriptImage.setInAnimation(in);
        descriptImage.setOutAnimation(out);
    }

    private void openAndQueryDb(final int mPage) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                toDoItems = new ArrayList<>();
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                pager.getAdapter().notifyDataSetChanged();
            }

            @Override
            protected Void doInBackground(Void... params) {
                TodoListDbHelper mDbHelper = new TodoListDbHelper(MainActivity.this);
                SQLiteDatabase db = mDbHelper.getReadableDatabase();
                Cursor cursor;

                switch(mPage)
                {
                    case 0: //All Reminders. No Filter
                        cursor = db.rawQuery("Select " + TodoListContract.TodoListEntries.COLUMN_NAME_CONTENT + ", "
                                        + TodoListContract.TodoListEntries.COLUMN_NAME_ADDRESS + ", "
                                        + TodoListContract.TodoListEntries.COLUMN_NAME_DONE + ", "
                                        + TodoListContract.TodoListEntries.COLUMN_NAME_REMINDERDATE
                                        + " FROM "
                                        + TodoListContract.TodoListEntries.TABLE_NAME
                                , null);
                        break;
                    case 1: // For Today's Filter
                        cursor = db.rawQuery(
                                "Select "
                                + TodoListContract.TodoListEntries.COLUMN_NAME_CONTENT + ", "
                                + TodoListContract.TodoListEntries.COLUMN_NAME_ADDRESS + ","
                                + TodoListContract.TodoListEntries.COLUMN_NAME_DONE + ", "
                                + TodoListContract.TodoListEntries.COLUMN_NAME_REMINDERDATE
                                + " FROM "
                                + TodoListContract.TodoListEntries.TABLE_NAME
                                + " WHERE "
                                + TodoListContract.TodoListEntries.COLUMN_NAME_REMINDERDATE
                                + " BETWEEN "
                                + " date('now') AND date('now', '+1 day') "
                                , null);
                        break;
                    case 2: // For Seven Days filter
                        cursor = db.rawQuery(
                                "Select "
                                + TodoListContract.TodoListEntries.COLUMN_NAME_CONTENT + ", "
                                + TodoListContract.TodoListEntries.COLUMN_NAME_ADDRESS + ","
                                + TodoListContract.TodoListEntries.COLUMN_NAME_DONE + ", "
                                + TodoListContract.TodoListEntries.COLUMN_NAME_REMINDERDATE
                                + " FROM "
                                + TodoListContract.TodoListEntries.TABLE_NAME
                                + " WHERE "
                                + TodoListContract.TodoListEntries.COLUMN_NAME_REMINDERDATE
                                + " BETWEEN "
                                + " date('now') AND date('now', '+7 day') "
                                , null);
                        break;
                    default:
                        cursor = null;
                }

                if(cursor.moveToFirst())
                {
                    do {
                        String content = cursor.getString(cursor.getColumnIndex(
                                TodoListContract.TodoListEntries.COLUMN_NAME_CONTENT
                        ));
                        String address = cursor.getString(cursor.getColumnIndex(
                                TodoListContract.TodoListEntries.COLUMN_NAME_ADDRESS
                        ));
                        int doneInt = cursor.getInt(cursor.getColumnIndex(
                                TodoListContract.TodoListEntries.COLUMN_NAME_DONE
                        ));
                        String reminderDate = cursor.getString(cursor.getColumnIndex(
                                TodoListContract.TodoListEntries.COLUMN_NAME_REMINDERDATE
                        ));
                        Boolean done = (doneInt == 1);
                        if (content == null && reminderDate == null) break;
                        if (reminderDate == null)
                            toDoItems.add(new ToDoItem(content, address, done, " ", false));
                        else toDoItems.add(new ToDoItem(content, address, done, reminderDate, true));
                    } while (cursor.moveToNext());
                }
                cursor.close();
                return null;
            }
        }.execute();
    }

    private void changeColor(int position) {
        switch (position) {
            case 0:
                applyNewColor("#303f9f", "#757de8", "#3f51b5");
                break;
            case 1:
                applyNewColor("#303f9f", "#757de8", "#3f51b5");
                break;
            case 2:
                applyNewColor("#303f9f", "#757de8", "#3f51b5");
                break;
            default:
                break;
        }
    }

    private void applyNewColor (String actionBarColor, String tabStripColor, String indicatorColor) {
        ActionBar actionBar = getSupportActionBar();
        Window window = this.getWindow();

        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(actionBarColor)));
        window.setStatusBarColor(Color.parseColor(indicatorColor));
        tabStrip.setBackground(new ColorDrawable((Color.parseColor(tabStripColor))));
        tabStrip.setIndicatorColor(Color.parseColor(indicatorColor));
        actionButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(actionBarColor)));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.clean_all_done:
                updateDatabase.removeAllDoneItem(MainActivity.this);
                return true;
            case R.id.AppInfo:
                String info = "App created by:-\n\nLt Cdr Rahul Raj\nLt Cdr Karan Basson\n\n" +
                        "Course: CS634A\nInstructor: Prof. (Dr.) R K Gosh\nIIT Kanpur";
                Toast.makeText(this, info, Toast.LENGTH_LONG).show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
