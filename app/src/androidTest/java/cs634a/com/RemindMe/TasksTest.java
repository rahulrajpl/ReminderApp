package cs634a.com.RemindMe;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import cs634a.com.RemindMe.Pages.AddTask;
import cs634a.com.RemindMe.Pages.Home;
import cs634a.com.RemindMe.Utils.CustomAssertions;

/**
 * Created by Daniel Blokus on 30.09.2017.
 */

@RunWith(AndroidJUnit4.class)
public class TasksTest {
    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void createDefaultTask_ShouldDisplayHomeView() {
        new Home().navigateToAddTask().addTask();
        CustomAssertions.shouldDisplayNewTaskInTheList(AddTask.TASK_NAME);
    }
}
