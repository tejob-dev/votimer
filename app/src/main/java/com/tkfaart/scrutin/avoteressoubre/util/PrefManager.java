package com.tkfaart.scrutin.avoteressoubre.util;

/**
 * Created by Tamim on 30/09/2017.
 */

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;


public class PrefManager {

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;

    // shared pref mode
    int PRIVATE_MODE = 0;

    // Shared preferences file name
    private static final String PREF_NAME = "scrutin_app";

    private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";

    public PrefManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void setArray(String PREF_NAME, Set<String> val) {
        editor.putStringSet(PREF_NAME, val);
        editor.commit();
    }

    public void setBoolean(String PREF_NAME,Boolean val) {
        editor.putBoolean(PREF_NAME, val).apply();
        editor.commit();
    }
    public void setString(String PREF_NAME,String VAL) {
        editor.putString(PREF_NAME, VAL);
        editor.commit();
    }
    public void setInt(String PREF_NAME,int VAL) {
        editor.putInt(PREF_NAME, VAL);
        editor.commit();
    }
    public boolean getBoolean(String PREF_NAME) {
        return pref.getBoolean(PREF_NAME,false);
    }
    public void remove(String PREF_NAME){
        if(pref.contains(PREF_NAME)){
            editor.remove(PREF_NAME);
            editor.commit();
        }
    }
    public String getString(String PREF_NAME) {
        if(pref.contains(PREF_NAME)){

            return pref.getString(PREF_NAME,null);
        }
        return  "";
    }

    public int getInt(String key) {
        return pref.getInt(key,0);
    }

    public void setStringForTok(String tokenner, String token_of_app) {
        editor.putString(tokenner, token_of_app);
        editor.apply();
        editor.commit();
    }

    public void setValueByName(String key_v, String value) {
        editor.putString(key_v,value).apply();
        editor.commit();
    }

    public void setNumbValueByName(String key_v, int value) {
        editor.putInt(key_v,value).apply();
        editor.commit();
    }

    public void setBoolByName(String key_v, boolean value) {
        editor.putBoolean(key_v,value).apply();
        editor.commit();
    }

    public boolean getBoolByName(String key_v) {
        return pref.getBoolean(key_v, false);
    }

    public Set<String> getArray(String key_array) {
        return pref.getStringSet(key_array, null);
    }

    public String getValueByName(String key_v) {
        return pref.getString(key_v,"");
    }

    public int getNumbValueByName(String key_v) {
        return pref.getInt(key_v,0);
    }

    public boolean getBooleanSpecial(String is_plug_install) {
        return pref.getBoolean(is_plug_install, true);
    }
}
