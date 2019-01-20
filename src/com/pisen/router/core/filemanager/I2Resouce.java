package com.pisen.router.core.filemanager;

import java.io.InputStream;
import java.util.List;

import com.pisen.router.core.filemanager.ResourceCategory.FileType;
import com.pisen.router.core.filemanager.SortComparator.FileSort;

public interface I2Resouce {

	List<ResourceInfo> list(String path, FileSort sort);

	List<ResourceInfo> list(String path, FileType type);

	void search(String path, String word, FileType type, Callback call);

	boolean exits(String path);

	void createDir(String path);

	void delete(String path);

	void rename(String path, String newName);

	void move(String src, String dst);

	void copy(String src, String dst);

	void put(String path, InputStream inStream);

	public interface Callback {
		
		void filter(ResourceInfo resource);

		void press(ResourceInfo resource);

		void comp();
	}
}
