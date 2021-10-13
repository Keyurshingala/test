package com.example.anew.readContact;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.anew.BaseActivity;
import com.example.anew.databinding.ActivityReadContactBinding;
import com.kaopiz.kprogresshud.KProgressHUD;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class ReadContactActivity extends BaseActivity {

    ActivityReadContactBinding binding;

    ContactAdapter contactAdapter;

    List<Contact> contactList = new ArrayList<>();

    KProgressHUD dialog;
    HashMap<String, String> contactMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        binding = ActivityReadContactBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String s = "abcd";
        char[] charArray = s.replaceAll(" ", "").toCharArray();
        for (char c : charArray) {
            System.out.println(c);
        }

        initui();
    }

    private void initui() {
        setToolBar();

        new YourAsyncTask(this).execute();

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence cs, int start, int before, int count) {
                List<Contact> tempList;
                if (cs != null && !cs.toString().trim().equals("")) {
                    tempList = new ArrayList<>();
                    for (int i = 0; i < contactList.size(); i++) {
                        
                        if (contactList.get(i).getName().toLowerCase().contains(String.valueOf(cs).toLowerCase())) {
                            tempList.add(contactList.get(i));
                        }
                    }
                    showContactList(tempList);
                } else {
                    showContactList();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private class YourAsyncTask extends AsyncTask<Void, Void, Void> {

        public YourAsyncTask(ReadContactActivity activity) {
            setProgress();
        }

        @Override
        protected void onPreExecute() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dialog.show();
                }
            });

        }

        @Override
        protected Void doInBackground(Void... args) {
            getContactList();

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (dialog != null && dialog.isShowing()) dialog.dismiss();
                    showContactList();
                }
            });

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dialog != null && dialog.isShowing()) dialog.dismiss();
    }

    private void showContactList() {
        if (contactList.size() == 0) {
            binding.tvEmpty.setVisibility(View.VISIBLE);
        } else {
            binding.tvEmpty.setVisibility(View.GONE);
        }

        binding.tvContactNum.setText(contactList.size() + " contacts");
        contactAdapter = new ContactAdapter(this, contactList);
        binding.rvContact.setLayoutManager(new LinearLayoutManager(this));
        binding.rvContact.setAdapter(contactAdapter);

        contactAdapter.setOnContactClickListener((contact, pos) -> {

        });
    }

    //For Filter
    private void showContactList(List<Contact> tempList) {

        if (tempList.size() == 0) {
            binding.tvEmpty.setVisibility(View.VISIBLE);
        } else {
            binding.tvEmpty.setVisibility(View.GONE);
        }
        binding.tvContactNum.setText(tempList.size() + " contacts");
        contactAdapter = new ContactAdapter(this, tempList);
        binding.rvContact.setLayoutManager(new LinearLayoutManager(this));
        binding.rvContact.setAdapter(contactAdapter);

        contactAdapter.setOnContactClickListener((contact, pos) -> {

        });
    }

    public static class SortByName implements Comparator<Contact> {
        @Override
        public int compare(Contact a, Contact b) {
            return a.getName().compareToIgnoreCase(b.getName());
        }
    }

    private void getContactList() {
        contactList = new ArrayList<>();
        contactMap = new HashMap<>();
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

        try {
            if ((cur != null ? cur.getCount() : 0) > 0) {
                while (cur.moveToNext()) {
                    String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                    String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    Log.i(TAG, "getContactList: " + name);
                    String phoneNo = "";


                    if (cur.getInt(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                        Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                new String[]{id}, null);

                        while (pCur.moveToNext()) {
                            int type = pCur.getInt(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));

                            switch (type) {

                                case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                                    phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                    Log.i(TAG, "getContactList: TYPE_HOME " + name + " " + phoneNo);
                                    setContact(name, phoneNo);
                                    break;
                                case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                                    phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                    Log.i(TAG, "getContactList: TYPE_MOBILE " + name + " " + phoneNo);
                                    setContact(name, phoneNo);
                                    break;
                                case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                                    phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                    Log.i(TAG, "getContactList: TYPE_WORK " + name + " " + phoneNo);
                                    setContact(name, phoneNo);
                                    break;

                                case ContactsContract.CommonDataKinds.Phone.TYPE_MAIN:
                                    phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                    Log.i(TAG, "getContactList: TYPE_MAIN " + name + " " + phoneNo);
                                    setContact(name, phoneNo);
                                    break;
                                case ContactsContract.CommonDataKinds.Phone.TYPE_OTHER:
                                    phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                    Log.i(TAG, "getContactList: TYPE_OTHER " + name + " " + phoneNo);
                                    setContact(name, phoneNo);
                                    break;
                                case ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM:
                                    phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                    Log.i(TAG, "getContactList: TYPE_CUSTOM " + name + " " + phoneNo);
                                    setContact(name, phoneNo);
                                    break;

                            }
                        }
                        pCur.close();
                    }


                }
            }
            if (cur != null) {
                cur.close();
            }
            Collections.sort(contactList, new SortByName());
        } catch (Exception e) {
            e.printStackTrace();
        }
//        pref.setContactList(contactList);
    }

    void setContact(String name, String phoneNo) {

        if (name == null) name = "";

        if (phoneNo == null) phoneNo = "";

        String s = phoneNo;
        phoneNo = s.replaceAll("[^0-9+]", "").trim();

        phoneNo = phoneNo.replaceAll(" ", "");

        if (!contactMap.containsKey(phoneNo)) {
            contactMap.put(phoneNo, name);
            contactList.add(new Contact(name, phoneNo));
        }
    }

    private void setToolBar() {
        binding.back.setOnClickListener(v -> {
            if (binding.etSearch.getVisibility() == View.VISIBLE) {
                binding.etSearch.setVisibility(View.GONE);
                binding.ll.setVisibility(View.VISIBLE);
                binding.ivSearch.setVisibility(View.VISIBLE);
                binding.etSearch.setText("");


            } else {

                finish();
            }
        });

        binding.ivSearch.setOnClickListener(v -> {
            binding.etSearch.setVisibility(View.VISIBLE);
            binding.ll.setVisibility(View.GONE);
            binding.ivSearch.setVisibility(View.GONE);

            binding.etSearch.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.showSoftInput(binding.etSearch, InputMethodManager.SHOW_IMPLICIT);


        });
    }

    private void setProgress() {
        dialog = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Please wait")
                .setCancellable(true)
                .setAnimationSpeed(1)
                .setDimAmount(0.25f);
    }

}