package edu.ehapplab.sidraenterprises.addemp;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.nguyenhoanglam.imagepicker.model.Config;
import com.nguyenhoanglam.imagepicker.model.Image;
import com.nguyenhoanglam.imagepicker.ui.imagepicker.ImagePicker;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import edu.ehapplab.sidraenterprises.R;

import static android.app.Activity.RESULT_OK;

public class AddEmployeeFragment extends Fragment {
    private static final int RESULT_PICK_CONTACT = 8;
    Intent intent;
    private CheckBox cbAdvance, cbSalary, cbID;
    private TextInputLayout inputAdvance, inputSal;
    private ImageAdapter adapter;
    private ArrayList<Image> images = new ArrayList<>();
    private RecyclerView recyclerView;
    private Button btnSignUp, btnPick;
    private EditText etName, etPhone,etAdvance,etSal;
    private String phoneNo;
    private Bitmap qrImage;
    private Snackbar snackbar;

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
        etAdvance = view.findViewById(R.id.input_advance);
        etSal = view.findViewById(R.id.input_sal);
        etName = view.findViewById(R.id.input_name);
        etPhone = view.findViewById(R.id.input_number);
        recyclerView = view.findViewById(R.id.recyclerViewImage);
        btnSignUp = view.findViewById(R.id.btn_signup);
        btnPick = view.findViewById(R.id.button1);
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

                // Access a Cloud Firestore instance from your Activity
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                generateImage();
                // Create a new user with a first and last name
                Map<String, Object> user = new HashMap<>();
                user.put("first", etName.getText().toString());
                user.put("last", etPhone.getText().toString());
                //  user.put("born", 1815);

// Add a new document with a generated ID
                db.collection("users")
                        .add(user)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                //  Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                //  Log.w(TAG, "Error adding document", e);
                            }
                        });
                Bundle b = new Bundle();
                b.putString("name", etName.getText().toString());
                b.putString("phone", etPhone.getText().toString());

              /*  Intent intent = new Intent(getActivity(), QRGenerateActivity.class);
                intent.putExtras(b);
                startActivity(intent);*/
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
                    etAdvance.setText("");
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
                    etSal.setText("");
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
                    if (data != null) {
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
            phoneNo = null;
            String name = null;
            // getData() method will have the Content Uri of the selected contact
            Uri uri = data.getData();
            //Query the content uri
            cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
            cursor.moveToFirst();
            // column index of the phone number
            int phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            // column index of the contact name
            int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            phoneNo = cursor.getString(phoneIndex).replaceAll("\\s+", "");
            name = cursor.getString(nameIndex);
            // Set the value to the textviews
            etName.setText(name);
            formatPhone();
            etPhone.setText(phoneNo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void formatPhone() {
        if (phoneNo.length() == 10) {
            return ;
        } else if (phoneNo.length() > 10) {
           phoneNo= phoneNo.substring(phoneNo.length() - 10);

        } else {
            throw new IllegalArgumentException("word has less than 3 characters!");
        }

    }

    private void generateImage() {
        final String text = etName.getText().toString()+etPhone.getText().toString();
        if (text.trim().isEmpty()) {
            alert("First type the data you want to create QR Code");
            return;
        }

        //   endEditing();
       // showLoadingVisible(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
              /*  int size = imgResult.getMeasuredWidth();
                if (size > 1) {
                    Log.e(tag, "size is set manually");
                    size = 260;
                }
*/
                Map<EncodeHintType, Object> hintMap = new EnumMap<>(EncodeHintType.class);
                hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");
                hintMap.put(EncodeHintType.MARGIN, 1);
                QRCodeWriter qrCodeWriter = new QRCodeWriter();
                try {
                    BitMatrix byteMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 260,
                            260, hintMap);
                    int height = byteMatrix.getHeight();
                    int width = byteMatrix.getWidth();
                    qrImage = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            qrImage.setPixel(x, y, byteMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                        }
                    }

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                          snackbar("QRCode has been created");
                        }
                    });

                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageReference = storage.getReference().child(etName.getText().toString()).child("id");
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    qrImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] data = baos.toByteArray();

                    UploadTask uploadTask = storageReference.putBytes(data);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                            Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        }
                    });


                } catch (WriterException e) {
                    e.printStackTrace();
                    alert(e.getMessage());
                }
            }
        }).start();
    }
    private void alert(String message) {
        AlertDialog dlg = new AlertDialog.Builder(getActivity())
                .setTitle("QRCode Generator")
                .setMessage(message)
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        dlg.show();
    }
    private void snackbar(String msg) {
        if (snackbar != null) {
           snackbar.dismiss();
        }

      snackbar = Snackbar.make(getActivity().findViewById(R.id.mbody),
                msg, Snackbar.LENGTH_SHORT);

        snackbar.show();
    }


}