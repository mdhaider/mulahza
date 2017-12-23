package edu.bluerocket.mulahza.addemp;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.Calendar;
import java.util.EnumMap;
import java.util.Map;

import edu.bluerocket.mulahza.R;

public class QRGenerateActivity extends AppCompatActivity {
    private final String tag = "QRCGEN";
    private final int REQUEST_PERMISSION = 0xf0;

    private QRGenerateActivity self;
    private Snackbar snackbar;
    private Bitmap qrImage;

    private TextView txtSaveHint;
    private ImageView imgResult;
    private ProgressBar loader;
    private String s1,s2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_add_new_member);

        Intent in=getIntent();

        Bundle b=in.getExtras();

         s1=b.getString("name");

         s2 = b.getString("phone");
        self = this;

        txtSaveHint = (TextView) findViewById(R.id.txtSaveHint);
        imgResult = (ImageView) findViewById(R.id.imgResult);
        loader = (ProgressBar) findViewById(R.id.loader);

        self.generateImage();

        imgResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                self.confirm("Save Image?", "Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveImage();
                    }
                });
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveImage();
            } else {
                alert("The app does not have access to add images.");
            }
        }
    }

    private void saveImage() {
        if (qrImage == null) {
            alert("No pictures yet.");
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION);
            return;
        }


        String fname = "qrcode-" + Calendar.getInstance().getTimeInMillis();
        boolean success = true;
        try {
            String result = MediaStore.Images.Media.insertImage(
                    getContentResolver(),
                    qrImage,
                    fname,
                    "QRCode Image"
            );
            if (result == null) {
                success = false;
            } else {
                Log.e(tag, result);
            }
        } catch (Exception e) {
            e.printStackTrace();
            success = false;
        }

        if (!success) {
            alert("Failed to save image");
        } else {
            self.snackbar("Image saved to gallery.");
        }
    }

    private void alert(String message) {
        AlertDialog dlg = new AlertDialog.Builder(self)
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

    private void confirm(String msg, String yesText, final AlertDialog.OnClickListener yesListener) {
        AlertDialog dlg = new AlertDialog.Builder(self)
                .setTitle("Confirmation")
                .setMessage(msg)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(yesText, yesListener)
                .create();
        dlg.show();
    }

    private void snackbar(String msg) {
        if (self.snackbar != null) {
            self.snackbar.dismiss();
        }

        self.snackbar = Snackbar.make(
                findViewById(R.id.mainBody),
                msg, Snackbar.LENGTH_SHORT);

        self.snackbar.show();
    }

    /*private void endEditing() {
        txtQRText.clearFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.
                INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }*/


    private void generateImage() {
        final String text = s1+s2;
        if (text.trim().isEmpty()) {
            alert("First type the data you want to create QR Code");
            return;
        }

     //   endEditing();
        showLoadingVisible(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                int size = imgResult.getMeasuredWidth();
                if (size > 1) {
                    Log.e(tag, "size is set manually");
                    size = 260;
                }

                Map<EncodeHintType, Object> hintMap = new EnumMap<>(EncodeHintType.class);
                hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");
                hintMap.put(EncodeHintType.MARGIN, 1);
                QRCodeWriter qrCodeWriter = new QRCodeWriter();
                try {
                    BitMatrix byteMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, size,
                            size, hintMap);
                    int height = byteMatrix.getHeight();
                    int width = byteMatrix.getWidth();
                    self.qrImage = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            qrImage.setPixel(x, y, byteMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                        }
                    }

                    self.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            self.showImage(self.qrImage);
                            self.showLoadingVisible(false);
                            self.snackbar("QRCode has been created");
                        }
                    });
                } catch (WriterException e) {
                    e.printStackTrace();
                    alert(e.getMessage());
                }
            }
        }).start();
    }

    private void showLoadingVisible(boolean visible) {
        if (visible) {
            showImage(null);
        }

        loader.setVisibility(
                (visible) ? View.VISIBLE : View.GONE
        );
    }

    private void reset() {
        showImage(null);
      //  endEditing();
    }

    private void showImage(Bitmap bitmap) {
        if (bitmap == null) {
            imgResult.setImageResource(android.R.color.transparent);
            qrImage = null;
            txtSaveHint.setVisibility(View.GONE);
        } else {
            imgResult.setImageBitmap(bitmap);
            txtSaveHint.setVisibility(View.VISIBLE);
        }
    }
}

