package com.pisen.router.core.filemanager;

import java.io.InputStream;
import java.util.List;

import org.apache.http.entity.InputStreamEntity;

import com.pisen.router.core.filemanager.ResourceCategory.FileType;
import com.pisen.router.core.filemanager.SortComparator.FileSort;

import de.aflx.sardine.impl.handler.VoidResponseHandler;

public interface IResource {

	/**
	 * 获取给定目录下所有目录与文件
	 * 
	 * @param dir
	 * 
	 * @return
	 * @throws Exception 
	 */
	List<ResourceInfo> list(String dir) throws Exception;

	/**
	 * 获取给定目录下所有目录不包括文件
	 * 
	 * @param dir
	 * @return
	 */
	List<ResourceInfo> listFileChooser(String dir);

	/**
	 * 获取当前目录下所有文件分类(如果是All，那么只查询当前目录下所有文件与目录)
	 * 
	 * @param dir
	 * @param type
	 * @return
	 */
	List<ResourceInfo> listRecursively(String dir, FileType type);

	/**
	 * 查询给定目标，当前路径以及子目录都会查询。
	 * 
	 * @param dir
	 * @param keyword
	 * @param callback
	 *            可为NULl
	 * @return
	 * @throws Exception
	 */
	List<ResourceInfo> search(String dir, String keyword, FileType type, SearchCallback callback) throws Exception;

	/**
	 * 文件排序，当前支持文件名称，文件最后修改时间排序。
	 * 
	 * @param ResList
	 * @param sortType
	 */
	List<ResourceInfo> sort(List<ResourceInfo> dataSource, FileSort sortType);

	/**
	 * 判断当前路径是否存在
	 * 
	 * @param path
	 * @return
	 * @throws Exception
	 */
	boolean exists(String path) throws Exception;

	/**
	 * 创建路径目录
	 * 
	 * @param path
	 * @throws Exception
	 */
	void createDir(String path) throws Exception;

	/**
	 * 获取输入流，输入流可以是网络流，本地流
	 * 
	 * @param path
	 * @return
	 * @throws Exception
	 */
	InputStream get(String path) throws Exception;

	/**
	 * 通过inputstream流写入到path路径。
	 * 
	 * @param path
	 *            ： 必须是一个具体的文件，不能是文件夹，否则抛出异常
	 * @param inStream
	 * @throws Exception
	 */
	void put(String path, InputStream inStream) throws Exception;

	/**
	 * 与上面类同
	 * 
	 * @param path
	 * @param inStream
	 * @throws Exception
	 */
	void put(String path, InputStreamEntity inStream) throws Exception;

	/**
	 * 增加Content-Length,Date字段
	 * @param path
	 * @param inStream
	 * @param responseHandler
	 * @throws Exception
	 */
	public void put(String path, InputStreamEntity inStream,VoidResponseHandler responseHandler) throws Exception;

	/**
	 * 文件复制
	 * 
	 * @param sourcePath
	 * @param targetPath
	 * @throws Exception
	 */
	void copy(String sourcePath, String targetPath) throws Exception;

	/**
	 * 移动，移动之后会删除原始文件（文件，文件夹）
	 * 
	 * @param sourcePath
	 * @param targetPath
	 * @throws Exception
	 */
	void move(String sourcePath, String targetPath) throws Exception;

	/**
	 * 删除（文件，文件夹）
	 * 
	 * @param path
	 * @throws Exception
	 */
	void delete(String path) throws Exception;

	/**
	 * 重命名（文件，文件夹）
	 * 
	 * @param sourcePath
	 * @param newName
	 * @throws Exception
	 */
	void rename(String sourcePath, String newName) throws Exception;

	public interface SearchCallback {
		void onSearch(ResourceInfo resource);
	}
}
