package com.hindu.lordpromptsai.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "favorites")
public class FavoriteEntity {

    @PrimaryKey
    @NonNull
    private String imageName;      // ganesha_1

    private String tabName;        // Ganesha
    private int tabOrder;          // Krishna=0, Shiva=1, Ganesha=2
    private int imageIndex;        // 1,2,3...

    private String imageResId;
    private String description;
    private boolean isFavorite;

    // ---- constructor ----
    public FavoriteEntity(
            @NonNull String imageName,
            String tabName,
            int tabOrder,
            int imageIndex,
            String imageResId,
            String description,
            boolean isFavorite
    ) {
        this.imageName = imageName;
        this.tabName = tabName;
        this.tabOrder = tabOrder;
        this.imageIndex = imageIndex;
        this.imageResId = imageResId;
        this.description = description;
        this.isFavorite = isFavorite;
    }

    // ---- getters ----
    @NonNull public String getImageName() { return imageName; }
    public String getTabName() { return tabName; }
    public int getTabOrder() { return tabOrder; }
    public int getImageIndex() { return imageIndex; }
    public String getImageResId() { return imageResId; }
    public String getDescription() { return description; }
    public boolean isFavorite() { return isFavorite; }
}