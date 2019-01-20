package com.pisen.router.core.filemanager.cancheinfo;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import android.studio.util.URLUtils;
import android.util.Log;

import com.pisen.router.core.monitor.entity.RouterConfig;
import com.pisen.router.core.monitor.entity.Section;

import de.aflx.sardine.DavResource;
import de.aflx.sardine.Sardine;
import de.aflx.sardine.SardineFactory;

/**
 * 缓存扫描处理<采用了生产/消息模式>
 * 
 * @author yangyp
 * @version 1.0, 2014年8月5日 上午11:50:36
 */
public class CacheThreadQueue extends Thread {

	static final String TAG = CacheThreadQueue.class.getSimpleName();
	private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
	private static final int CORE_POOL_SIZE = Math.min(CPU_COUNT * 2 + 1, 7);
	private BlockingQueue<CacheThread> queue;

	private WebDAVCacheManager cacheManager;
	private RouterConfig routerConfig;
	// 子线程引用计数器
	private Vector<CacheThread> threadCount = new Vector<CacheThread>();
	private boolean isCancelled = false;
	private Sardine sardine;

	public CacheThreadQueue(WebDAVCacheManager cacheManager, RouterConfig routerConfig) {
		super("CacheThreadQueue");
		this.cacheManager = cacheManager;
		this.routerConfig = routerConfig;
		this.queue = new ArrayBlockingQueue<CacheThread>(CORE_POOL_SIZE);
	}

	@Override
	public void run() {
		long currentTimeMillis = System.currentTimeMillis();
		isCancelled = false;

		try {
			if (routerConfig == null) {
				throw new NullPointerException("路由器配置文件 is null");
			}

			sardine = SardineFactory.begin(routerConfig.getWebdavUsername(), routerConfig.getWebdavPassword());
			for (Section section : routerConfig.sectionList) {
				String sectionUrl = routerConfig.getSectionWebdavUrl(section);
				if (sardine.exists(URLUtils.encodeURL(sectionUrl))) {
					addSubThread(sectionUrl);
				}
			}
		} catch (Exception e) {
			Log.e(TAG, "缓存数据出错cacheRunnable：", e);
		}

		// 等待子线程执行完成
		while (threadCount.size() > 0) {
		}

		if (isCancelled) {
			Log.i(TAG, "操作已取消.");
		}

		cacheManager.setCacheCompleted(true);
		Log.i(TAG, String.format("执行完成,花费了%.2f秒.", (System.currentTimeMillis() - currentTimeMillis) / 1000f));
	}

	/**
	 * 添加子线程并执行
	 * 
	 * @param webdavRoot
	 * @param path
	 */
	public void addSubThread(String url) {
		CacheThread thread = new CacheThread(this, sardine, url);
		threadCount.add(thread);
		thread.start();
	}

	/**
	 * 删除子线程
	 * 
	 * @param thread
	 */
	public void removeSubThread(CacheThread thread) {
		threadCount.remove(thread);
	}

	/**
	 * 取消线程
	 */
	public void cancel() {
		isCancelled = true;
		Log.i(TAG, "操作取消中...");
	}

	public boolean isCancelled() {
		return isCancelled;
	}

	public void putQueue(CacheThread thread) throws InterruptedException {
		queue.put(thread);
	}

	public void takeQueue() throws InterruptedException {
		queue.take();
	}

	public Vector<CacheThread> getThreadCount() {
		return threadCount;
	}

	/**
	 * 缓存子线程
	 * 
	 * @author yangyp
	 * @version 1.0, 2014年8月5日 下午12:54:34
	 */
	class CacheThread extends Thread {

		private CacheThreadQueue queueThread;
		private Sardine sardine;
		private String parentUrl;

		public CacheThread(CacheThreadQueue queueThread, Sardine sardine, String parentUrl) {
			super("CacheThread-Sub");
			this.queueThread = queueThread;
			this.sardine = sardine;
			this.parentUrl = parentUrl;
		}

		@Override
		public void run() {
			try {
				queueThread.putQueue(this);

				List<DavResource> resources = sardine.list(URLUtils.encodeURL(parentUrl));
				DavResource dir = resources.remove(0); // 删除第一个父节点级
				cacheManager.addResourceCache(parentUrl, dir);
				for (DavResource resource : resources) {
					if (queueThread.isCancelled()) {
						break;
					}

					if (resource.isDirectory()) {
						queueThread.addSubThread(String.format("%s%s/", parentUrl, resource.getName()));
					} else {
						cacheManager.addResourceCache(parentUrl + resource.getName(), resource);
					}
				}

			} catch (Exception e) {
				Log.e(TAG, "缓存数据出错cacheRunnable：" + parentUrl, e);
			} finally {
				try {
					queueThread.takeQueue();
				} catch (InterruptedException e) {
				}
				queueThread.removeSubThread(this);
			}
		}
	}
}
