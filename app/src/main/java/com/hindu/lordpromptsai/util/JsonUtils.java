package com.hindu.lordpromptsai.util;

import android.content.Context;

import com.hindu.lordpromptsai.adapter.ImageItem;
import com.hindu.lordpromptsai.dao.FavoriteDao;
import com.hindu.lordpromptsai.entity.FavoriteEntity;
import com.hindu.lordpromptsai.repository.AppDatabase;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class JsonUtils {

    public static List<TabData> loadTabsFromJson(Context context) {

        List<TabData> tabList = new ArrayList<>();

        try {
            InputStream is = context.getAssets().open("HinduPromptsJsonFile.json");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();

            String json = new String(buffer, StandardCharsets.UTF_8);
            JSONObject root = new JSONObject(json);
            JSONArray tabs = root.getJSONArray("tabs");
            AppDatabase db = AppDatabase.get(context);
            FavoriteDao dao = db.favoriteDao();

            List<ImageItem> favImages = new ArrayList<>();
            for (int i = 0; i < tabs.length(); i++) {

                JSONObject tabObj = tabs.getJSONObject(i);
                String title = tabObj.getString("title");

                JSONArray imagesArray = tabObj.getJSONArray("images");
                List<ImageItem> images = new ArrayList<>();

                for (int j = 0; j < imagesArray.length(); j++) {

                    JSONObject imgObj = imagesArray.getJSONObject(j);

                    String imageName = imgObj.getString("imageName");
                    String description = imgObj.getString("description");
                   // boolean isFavorite = imgObj.optBoolean("isFavorite", false);
                    int resId = context.getResources()
                            .getIdentifier(imageName, "drawable", context.getPackageName());

                    ImageItem item = new ImageItem(resId, imageName, description);
                    item.setFavorite(false);
                    FavoriteEntity entity = dao.selectByImageName(imageName);
                    if(entity!= null && entity.isFavorite()){
                        item.setFavorite(entity.isFavorite());
                        favImages.add(item);
                    }

                    images.add(item);
                }

                tabList.add(new TabData(title, images));
            }
            tabList.add(new TabData("Favorites", favImages));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return tabList;
    }
}