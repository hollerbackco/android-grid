package com.moziy.hollerback.model;

import java.util.ArrayList;
import java.util.HashMap;

public class SortedArray {

	public ArrayList<Integer> indexes;
	public ArrayList<UserModel> array;
	public ArrayList<String> sortedKeys;
	public HashMap<String, UserModel> mUserModelHash;

	public SortedArray() {
		indexes = new ArrayList<Integer>();
		array = new ArrayList<UserModel>();
		sortedKeys = new ArrayList<String>();
		mUserModelHash = new HashMap<String, UserModel>();

	}

}
