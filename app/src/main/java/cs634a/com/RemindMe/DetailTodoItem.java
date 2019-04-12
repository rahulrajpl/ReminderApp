package cs634a.com.RemindMe;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import cs634a.com.RemindMe.Utils.MyDateTimeUtils;
import cs634a.com.RemindMe.Utils.UpdateDatabase;
import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailTodoItem extends AppCompatActivity {

    @BindView(R.id.todoInfo) TextView todoInfo;
    @BindView(R.id.ivAddress) ImageView ivAddress;
    @BindView(R.id.reminderInfo) TextView reminderInfo;
    @BindView(R.id.editTodoBtn) FloatingActionButton editTodo;
    @BindView(R.id.deleteTodoBtn) FloatingActionButton deleteTodo;

    String content;
    String address;
    String reminder;
    Boolean hasReminder;
    Boolean done;
    UpdateDatabase updateDatabase; // util to do update stuffs in db
    MyDateTimeUtils dateTimeUtils; // util to do stuffs with notification

    private long oldRowId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_todo_item);
        initComponents();
        getDataFromIntent();
        assignComponents();

        ivAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(DetailTodoItem.this, "Clicked", Toast.LENGTH_SHORT).show();
                if(address.toString().isEmpty()){
                    Toast.makeText(DetailTodoItem.this, "No Address Entry for this reminder", Toast.LENGTH_SHORT).show();
                }
                else{
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + address));
                    startActivity(intent);
                }
            }
        });
    }

    private void initComponents() {
        ButterKnife.bind(this);
        getSupportActionBar().setTitle(getString(R.string.detail));
        updateDatabase = new UpdateDatabase();
        dateTimeUtils = new MyDateTimeUtils();
    }

    private void getDataFromIntent() {
        // because we go from main activity to here
        Intent intent = getIntent();
        content = intent.getStringExtra("content");
        address = intent.getStringExtra("address");
        reminder = intent.getStringExtra("reminder");
        hasReminder = intent.getExtras().getBoolean("hasReminder");
        done = intent.getExtras().getBoolean("done");
    }


    private void assignComponents() {
        // update UI with the content taken from intent
        todoInfo.setText(content);
        if (hasReminder)
            reminderInfo.setText(reminder);
        else reminderInfo.setText(getString(R.string.not_found));
        // if edit button is press fire add activity with a little tweak
        editTodo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailTodoItem.this, AddTodoItem.class);
                intent.putExtra("content", content);
                intent.putExtra("address", address);
                intent.putExtra("reminder", reminder);
                intent.putExtra("hasReminder", hasReminder);
                intent.putExtra("done", done);
                finish();
                startActivity(intent);
            }
        });

        // if delete is pressed then delete item in databse and alro remove object for notifydatsetchanged
        deleteTodo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(DetailTodoItem.this, getString(R.string.item_deleted), Toast.LENGTH_SHORT).show();
                ToDoItem toDoItem = new ToDoItem(content,address, done, reminder, hasReminder);
                PageFragment.toDoItems.remove(toDoItem);
                // remove in database
                oldRowId = updateDatabase.removeInDatabase(content, reminder, DetailTodoItem.this);
                // remove existing scheduled notification if existed
                if (!reminder.equals(" "))
                    dateTimeUtils.cancelScheduledNotification(dateTimeUtils.getNotification(content,DetailTodoItem.this),
                            DetailTodoItem.this, (int) oldRowId);
                finish();
            }
        });
    }
}
