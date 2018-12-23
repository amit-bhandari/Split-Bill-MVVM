package com.splitbill.amit.splitbill.repo;

import androidx.room.TypeConverter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class Converter {

    @TypeConverter
    public static List<UserMoneyComposite> from(String value) {
        Type listType = new TypeToken<List<UserMoneyComposite>>() {}.getType();
        return new Gson().fromJson(value, listType);
    }

    @TypeConverter
    public static  String to(List<UserMoneyComposite> value) {
        Gson gson = new Gson();
        return gson.toJson(value);
    }

}
