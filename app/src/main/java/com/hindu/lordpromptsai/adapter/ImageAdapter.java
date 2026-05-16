package com.hindu.lordpromptsai.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.hindu.lordpromptsai.R;
import com.hindu.lordpromptsai.activities.ImageDetailActivity;
import com.hindu.lordpromptsai.dao.FavoriteDao;
import com.hindu.lordpromptsai.entity.FavoriteEntity;
import com.hindu.lordpromptsai.repository.AppDatabase;
import com.hindu.lordpromptsai.util.AppExecutors;
import com.hindu.lordpromptsai.util.UtilityHelper;

import java.util.ArrayList;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    // 🔥 Payload constant (VERY IMPORTANT)
    public static final int PAYLOAD_FAVORITE = 1001;

    public Runnable onFavoriteChanged;
    private List<ImageItem> imageList;
    private final Context context;
    private final ActivityResultLauncher<Intent> launcher;
    private String tabTitle;
    private boolean isFavoritesTab;
    private boolean isLaunching = false;

    public ImageAdapter(
            Context context,
            List<ImageItem> images,
            ActivityResultLauncher<Intent> launcher,
            String tabTitle
    ) {
        this.context = context;
        this.imageList = images != null ? images : new ArrayList<>();
        this.launcher = launcher;
        this.tabTitle = tabTitle;
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image, parent, false);
        return new ViewHolder(view);
    }

    // 🔥 PAYLOAD-AWARE BIND (ONLY UPDATE HEART ICON)
    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position,
            @NonNull List<Object> payloads
    ) {
        if (!payloads.isEmpty() && payloads.contains(PAYLOAD_FAVORITE)) {
            ImageItem item = imageList.get(position);
            holder.favButton.setImageResource(
                    item.isFavorite()
                            ? R.drawable.ic_favorite
                            : R.drawable.ic_favorite_border
            );
            return; // ❌ STOP full bind
        }
        super.onBindViewHolder(holder, position, payloads);
    }


    public boolean isTabFavorite() {
        return isFavoritesTab;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {

        ImageItem imageItem = imageList.get(position);

        // IMAGE
        holder.imageButton.setImageResource(imageItem.getImageResId());

        // TAB MODE
        if (isFavoritesTab) {
            holder.favButton.setVisibility(View.GONE);
            holder.btnRemove.setVisibility(View.VISIBLE);
        } else {
            holder.favButton.setVisibility(View.VISIBLE);
            holder.btnRemove.setVisibility(View.GONE);
            holder.favButton.setImageResource(
                    imageItem.isFavorite()
                            ? R.drawable.ic_favorite
                            : R.drawable.ic_favorite_border
            );
        }

        // OPEN DETAIL PAGE
        holder.imageButton.setOnClickListener(v -> {

            if (launcher == null) return;

            if (isLaunching) {
                return;
            }
              isLaunching = true;

            Intent intent = new Intent(context, ImageDetailActivity.class);
            intent.putExtra("position", position);
            intent.putParcelableArrayListExtra(
                    "imageList",
                    new ArrayList<>(imageList)
            );

            if ("Favorites".equalsIgnoreCase(tabTitle)) {
                intent.putExtra(
                        "tabTitle",
                        getFormattedTitle(imageItem.getImageName())
                );
            } else {
                intent.putExtra("tabTitle", tabTitle);
            }

            launcher.launch(intent);

            v.postDelayed(() -> isLaunching = false, 400);
        });

        // PRESS EFFECT ONLY (NO ENTRY ANIMATION)
        holder.itemView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.startAnimation(
                        AnimationUtils.loadAnimation(context, R.anim.card_press)
                );
            } else if (event.getAction() == MotionEvent.ACTION_UP
                    || event.getAction() == MotionEvent.ACTION_CANCEL) {
                v.startAnimation(
                        AnimationUtils.loadAnimation(context, R.anim.card_release)
                );
            }
            return false;
        });

        // ❌ REMOVE FROM FAVORITES (FINAL + CORRECT)
        holder.btnRemove.setOnClickListener(v -> {

            int adapterPosition = holder.getBindingAdapterPosition();
            if (adapterPosition == RecyclerView.NO_POSITION) return;

            ImageItem removedItem = imageList.get(adapterPosition);

            removedItem.setFavorite(false);
            imageList.remove(adapterPosition);
            notifyItemRemoved(adapterPosition);

            AppExecutors.IO.execute(() -> {
                AppDatabase.get(context)
                        .favoriteDao()
                        .deleteByImageName(removedItem.getImageName());

                if (onFavoriteChanged != null) {
                    ((Activity) context).runOnUiThread(onFavoriteChanged);
                }
            });

            v.performHapticFeedback(
                    android.view.HapticFeedbackConstants.VIRTUAL_KEY
            );
        });

        // ❤️ FAVORITE TOGGLE (NO BLINK, ICON ONLY)
        holder.favButton.setOnClickListener(v -> {

            int adapterPosition = holder.getBindingAdapterPosition();
            if (adapterPosition == RecyclerView.NO_POSITION) return;

            boolean currentlyFavorite = imageItem.isFavorite();

            // 🎯 Update MODEL first
            imageItem.setFavorite(!currentlyFavorite);

            // 🎯 Update ICON only (payload)
            notifyItemChanged(adapterPosition, PAYLOAD_FAVORITE);

            v.performHapticFeedback(
                    android.view.HapticFeedbackConstants.VIRTUAL_KEY
            );

            holder.favButton.startAnimation(holder.favAnim);

            playFavoriteCardAnimation(holder.cardRoot);

            // 🔥 DB update async
            AppExecutors.IO.execute(() -> {

                FavoriteDao dao = AppDatabase.get(context).favoriteDao();

                if (currentlyFavorite) {
                    dao.deleteByImageName(imageItem.getImageName());
                } else {
                    String tabName = getFormattedTitle(imageItem.getImageName());
                    dao.insert(new FavoriteEntity(
                            imageItem.getImageName(),
                            tabName,
                            UtilityHelper.getTabOrder(tabName),
                            UtilityHelper.getImageIndex(imageItem.getImageName()),
                            String.valueOf(imageItem.getImageResId()),
                            imageItem.getDescription(),
                            true
                    ));
                }

                if (onFavoriteChanged != null) {
                    ((Activity) context).runOnUiThread(onFavoriteChanged);
                }
            });
        });

        // TUTORIAL OVERLAY
        if (position == 0) {
            showPulse(holder.tutorialOverlay);
        } else {
            holder.tutorialOverlay.setVisibility(View.GONE);
        }
    }

    private String getFormattedTitle(String imageName) {
        if (imageName == null || imageName.isEmpty()) return "";
        String base = imageName.split("_")[0];
        return base.substring(0, 1).toUpperCase() + base.substring(1);
    }

    @Override
    public long getItemId(int position) {
        return imageList.get(position).getImageName().hashCode();
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    // 🔥 FULL RELOAD ONLY FOR TAB SWITCH
    @SuppressLint("NotifyDataSetChanged")
    public void updateImages(List<ImageItem> newImages) {
        imageList = newImages != null ? newImages : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setTabTitle(String tabTitle) {
        this.tabTitle = tabTitle;
    }

    public void setFavoritesTab(boolean isFavoritesTab) {
        this.isFavoritesTab = isFavoritesTab;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageButton imageButton;
        ImageButton favButton;
        ImageButton btnRemove;
        CardView cardRoot;
        View tutorialOverlay;
        Animation favAnim;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            favAnim = AnimationUtils.loadAnimation(
                    itemView.getContext(),
                    R.anim.fav_pop
            );
            imageButton = itemView.findViewById(R.id.imageButton);
            favButton = itemView.findViewById(R.id.favButton);
            btnRemove = itemView.findViewById(R.id.btnRemove);
            cardRoot = itemView.findViewById(R.id.cardRoot);
            tutorialOverlay = itemView.findViewById(R.id.cardPulseOverlay);
        }
    }

    private void showPulse(View overlay) {
        if (overlay == null) return;
        overlay.setVisibility(View.VISIBLE);
        overlay.setAlpha(0f);
        overlay.animate()
                .alpha(1f)
                .setDuration(500)
                .withEndAction(() ->
                        overlay.animate()
                                .alpha(0f)
                                .setDuration(500)
                                .withEndAction(() ->
                                        overlay.setVisibility(View.GONE)
                                )
                );
    }

    private void playFavoriteCardAnimation(View cardView) {
        if (cardView == null) return;
        cardView.animate()
                .scaleX(1.04f)
                .scaleY(1.04f)
                .alpha(0.96f)
                .setDuration(120)
                .withEndAction(cardView.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .alpha(1f)
                        .setDuration(150)::start)
                .start();
    }
}