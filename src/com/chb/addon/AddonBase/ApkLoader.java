package com.chb.addon.AddonBase;

import dalvik.system.DexClassLoader;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ApkLoader {
	/**
	 * odex优化目录文件夹名
	 */
	public static final String DEX_CACHE_DIR = "dex";
	/**
	 * 运行期动态库目录名
	 */
	public static final String LIB_CACHE_DIR = "lib";

	public String getApkFilePath() {
		return apkFilePath;
	}

	/**
	 * Apk包运行期文件
	 */
	private final String apkFilePath;
	/**
	 * odex优化目录
	 */
	private final String dexDirPath;
	/**
	 * 运行期动态库目录
	 */
	private final String libDirPath;
	/**
	 * 运行期动态库目录
	 */
	private final ClassLoader parentClassLoader;
	/**
	 * 类加载器
	 */
	private DexClassLoader dexClassLoader = null;

	/**
	 * 构造方法
	 *
	 * @param apkFilePath 运行期apk文件
	 * @param workDir     工作目录（将会在此目录中创建lib、dex子目录，用来存在运行期 so库、odex优化文件）
	 * @param parent      父类加载器
	 */
	public ApkLoader(String apkFilePath, String workDir, ClassLoader parent) {
		// create dex and lib cache directory
		this.apkFilePath = apkFilePath;
		dexDirPath = workDir + File.separatorChar + DEX_CACHE_DIR;
		libDirPath = workDir + File.separatorChar + LIB_CACHE_DIR;
		parentClassLoader = parent;
	}

	/**
	 * 将zip中文件解压到指定目录
	 */
	public static void decompressZipFile(ZipInputStream zipInputStream, ZipEntry entry, String destPath) throws IOException {
		String entryName = entry.getName();
		String fileName = entryName.substring(entryName.lastIndexOf("/") + 1);
		File outFile = new File(destPath, fileName);
		outFile.createNewFile();
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outFile));

		int count = 0;
		byte buffer[] = new byte[2048];
		while ((count = zipInputStream.read(buffer)) > 0) {
			bos.write(buffer, 0, count);
		}

		bos.flush();
		bos.close();
	}

	/**
	 * 创建工作目录，并加载包内 .so/dex
	 *
	 * @return apk包类加载器
	 * @throws java.io.IOException apkFile不存在、创建lib或dex失败，都会导致异常抛出
	 */
	public DexClassLoader load() throws IOException {
		if (null == dexClassLoader) {
			FileUtils.mkDirs(dexDirPath);
			FileUtils.mkDirs(libDirPath);

			extractLibraries();

			// create class loader
			dexClassLoader = new DexClassLoader(apkFilePath, dexDirPath, libDirPath, parentClassLoader);
		}
		return dexClassLoader;
	}

	/**
	 * 从apk中提取出 .so 库到运行期动态库目录
	 */
	private void extractLibraries() throws IOException {
		FileInputStream fis = new FileInputStream(new File(apkFilePath));
		ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
		ZipEntry entry = null;
		while (null != (entry = zis.getNextEntry())) {
			if (entry.getName().startsWith("lib/")) {
				decompressZipFile(zis, entry, libDirPath);
			}
		}
	}

	public DexClassLoader getClassLoader() {
		return dexClassLoader;
	}
}
