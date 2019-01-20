package com.pisen.router.core.filemanager.transfer;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Binder;
import android.studio.os.LogCat;
import android.util.Log;

public class TransferProvider extends ContentProvider {

	public static final String AUTHORITY = "com.pisen.router";
	public static final Uri CONTENT_URI = Uri.parse("content://" + TransferProvider.AUTHORITY + "/transfer");
	public static final Uri TRANSFER_UPLOAD_CONTENT_URI = Uri.parse("content://" + TransferProvider.AUTHORITY + "/transfer");
	private static final int TRANSFER = 1;
	private static final int TRANSFER_ID = 2;
	private static final int TRANSFER_UPLOAD = 3;
	private static final int TRANSFER_DOWNLOAD = 4;
	private static final int TRANSFER_FLASH_SEND = 5;
	private static final int TRANSFER_FLASH_RECV = 6;
	private static final int TRANSFER_RECORD_UPLOAD = 7;
	private static final int TRANSFER_CAMERA_UPLOAD = 8;

	private static final String DOWNLOAD_LIST_TYPE = "vnd.android.cursor.dir/transfer";
	private static final String DOWNLOAD_TYPE = "vnd.android.cursor.item/transfer";

	private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		sURIMatcher.addURI(AUTHORITY, "transfer", TRANSFER);
		sURIMatcher.addURI(AUTHORITY, "transfer/#", TRANSFER_ID);
		sURIMatcher.addURI(AUTHORITY, "transfer/upload", TRANSFER_UPLOAD);
		sURIMatcher.addURI(AUTHORITY, "transfer/download", TRANSFER_DOWNLOAD);
		sURIMatcher.addURI(AUTHORITY, "transfer/flash_send", TRANSFER_FLASH_SEND);
		sURIMatcher.addURI(AUTHORITY, "transfer/flash_recv", TRANSFER_FLASH_RECV);
		sURIMatcher.addURI(AUTHORITY, "transfer/record_upload", TRANSFER_RECORD_UPLOAD);
		sURIMatcher.addURI(AUTHORITY, "transfer/camera_upload", TRANSFER_CAMERA_UPLOAD);
	}

	private TransferDbHelper mOpenHelper = null;
	private static final Uri[] BASE_URIS = new Uri[] { TransferProvider.CONTENT_URI, TransferProvider.TRANSFER_UPLOAD_CONTENT_URI };

	@Override
	public boolean onCreate() {
		Context context = getContext();
		mOpenHelper = TransferDbHelper.getInstance(context);
		Log.i("startService","oncreate...");
		//context.startService(new Intent(context, TransferService.class));
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		int match = sURIMatcher.match(uri);
		if (match == -1) {
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		Cursor ret = db.query(TransferInfo.Table.TABLE_NAME, projection, selection, selectionArgs, null, null, null);
		if (ret != null) {
			LogCat.i("created cursor " + ret + " on behalf of " + Binder.getCallingPid());
			ret.setNotificationUri(getContext().getContentResolver(), uri);
		} else {
			LogCat.i("query failed in downloads database");
		}

		return ret;
	}

	@Override
	public String getType(Uri uri) {
		int match = sURIMatcher.match(uri);
		switch (match) {
		case TRANSFER:
		case TRANSFER_UPLOAD:
		case TRANSFER_DOWNLOAD:
		case TRANSFER_FLASH_SEND:
		case TRANSFER_FLASH_RECV:
		case TRANSFER_RECORD_UPLOAD:
		case TRANSFER_CAMERA_UPLOAD: {
			return DOWNLOAD_LIST_TYPE;
		}
		case TRANSFER_ID: {
			return DOWNLOAD_TYPE;
		}
		default: {
			if (LogCat.DEBUG) {
				LogCat.e("calling getType on an unknown URI: " + uri);
			}
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int match = sURIMatcher.match(uri);
		if (match != TRANSFER) {
			LogCat.i("calling insert on an unknown/invalid URI: " + uri);
			throw new IllegalArgumentException("Unknown/Invalid URI " + uri);
		}

		long keyID = mOpenHelper.insert(values);
		if (keyID == -1) {
			LogCat.i("couldn't insert into downloads database");
			return null;
		}

		notifyContentChanged(uri, match);
		return ContentUris.withAppendedId(TransferProvider.CONTENT_URI, keyID);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int count = 0;
		int match = sURIMatcher.match(uri);
		switch (match) {
		case TRANSFER:
		case TRANSFER_UPLOAD:
		case TRANSFER_DOWNLOAD:
		case TRANSFER_FLASH_SEND:
		case TRANSFER_FLASH_RECV:
		case TRANSFER_RECORD_UPLOAD:
		case TRANSFER_CAMERA_UPLOAD:
		case TRANSFER_ID:
			count = mOpenHelper.delete(selection, selectionArgs);
			break;
		default:
			LogCat.i("deleting unknown/invalid URI: " + uri);
			throw new UnsupportedOperationException("Cannot delete URI: " + uri);
		}

		notifyContentChanged(uri, match);
		return count;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		int count = 0;
		int match = sURIMatcher.match(uri);
		switch (match) {
		case TRANSFER:
		case TRANSFER_UPLOAD:
		case TRANSFER_DOWNLOAD:
		case TRANSFER_FLASH_SEND:
		case TRANSFER_FLASH_RECV:
		case TRANSFER_RECORD_UPLOAD:
		case TRANSFER_CAMERA_UPLOAD:
		case TRANSFER_ID:
			count = mOpenHelper.update(values, selection, selectionArgs);
			break;
		default:
			LogCat.i("updating unknown/invalid URI: " + uri);
			throw new UnsupportedOperationException("Cannot update URI: " + uri);
		}

		notifyContentChanged(uri, match);
		return count;
	}

	/**
	 * Notify of a change through both URIs (/my_downloads and /all_downloads)
	 * 
	 * @param uri
	 *            either URI for the changed download(s)
	 * @param uriMatch
	 *            the match ID from {@link #sURIMatcher}
	 */
	private void notifyContentChanged(final Uri uri, int uriMatch) {
		Long downloadId = null;
		if (uriMatch == TRANSFER_ID) {
			downloadId = Long.parseLong(getDownloadIdFromUri(uri));
		}
		for (Uri uriToNotify : BASE_URIS) {
			if (downloadId != null) {
				uriToNotify = ContentUris.withAppendedId(uriToNotify, downloadId);
			}
			getContext().getContentResolver().notifyChange(uriToNotify, null);
		}
	}

	private String getDownloadIdFromUri(final Uri uri) {
		return uri.getPathSegments().get(1);
	}
}
