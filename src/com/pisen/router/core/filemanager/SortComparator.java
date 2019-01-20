package com.pisen.router.core.filemanager;

import java.util.Comparator;

/**
 * 实现collection集合中的comparator接口。 目前有： 名称升序，降序 文件最后修改时间升序，降序
 * 
 * @author mugabutie
 *
 */
public class SortComparator implements Comparator<ResourceInfo> {
	public static enum FileSort {
		NONE, //
		NAME_ASC, // 名称升序
		NAME_DESC, // 名称降序
		DATA_ASC, // 时间升序
		DATA_DESC; // 时间降序
		public static FileSort valueOfEnum(String sort) {
			for (FileSort fs : FileSort.values()) {
				if (fs.name().equalsIgnoreCase(sort)) {
					return fs;
				}
			}
			return NONE;
		}

		public FileSort nextShort() {
			switch (valueOfEnum(name())) {
			case NAME_ASC:
				// case NAME_DESC:
				return DATA_DESC;
				// case DATA_ASC:
			case DATA_DESC:
				return NAME_ASC;
			default:
				return NAME_ASC;
			}
		}
	}

	private FileSort sortField;

	public SortComparator(FileSort sort) {
		this.sortField = sort;
	}

	@Override
	public int compare(ResourceInfo file1, ResourceInfo file2) {
		if (file1.isDirectory && !file2.isDirectory) {
			return -1;
		}

		if (!file1.isDirectory && file2.isDirectory) {
			return 1;
		}

		if (file1.isDirectory && file2.isDirectory) {
			if (sortField == FileSort.NAME_ASC)
				return file1.path.compareToIgnoreCase(file2.path);
			else if (sortField == FileSort.DATA_DESC)
				return compare2(file1.lastModified, file2.lastModified);
		}

		switch (sortField) {
		case NAME_ASC:
			return file1.name.compareToIgnoreCase(file2.name);
		case NAME_DESC:
			return file2.name.compareToIgnoreCase(file1.name);
		case DATA_ASC:
			return compare(file1.lastModified, file2.lastModified);
		case DATA_DESC:
			return compare2(file1.lastModified, file2.lastModified);
		default:
			return 0;
		}

	}

	/**
	 * 时间对比
	 * 
	 * @param lhs
	 * @param rhs
	 * @return
	 */
	public static int compare(long lhs, long rhs) {
		return lhs < rhs ? -1 : (lhs == rhs ? 0 : 1);
	}

	public static int compare2(long lhs, long rhs) {
		return lhs > rhs ? -1 : (lhs == rhs ? 0 : 1);
	}
}
