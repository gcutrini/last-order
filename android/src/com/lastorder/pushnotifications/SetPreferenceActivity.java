package com.lastorder.pushnotifications;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

public class SetPreferenceActivity extends PreferenceActivity {

	
	protected Method mLoadHeaders = null;
    protected Method mHasHeaders = null;
 
    /**
     * Checks to see if using new v11+ way of handling PrefsFragments.
     * @return Returns false pre-v11, else checks to see if using headers.
     */
    public boolean isNewV11Prefs() {
        if (mHasHeaders!=null && mLoadHeaders!=null) {
            try {
                return (Boolean)mHasHeaders.invoke(this);
            } catch (IllegalArgumentException e) {
            } catch (IllegalAccessException e) {
            } catch (InvocationTargetException e) {
            }
        }
        return false;
    }
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		//onBuildHeaders() will be called during super.onCreate()
        try {
            mLoadHeaders = getClass().getMethod("loadHeadersFromResource", int.class, List.class );
            mHasHeaders = getClass().getMethod("hasHeaders");
        } catch (NoSuchMethodException e) {
        }
        super.onCreate(savedInstanceState);
        if (!isNewV11Prefs()) {
            addPreferencesFromResource(R.xml.app_prefs_cat1);
           // addPreferencesFromResource(R.xml.app_prefs_cat2);
          //  addPreferencesFromResource(R.xml.app_prefs_cat3);
        }
	}
	
	@Override
    public void onBuildHeaders(List<Header> aTarget) {
        try {
            mLoadHeaders.invoke(this,new Object[]{R.xml.prefs_headers,aTarget});
        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        }   
    }
 
    @SuppressLint("NewApi")
	static public class PrefsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle aSavedState) {
            super.onCreate(aSavedState);
            Context anAct = getActivity().getApplicationContext();
            int thePrefRes = anAct.getResources().getIdentifier(getArguments().getString("pref-resource"),
                    "xml",anAct.getPackageName());
            addPreferencesFromResource(thePrefRes);
        }
    }

}
