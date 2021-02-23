package com.skinplush.register;

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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.skinplush.R;
import com.skinplush.models.User;
import com.skinplush.utils.Utils;

public class RegisterFragment extends Fragment {

    private final String TAG = RegisterFragment.class.getSimpleName();

    private EditText editName;
    private EditText editEmail;
    private EditText editPassword;
    private EditText editRepeatPassword;

    private TextView tvGoToLogin;

    private Button btnRegister;

    private NavController navController;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;


    public static RegisterFragment newInstance() {
        return new RegisterFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        View view = inflater.inflate(R.layout.register_fragment, container, false);
        navController = Navigation.findNavController(requireActivity(), R.id.navController);
        editName = view.findViewById(R.id.edit_name);
        editEmail = view.findViewById(R.id.edit_email);
        editPassword = view.findViewById(R.id.edit_password);
        editRepeatPassword = view.findViewById(R.id.edit_repeat_password);


        tvGoToLogin = view.findViewById(R.id.tv_go_to_login);
        tvGoToLogin.setOnClickListener(v -> {
            Navigation.findNavController(requireActivity(), R.id.navController)
                    .navigate(R.id.action_register_to_login);

        });

        btnRegister = view.findViewById(R.id.btn_register);
        btnRegister.setOnClickListener(v -> {
            validate();
        });


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void validate() {
        String name = editName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();
        String repeatPassword = editRepeatPassword.getText().toString();

        if (TextUtils.isEmpty(name)) {
            editName.setError(getString(R.string.error_blank_name));
            editName.requestFocus();

        } else if (name.length() < 3) {
            editName.setError(getString(R.string.error_short_name));
            editName.requestFocus();

        } else if (TextUtils.isEmpty(email)) {
            editEmail.setError(getString(R.string.error_blank_email));
            editEmail.requestFocus();

        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editEmail.setError(getString(R.string.error_format_email));
            editEmail.requestFocus();

        } else if (TextUtils.isEmpty(password)) {
            editPassword.setError(getString(R.string.error_password_blank));
            editPassword.requestFocus();

        } else if (password.length() < 6) {
            editPassword.setError(getString(R.string.error_length_password));
            editPassword.requestFocus();

        } else if (TextUtils.isEmpty(repeatPassword)) {
            editRepeatPassword.setError(getString(R.string.error_repeat_pass_req));
            editRepeatPassword.requestFocus();

        } else if (!password.equals(repeatPassword)) {
            editRepeatPassword.setError(getString(R.string.error_password_match));
            editRepeatPassword.requestFocus();

        } else {
            if (Utils.isInternetConnected(requireActivity())) {
                register();
            } else {
                Toast.makeText(requireActivity(),
                        getString(R.string.error_internet),
                        Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void register() {
        Utils.showProgress(requireActivity());
        String name = editName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in firebaseUser's information
                        Utils.hideProgress();
                        Log.d(TAG, "createUserWithEmail:success");
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        User user = new User();
                        user.setName(name);

                        if (firebaseUser != null) {
                            mDatabase.child("users").child(firebaseUser.getUid()).setValue(user);
                            Toast.makeText(requireActivity(),
                                    "Registration Successful",
                                    Toast.LENGTH_SHORT).show();
                            Navigation.findNavController(requireActivity(), R.id.navController)
                                    .navigate(R.id.action_register_to_home);
                        }

                    } else {
                        Utils.hideProgress();
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        Toast.makeText(requireActivity(),
                                "Authentication failed.",
                                Toast.LENGTH_SHORT).show();

                    }
                });
    }

}