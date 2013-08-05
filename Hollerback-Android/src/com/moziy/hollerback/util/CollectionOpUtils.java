package com.moziy.hollerback.util;

import java.util.ArrayList;

import com.kpbird.chipsedittextlibrary.ChipsItem;
import com.moziy.hollerback.model.SortedArray;
import com.moziy.hollerback.model.UserModel;

public class CollectionOpUtils {

	public static SortedArray sortContacts(ArrayList<UserModel> users) {

		SortedArray sortedArray = new SortedArray();

		ArrayList<UserModel> recentUsers = new ArrayList<UserModel>();
		ArrayList<UserModel> hollerbackUsers = new ArrayList<UserModel>();
		ArrayList<UserModel> phoneBookUsers = new ArrayList<UserModel>();

		for (UserModel user : users) {
			if (user.isRecentUser && user.isHollerbackUser) {
				recentUsers.add(user);
			} else if (user.isHollerbackUser) {
				hollerbackUsers.add(user);
			} else {
				phoneBookUsers.add(user);
			}
		}

		sortedArray.array.addAll(recentUsers);
		sortedArray.array.addAll(hollerbackUsers);
		sortedArray.array.addAll(phoneBookUsers);

		for (UserModel user : sortedArray.array) {

			sortedArray.mUserModelHash.put(user.phone, user);
			sortedArray.sortedKeys.add(user.phone);
		}

		sortedArray.indexes.add(0);
		sortedArray.indexes.add(recentUsers.size());
		sortedArray.indexes.add(hollerbackUsers.size());

		return sortedArray;
	}

	public static ArrayList<ChipsItem> setChipItems(ArrayList<UserModel> users) {
		ArrayList<ChipsItem> chips = new ArrayList<ChipsItem>();
		for (UserModel user : users) {
			ChipsItem item = new ChipsItem();
			item.setTitle(user.getName());
			item.setUserHash(user.phone);
			chips.add(item);
		}
		return chips;

	}

}
