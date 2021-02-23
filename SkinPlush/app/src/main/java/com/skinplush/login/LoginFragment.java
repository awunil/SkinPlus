package com.skinplush.login;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import com.google.firebase.auth.FirebaseUser;
import com.skinplush.R;
import com.skinplush.utils.Utils;

public class LoginFragment extends Fragment {

    private final String TAG = LoginFragment.class.getSimpleName();

    private EditText editEmail;
    private EditText editPassword;

    private Button btnLogin;

    private TextView tvGoToRegister;
    private TextView tvForgotPassword;

    private FirebaseAuth mAuth;

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            Navigation.findNavController(requireActivity(), R.id.navController)
                    .navigate(R.id.action_login_to_home);
        }

        View view = inflater.inflate(R.layout.login_fragment, container, false);
        editEmail = view.findViewById(R.id.edit_email);
        editPassword = view.findViewById(R.id.edit_password);

        btnLogin = view.findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(v -> validate());

        tvGoToRegister = view.findViewById(R.id.tv_go_to_sign_up);
        tvGoToRegister.setOnClickListener(v -> {
            Navigation.findNavController(requireActivity(), R.id.navController)
                    .navigate(R.id.action_login_to_register);
        });

        tvForgotPassword = view.findViewById(R.id.tv_forgot_password);
        tvForgotPassword.setOnClickListener(v ->
                {
                    Navigation.findNavController(requireActivity(), R.id.navController)
                            .navigate(R.id.action_login_to_forgotPassword);
                }
        );


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void validate() {
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            editEmail.setError(getString(R.string.error_blank_email));
            editEmail.requestFocus();

        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editEmail.setError(getString(R.string.error_format_email));
            editEmail.requestFocus();

        } else if (TextUtils.isEmpty(password)) {
            editPassword.setError(getString(R.string.error_password_blank));
            editPassword.requestFocus();

        } else {
            if (Utils.isInternetConnected(requireActivity())) {
                login();
            } else {
                Toast.makeText(requireActivity(),
                        getString(R.string.error_internet),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void login() {
        Utils.showProgress(requireActivity());
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        Utils.hideProgress();
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(requireActivity(), "Logged In Successfully",
                                Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(requireActivity(), R.id.navController)
                                .navigate(R.id.action_login_to_home);
                    } else {
                        Utils.hideProgress();
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        Toast.makeText(requireActivity(), "Incorrect Email or Password",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }


}