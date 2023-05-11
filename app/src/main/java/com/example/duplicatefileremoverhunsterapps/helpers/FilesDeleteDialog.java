package com.example.duplicatefileremoverhunsterapps.helpers;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.example.duplicatefileremoverhunsterapps.R;
import com.example.duplicatefileremoverhunsterapps.databinding.DeleteDialogBinding;


public class FilesDeleteDialog extends Dialog {

    public FilesDeleteDialog(@NonNull Context context) {
        super(context, R.style.DialogTheme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DeleteDialogBinding binding=DeleteDialogBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
      getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
    }
}
