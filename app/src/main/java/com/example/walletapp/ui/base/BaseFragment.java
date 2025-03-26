package com.example.walletapp.ui.base;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.airbnb.lottie.LottieAnimationView;
import com.example.walletapp.R;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BaseFragment extends Fragment {

    private ImageView homeTab, transactionHistoryTab, profileTab;

    private final NavOptions commonNavOptions = new NavOptions.Builder()
            .setEnterAnim(R.anim.enter_anim)
            .setExitAnim(R.anim.exit_anim)
            .setPopEnterAnim(R.anim.pop_enter_anim)
            .setPopExitAnim(R.anim.pop_exit_anim)
            .build();

    @Override
    public void onViewCreated(@Nonnull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupBottomNavigation();
    }

    protected void navigateTo(int fragmentId, @Nullable Bundle bundle) {
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(fragmentId, bundle, commonNavOptions);
    }

    protected void popBack() {
        NavController navController = NavHostFragment.findNavController(this);
        navController.popBackStack();
    }

    protected void popBackToParticularFragment(int fragmentId) {
        NavController navController = NavHostFragment.findNavController(this);
        navController.popBackStack(fragmentId, false);
    }

    protected void setBottomMenuVisibility(boolean isVisible) {
        if (getActivity() != null) {
            View bottomMenu = getActivity().findViewById(R.id.customTabCv);
            if (bottomMenu != null) {
                bottomMenu.setVisibility(isVisible ? View.VISIBLE : View.GONE);
            }
        }
    }

    protected void showLoader(boolean canShow) {
        if (getActivity() != null) {
            RelativeLayout loaderRl = getActivity().findViewById(R.id.loaderRl);
            if (loaderRl != null) {
                if (canShow) {
                    loaderRl.bringToFront();
                    loaderRl.requestLayout();
                    loaderRl.setVisibility(View.VISIBLE);
                } else {
                    loaderRl.setVisibility(View.GONE);
                }
            }
        }
    }

    protected void showTransactionLottie(int lottieResId, int statusTxt) {
        if (getActivity() != null) {
            RelativeLayout transactionLottieRl = getActivity().findViewById(R.id.transactionLottieRl);
            LottieAnimationView transactionLottie = getActivity().findViewById(R.id.transactionLottie);
            TextView transactionStatusTv = getActivity().findViewById(R.id.transactionStatusTv);

            if (transactionLottieRl != null && transactionLottie != null) {
                transactionLottie.setAnimation(lottieResId);
                transactionLottie.playAnimation();

                transactionLottieRl.bringToFront();
                transactionLottieRl.requestLayout();
                transactionLottieRl.setVisibility(View.VISIBLE);
                transactionStatusTv.setText(statusTxt);

                transactionLottie.addAnimatorListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        transactionLottieRl.setVisibility(View.GONE);
                    }
                });
            }
        }
    }

    private void setupBottomNavigation() {
        if (getActivity() != null) {
            homeTab = getActivity().findViewById(R.id.homeMenuIv);
            transactionHistoryTab = getActivity().findViewById(R.id.transactionHistoryMenuIv);
            profileTab = getActivity().findViewById(R.id.profileMenuIv);

            homeTab.setOnClickListener(v -> selectTab(R.id.homeFragment));
            transactionHistoryTab.setOnClickListener(v -> selectTab(R.id.transactionsFragment));
            profileTab.setOnClickListener(v -> selectTab(R.id.profileFragment));
        }
    }

    private void selectTab(int fragmentId) {
        resetTabIcons();

        if (fragmentId == R.id.homeFragment) {
            homeTab.setImageResource(R.drawable.home_select);
        } else if (fragmentId == R.id.transactionsFragment) {
            transactionHistoryTab.setImageResource(R.drawable.transaction_select);
        } else if (fragmentId == R.id.profileFragment) {
            profileTab.setImageResource(R.drawable.profile_select);
        }

        NavController navController = NavHostFragment.findNavController(this);

        if (navController.getCurrentDestination() != null &&
                navController.getCurrentDestination().getId() == fragmentId) {
            return;
        }

        NavOptions navOptions;

        if ((fragmentId == R.id.homeFragment && navController.popBackStack(R.id.homeFragment, false)) ||
                (fragmentId == R.id.transactionsFragment && navController.popBackStack(R.id.transactionsFragment, false))) {
            navOptions = new NavOptions.Builder()
                    .setEnterAnim(R.anim.pop_enter_anim)  // Right to Left animation
                    .setExitAnim(R.anim.pop_exit_anim)
                    .build();
        } else {
            navOptions = commonNavOptions;
        }

        navController.navigate(fragmentId, null, navOptions);
    }

    private void resetTabIcons() {
        homeTab.setImageResource(R.drawable.home_unselect);
        transactionHistoryTab.setImageResource(R.drawable.transaction_unselect);
        profileTab.setImageResource(R.drawable.profile_unselct);
    }

    public void resetBottomNavigation() {
        if (getActivity() != null) {
            homeTab.setImageResource(R.drawable.home_select);
            transactionHistoryTab.setImageResource(R.drawable.transaction_unselect);
            profileTab.setImageResource(R.drawable.profile_unselct);
        }
    }

}
