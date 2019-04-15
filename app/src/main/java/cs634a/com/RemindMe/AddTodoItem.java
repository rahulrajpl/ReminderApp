package cs634a.com.RemindMe;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.rengwuxian.materialedittext.validation.RegexpValidator;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.Calendar;

import cs634a.com.RemindMe.Utils.MyDateTimeUtils;
import cs634a.com.RemindMe.Utils.UpdateDatabase;
import butterknife.BindView;
import butterknife.ButterKnife;

public class AddTodoItem extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    // Bind components
    @BindView(R.id.todoEditText) MaterialEditText materialTextInput;
    @BindView(R.id.metAddress) MaterialEditText metAddress;
    @BindView(R.id.buttonSetDate) Button buttonSetDate;
    @BindView(R.id.buttonSetTime) Button buttonSetTime;
    @BindView(R.id.reminderSwitch) Switch reminderSwitch;
    @BindView(R.id.reminderText) TextView reminderText;
    @BindView(R.id.addTodoBtn) FloatingActionButton addTodoBtn;


    ToDoItem toDoItem;
    MyDateTimeUtils dateTimeUtils;
    UpdateDatabase updateDatabase;

    String content;
    String address;
    String date;
    String time;

    String oldContent = "";
    String oldAddress = "";
    String oldReminder = "";
    Boolean oldHasReminder;
    Boolean oldDone;
    Boolean existingData;

    private long newRowId;

    private long oldRowId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_todo_item);
        initializeComponents();
        existingData = loadDataIfExist();
    }

    private void initializeComponents() {
        getSupportActionBar().setTitle(R.string.add_todo_item);
        ButterKnife.bind(this);
        updateDatabase = new UpdateDatabase();
        dateTimeUtils = new MyDateTimeUtils();
        date ="";
        time ="";

        reminderSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    buttonSetDate.setVisibility(View.GONE);
                    buttonSetTime.setVisibility(View.GONE);
                    reminderText.setVisibility(View.GONE);
                    reminderText.setText(getString(R.string.reminder_set_at));
                    date = ""; // reset date time field
                    time = "";
                }
                else {
                    buttonSetDate.setVisibility(View.VISIBLE);
                    buttonSetTime.setVisibility(View.VISIBLE);
                    reminderText.setVisibility(View.VISIBLE);
                }
            }
        });

        buttonSetDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar now = Calendar.getInstance();
                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        AddTodoItem.this,
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                );
                dpd.vibrate(true);
                dpd.dismissOnPause(true);
                dpd.show(getFragmentManager(), "DatepickerDialog");
            }
        });

        buttonSetTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                date = dateTimeUtils.fillDateIfEmpty(date);
                Calendar now = Calendar.getInstance();
                TimePickerDialog tpd = TimePickerDialog.newInstance(
                        AddTodoItem.this,
                        now.get(Calendar.HOUR_OF_DAY),
                        now.get(Calendar.MINUTE),
                        true
                );
                tpd.vibrate(true);
                tpd.dismissOnPause(true);
                tpd.show(getFragmentManager(), "TimepickerDialog" );
            }
        });

        addTodoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validateAllInput())
                    return;
                content = materialTextInput.getText().toString();


                addItemToDatabase();
                if (existingData) {
                    UpdateDatabase updateDatabaseInstance = new UpdateDatabase();
                    oldRowId = updateDatabaseInstance.removeInDatabase(oldContent,oldAddress, oldReminder, AddTodoItem.this);
                    toDoItem = new ToDoItem(oldContent, oldAddress, oldDone, oldReminder, oldHasReminder);
                    PageFragment.toDoItems.remove(toDoItem);
                    dateTimeUtils.cancelScheduledNotification(dateTimeUtils.getNotification(oldContent, AddTodoItem.this),
                            AddTodoItem.this, (int)oldRowId);
                }
                if (!(date+" "+time).equals(" "))
                    dateTimeUtils.ScheduleNotification(dateTimeUtils.getNotification(content, AddTodoItem.this),
                        AddTodoItem.this, (int)newRowId, date + " " + time);
                finish();
            }
        });
    }

    private Boolean validateAllInput() {
        if (time.equals("") && !date.equals("")) {
            AlertDialog alertDialog = new AlertDialog.Builder(AddTodoItem.this).create();
            alertDialog.setTitle(getString(R.string.time_error));
            alertDialog.setMessage(getString(R.string.time_error_purpose));
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "CHOOSE DEFAULT 9 A.M.", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    onTimeSet(null, 9, 0, 0); // default picked time at 9:00 A.M
                }
            });
            alertDialog.show();
            return false;
        }
        if (!materialTextInput.validateWith(
                new RegexpValidator("String must not be empty", "^(?!\\s*$).+"))) {
            Toast.makeText(AddTodoItem.this, "Empty task detected! No task added!", Toast.LENGTH_SHORT).show();
            return false;
        }
        if ((oldContent.equals(content) || content == null) && oldReminder.equals(date + " " + time)){
            finish();
        }
        return true;
    }

    private Boolean loadDataIfExist() {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras == null) return false;
        oldContent = extras.getString("content");
        oldAddress = extras.getString("address");
        oldReminder = extras.getString("reminder");
        oldHasReminder = extras.getBoolean("hasReminder");
        oldDone = extras.getBoolean("done");

        materialTextInput.setText(oldContent);
        metAddress.setText(oldAddress);
        if (oldReminder.equals(" "))
            return true;
        date = oldReminder.split("\\s+")[0];
        time = oldReminder.split("\\s+")[1];
        reminderText.setText(getString(R.string.reminder_set_at) + " " + date + " " + time);
        reminderSwitch.setChecked(true);
        buttonSetDate.setVisibility(View.VISIBLE);
        buttonSetTime.setVisibility(View.VISIBLE);
        reminderText.setVisibility(View.VISIBLE);
        return true;
    }

    private void addItemToDatabase() {
        String reminderDate = date + " " + time;
        address = metAddress.getText().toString().trim();
        if (reminderDate.equals(" ")) // no reminder
            toDoItem = new ToDoItem(content, address,false, reminderDate, false);
        else  //  with reminder
            toDoItem = new ToDoItem(content, address,false, reminderDate, true);
        PageFragment.toDoItems.add(toDoItem);
        newRowId = updateDatabase.addItemToDatabase(content,address, false, reminderDate, AddTodoItem.this);
    }


    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        if (dateTimeUtils.checkInvalidDate(year, monthOfYear, dayOfMonth)){
            AlertDialog alertDialog = new AlertDialog.Builder(AddTodoItem.this).create();
            alertDialog.setTitle("Date not valid!");
            alertDialog.setIcon(R.drawable.ic_warning_black_24dp);
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alertDialog.show();
            return;
        }

        date = dateTimeUtils.dateToString(year, monthOfYear, dayOfMonth);
        reminderText.setText(getString(R.string.reminder_set_at) + " " + date + " " + time);
    }

    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
        if (date.equals(dateTimeUtils.fillDateIfEmpty("")) && dateTimeUtils.checkInvalidTime(hourOfDay, minute)) {
            AlertDialog alertDialog = new AlertDialog.Builder(AddTodoItem.this).create();
            alertDialog.setTitle("Time not valid!");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alertDialog.show();
            return;
        }

        time = dateTimeUtils.timeToString(hourOfDay, minute);
        reminderText.setText(getString(R.string.reminder_set_at) + " " + date + " " + time);
    }
}
