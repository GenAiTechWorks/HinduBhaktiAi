package com.hindu.lordpromptsai.adapter;
import android.os.Parcel;
import android.os.Parcelable;

public class ImageItem implements Parcelable {

    private final int imageResId;
    private final String imageName;

    public String getImageName() {
        return imageName;
    }

    private final String description;
    private boolean isFavorite;

    private boolean promptGenerated;
    private String generatedPrompt;

    public ImageItem(int imageResId, String imageName, String description) {
        this.imageResId = imageResId;
        this.imageName = imageName;
        this.description = description;
        this.isFavorite = false;
    }

    public ImageItem(int imageResId, String imageName, String description, boolean isFavorite) {
        this.imageResId = imageResId;
        this.imageName = imageName;
        this.description = description;
        this.isFavorite = isFavorite;
    }

    protected ImageItem(Parcel in) {
        imageResId = in.readInt();
        imageName = in.readString();
        description = in.readString();
        isFavorite = in.readByte() != 0;
    }

    public static final Creator<ImageItem> CREATOR = new Creator<>() {
        @Override
        public ImageItem createFromParcel(Parcel in) {
            return new ImageItem(in);
        }

        @Override
        public ImageItem[] newArray(int size) {
            return new ImageItem[size];
        }
    };


    public boolean isPromptGenerated() {
        return promptGenerated;
    }

    public void setPromptGenerated(boolean promptGenerated) {
        this.promptGenerated = promptGenerated;
    }

    public String getGeneratedPrompt() {
        return generatedPrompt;
    }

    public void setGeneratedPrompt(String generatedPrompt) {
        this.generatedPrompt = generatedPrompt;
    }


    public int getImageResId() {
        return imageResId;
    }

    public String getDescription() {
        return description;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(imageResId);
        dest.writeString(imageName);
        dest.writeString(description);
        dest.writeByte((byte) (isFavorite ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }
}