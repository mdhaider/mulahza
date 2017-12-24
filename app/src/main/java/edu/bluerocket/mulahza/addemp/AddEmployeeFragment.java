package edu.bluerocket.mulahza.addemp;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.nguyenhoanglam.imagepicker.model.Config;
import com.nguyenhoanglam.imagepicker.model.Image;
import com.nguyenhoanglam.imagepicker.ui.imagepicker.ImagePicker;

import java.util.ArrayList;

import edu.bluerocket.mulahza.R;

import static android.app.Activity.RESULT_OK;

public class AddEmployeeFragment extends Fragment {
    private static final int RESULT_PICK_CONTACT = 8;
    private CheckBox cbAdvance, cbSalary, cbID;
    private TextInputLayout inputAdvance, inputSal;
    private ImageAdapter adapter;
    private ArrayList<Image> images = new ArrayList<>();
    private RecyclerView recyclerView;
    private Button btnSignUp,btnPick;
    private EditText etName,etPhone;
    Intent intent ;
    public AddEmployeeFragment() {
    }

    public static AddEmployeeFragment newInstance() {
        AddEmployeeFragment fragment = new AddEmployeeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_employee, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cbAdvance = view.findViewById(R.id.checkBoxAdvanced);
        cbSalary = view.findViewById(R.id.checkBoxSalary);
        cbID = view.findViewById(R.id.checkBoxID);
        inputAdvance = view.findViewById(R.id.advanceEt);
        inputSal = view.findViewById(R.id.salEt);
        etName = view.findViewById(R.id.input_name);
        etPhone = view.findViewById(R.id.input_number);
        recyclerView = view.findViewById(R.id.recyclerViewImage);
        btnSignUp=view.findViewById(R.id.btn_signup);
        btnPick=view.findViewById(R.id.button1);
        checkBoxStateListener();

        btnPick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                startActivityForResult(contactPickerIntent, RESULT_PICK_CONTACT);
            }
        });


        adapter = new ImageAdapter(getActivity());
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle b = new Bundle();
                b.putString("name",etName.getText().toString() );
                b.putString("phone", etPhone.getText().toString());
                Intent intent = new Intent(getActivity(), QRGenerateActivity.class);
                intent.putExtras(b);
                startActivity(intent);
            }
        });
    }

    public void checkBoxStateListener() {

        cbAdvance.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (cbAdvance.isChecked()) {
                    inputAdvance.setVisibility(View.VISIBLE);
                } else {
                    inputAdvance.setVisibility(View.GONE);
                }
            }
        });
        cbSalary.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (cbSalary.isChecked()) {
                    inputSal.setVisibility(View.VISIBLE);
                } else {
                    inputSal.setVisibility(View.GONE);
                }
            }
        });
        cbID.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (cbID.isChecked()) {
                    start();
                    recyclerView.setVisibility(View.VISIBLE);
                } else {
                    adapter.setData(null);
                    recyclerView.setVisibility(View.GONE);
                }
            }
        });

    }

     private void start() {

        ImagePicker.with(this)                         //  Initialize ImagePicker with activity or fragment context
                .setToolbarColor("#212121")         //  Toolbar color
                .setStatusBarColor("#000000")       //  StatusBar color (works with SDK >= 21  )
                .setToolbarTextColor("#FFFFFF")     //  Toolbar text color (Title and Done button)
                .setToolbarIconColor("#FFFFFF")     //  Toolbar icon color (Back and Camera button)
                .setProgressBarColor("#4CAF50")     //  ProgressBar color
                .setBackgroundColor("#212121")      //  Background color
                .setCameraOnly(false)               //  Camera mode
                .setMultipleMode(true)              //  Select multiple images or single image
                .setFolderMode(true)                //  Folder mode
                .setShowCamera(true)                //  Show camera button
                .setFolderTitle("Albums")           //  Folder title (works with FolderMode = true)
                .setImageTitle("Galleries")         //  Image title (works with FolderMode = false)
                .setDoneTitle("Done")               //  Done button title
                .setLimitMessage("You have reached selection limit")    // Selection limit message
                .setMaxSize(5)                     //  Max images can be selected
                .setSavePath("SidraEnterPrises")         //  Image capture folder name
                .setSelectedImages(images)          //  Selected images
                .setKeepScreenOn(true)              //  Keep screen on when selecting images
                .start();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case RESULT_PICK_CONTACT:
                    contactPicked(data);
                    break;
                case Config.RC_PICK_IMAGES:
                    if(data !=null){
                        images = data.getParcelableArrayListExtra(Config.EXTRA_IMAGES);
                        adapter.setData(images);
                    }
                    break;
            }
        } else {
            Log.e("MainActivity", "Operation Failed");
        }

        super.onActivityResult(requestCode, resultCode, data); // THIS METHOD SHOULD BE HERE so that ImagePicker works with fragment
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void contactPicked(Intent data) {
        Cursor cursor = null;
        try {
            String phoneNo = null ;
            String name = null;
            // getData() method will have the Content Uri of the selected contact
            Uri uri = data.getData();
            //Query the content uri
            cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
            cursor.moveToFirst();
            // column index of the phone number
            int  phoneIndex =cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            // column index of the contact name
            int  nameIndex =cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            phoneNo = cursor.getString(phoneIndex);
            name = cursor.getString(nameIndex);
            // Set the value to the textviews
            etName.setText(name);
            etPhone.setText(phoneNo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}