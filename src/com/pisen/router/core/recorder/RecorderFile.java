package com.pisen.router.core.recorder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.os.Environment;
import android.util.Log;
/**
 * 录音文件
 */
public class RecorderFile {
	public static final String FILE_EXTENSION_AMR = ".amr"; //文件后缀
	public static File saveDirectory = Environment.getExternalStoragePublicDirectory("PisenRouter/testRecord"); //文件保存目录
	// 记录需要合成的几段amr语音文件
	public List<String> recordingList;
	//临时录音文件
    public File recordFile; 
    //临时文件开始时间
    public long recordStart = 0; 
    //临时文件录音长度（时间秒）
    public int recordLength = 0; 
    //最终录音文件
    public  File finalFile;
    //最终录音文件长度
    public int finalLength = 0;
    
    public RecorderFile(){
    	this.recordingList = new ArrayList<String>();
    }
    
    /**
     *获得录音文件 
     */
	public static File getRecorderFile() {
		SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
		String filename = String.format("Rec_%s", dateTimeFormat.format(new Date()));
		if(!saveDirectory.exists()){
			saveDirectory.mkdirs();
		}
		File file = new File(saveDirectory, filename + FILE_EXTENSION_AMR);
		if(!file.exists()){
			try {
				file.createNewFile();
			} catch (IOException e) {
				Log.i("testMsg","create file failed");
				e.printStackTrace();
			}
		}
		return file;
	}

	public void reset(){
		deleteListRecord(true);
		recordStart = 0;
		recordLength = 0;
		finalLength = 0;
		this.recordingList = new ArrayList<String>();
	}
	
	
	/**
	 * 合成录音文件:每次暂停录音会生成一个录音文件，录音结束后需要把这些录音文件合成一个文件
	 */
	public String getInputCollection( boolean isAddLastRecord) {

		SimpleDateFormat dateTimeFormat = new SimpleDateFormat(
				"yyyyMMddHHmmss", Locale.getDefault());
		String filename = String.format("Rec_%s",
				dateTimeFormat.format(new Date()));
		// 创建音频文件,合并的文件放这里
		File file1 = new File(saveDirectory, filename + FILE_EXTENSION_AMR);
		FileOutputStream fileOutputStream = null;

		if (!file1.exists()) {
			try {
				file1.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			fileOutputStream = new FileOutputStream(file1);

		} catch (IOException e) {
			e.printStackTrace();
		}
		// list里面为暂停录音 所产生的 几段录音文件的名字，中间几段文件的减去前面的6个字节头文件
		for (int i = 0; i < recordingList.size(); i++) {
			File file = new File((String) recordingList.get(i));
			Log.d("list的长度", recordingList.size() + "");
			try {
				FileInputStream fileInputStream = new FileInputStream(file);
				byte[] myByte = new byte[fileInputStream.available()];
				// 文件长度
				
				int length = myByte.length;
				if(file.length()<=0)
				{
					
				}else
				{
				// 头文件
				if (i == 0) {
					 while (fileInputStream.read(myByte) != -1) {
						fileOutputStream.write(myByte, 0, length);
					}
				}

				// 之后的文件，去掉头文件就可以了
				else {
					while (fileInputStream.read(myByte) != -1) {
						fileOutputStream.write(myByte, 6, length - 6);
					}
				}
				}
				fileOutputStream.flush();
				fileInputStream.close();
				

			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		// 结束后关闭流
		try {
			fileOutputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// 合成一个文件后，删除之前暂停录音所保存的零碎合成文件
		deleteListRecord(isAddLastRecord);
		return file1.getAbsolutePath();

	}
	
	/**
	 * 删除临时文件列表
	 * 
	 * @param isDeleteRecord 是否删除录音文件
	 */
	private void deleteListRecord(boolean isDeleteRecord) {
		for (int i = 0; i < recordingList.size(); i++) {
			File file = new File((String) recordingList.get(i));
			if (file.exists()) {
				file.delete();
			}
		}
		// 正在暂停后，继续录音的这一段音频文件
		if (isDeleteRecord) {
			recordFile.delete();
		}
	}
	
	/**
	 * 从目录中获取播放文件的列表
	 */
	static public List<File> getPlaybackFiles(){
		List<File> list = new ArrayList<File>();
        File[] f = (saveDirectory.listFiles());
        //list =  Arrays.asList(f);
        for (int i=0 ;i<f.length;i++){
        	String filename = f[i].getAbsolutePath();
        	if(filename.contains(".amr")){
        		list.add(f[i]);
        	}
        }
		return list;
	}
}
