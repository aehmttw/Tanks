package utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonUtils
{
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
}
