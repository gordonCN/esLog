package com.log.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;

import com.log.bean.Ftp;

/**
 * Ftp工具类 需要导入commons-net-3.4.jar这个包
 */
public class FtpUtil {

	/**
	 * 获取ftp连接
	 * 
	 * @param f
	 * @return
	 * @throws Exception
	 */
	public FTPClient connectFtp(Ftp f) throws Exception {

		FTPClient ftp = new FTPClient();
		boolean flag = false;
		int reply;
		try {
			if (f.getPort() == null) {
				ftp.connect(f.getIpAddr(), 21);
			} else {
				ftp.connect(f.getIpAddr(), f.getPort());
			}
			// ftp登陆
			ftp.login(f.getUserName(), f.getPwd());
			// 设置文件传输类型
			ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
			// 检查延时
			reply = ftp.getReplyCode();
			// 如果延时不在200到300之间，就关闭连接
			if (!FTPReply.isPositiveCompletion(reply)) {
				ftp.disconnect();
				System.out.println("close connect ftp ");
				return null;
			}

			ftp.setRemoteVerificationEnabled(false);
			ftp.enterLocalPassiveMode();

			ftp.changeWorkingDirectory(f.getPath());
			flag = true;
			return ftp;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}
	}

	/**
	 * 关闭ftp连接
	 */
	public static void closeFtp(FTPClient ftp) {
		if (ftp != null && ftp.isConnected()) {
			try {
				ftp.logout();
				ftp.disconnect();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * ftp上传文件
	 * 
	 * @param f
	 * @throws Exception
	 */
	/*
	 * public static void upload(File f) throws Exception{ if (f.isDirectory()) {
	 * ftp.makeDirectory(f.getName()); ftp.changeWorkingDirectory(f.getName());
	 * //返回目录名和文件名组成的字符串数组 String[] files=f.list(); for(String fstr : files){ File
	 * file1=new File(f.getPath()+"/"+fstr); if (file1.isDirectory()) {
	 * upload(file1); ftp.changeToParentDirectory(); }else{ File file2=new
	 * File(f.getPath()+"/"+fstr); FileInputStream input=new FileInputStream(file2);
	 * ftp.storeFile(file2.getName(),input); input.close(); } } }else{ File
	 * file2=new File(f.getPath()); FileInputStream input=new
	 * FileInputStream(file2); ftp.storeFile(file2.getName(),input); input.close();
	 * } }
	 */

	/**
	 * 下载链接配置
	 * 
	 * @param f
	 * @param localBaseDir  本地目录
	 * @param remoteBaseDir 远程目录
	 * @throws Exception
	 */
	public boolean startDown(Ftp f, String localBaseDir, String remoteBaseDir) throws Exception {
		boolean downloadFlag = false;
		Pattern pattern = null;

		PropertiesUtils proConfig = new PropertiesUtils("conf");
		String filter = proConfig.getKeyValue("ftp.filter");

		if (pattern == null) {
			pattern = Pattern.compile(filter);
		}

		FTPClient ftp = null;
		ftp = connectFtp(f);
		boolean download_flag = false; 
		if (null != ftp) {

		//	System.out.println("ftp connected ");
			try {
				FTPFile[] files = null;
				// 定义本地下载文件名称(文件名自己定义)
				boolean changedir = ftp.changeWorkingDirectory(remoteBaseDir);
				if (changedir) {
					ftp.setControlEncoding("utf-8");
					files = ftp.listFiles();
					for (int i = 0; i < files.length; i++) {
						try {

							if (pattern.matcher(files[i].getName()).matches()) {
							//	System.out.println("file " + files[i].getName());
								download_flag = downloadFile(ftp, files[i], localBaseDir, remoteBaseDir);
								if(download_flag) {
									deleteFile(ftp,remoteBaseDir,files[i].getName());
								}
							}
						} catch (Exception e) {
							System.out.println("<" + files[i].getName() + ">下载失败");
						}
					}

				}
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
				
				System.out.println("got file end ."  + sdf.format(new Date()) );
				downloadFlag = true;
			} catch (Exception e) {
				System.out.println("下载过程中出现异常");
				e.printStackTrace();

			} finally {
				if (null != ftp) {
					closeFtp(ftp);
				}

			}

		} else {
			System.out.println("链接失败！");
		}
		return downloadFlag;
	}

	/**
	 * 
	 * 下载FTP文件 当你需要下载FTP文件的时候，调用此方法 根据获取的文件名，本地地址，远程地址进行下载
	 * 
	 * @param ftpFile
	 * @param relativeLocalPath
	 * @param relativeRemotePath
	 */
	private boolean downloadFile(FTPClient ftp, FTPFile ftpFile, String relativeLocalPath, String relativeRemotePath) {
		boolean down_flag = false;
		if (ftpFile.isFile()) {
			if (ftpFile.getName().indexOf("?") == -1) {
				OutputStream outputStream = null;
				try {
					File entryDir = new File(relativeLocalPath);
				//	System.out.println("local url " + relativeLocalPath);
					// 如果文件夹路径不存在，则创建文件夹
					if (!entryDir.exists() || !entryDir.isDirectory()) {
						entryDir.mkdirs();
					}
					File locaFile = new File(relativeLocalPath + ftpFile.getName());
					// 判断文件是否存在，存在则返回
					if (locaFile.exists()) {
						System.out.println("file existed ");
						return true;
					} else {
						outputStream = new FileOutputStream(relativeLocalPath + ftpFile.getName());
						ftp.retrieveFile(ftpFile.getName(), outputStream);
				//		System.out.println("file got!  ");

						outputStream.flush();
						outputStream.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					try {
						if (outputStream != null) {
							outputStream.close();
						}
						down_flag = true ;
						
								
					} catch (IOException e) {
						System.out.println("输出文件流异常");
					}
				}
			}
		}
		
		return down_flag;
	}
	
	
	
	 /** * 删除文件 * 
     * @param pathname FTP服务器保存目录 * 
     * @param filename 要删除的文件名称 * 
     * @return */ 
     public boolean deleteFile(FTPClient ftp,String pathname, String filename){ 
         boolean flag = false; 
         try { 
        //     System.out.println("开始删除文件");
            
             //切换FTP目录 
             ftp.changeWorkingDirectory(pathname); 
             ftp.dele(filename); 
      //       ftp.logout();
             flag = true; 
            // System.out.println("删除 "+ filename +" 文件成功");
         } catch (Exception e) { 
             System.out.println("删除"+ filename +" 文件失败");
             e.printStackTrace(); 
         } finally {
           
         }
         return flag; 
     }
     

}
