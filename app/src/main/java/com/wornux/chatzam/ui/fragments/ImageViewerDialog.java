package com.wornux.chatzam.ui.fragments;

import android.app.Dialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.wornux.chatzam.databinding.DialogImageViewerBinding;
import org.jetbrains.annotations.NotNull;

public class ImageViewerDialog extends DialogFragment {

    private static final String ARG_IMAGE_URL = "image_url";
    private DialogImageViewerBinding binding;
    private String imageUrl;

    public static ImageViewerDialog newInstance(String imageUrl) {
        ImageViewerDialog dialog = new ImageViewerDialog();
        Bundle args = new Bundle();
        args.putString(ARG_IMAGE_URL, imageUrl);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);

        if (getArguments() != null) {
            imageUrl = getArguments().getString(ARG_IMAGE_URL);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        Window window = dialog.getWindow();
        if (window != null) {
            window.requestFeature(Window.FEATURE_NO_TITLE);
        }
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogImageViewerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupPhotoView();
        setupClickListeners();
        loadImage();
    }

    private void setupPhotoView() {
        binding.photoView.setMaximumScale(3.0f);
        binding.photoView.setMediumScale(2.0f);
        binding.photoView.setMinimumScale(1.0f);
    }

    private void setupClickListeners() {
        binding.photoView.setOnClickListener(v -> dismiss());
        binding.retryButton.setOnClickListener(v -> loadImage());
    }

    private void loadImage() {
        if (imageUrl == null || imageUrl.isEmpty()) {
            showError();
            return;
        }

        showLoading();

        Glide.with(this)
                .load(imageUrl)
                .fitCenter()
                .listener(new RequestListener<>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                @NotNull Target<Drawable> target, boolean isFirstResource) {
                        showError();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(@NotNull Drawable resource, @NotNull Object model,
                                                   Target<Drawable> target, @NotNull DataSource dataSource,
                                                   boolean isFirstResource) {
                        hideLoading();
                        return false;
                    }
                })
                .into(binding.photoView);
    }

    private void showLoading() {
        binding.loadingProgressBar.setVisibility(View.VISIBLE);
        binding.errorLayout.setVisibility(View.GONE);
        binding.photoView.setVisibility(View.INVISIBLE);
    }

    private void hideLoading() {
        binding.loadingProgressBar.setVisibility(View.GONE);
        binding.photoView.setVisibility(View.VISIBLE);
    }

    private void showError() {
        binding.loadingProgressBar.setVisibility(View.GONE);
        binding.errorLayout.setVisibility(View.VISIBLE);
        binding.photoView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
