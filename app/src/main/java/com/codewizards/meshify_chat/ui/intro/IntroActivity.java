package com.codewizards.meshify_chat.ui.intro;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.codewizards.meshify_chat.R;
import com.codewizards.meshify_chat.main.MeshifyConstants;
import com.codewizards.meshify_chat.ui.intro.onboarding.OnboardingAdapter;
import com.codewizards.meshify_chat.ui.intro.onboarding.OnboardingItem;
import com.codewizards.meshify_chat.ui.signup.SignupActivity;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class IntroActivity extends AppCompatActivity {

    @BindView(R.id.btn_verify)
    Button btnVerify;
    @BindView(R.id.pager)
    ViewPager2 mViewPager;
    @BindView(R.id.onboardingIndicators)
    LinearLayout mIndicators;
    int AUTHUI_REQUEST_CODE = 10001;
    private OnboardingAdapter onboardingAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        ButterKnife.bind(this);

        setupOnboardingItems();

        this.mViewPager.setAdapter(onboardingAdapter);
        this.mViewPager.setPageTransformer(new DepthPageTransformer());

        setupOnbordingIndicators();
        setCurrentOnboardingIndicator(0);

        mViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setCurrentOnboardingIndicator(position);
            }
        });

    }

    private void setupOnboardingItems() {
        List<OnboardingItem> onboardingItems = new ArrayList<>();

        OnboardingItem itemOne = new OnboardingItem();
        itemOne.setTitle(getString(MeshifyConstants.string.onboarding_title_1));
        itemOne.setDescription(getString(MeshifyConstants.string.onboarding_description_1));
        itemOne.setImage(R.drawable.onboarding_1);

        OnboardingItem itemTwo = new OnboardingItem();
        itemTwo.setTitle(getString(MeshifyConstants.string.onboarding_title_2));
        itemTwo.setDescription(getString(MeshifyConstants.string.onboarding_description_2));
        itemTwo.setImage(R.drawable.onboarding_2);

        onboardingItems.add(itemOne);
        onboardingItems.add(itemTwo);

        onboardingAdapter = new OnboardingAdapter(onboardingItems);

    }

    private void setupOnbordingIndicators() {
        ImageView[] indicators = new ImageView[onboardingAdapter.getItemCount()];
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(8, 0, 8, 0);
        for (int i = 0; i < indicators.length; i++) {
            indicators[i] = new ImageView(getApplicationContext());
            indicators[i].setImageDrawable(ContextCompat.getDrawable(
                    getApplicationContext(),
                    R.drawable.onboarding_indicator_inactive
            ));
            indicators[i].setLayoutParams(layoutParams);
            mIndicators.addView(indicators[i]);
        }
    }

    private void setCurrentOnboardingIndicator(int index) {
        int childCount = mIndicators.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ImageView imageView = (ImageView) mIndicators.getChildAt(i);
            if (i == index) {
                imageView.setImageDrawable(
                        ContextCompat.getDrawable(getApplicationContext(), R.drawable.onboarding_indicator_active)
                );
            } else {
                imageView.setImageDrawable(
                        ContextCompat.getDrawable(getApplicationContext(), R.drawable.onboarding_indicator_inactive)
                );
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTHUI_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                startActivity(new Intent(getApplicationContext(), SignupActivity.class));
                finish();
                return;
            }
        } else {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (response == null) {
                //Log
            } else {
                //Log
            }
        }
    }

    private boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo wifiConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if ((wifiConn != null && wifiConn.isConnected()) || (mobileConn != null && mobileConn.isConnected())) {
            return true;
        }

        return false;
    }

    @OnClick({R.id.btn_verify})
    public void startVerification(View v) {
        if (mViewPager.getCurrentItem() + 1 < onboardingAdapter.getItemCount()) {
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
        } else {


            if (!isConnected()) {
                showConnectivityDialog();
                return;
            }

            List<AuthUI.IdpConfig> provider = Arrays.asList(
                    new AuthUI.IdpConfig.PhoneBuilder().build()
            );

            Intent intent = AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(provider)
                    .setTosAndPrivacyPolicyUrls(getString(MeshifyConstants.string.url_tos), getString(MeshifyConstants.string.url_privacy))
                    .setLogo(R.drawable.ic_logo_grey)
                    .setTheme(R.style.GreenTheme)
                    .build();

            startActivityForResult(intent, AUTHUI_REQUEST_CODE);

        }
    }

    private void showConnectivityDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(IntroActivity.this);
        builder.setMessage(getString(MeshifyConstants.string.connectivity_dialog_text))
                .setCancelable(false)
                .setPositiveButton("Connect", (dialog, which) -> startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)))
                .setNegativeButton("Skip", (dialog, which) -> startActivity(new Intent(getApplicationContext(), SignupActivity.class)));
        builder.show();
    }
}