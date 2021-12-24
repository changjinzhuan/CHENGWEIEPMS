package cn.kcrxorg.chengweiepms.mbutil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintWriter;


public class CopyFile {

	public CopyFile() {
	}

	/**
	 * 鏂板缓鐩綍
	 * 
	 * @param folderPath
	 *            String 濡� c:/fqf
	 * @return boolean
	 */
	public void newFolder(String folderPath) {
		try {
			String filePath = folderPath;
			filePath = filePath.toString();
			File myFilePath = new File(filePath);
			if (!myFilePath.exists()) {
				myFilePath.mkdir();

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 鏂板缓鏂囦欢
	 * 
	 * @param filePathAndName
	 *            String 鏂囦欢璺緞鍙婂悕绉� 濡俢:/fqf.txt
	 * @param fileContent
	 *            String 鏂囦欢鍐呭
	 * @return boolean
	 */
	public void newFile(String filePathAndName, String fileContent) {

		try {
			String filePath = filePathAndName;
			filePath = filePath.toString(); // 鍙栫殑璺緞鍙婃枃浠跺悕
			File myFilePath = new File(filePath);
			/** 濡傛灉鏂囦欢涓嶅瓨鍦ㄥ氨寤轰竴涓柊鏂囦欢 */
			if (!myFilePath.exists()) {
				myFilePath.createNewFile();
			}
			FileWriter resultFile = new FileWriter(myFilePath); // 鐢ㄦ潵鍐欏叆瀛楃鏂囦欢鐨勪究鎹风被,
																// 鍦ㄧ粰鍑� File
																// 瀵硅薄鐨勬儏鍐典笅鏋勯�犱竴涓�
																// FileWriter 瀵硅薄
			PrintWriter myFile = new PrintWriter(resultFile); // 鍚戞枃鏈緭鍑烘祦鎵撳嵃瀵硅薄鐨勬牸寮忓寲琛ㄧず褰㈠紡,浣跨敤鎸囧畾鏂囦欢鍒涘缓涓嶅叿鏈夎嚜鍔ㄨ鍒锋柊鐨勬柊
																// PrintWriter銆�
			String strContent = fileContent;
			myFile.println(strContent);
			resultFile.close();

		} catch (Exception e) {
			System.out.println("鏂板缓鏂囦欢鎿嶄綔鍑洪敊");
			e.printStackTrace();

		}

	}

	/**
	 * 鍒犻櫎鏂囦欢
	 * 
	 * @param filePathAndName
	 *            String 鏂囦欢璺緞鍙婂悕绉� 濡俢:/fqf.txt
	 * @param fileContent
	 *            String
	 * @return boolean
	 */
	public void delFile(String filePathAndName) {
		try {
			File myDelFile = new File(filePathAndName);
			if(myDelFile.exists()){
				myDelFile.delete();
			}
			else{
				//log.info("delFile():"+filePathAndName+"姝ゆ枃浠朵笉瀛樺湪!");
			}
		} catch (Exception e) {
			System.out.println("鍒犻櫎鏂囦欢鎿嶄綔鍑洪敊");
			//log.info("delFile():"+"鍒犻櫎鏂囦欢鎿嶄綔鍑洪敊");
			e.printStackTrace();
		}

	}

	/**
	 * 鍒犻櫎鏂囦欢澶�
	 * 
	 * @param filePathAndName
	 *            String 鏂囦欢澶硅矾寰勫強鍚嶇О 濡俢:/fqf
	 * @param fileContent
	 *            String
	 * @return boolean
	 */
	public void delFolder(String folderPath) {
		try {
			delAllFile(folderPath); // 鍒犻櫎瀹岄噷闈㈡墍鏈夊唴瀹�
			String filePath = folderPath;
			filePath = filePath.toString();
			File myFilePath = new File(filePath);
			myFilePath.delete(); // 鍒犻櫎绌烘枃浠跺す

		} catch (Exception e) {
			System.out.println("鍒犻櫎鏂囦欢澶规搷浣滃嚭閿�");
			e.printStackTrace();

		}

	}

	/**
	 * 鍒犻櫎鏂囦欢澶归噷闈㈢殑鎵�鏈夋枃浠�
	 * 
	 * @param path
	 *            String 鏂囦欢澶硅矾寰� 濡� c:/fqf
	 */
	public void delAllFile(String path) {
		File file = new File(path);
		if (!file.exists()) {
			return;
		}
		if (!file.isDirectory()) {
			return;
		}
		String[] tempList = file.list();
		File temp = null;
		for (int i = 0; i < tempList.length; i++) {
			if (path.endsWith(File.separator)) {
				temp = new File(path + tempList[i]);
			} else {
				temp = new File(path + File.separator + tempList[i]);
			}
			if (temp.isFile()) {
				
				System.out.println("删除结果:"+temp.delete());
			}
			if (temp.isDirectory()) {
				delAllFile(path + "/" + tempList[i]);// 鍏堝垹闄ゆ枃浠跺す閲岄潰鐨勬枃浠�
				delFolder(path + "/" + tempList[i]);// 鍐嶅垹闄ょ┖鏂囦欢澶�
			}
		}
	}

	/**
	 * 澶嶅埗鍗曚釜鏂囦欢
	 * 
	 * @param oldPath
	 *            String 鍘熸枃浠惰矾寰� 濡傦細c:/fqf.txt
	 * @param newPath
	 *            String 澶嶅埗鍚庤矾寰� 濡傦細f:/fqf.txt
	 * @return boolean
	 */
	public void copyFile(String oldPath, String newPath) {
		try {
			// int bytesum = 0;
			int byteread = 0;
			File oldfile = new File(oldPath);
			if (oldfile.exists()) { // 鏂囦欢瀛樺湪鏃�
				InputStream inStream = new FileInputStream(oldPath); // 璇诲叆鍘熸枃浠�
				FileOutputStream fs = new FileOutputStream(newPath);
				byte[] buffer = new byte[1444];
				// int length;
				while ((byteread = inStream.read(buffer)) != -1) {
					// bytesum += byteread; //瀛楄妭鏁� 鏂囦欢澶у皬
					// System.out.println(bytesum);
					fs.write(buffer, 0, byteread);
				}
				inStream.close();
				fs.close();
			}
		} catch (Exception e) {
			System.out.println("澶嶅埗鍗曚釜鏂囦欢鎿嶄綔鍑洪敊");
			e.printStackTrace();

		}

	}

	/**
	 * 澶嶅埗鏁翠釜鏂囦欢澶瑰唴瀹�
	 * 
	 * @param oldPath
	 *            String 鍘熸枃浠惰矾寰� 濡傦細c:/fqf
	 * @param newPath
	 *            String 澶嶅埗鍚庤矾寰� 濡傦細f:/fqf/ff
	 * @return boolean
	 */
	public void copyFolder(String oldPath, String newPath) {

		try {
			(new File(newPath)).mkdirs(); // 濡傛灉鏂囦欢澶逛笉瀛樺湪 鍒欏缓绔嬫柊鏂囦欢澶�
			File a = new File(oldPath);
			String[] file = a.list();
			File temp = null;
			for (int i = 0; i < file.length; i++) {
				if (oldPath.endsWith(File.separator)) {
					temp = new File(oldPath + file[i]);
				} else {
					temp = new File(oldPath + File.separator + file[i]);
				}

				if (temp.isFile()) {
					FileInputStream input = new FileInputStream(temp);
					FileOutputStream output = new FileOutputStream(newPath
							+ "/" + (temp.getName()).toString());
					byte[] b = new byte[1024 * 5];
					int len;
					while ((len = input.read(b)) != -1) {
						output.write(b, 0, len);
					}
					output.flush();
					output.close();
					input.close();
				}
				if (temp.isDirectory()) {// 濡傛灉鏄瓙鏂囦欢澶�
					copyFolder(oldPath + "/" + file[i], newPath + "/" + file[i]);
				}
			}
		} catch (Exception e) {
			System.out.println("澶嶅埗鏁翠釜鏂囦欢澶瑰唴瀹规搷浣滃嚭閿�");
			e.printStackTrace();

		}

	}

	/**
	 * 绉诲姩鏂囦欢鍒版寚瀹氱洰褰�
	 * 
	 * @param oldPath
	 *            String 濡傦細c:/fqf.txt
	 * @param newPath
	 *            String 濡傦細d:/fqf.txt
	 */
	public void moveFile(String oldPath, String newPath) {
		this.copyFile(oldPath, newPath);
		this.delFile(oldPath);

	}

	/**
	 * 绉诲姩鏂囦欢鍒版寚瀹氱洰褰�
	 * 
	 * @param oldPath
	 *            String 濡傦細c:/fqf.txt
	 * @param newPath
	 *            String 濡傦細d:/fqf.txt
	 */
	public void moveFolder(String oldPath, String newPath) {
		copyFolder(oldPath, newPath);
		delFolder(oldPath);

	}

	public static void main(String[] args) {
		CopyFile file = new CopyFile();
		String path = "D:\\Program Files\\RMB\\data\\201509231633311921680010116729.FSN";
		try{
		file.delFile(path);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	// 鎷疯礉鏂囦欢
	/**
	private void copyFile2(String source, String dest) {
		try {
			File in = new File(source);
			File out = new File(dest);
			FileInputStream inFile = new FileInputStream(in);
			FileOutputStream outFile = new FileOutputStream(out);
			byte[] buffer = new byte[10240];
			int i = 0;
			while ((i = inFile.read(buffer)) != -1) {
				outFile.write(buffer, 0, i);
			}// end while
			inFile.close();
			outFile.close();
		}// end try
		catch (Exception e) {

		}// end catch
	}// end copyFile
**/
}
