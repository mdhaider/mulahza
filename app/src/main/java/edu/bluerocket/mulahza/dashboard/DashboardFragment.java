package edu.bluerocket.mulahza.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.robertlevonyan.views.customfloatingactionbutton.FloatingActionLayout;

import edu.bluerocket.mulahza.R;


/**
 * Created by yarolegovich on 25.03.2017.
 */

public class DashboardFragment extends Fragment implements View.OnClickListener {
    private FloatingActionLayout fab;
    private TextView textViewName;
    private IntentIntegrator qrScan;


    public DashboardFragment() {
    }

    public static DashboardFragment newInstance() {
        DashboardFragment fragment = new DashboardFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_text, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        qrScan = new IntentIntegrator(getActivity());
        textViewName = view.findViewById(R.id.textViewName);

        fab = view.findViewById(R.id.customFABL);
        fab.setOnClickListener(this);
    }
    //Getting the scan results
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            //if qrcode has nothing in it
            if (result.getContents() == null) {
                Toast.makeText(getActivity(), "Result Not Found", Toast.LENGTH_LONG).show();
            } else {

                try {

                    textViewName.setText(result.getContents());
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), result.getContents(), Toast.LENGTH_LONG).show();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    @Override
    public void onClick(View view) {

        qrScan.setOrientationLocked(false);
        qrScan.forSupportFragment(DashboardFragment.this).initiateScan();
    }
}