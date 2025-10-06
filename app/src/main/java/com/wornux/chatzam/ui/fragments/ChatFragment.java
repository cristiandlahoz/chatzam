package com.wornux.chatzam.ui.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.wornux.chatzam.R;
import com.wornux.chatzam.databinding.FragmentChatBinding;
import com.wornux.chatzam.data.entities.Message;
import com.wornux.chatzam.ui.adapters.MessageAdapter;
import com.wornux.chatzam.ui.base.BaseFragment;
import com.wornux.chatzam.ui.viewmodels.ChatViewModel;
import dagger.hilt.android.AndroidEntryPoint;

import java.util.Objects;

@AndroidEntryPoint
public class ChatFragment extends BaseFragment<ChatViewModel> {

  private FragmentChatBinding binding;
  private MessageAdapter messageAdapter;

  private static final String ARG_CHAT_ID = "chat_id";
  private static final String ARG_CHAT_NAME = "chat_name";

  private ActivityResultLauncher<PickVisualMediaRequest> photoPickerLauncher;
  private ActivityResultLauncher<Intent> legacyPickerLauncher;
  private ActivityResultLauncher<String> permissionLauncher;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setupActivityResultLaunchers();
  }

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    binding = FragmentChatBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    String chatId = getArguments() != null ? getArguments().getString(ARG_CHAT_ID) : null;
    String chatName = getArguments() != null ? getArguments().getString(ARG_CHAT_NAME) : null;
    if (chatId != null) {
      viewModel.setChatId(chatId);
    }

    if(requireActivity() instanceof AppCompatActivity activity) {
        Objects.requireNonNull(activity.getSupportActionBar()).setTitle(chatName);
    }

    setupRecyclerView();
  }

  private void setupRecyclerView() {
    String currentUserId = viewModel.getCurrentUserId();
    messageAdapter = new MessageAdapter(currentUserId);

    LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
    layoutManager.setStackFromEnd(true);

    binding.messagesRecyclerView.setLayoutManager(layoutManager);
    binding.messagesRecyclerView.setAdapter(messageAdapter);

    messageAdapter.setOnMessageClickListener(
        new MessageAdapter.OnMessageClickListener() {
          @Override
          public void onMessageClick(Message message) {
            if (!viewModel.isMessageFromCurrentUser(message) && !message.isRead()) {
              viewModel.markMessageAsRead(message.getMessageId());
            }
          }

          @Override
          public void onImageClick(Message message) {
            if (message.hasMedia()) {
              ImageViewerDialog dialog = ImageViewerDialog.newInstance(message.getMediaUrl());
              dialog.show(getParentFragmentManager(), "ImageViewerDialog");
            }
          }
        });
  }

  @Override
  protected void setupObservers() {
    viewModel
        .getMessages()
        .observe(
            getViewLifecycleOwner(),
            messages -> {
              if (messages != null) {
                messageAdapter.submitList(messages, this::scrollToBottom);
              }
            });

    viewModel
        .getLoading()
        .observe(
            getViewLifecycleOwner(),
            isLoading ->
                binding.loadingProgressBar.setVisibility(
                    Boolean.TRUE.equals(isLoading) ? View.VISIBLE : View.GONE));

    viewModel
        .getError()
        .observe(
            getViewLifecycleOwner(),
            error -> {
              if (error != null && !error.isEmpty()) {
                showError(error);
              }
            });
  }

  @Override
  protected void setupClickListeners() {
    binding.sendButton.setOnClickListener(v -> sendMessage());
    binding.attachButton.setOnClickListener(v -> showAttachmentMenu());
  }

  private void sendMessage() {
    String message = binding.messageEditText.getText().toString().trim();
    if (!message.isEmpty()) {
      viewModel.sendMessage(message);
      binding.messageEditText.setText("");
    }
  }

  private void scrollToBottom() {
    if (messageAdapter.getItemCount() > 0) {
      binding.messagesRecyclerView.scrollToPosition(messageAdapter.getItemCount() - 1);
    }
  }

  private void setupActivityResultLaunchers() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      photoPickerLauncher = registerForActivityResult(
          new ActivityResultContracts.PickVisualMedia(),
          uri -> {
            if (uri != null) {
              viewModel.sendImageMessage(uri);
            }
          });
    }

    legacyPickerLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
          if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
            Uri imageUri = result.getData().getData();
            if (imageUri != null) {
              viewModel.sendImageMessage(imageUri);
            }
          }
        });

    permissionLauncher = registerForActivityResult(
        new ActivityResultContracts.RequestPermission(),
        isGranted -> {
          if (Boolean.TRUE.equals(isGranted)) {
            openLegacyImagePicker();
          } else {
            showError(getString(R.string.permission_denied));
          }
        });
  }

  private void showAttachmentMenu() {
    PopupMenu popupMenu = new PopupMenu(requireContext(), binding.attachButton);
    popupMenu.getMenuInflater().inflate(R.menu.menu_attachment_options, popupMenu.getMenu());
    popupMenu.setOnMenuItemClickListener(this::onAttachmentMenuItemClick);
    popupMenu.show();
  }

  private boolean onAttachmentMenuItemClick(MenuItem item) {
    if (item.getItemId() == R.id.action_send_image) {
      openImagePicker();
      return true;
    }
    return false;
  }

  private void openImagePicker() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      PickVisualMediaRequest request = new PickVisualMediaRequest.Builder()
          .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
          .build();
      photoPickerLauncher.launch(request);
    } else {
      checkPermissionAndOpenLegacyPicker();
    }
  }

  private void checkPermissionAndOpenLegacyPicker() {
    String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        ? Manifest.permission.READ_MEDIA_IMAGES
        : Manifest.permission.READ_EXTERNAL_STORAGE;

    if (ContextCompat.checkSelfPermission(requireContext(), permission) 
        == PackageManager.PERMISSION_GRANTED) {
      openLegacyImagePicker();
    } else {
      permissionLauncher.launch(permission);
    }
  }

  private void openLegacyImagePicker() {
    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    intent.setType("image/*");
    legacyPickerLauncher.launch(intent);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }

  @Override
  protected Class<ChatViewModel> getViewModelClass() {
    return ChatViewModel.class;
  }
}
