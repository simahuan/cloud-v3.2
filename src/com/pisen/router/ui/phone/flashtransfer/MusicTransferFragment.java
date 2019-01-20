package com.pisen.router.ui.phone.flashtransfer;

import java.util.List;

import android.os.Environment;

import com.pisen.router.R;
import com.pisen.router.core.filemanager.LocalResourceManager;
import com.pisen.router.core.filemanager.ResourceInfo;
import com.pisen.router.core.filemanager.ResourceCategory.FileType;
import com.pisen.router.core.flashtransfer.FlashTransferManager;

public class MusicTransferFragment extends FlashTransferListFragment {

	@Override
	public String getFragmentTitle() {
		return "音乐";
	}

	@Override
	public String getTypeDescription() {
		return getResources().getString(R.string.flashtransfer_count_music);
	}

	@Override
	public String getRefreshAction() {
		return FlashTransferManager.ACTION_TRANSFER_COMPLETE_RECEIVE_MUSIC;
	}

	@Override
	public List<ResourceInfo> getData() {
		LocalResourceManager rm = new LocalResourceManager(getActivity());
		return rm.listRecursively(Environment.getExternalStorageDirectory().getAbsolutePath(), FileType.Audio);
	}
}
