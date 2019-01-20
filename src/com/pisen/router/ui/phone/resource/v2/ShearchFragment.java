package com.pisen.router.ui.phone.resource.v2;

import java.util.List;

import android.app.Service;
import android.content.Context;
import android.os.Bundle;
import android.studio.os.AsyncTaskUtils;
import android.studio.os.AsyncTaskUtils.InBackgroundCallback;
import android.studio.os.AsyncTaskUtils.TaskContainer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.pisen.router.R;
import com.pisen.router.core.filemanager.IResource;
import com.pisen.router.core.filemanager.ResourceCategory.FileType;
import com.pisen.router.core.filemanager.ResourceInfo;
import com.pisen.router.core.filemanager.SardineCacheResource;
import com.pisen.router.ui.base.FragmentActivity;
import com.pisen.router.ui.base.FragmentSupport;
import com.pisen.router.ui.phone.resource.v2.category.ResourceListAdapter;

public class ShearchFragment extends FragmentSupport implements View.OnClickListener, OnItemClickListener {

	private EditText edtKeyword;
	private ImageButton btnDelete;
	private Button btnCancel;

	private TextView txtMessage;
	private ListView lstContent;
	private ResourceListAdapter resourceAdapter;

	private IResource sardineManager;
	private String parentPath;
	private FileType fileType;

	private TaskContainer taskContainer;

	public static void start(Context context, String parentPath) {
		start(context, parentPath, FileType.All);
	}

	public static void start(Context context, String parentPath, FileType type) {
		Bundle bundle = new Bundle();
		bundle.putString("path", parentPath);
		bundle.putString("type", type.name());
		FragmentActivity.startFragment(context, ShearchFragment.class, bundle);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.resource_home_file_search, container, false);
	}

	@Override
	public void onDestroyView() {
		if (taskContainer != null) {
			taskContainer.cancelRequest();
		}
		super.onDestroyView();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initViews();

		parentPath = getArguments().getString("path");
		fileType = FileType.valueOfEnum(getArguments().getString("type"));
		sardineManager = new SardineCacheResource();
		edtKeyword.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				if (s.toString().length() > 0) {
					btnDelete.setVisibility(View.VISIBLE);
					btnCancel.setText("搜索");
				} else {
					btnDelete.setVisibility(View.INVISIBLE);
					btnCancel.setText("取消");
				}
			}
		});
		edtKeyword.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					searchAnsyTask();
				}
				return false;
			}
		});
	}

	private void initViews() {
		edtKeyword = (EditText) findViewById(R.id.edtKeyword);
		btnDelete = (ImageButton) findViewById(R.id.btnDelete);
		btnCancel = (Button) findViewById(R.id.btnCancel);
		txtMessage = (TextView) findViewById(R.id.txtMessage);
		lstContent = (ListView) findViewById(R.id.lstContent);
		btnDelete.setOnClickListener(this);
		btnCancel.setOnClickListener(this);

		btnDelete.setVisibility(View.INVISIBLE);
		txtMessage.setVisibility(View.GONE);
		txtMessage.setText("搜索中,请稍候...");

		resourceAdapter = new ResourceListAdapter(getActivity());
		resourceAdapter.setOnItemClickListener(this);
		lstContent.setAdapter(resourceAdapter);
	}

	public void searchAnsyTask() {
		showLoadingView();
		resourceAdapter.clear();
		taskContainer = AsyncTaskUtils.execute(new InBackgroundCallback<List<ResourceInfo>>() {
			@Override
			public List<ResourceInfo> doInBackground() {
				try {
					return sardineManager.search(parentPath, edtKeyword.getText().toString(), fileType, null);
				} catch (Exception e) {
				}
				return null;
			}

			@Override
			public void onPostExecute(List<ResourceInfo> result) {
				if (result.isEmpty()) {
					txtMessage.setText("没有搜索到相关内容");
					resourceAdapter.notifyDataSetChanged();
				} else {
					txtMessage.setVisibility(View.GONE);
					resourceAdapter.setData(result);
					resourceAdapter.notifyDataSetChanged();
				}
			}
		});
	}

	private void showLoadingView() {
		txtMessage.setVisibility(View.VISIBLE);
		txtMessage.setText("搜索中,请稍候...");
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnCancel:
			if (edtKeyword.getText().toString().length() > 0) {
				searchAnsyTask();
			} else {
				// finish();
				popBackStack();
			}
			break;
		case R.id.btnDelete:
			edtKeyword.setText(null);
			btnDelete.setVisibility(View.INVISIBLE);
			btnCancel.setText("取消");
			break;
		}
		hideSoftInputFromWindow(edtKeyword);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		ResourceInfo item = resourceAdapter.getItem(position);
		if (item.isDirectory) {

		} else {
			ResourceInfo.doOpenFile(getActivity(), item);
		}
	}

	/**
	 * @des 隐藏软键盘
	 * @param et
	 */
	private void hideSoftInputFromWindow(EditText et) {
		final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Service.INPUT_METHOD_SERVICE);
		if (imm.isActive()) {
			imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
		}
	}
}
