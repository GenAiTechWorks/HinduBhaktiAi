package com.hindu.lordpromptsai.repository;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.hindu.lordpromptsai.dao.FavoriteDao;
import com.hindu.lordpromptsai.entity.FavoriteEntity;

@Database(
        entities = {FavoriteEntity.class},
        version = 2,               // 🔥 INCREMENT VERSION
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract FavoriteDao favoriteDao();

    public static AppDatabase get(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "lord_prompts_db"
                            )
                            // 🔥 CRITICAL FIX
                            .fallbackToDestructiveMigration()

                            // ⚠️ Keep only because data is small
                            .allowMainThreadQueries()

                            .build();
                }
            }
        }
        return INSTANCE;
    }
}