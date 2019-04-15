package cs634a.com.RemindMe;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import cs634a.com.RemindMe.Adapters.TodoListAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;


public class PageFragment extends Fragment {

    @BindView(R.id.todoList) ListView todoList;

    TodoListAdapter fragmentPagerAdapter;
    public static ArrayList<ToDoItem> toDoItems;

    private static final String ARG_PAGE = "ARG_PAGE";

    public static PageFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        PageFragment fragment = new PageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_page, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fragmentPagerAdapter = new TodoListAdapter(getActivity().getApplicationContext(), R.layout.todo_item, toDoItems);
        todoList.setAdapter(fragmentPagerAdapter);
        todoList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            // open the detail of each item if clicked
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getContext(), DetailTodoItem.class);
                ToDoItem item = toDoItems.get(position);
                intent.putExtra("content", item.getContent());
                intent.putExtra("address", item.getAddress());
                intent.putExtra("reminder", item.getReminderDate());
                intent.putExtra("hasReminder", item.getHasReminder());
                intent.putExtra("done", item.getDone());
                startActivity(intent);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        fragmentPagerAdapter.notifyDataSetChanged();
    }
}
