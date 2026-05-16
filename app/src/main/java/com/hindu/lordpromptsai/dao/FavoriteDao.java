package com.hindu.lordpromptsai.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.hindu.lordpromptsai.entity.FavoriteEntity;

import java.util.List;

@Dao
public interface FavoriteDao {

    @Query("""
        SELECT * FROM favorites ORDER BY tabOrder ASC, imageIndex ASC
    """)
    List<FavoriteEntity> getAllFavoritesSorted();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(FavoriteEntity entity);

    @Query("DELETE FROM favorites WHERE imageName = :imageName")
    void deleteByImageName(String imageName);

    @Query("SELECT * FROM favorites WHERE imageName = :imageName")
    FavoriteEntity selectByImageName(String imageName);
    
    @Query("SELECT imageName FROM favorites")
    List<String> getAllFavoriteNames();

}