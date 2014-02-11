package com.chb.addon.AddonBase;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import dalvik.system.DexClassLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class AddonLoader {

	public static final String ADDON_WORK_DIR = "addon_only";
	/**
	 * Android环境
	 */
	private Context context;
	/**
	 * 扩展插件apk加载器
	 */
	private ApkLoader apkLoader;


	public AddonLoader(Context context) {
		if (null == context) throw new IllegalArgumentException("AddonLoader must be created with a not null context");
		this.context = context;
	}

	/**
	 * 获取Apk的包名
	 */
	protected String getPackageName(String apkFilePath) {
		PackageManager pm = context.getPackageManager();
		PackageInfo pi = pm.getPackageArchiveInfo(apkFilePath, 0);
		return pi.packageName;
	}

	/**
	 * 根据PackageName获取工作目录
	 *
	 * @param packageName 包名
	 */
	protected String getWorkDir(String packageName) {
		return String.format("%s/%s/w%d", context.getCacheDir().getAbsolutePath(), ADDON_WORK_DIR, packageName.hashCode());
	}

	/**
	 * 根据PackageName获取工作区Apk文件名
	 *
	 * @param packageName 包名
	 */
	protected String getWorkApkFilename(String packageName) {
		return String.format("w%d.apk", packageName.hashCode());
	}

	/**
	 * 加载扩展插件
	 *
	 * @param src           源InputStream
	 * @param indentifyName 标识名
	 * @param parent        父ClassLoader
	 */
	protected DexClassLoader loadAddon(InputStream src, String indentifyName, ClassLoader parent) throws IOException {
		String workDir = getWorkDir(indentifyName);
		String workFilename = getWorkApkFilename(indentifyName);
		updateWorkApk(src, workDir, workFilename);
		// 加载 apk
		apkLoader = new ApkLoader(workDir + File.separatorChar + workFilename, workDir, parent);
		return apkLoader.load();
	}

	/**
	 * 更新工作区apk（如果文件的size是无变化则无需更新）
	 */
	protected void updateWorkApk(InputStream src, String workDir, String workFilename) throws IOException {
		boolean needReplace = true;
		File destFile = new File(workDir + File.separatorChar + workFilename);

		// 当且仅目标apk存在并且与源apk的大小相等时，才不需要再次覆盖
		if (FileUtils.directoryExists(workDir)) {
			if (destFile.exists()) {
				FileInputStream dest = new FileInputStream(destFile);
				needReplace = dest.available() != src.available();
			}
		} else {
			FileUtils.mkDirs(workDir);
		}

		if (needReplace) {
			FileUtils.copyToFile(src, destFile, 512 * 1024);
		}
	}

	public ApkLoader getApkLoader() {
		return apkLoader;
	}

	/**
	 * 加载Assert资源中的扩展插件 (根据 Asset路径名的hash 确定工作目录)
	 */
	public DexClassLoader loadAssetAddon(String addonAssetPath, ClassLoader parent) throws IOException {
		InputStream src = context.getAssets().open(addonAssetPath);
		return loadAddon(src, addonAssetPath, parent);
	}
}
