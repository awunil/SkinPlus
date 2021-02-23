package com.skinplush.forgotpassword;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.skinplush.R;
import com.skinplush.utils.Utils;

public class ForgotPassword extends Fragment {

    private final String TAG = ForgotPassword.class.getSimpleName();

    private EditText editEmail;
    private TextView tvBackToLogin;
    private Button btnSend;

    private FirebaseAuth mAuth;


    public ForgotPassword() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_forgot_password, container, false);
        mAuth = FirebaseAuth.getInstance();
        editEmail = view.findViewById(R.id.edit_email);
        btnSend = view.findViewById(R.id.btn_send);
        tvBackToLogin = view.findViewById(R.id.tv_back_to_login);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnSend.setOnClickListener(v -> sendPasswordResetLink());

        tvBackToLogin.setOnClickListener(v -> Navigation.findNavController(requireActivity(),
                R.id.navController)
                .navigate(R.id.action_forgot_to_login));
    }

    private void sendPasswordResetLink() {
        String email = editEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            editEmail.setError(getString(R.string.error_blank_email));
            editEmail.requestFocus();

        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editEmail.setError(getString(R.string.error_format_email));
            editEmail.requestFocus();

        } else {
            if (Utils.isInternetConnected(requireActivity())) {
                Utils.showProgress(requireActivity());

                mAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Utils.hideProgress();
                        Toast.makeText(requireActivity(),
                                "A password reset link is sent to your mail.",
                                Toast.LENGTH_LONG).show();
                        requireActivity().onBackPressed();
                    } else {
                        Utils.hideProgress();
                    }
                });
            } else {
                Toast.makeText(requireActivity(),
                        R.string.error_internet,
                        Toast.LENGTH_LONG).show();

            }
        }
    }
}