package com.skinplush.compare;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.skinplush.R;
import com.skinplush.models.CompareResponse;
import com.skinplush.utils.Utils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Awuni junior on 16/11/2020.
 */
public class CompareFragment extends Fragment {

    private final String TAG = CompareFragment.class.getSimpleName();

    private ImageView ivImg1;
    private ImageView ivImg2;
    private ImageView ivOutput;

    String image1Url;
    String image2Url;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.compare_fragment, container, false);
        ivImg1 = view.findViewById(R.id.iv_img1);
        ivImg2 = view.findViewById(R.id.iv_img2);
        ivOutput = view.findViewById(R.id.iv_output);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            Log.i(TAG, "onViewCreated:1 " + bundle.getString("image1"));
            Log.i(TAG, "onViewCreated:2 " + bundle.getString("image2"));
            image1Url = bundle.getString("image1");
            image2Url = bundle.getString("image2");
                compareImages();
                  }
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }


    private void compareImages() {

        if (Utils.isInternetConnected(requireActivity())) {
            RequestParams params = new RequestParams();

            File imgFile1 = new File(image1Url);
            File imgFile2 = new File(image2Url);

            try {
                params.put("image1", imgFile1);
                params.put("image2", imgFile2);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            compareMolesApiCall(params);

        } else {
            Toast.makeText(requireActivity(),
                    R.string.error_internet,
                    Toast.LENGTH_LONG).show();
        }

    }

    private void compareMolesApiCall(final RequestParams params) {
        Log.i(TAG, "loginAPICall: ");
        try {
            AsyncHttpClient client = new AsyncHttpClient();
            client.setTimeout(Utils.DEFAULT_TIMEOUT);
//            client.addHeader(HttpHeaders.AUTHORIZATION,
//                    "Bearer " + prefrenceManager.getUserData().getSecurityToken());
            client.post(requireActivity(),
                    Utils.COMPARE_API,
                    params,
                    new AsyncHttpResponseHandler() {

                        @Override
                        public void onStart() {
                            super.onStart();

                            Utils.showProgress(requireActivity());

                            Log.i(TAG, "URL => " + this.getRequestURI());
                            Log.i(TAG, "PARAMS => " + params.toString());
                        }

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            Log.i(TAG, "onSuccess: ");

                            try {
//                                if (statusCode == 200) {

                                    if (responseBody != null && responseBody.length > 0) {

                                        String str = new String(responseBody);
                                        Log.i(TAG, "Response: " + str);

                                        Gson gson = new Gson();
                                        CompareResponse compareResponse = gson
                                                .fromJson(str, CompareResponse.class);

                                        if (compareResponse != null) {

                                            Log.i(TAG, "onSuccess: " + compareResponse.toString());

                                            if (compareResponse.getCompareData() != null) {

                                                if (!TextUtils.isEmpty(compareResponse
                                                        .getCompareData().getImage1())) {
                                                    String image1 = Utils.BASE_API + compareResponse
                                                            .getCompareData().getImage1();
                                                    Picasso.get().load(image1).fit().into(ivImg1);
                                                }

                                                if (!TextUtils.isEmpty(compareResponse
                                                        .getCompareData().getImage2())) {
                                                    String image2 = Utils.BASE_API + compareResponse
                                                            .getCompareData().getImage2();
                                                    Picasso.get().load(image2).fit().into(ivImg2);
                                                }

                                                if (!TextUtils.isEmpty(compareResponse
                                                        .getCompareData().getOutputImage())) {
                                                    String outputImage = Utils.BASE_API + compareResponse
                                                            .getCompareData().getOutputImage();
                                                    Picasso.get().load(outputImage).fit().into(ivOutput, new Callback() {
                                                        @Override
                                                        public void onSuccess() {
                                                            Utils.hideProgress();
                                                        }

                                                        @Override
                                                        public void onError(Exception e) {
                                                            Utils.hideProgress();
                                                            Log.i(TAG, "onError: " + e.getMessage());
                                                        }
                                                    });
                                                }

                                                if (compareResponse.getMessage() != null) {
                                                    Toast.makeText(requireActivity(),
                                                            compareResponse.getMessage(),
                                                            Toast.LENGTH_LONG).show();
                                                }

                                            }

                                        } else {
                                            Log.i(TAG, "onSuccess: " + responseBody.toString());

                                        }
                                    }

                            } catch (Exception e) {
                                Log.e(TAG, "on success Exception:"
                                        + Log.getStackTraceString(e));
                            }
                        }

                        @Override
                        public void onFailure(int statusCode,
                                              Header[] headers,
                                              byte[] responseBody,
                                              Throwable error) {

                            Log.i(TAG, "onFailure:Message " + error.getMessage());
                            Log.i(TAG, "onFailure:Status Code " + statusCode);

                            Utils.hideProgress();

                            if (responseBody != null && responseBody.length > 0) {
                                Log.i(TAG, "onFailure: response " + Arrays.toString(responseBody));
                                Gson gson = new Gson();
                                String responseString = new String(responseBody);

                                CompareResponse fateResponse = gson
                                        .fromJson(responseString, CompareResponse.class);

                                if (fateResponse != null) {

                                    if (!TextUtils.isEmpty(fateResponse.getError())) {

                                        Toast.makeText(requireActivity(),
                                                fateResponse.getError(),
                                                Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(requireActivity(),
                                                error.getMessage(),
                                                Toast.LENGTH_LONG).show();
                                    }
                                }
                            }
                        }
                    });

        } catch (Exception e) {
            Log.e(TAG, "EXCEPTION " + Log.getStackTraceString(e));
        }
    }
}
