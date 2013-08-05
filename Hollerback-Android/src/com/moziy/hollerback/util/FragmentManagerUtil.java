package com.moziy.hollerback.util;

import java.util.HashMap;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.moziy.hollerback.R;
import com.moziy.hollerback.cache.memory.TempMemoryStore;
import com.moziy.hollerback.fragment.BaseFragment;
import com.moziy.hollerback.fragment.ConversationFragment;

public class FragmentManagerUtil {

	private HashMap<String, BaseFragment> mFragmentMap;

	public void startFragment() {

	}

	public void setFragmentAnimation() {

	}

	public void popBackFragmentStack(String fragment) {

	}

	class FragmentHolder {
		public BaseFragment fragment;
		public boolean isActivated;
		public String fragmentName;
	}

//	public static void startFragment(FragmentActivity activity, Bundle b) {
//		FragmentManager fragmentManager = activity.getSupportFragmentManager();
//		FragmentTransaction fragmentTransaction = fragmentManager
//				.beginTransaction();
//		ConversationFragment fragment = ConversationFragment.newInstance(
//				Integer.toString(TempMemoryStore.conversations.get(index)
//						.getConversation_id()), index);
//		fragmentTransaction.replace(R.id.fragment_holder, fragment);
//		fragmentTransaction.addToBackStack(ConversationFragment.class
//				.getSimpleName());
//		fragmentTransaction.commit();
//	}

}
