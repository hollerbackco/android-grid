package com.moziy.hollerback.model;

import com.moziy.hollerback.debug.LogUtil;

public class Country {
	@Override
	public boolean equals(Object o) {

		if (o != null && o instanceof Country) {
			if (((Country) o).code.equals(code)) {
				return true;
			}
		}

		return false;
	}

	public String iso;

	public String code;

	public String name;

	public Country(String iso, String code, String name) {
		this.iso = iso;
		this.code = code;
		this.name = name;
	}

	public String toString() {
		return iso + " - " + code + " - " + name.toUpperCase();
	}
}