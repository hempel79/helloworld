package com.chb.addon;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import com.chb.addon.AddonBase.AddonLoader;

public class InnerAppActivity extends FragmentActivity {
	private String mAddonLunchFragment = "com.chb.addon.otherapp.OtherFragment";
	private String mAddonApkPath = "OtherActivity.apk";

	private AssetManager asm;
	private Resources res;
	private Resources.Theme thm;
	private ClassLoader classLoader = null;
	private AddonLoader loader = new AddonLoader(this);
	private boolean keepTheme = true;


	public void onCreate(Bundle savedInstanceState) {
		try {
			// 从asset加载子进程包
			if (null == classLoader) {
				classLoader = loader.loadAssetAddon(mAddonApkPath, getClassLoader());
			}

			AssetManager am = AssetManager.class.newInstance();
			am.getClass().getMethod("addAssetPath", String.class).invoke(am, loader.getApkLoader().getApkFilePath());
			asm = am;
			Resources superRes = super.getResources();
			res = new Resources(am, superRes.getDisplayMetrics(), superRes.getConfiguration());
			thm = res.newTheme();
			thm.setTo(super.getTheme());
		} catch (Exception e) {
			e.printStackTrace();
		}

		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		FrameLayout rootView = new FrameLayout(this);
		rootView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		rootView.setId(android.R.id.primary);
		setContentView(rootView);
		keepTheme = false;

		try {
			String fragmentClass = mAddonLunchFragment;
			Fragment f = (Fragment) classLoader.loadClass(fragmentClass).newInstance();

			FragmentManager fm = getSupportFragmentManager();
			FragmentTransaction ft = fm.beginTransaction();
			ft.add(android.R.id.primary, f);
			ft.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public AssetManager getAssets() {
		return asm == null ? super.getAssets() : asm;
	}

	@Override
	public Resources getResources() {
		return res == null ? super.getResources() : res;
	}

	@Override
	public Resources.Theme getTheme() {
		return keepTheme || thm == null ? super.getTheme() : thm;
	}

	@Override
	public ClassLoader getClassLoader() {
		return classLoader == null ? super.getClassLoader() : classLoader;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	@Override
	public String getPackageResourcePath() {
		return (null == loader) ? super.getPackageResourcePath() : loader.getApkLoader().getApkFilePath();
	}
}