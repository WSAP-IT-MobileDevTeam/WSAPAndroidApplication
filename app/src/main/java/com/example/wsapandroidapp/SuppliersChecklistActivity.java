package com.example.wsapandroidapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.wsapandroidapp.Adapters.SupplierChecklistAdapter;
import com.example.wsapandroidapp.Classes.ComponentManager;
import com.example.wsapandroidapp.Classes.Enums;
import com.example.wsapandroidapp.DataModel.Application;
import com.example.wsapandroidapp.DataModel.SupplierChecklist;
import com.example.wsapandroidapp.DataModel.SupplierChecklistCategory;
import com.example.wsapandroidapp.DataModel.UserSupplierChecklist;
import com.example.wsapandroidapp.DataModel.UserSupplierChecklistCategory;
import com.example.wsapandroidapp.DataModel.UserSupplierChecklistCategoryList;
import com.example.wsapandroidapp.DialogClasses.AppStatusPromptDialog;
import com.example.wsapandroidapp.DialogClasses.LoadingDialog;
import com.example.wsapandroidapp.DialogClasses.MessageDialog;
import com.example.wsapandroidapp.DialogClasses.NewVersionPromptDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SuppliersChecklistActivity extends AppCompatActivity {

    EditText etSearch;
    TextView tvMessage;
    RecyclerView recyclerView;

    Context context;

    LoadingDialog loadingDialog;
    MessageDialog messageDialog;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;

    Query supplierChecklistQuery, userSupplierChecklistQuery;

    boolean isListening;
    boolean isUpdateMode;

    List<UserSupplierChecklistCategory> userSupplierChecklistCategories = new ArrayList<>(),
            userSupplierChecklistCategoriesCopy = new ArrayList<>();

    List list = new ArrayList();

    SupplierChecklistAdapter supplierChecklistAdapter;

    UserSupplierChecklistCategoryList userSupplierChecklistCategoryList;

    ComponentManager componentManager;

    String searchValue = "";

    AppStatusPromptDialog appStatusPromptDialog;
    NewVersionPromptDialog newVersionPromptDialog;
    Query applicationQuery;
    Application application = new Application();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suppliers_checklist);

        etSearch = findViewById(R.id.etSearch);
        tvMessage = findViewById(R.id.tvMessage);
        recyclerView = findViewById(R.id.recyclerView);

        context = SuppliersChecklistActivity.this;

        loadingDialog = new LoadingDialog(context);
        messageDialog = new MessageDialog(context);
        appStatusPromptDialog = new AppStatusPromptDialog(context);
        newVersionPromptDialog = new NewVersionPromptDialog(context);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        firebaseDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_RTDB_url));
        supplierChecklistQuery = firebaseDatabase.getReference("supplierChecklist");
        userSupplierChecklistQuery = firebaseDatabase.getReference("userSupplierChecklist").orderByChild("id").equalTo(firebaseUser.getUid());
        applicationQuery = firebaseDatabase.getReference("application");

        loadingDialog.showDialog();
        isListening = true;
        isUpdateMode = false;
        userSupplierChecklistQuery.addValueEventListener(getUserSupplierChecklistCategory());
        applicationQuery.addValueEventListener(getApplicationValue());

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        supplierChecklistAdapter = new SupplierChecklistAdapter(context, list);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(supplierChecklistAdapter);

        supplierChecklistAdapter.setAdapterListener(supplierChecklist -> {
            String categoryId = supplierChecklist.getId(), checklistId = supplierChecklist.getId();

            for (UserSupplierChecklistCategory supplierChecklistCategory : userSupplierChecklistCategories)
                for (UserSupplierChecklist userSupplierChecklist : supplierChecklistCategory.getChecklist().values())
                    if (userSupplierChecklist.getId().equals(checklistId)) {
                        categoryId = supplierChecklistCategory.getId();
                        break;
                    }

            updateSupplierChecklist(categoryId, checklistId, supplierChecklist);
        });

        componentManager = new ComponentManager(context);
        componentManager.setInputRightDrawable(etSearch, true, Enums.VOICE_RECOGNITION);
        componentManager.setVoiceRecognitionListener(() -> startActivityForResult(componentManager.voiceRecognitionIntent(), Enums.VOICE_RECOGNITION_REQUEST_CODE));

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                searchValue = editable != null ? editable.toString() : "";

                filterTasks();
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void filterTasks() {
        List<UserSupplierChecklistCategory> userSupplierChecklistCategoriesTemp =
                new ArrayList<>(userSupplierChecklistCategoriesCopy);

        userSupplierChecklistCategories.clear();

        for (int i = 0; i < userSupplierChecklistCategoriesTemp.size(); i++) {
            UserSupplierChecklistCategory userSupplierChecklistCategory = userSupplierChecklistCategoriesTemp.get(i);

            if (isSearched(userSupplierChecklistCategory)) userSupplierChecklistCategories.add(userSupplierChecklistCategory);
        }

        list.clear();

        for (UserSupplierChecklistCategory userSupplierChecklistCategory : userSupplierChecklistCategories) {
            list.add(userSupplierChecklistCategory);

            List<UserSupplierChecklist> userSupplierChecklist =
                    new ArrayList<>(userSupplierChecklistCategory.getChecklist().values());

            userSupplierChecklist.sort(Comparator.comparingInt(UserSupplierChecklist::getOrder));

            for (UserSupplierChecklist supplierChecklist : userSupplierChecklist)
                if (supplierChecklist.getTask().toLowerCase().contains(searchValue.toLowerCase()) ||
                        userSupplierChecklistCategory.getCategory().toLowerCase().contains(searchValue.toLowerCase()))
                    list.add(supplierChecklist);
        }

        if (userSupplierChecklistCategories.size() == 0) {
            tvMessage.setVisibility(View.VISIBLE);

            String noRecordsError = getString(R.string.no_record, "Record");

            tvMessage.setText(noRecordsError);
        } else tvMessage.setVisibility(View.GONE);
        tvMessage.bringToFront();

        etSearch.setVisibility(View.VISIBLE);

        supplierChecklistAdapter.notifyDataSetChanged();

        loadingDialog.dismissDialog();
    }

    private boolean isSearched(UserSupplierChecklistCategory userSupplierChecklistCategory) {
        boolean isSearched = userSupplierChecklistCategory.getCategory().toLowerCase().contains(searchValue.toLowerCase());

        for (UserSupplierChecklist userSupplierChecklist : userSupplierChecklistCategory.getChecklist().values())
            if (userSupplierChecklist.getTask().toLowerCase().contains(searchValue.toLowerCase())) {
                isSearched = true;
                break;
            }

        return searchValue.trim().length() == 0 || isSearched;
    }

    private ValueEventListener getUserSupplierChecklistCategory() {
        return new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    boolean isRead = false;
                    userSupplierChecklistCategories.clear();

                    if (snapshot.exists())
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            userSupplierChecklistCategoryList = dataSnapshot.getValue(UserSupplierChecklistCategoryList.class);

                            if (userSupplierChecklistCategoryList != null) {
                                userSupplierChecklistCategories.addAll(userSupplierChecklistCategoryList.getSupplierChecklistCategories().values());
                                isRead = true;
                                isUpdateMode = true;
                                break;
                            }
                        }
                    else {
                        loadingDialog.showDialog();
                        supplierChecklistQuery.addListenerForSingleValueEvent(getSupplierChecklistCategory());
                    }

                    if (isRead) {
                        userSupplierChecklistCategories.sort(Comparator.comparingInt(UserSupplierChecklistCategory::getOrder));

                        userSupplierChecklistCategoriesCopy = new ArrayList<>(userSupplierChecklistCategories);

                        updateUI();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "onCancelled", error.toException());
                loadingDialog.showDialog();

                messageDialog.setMessage(getString(R.string.fd_on_cancelled, "User Supplier Checklist"));
                messageDialog.setMessageType(Enums.ERROR_MESSAGE);
                messageDialog.showDialog();
            }
        };
    }

    private ValueEventListener getSupplierChecklistCategory() {
        return new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    List<SupplierChecklist> supplierChecklist = new ArrayList<>();
                    Map<String, UserSupplierChecklistCategory> userSupplierChecklistCategoryMap = new HashMap<>();

                    if (snapshot.exists())
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            SupplierChecklistCategory supplierChecklistCategory =
                                    dataSnapshot.getValue(SupplierChecklistCategory.class);

                            if (supplierChecklistCategory != null) {
                                supplierChecklist.clear();
                                supplierChecklist.addAll(supplierChecklistCategory.getChecklist().values());

                                Map<String, UserSupplierChecklist> userSupplierChecklistMap = new HashMap<>();

                                for (SupplierChecklist supplierChecklist1 : supplierChecklist)
                                    userSupplierChecklistMap.put(
                                            supplierChecklist1.getId(),
                                            new UserSupplierChecklist(
                                                    supplierChecklist1.getId(),
                                                    supplierChecklist1.getTask(),
                                                    supplierChecklist1.getOrder(),
                                                    "", "", "", 0
                                            )
                                    );

                                UserSupplierChecklistCategory userSupplierChecklistCategory =
                                        new UserSupplierChecklistCategory(
                                                supplierChecklistCategory.getId(),
                                                supplierChecklistCategory.getCategory(),
                                                supplierChecklistCategory.getOrder(),
                                                userSupplierChecklistMap
                                        );

                                userSupplierChecklistCategoryMap.put(userSupplierChecklistCategory.getId(), userSupplierChecklistCategory);
                            }
                        }

                    userSupplierChecklistCategoryList = new UserSupplierChecklistCategoryList(firebaseUser.getUid(), userSupplierChecklistCategoryMap);

                    userSupplierChecklistQuery.getRef().child(firebaseUser.getUid()).setValue(userSupplierChecklistCategoryList)
                            .addOnCompleteListener(task -> {
                                if (!task.isSuccessful() && task.getException() != null) {
                                    messageDialog.setMessageType(Enums.ERROR_MESSAGE);
                                    messageDialog.setMessage(task.getException().toString());
                                    messageDialog.showDialog();
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "onCancelled", error.toException());
                loadingDialog.showDialog();

                messageDialog.setMessage(getString(R.string.fd_on_cancelled, "Supplier Checklist"));
                messageDialog.setMessageType(Enums.ERROR_MESSAGE);
                messageDialog.showDialog();
            }
        };
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateUI() {
        filterTasks();
    }

    private void updateSupplierChecklist(String categoryId, String checklistId, UserSupplierChecklist supplierChecklist) {
        userSupplierChecklistQuery.getRef().child(firebaseUser.getUid())
                .child("supplierChecklistCategories").child(categoryId)
                .child("checklist").child(checklistId).setValue(supplierChecklist)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                        supplierChecklistAdapter.setChangesSaved();
                });
    }

    private ValueEventListener getApplicationValue() {
        return new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    appStatusPromptDialog.dismissDialog();
                    newVersionPromptDialog.dismissDialog();

                    if (snapshot.exists()) {
                        application = snapshot.getValue(Application.class);

                        if (application != null) {
                            if (!application.getStatus().equals(getString(R.string.live)) && !application.isForDeveloper()) {
                                appStatusPromptDialog.setTitle(application.getStatus());
                                appStatusPromptDialog.showDialog();
                            } else if (application.getCurrentVersion() < application.getLatestVersion()
                                    && !application.isForDeveloper()) {
                                newVersionPromptDialog.setVersion(application.getCurrentVersion(), application.getLatestVersion());
                                newVersionPromptDialog.showDialog();
                            }

                            appStatusPromptDialog.setDialogListener(() -> {
                                appStatusPromptDialog.dismissDialog();
                                finish();
                            });

                            newVersionPromptDialog.setDialogListener(new NewVersionPromptDialog.DialogListener() {
                                @Override
                                public void onUpdate() {
                                    Intent intent = new Intent("android.intent.action.VIEW",
                                            Uri.parse(application.getDownloadLink()));
                                    startActivity(intent);

                                    newVersionPromptDialog.dismissDialog();
                                    finish();
                                }

                                @Override
                                public void onCancel() {
                                    newVersionPromptDialog.dismissDialog();
                                    finish();
                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "onCancelled", error.toException());
            }
        };
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Enums.VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            assert data != null;
            etSearch.setText(data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0));
        }
    }

    @Override
    public void onResume() {
        isListening = true;
        userSupplierChecklistQuery.addListenerForSingleValueEvent(getUserSupplierChecklistCategory());
        applicationQuery.addListenerForSingleValueEvent(getApplicationValue());

        super.onResume();
    }

    @Override
    public void onStop() {
        isListening = false;

        super.onStop();
    }

    @Override
    public void onDestroy() {
        isListening = false;

        super.onDestroy();
    }
}