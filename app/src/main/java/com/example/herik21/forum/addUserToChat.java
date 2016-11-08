package com.example.herik21.forum;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class addUserToChat extends AppCompatActivity {

    public ArrayList<User> members;
    public ArrayList<String> klist;
    private ListView memberslist;
    private UserAdapter uAdapter;
    private AutoCompleteTextView userToAdd;
    private User ctuser;
    private Button btadd;
    private SuggestionAdapter filterAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user_to_chat);

        members = new ArrayList<>();
        memberslist = (ListView) findViewById(R.id.members);
        uAdapter = new UserAdapter(this,members);
        memberslist.setAdapter(uAdapter);
        btadd = (Button)findViewById(R.id.addbutton);
        btadd.setEnabled(false);

        userToAdd = (AutoCompleteTextView) findViewById(R.id.newUser);
        userToAdd.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String user = ((TextView)view.findViewById(R.id.autoCompleteItem)).getText().toString();
                ctuser = filterAdapter.filteredData.get(i);
                userToAdd.setText(user);
                userToAdd.setEnabled(false);
                btadd.setEnabled(true);
            }
        });
        userToAdd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(final CharSequence charSequence, int i, int i1, int i2) {
                String input = charSequence.toString().trim();
                if (input.length() > 0) {
                    String text = ((AutoCompleteTextView) findViewById(R.id.newUser)).getText().toString();
                    DatabaseReference userTable = FirebaseDatabase.getInstance().getReference().child("Users");
                    userTable.orderByChild("Nickname").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            ArrayList<User> list = new ArrayList<>();
                            for (DataSnapshot suggestionSnapshot : dataSnapshot.getChildren()){
                                //Get the suggestion by childing the key of the string you want to get.
                                User suggestion = suggestionSnapshot.getValue(User.class);
                                //Add the retrieved string to the list
                                if(suggestion.Nickname!=null && suggestion.Nickname.toLowerCase().contains(charSequence)){
                                    list.add(suggestion);
                                }
                            }
                            filterAdapter = new SuggestionAdapter(addUserToChat.this, list);
                            ((AutoCompleteTextView) findViewById(R.id.newUser)).setAdapter(filterAdapter);

                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(addUserToChat.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
            @Override
            public void afterTextChanged(Editable editable) { }
        });
        //textView.setAdapter(adapter);
        final List<String> sugestions = new ArrayList<>();
        ArrayAdapter<String> userAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sugestions);
        userAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        userToAdd.setAdapter(userAdapter);
    }

    public void onClickAdd(View view){
        members.add(ctuser);
        uAdapter.setData(members);
        memberslist.setAdapter(uAdapter);
        userToAdd.setText("");
        userToAdd.setEnabled(true);
        btadd.setEnabled(false);
    }


    public void onClickConfirm(View view) {
        String cthreadkey = getIntent().getStringExtra("key");
        DatabaseReference threadsTable = FirebaseDatabase.getInstance().getReference().child("Threads");
        Map<String, Object> tChildUpdates = new HashMap<>();
        for (User u : members){
            if(u.idUsuario!=null && u.key!=null){
                //register users in thread
                tChildUpdates.put(u.idUsuario,"true");
            }
            threadsTable.child(cthreadkey).child("users").updateChildren(tChildUpdates);
            finish();
        }
    }
}
