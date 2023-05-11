package com.example.duplicatefileremoverhunsterapps.helpers;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.example.duplicatefileremoverhunsterapps.R;
import com.example.duplicatefileremoverhunsterapps.databinding.SearchingDialogBinding;


public class FilesScanningDialog extends Dialog {
    String name="Scanning Photos";

    public FilesScanningDialog(@NonNull Context context) {
        super(context, R.style.DialogTheme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SearchingDialogBinding binding= SearchingDialogBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
      getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
      binding.title.setText(name);
    }
    public void setNameTitle(String name){
        this.name=name;
    }
}
