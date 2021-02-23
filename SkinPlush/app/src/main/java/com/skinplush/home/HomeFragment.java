package com.skinplush.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.loader.content.CursorLoader;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.automl.AutoMLImageLabelerLocalModel;
import com.google.mlkit.vision.label.automl.AutoMLImageLabelerOptions;
import com.skinplush.R;
import com.skinplush.utils.Utils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

public class HomeFragment extends Fragment {

    private final String TAG = HomeFragment.class.getSimpleName();

    private ImageLabeler labeler;

    static final int REQUEST_TAKE_PHOTO = 1;
    static final int GALLERY_REQUEST_CODE = 2;
    private static final int PERMISSION_STORAGE = 3;


    private ImageView ivImg1;
    private ImageView ivImg2;
    private ImageView ivClear1;
    private ImageView ivClear2;
    private LinearLayout llRoot;

    private Button btnImg1;
    private Button btnImg2;

    private String currentPhotoPath;
    private String path;

    private final String IMG1 = "image1";
    private final String IMG2 = "image2";

    private Uri cameraURI;
    private Uri galleryUri;

    String image1Url;
    String image2Url;

    private String selectedImageView = IMG1;

    private AlertDialog.Builder builder;
    private Dialog dialog;
    private int count;
    private boolean isMole;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.home_fragment, container, false);

        setHasOptionsMenu(true);
        llRoot = view.findViewById(R.id.linear_layout);
        ivImg1 = view.findViewById(R.id.iv_img1);
        ivImg2 = view.findViewById(R.id.iv_img2);

        ivClear1 = view.findViewById(R.id.iv_clear1);
        ivClear2 = view.findViewById(R.id.iv_clear2);

        btnImg1 = view.findViewById(R.id.btn_img1);
        btnImg2 = view.findViewById(R.id.btn_img2);

        builder = new AlertDialog.Builder(requireActivity());
        builder.setView(R.layout.progress_dialog);
        builder.setCancelable(false);
        dialog = builder.create();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnImg1.setOnClickListener(v -> {
            if (checkStoragePermission()) {
                selectedImageView = IMG1;
                selectImage();
            }
        });

        btnImg2.setOnClickListener(v -> {
            if (checkStoragePermission()) {
                selectedImageView = IMG2;
                selectImage();
            }
        });

        ivClear1.setOnClickListener(v -> {
            ivImg1.setImageDrawable(null);
            ivClear1.setVisibility(View.GONE);
            btnImg1.setVisibility(View.VISIBLE);
        });

        ivClear2.setOnClickListener(v -> {
            ivImg2.setImageDrawable(null);
            ivClear2.setVisibility(View.GONE);
            btnImg2.setVisibility(View.VISIBLE);
        });

    }

    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (ContextCompat.checkSelfPermission(requireActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSION_STORAGE);
            }

        }

        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_STORAGE:
                if (grantResults.length > 0)
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                        checkStoragePermission();
                    } else {
                        if (ActivityCompat
                                .shouldShowRequestPermissionRationale(requireActivity(),
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            Toast.makeText(requireActivity(),
                                    "Storage permission is required to save images",
                                    Toast.LENGTH_SHORT).show();

                        } else {
                            //show settings snackbar
                            showPermissionSnackbar();
                        }
                    }

                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        }
    }

    private void showPermissionSnackbar() {
        Snackbar snackbar
                = Snackbar
                .make(llRoot,
                        "Go to settings and provide the storage permission",
                        Snackbar.LENGTH_LONG).setActionTextColor(getResources()
                        .getColor(android.R.color.white))

                .setAction("Settings",
                        view -> {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package",
                                    requireActivity().getPackageName(),
                                    null);
                            intent.setData(uri);
                            startActivity(intent);
                        });

        snackbar.show();
    }


    private void selectImage() {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle("Add Photo");
        builder.setItems(options, (dialog, item) -> {
            if (options[item].equals("Take Photo")) {
                dispatchTakePictureIntent();

            } else if (options[item].equals("Choose from Gallery")) {
                Intent gallery = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(gallery, GALLERY_REQUEST_CODE);

            } else if (options[item].equals("Cancel")) {
                dialog.dismiss();
            }
        });

        builder.show();

    }


    @SuppressLint("QueryPermissionsNeeded")
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                cameraURI = FileProvider.getUriForFile(requireActivity(),
                        "com.skinplush" + ".provider",
                        photoFile);

                if (selectedImageView.equals(IMG1))
                    image1Url = photoFile.getAbsolutePath();
                else
                    image2Url = photoFile.getAbsolutePath();


                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }


    private File createImageFile() throws IOException {
        // Create an image file name

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        path = image.getPath();

        return image;
    }

    File createGalleryImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date());
        ContentResolver c = requireActivity().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String fileExt = mime.getExtensionFromMimeType(c.getType(galleryUri));
        String imageFileName = "JPEG_" + timeStamp;

        File storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                "." + fileExt,         /* suffix */
                storageDir      /* directory */
        );

        return image;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        requireActivity().sendBroadcast(mediaScanIntent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            if (cameraURI != null) {

                if (selectedImageView.equals(IMG1)) {
                    Picasso.get().load(cameraURI).fit().into(ivImg1);
                    ivClear1.setVisibility(View.VISIBLE);
                    btnImg1.setVisibility(View.GONE);
                } else {
                    Picasso.get().load(cameraURI).fit().into(ivImg2);
                    btnImg2.setVisibility(View.GONE);
                    ivClear2.setVisibility(View.VISIBLE);
                }

                galleryAddPic();
            }
        }

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK) {

            if (data != null) {
                galleryUri = data.getData();
            }

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                    .format(new Date());
            ContentResolver c = requireActivity().getContentResolver();
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            String fileExt = mime.getExtensionFromMimeType(c.getType(galleryUri));
            String imageFileName = "JPEG_" + timeStamp + "." + fileExt;
            Log.d("tag", "onActivityResult: Gallery Image Uri:  " + imageFileName);


            InputStream imageStream = null;
            try {
                imageStream = requireActivity().getContentResolver().openInputStream(galleryUri);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Bitmap selectedBitmap = BitmapFactory.decodeStream(imageStream);

            File file = null;
            FileOutputStream fos = null;
            try {
                file = createGalleryImageFile();
                fos = new FileOutputStream(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.i(TAG, "onActivityResult: File " + file + "\nFOS " + fos + "\nbmp " + selectedBitmap);

            selectedBitmap = Bitmap.createScaledBitmap(selectedBitmap, 400, 350, true);
            selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            Log.i(TAG, "onActivityResult: resized " + selectedBitmap);


            if (selectedImageView.equals(IMG1)) {
                ivImg1.setImageBitmap(selectedBitmap);

                image1Url = file.getAbsolutePath();
                Log.i(TAG, "onActivityResult: image1 url " + image1Url);
                btnImg1.setVisibility(View.GONE);
                ivClear1.setVisibility(View.VISIBLE);

            } else if (selectedImageView.equals(IMG2)) {

                ivImg2.setImageBitmap(selectedBitmap);

                image2Url = file.getAbsolutePath();
                Log.i(TAG, "onActivityResult: image2 url " + image2Url);
                btnImg2.setVisibility(View.GONE);
                ivClear2.setVisibility(View.VISIBLE);
            }
        }
    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(requireActivity(), contentUri,
                proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.action_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_item_compare) {
            if (hasImage(ivImg1) && hasImage(ivImg2)) {
                Log.i(TAG, "Both images selected");
                count = 0;
                processImage1(((BitmapDrawable) ivImg1.getDrawable()).getBitmap());
            } else {
                Toast.makeText(requireActivity(),
                        "Please select both images",
                        Toast.LENGTH_SHORT).show();
            }
        }

        if (item.getItemId() == R.id.menu_item_exit) {
            exit();
        }

        return true;
    }

    private void exit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setMessage("Are you sure you want to exit?")
                .setCancelable(false)

                .setPositiveButton("Yes", (dialog, id) -> {
                    FirebaseAuth.getInstance().signOut();
                    Toast.makeText(requireActivity(),
                            "Logged Out Successfully",
                            Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireActivity(), R.id.navController)
                            .navigate(R.id.action_home_to_login);
                })

                .setNegativeButton("No", (dialog, id) -> dialog.cancel());

        androidx.appcompat.app.AlertDialog alert = builder.create();
        alert.show();

    }

    private void processImage1(Bitmap bitmap) {
        Log.i(TAG, "processImage1: ");
        count++;
        try {
            showProgressDialog(true);
            AutoMLImageLabelerLocalModel localModel =
                    new AutoMLImageLabelerLocalModel.Builder()
                            .setAssetFilePath("detectmodel/manifest.json")
                            .build();

            AutoMLImageLabelerOptions autoMLImageLabelerOptions =
                    new AutoMLImageLabelerOptions.Builder(localModel)
                            .setConfidenceThreshold(0.8f)
                            .build();
            labeler = ImageLabeling.getClient(autoMLImageLabelerOptions);

            assert bitmap != null;

            InputImage image = InputImage.fromBitmap(bitmap, 0);

            labeler.process(image)
                    .addOnSuccessListener(labels -> {
                        // Task completed successfully
                        Log.i(TAG, "onSuccess: ");
                        showProgressDialog(false);
                        String text = "";
                        float confidence = 0;
                        int index = 0;

                        for (ImageLabel label : labels) {
                            text = label.getText();
                            confidence = label.getConfidence();
                            index = label.getIndex();
                            Log.i(TAG, "processImage: label " + label);
                        }

                        if (text.equals("mole") && confidence > 0.8) {
                            Log.i(TAG, "processImage: mole");
                            Log.i(TAG, "Result\n" + "Text: " + text
                                    + "\nConfidence " + confidence +
                                    "\nindex" + index);
                            isMole = true;
                            Log.i(TAG, "processImage: count " + count);
                            processImage2(((BitmapDrawable) ivImg2.getDrawable()).getBitmap());

                        } else {
                            Log.i(TAG, "processImage: not mole");
                            Log.i(TAG, "Result\n" + "Text: " + text
                                    + "\nConfidence " + confidence +
                                    "\nindex" + index);
                            isMole = false;
                            Utils.showAlert(requireActivity(),
                                    "Unable to process image. Try again.");
                        }
                    })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.i(TAG, "onFailure: " + e.getMessage());

                                showProgressDialog(false);
                                isMole = false;
                                Log.i(TAG, "run: on failure ");
                                Utils.showAlert(requireActivity(),
                                        "Unable to process image. Try again.");
                            }
                        });


            Log.i(TAG, " count " + count);
        } catch (Exception e) {
            Log.i(TAG, "processImage: exception " + e.getMessage());
        }
    }

    private void processImage2(Bitmap bitmap) {
        Log.i(TAG, "processImage: ");
        count++;
        try {
            showProgressDialog(true);
            AutoMLImageLabelerLocalModel localModel =
                    new AutoMLImageLabelerLocalModel.Builder()
                            .setAssetFilePath("detectmodel/manifest.json")
                            .build();

            AutoMLImageLabelerOptions autoMLImageLabelerOptions =
                    new AutoMLImageLabelerOptions.Builder(localModel)
                            .setConfidenceThreshold(0.8f)  // Evaluate your model in the Firebase console
                            // to determine an appropriate value.
                            .build();
            labeler = ImageLabeling.getClient(autoMLImageLabelerOptions);

            assert bitmap != null;

            InputImage image = InputImage.fromBitmap(bitmap, 0);

            labeler.process(image)
                    .addOnSuccessListener(labels -> {
                        // Task completed successfully
                        Log.i(TAG, "onSuccess: ");

                        showProgressDialog(false);
                        String text = "";
                        float confidence = 0;
                        int index = 0;

                        for (ImageLabel label : labels) {
                            text = label.getText();
                            confidence = label.getConfidence();
                            index = label.getIndex();
                            Log.i(TAG, "processImage: label " + label);
                        }

                        if (text.equals("mole") && confidence > 0.8) {
                            Log.i(TAG, "processImage: mole");
                            Log.i(TAG, "Result\n" + "Text: " + text
                                    + "\nConfidence " + confidence +
                                    "\nindex" + index);
                            isMole = true;
                            Log.i(TAG, "process Image now");

                            Bundle bundle = new Bundle();
                            bundle.putString("image1", image1Url);
                            bundle.putString("image2", image2Url);

                            Navigation.findNavController(requireActivity(),
                                    R.id.navController).navigate(R.id.action_home_to_compare, bundle);
                        } else {
                            Log.i(TAG, "processImage: not mole");
                            Log.i(TAG, "Result\n" + "Text: " + text
                                    + "\nConfidence " + confidence +
                                    "\nindex" + index);
                            isMole = false;
                            Utils.showAlert(requireActivity(),
                                    "Unable to process image. Try again.");
                        }

                        if (count == 2 && isMole) {
                            Log.i(TAG, "Compare images now");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.i(TAG, "onFailure: " + e.getMessage());

                            showProgressDialog(false);
                            isMole = false;
                            Log.i(TAG, "run: on failure ");
                            Utils.showAlert(requireActivity(),
                                    "Unable to process image. Try again.");
                        }
                    });


            Log.i(TAG, " count " + count);
        } catch (Exception e) {
            Log.i(TAG, "processImage: exception " + e.getMessage());
        }
    }


    private void showProgressDialog(boolean show) {
        if (show) {
            dialog.show();
        } else {
            dialog.dismiss();
        }
    }

    private boolean hasImage(@NonNull ImageView view) {
        Drawable drawable = view.getDrawable();
        boolean hasImage = (drawable != null);

        if (hasImage && (drawable instanceof BitmapDrawable)) {
            hasImage = ((BitmapDrawable) drawable).getBitmap() != null;
        }

        return hasImage;
    }


}