/*L
 *  Copyright SAIC, Ellumen and RSNA (CTP)
 *
 *
 *  Distributed under the OSI-approved BSD 3-Clause License.
 *  See http://ncip.github.com/national-biomedical-image-archive/LICENSE.txt for details.
 */

package gov.nih.nci.nbia.download;

import gov.nih.nci.nbia.util.MD5Validator;
import gov.nih.nci.nbia.util.NBIAIOUtils;
import gov.nih.nci.nbia.util.StringUtil;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProxySelector;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.net.ssl.SSLHandshakeException;
import javax.swing.JOptionPane;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.impl.conn.ProxySelectorRoutePlanner;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

/**
 * This class downloads each series.
 * 
 * @author lethai
 * 
 */
public class LocalSeriesDownloader extends AbstractSeriesDownloader {

	/* Constructor for SeriesDownloader */
	public LocalSeriesDownloader() {

	}

	/**
	 * {@inheritDoc}
	 * 
	 * <p>
	 * Do a POST to the download servlet to retrieve the image files (with
	 * authentication/authorization for non-public artifacts)
	 */
	public void runImpl() throws Exception {
		this.sopUids = StringUtil.encodeListEntriesWithSingleQuotes(this.sopUidsList);
		computeTotalSize();
		URL url = new URL(serverUrl);
		this.connectAndReadFromURL(url, 0);
		
	}

// /////////////////////////////////////////PRIVATE//////////////////////////////////////

	/**
	 * images already received... sent back to server for pause and resume so
	 * server wont send these again.
	 */
	private List<String> sopUidsList = new ArrayList<String>();

	private String createDirectory() {
		String localLocation = createDownloadDir(dirType);
		File f = new File(localLocation);
		try {
			int count = 0;
			boolean mkdirResult = false;
			/*
			 * Attempt to create the directory again if it fails for 3 times.
			 * The reason for this is because 3 threads are started by default,
			 * sometime the directory is not created. We think it could be
			 * thread concurrency issue with parent directories creation, but
			 * couldn't reproduce the problem.
			 */
			while (count < 3) {
				if (!f.exists()) {
					mkdirResult = f.mkdirs();
					if (mkdirResult == false) {
						count++;
					} else {
						break;
					}
				} else {
					mkdirResult = true;
					break;
				}
			}
			if (!mkdirResult) {
				throw new RuntimeException("Could not create directory after 3 attempts: " + localLocation);
			}
		} catch (SecurityException se) {
			throw new RuntimeException(
					"SecurityException on creating directory: " + localLocation + "  " + se);
		}
		return localLocation;
	}
	
	private void letSleep(int attempt) {
		// Let it sleep some time when attempt is great than one already
		if (attempt >= 1 && attempt < noOfRetry) {
			Random randomGenerator = new Random();
			int randomInt = randomGenerator.nextInt(60000) + attempt * 120000;
//			System.out .println(getTimeStamp () + this.seriesInstanceUid +" attempt "+ attempt +" put into sleep for " + randomInt + "millisecond");
			try {
				
				Thread.sleep(randomInt);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
		}
	}
	
	private String getTimeStamp () {
		return new Timestamp(System.currentTimeMillis()).toString();
	}

	private void connectAndReadFromURL(URL url, int attempt) throws Exception {
		if (attempt >= noOfRetry) {
			additionalInfo.append(getTimeStamp() + " For series " + this.seriesInstanceUid +" Reached max retry (" + noOfRetry + ") attempts.\n");
			error();
			return;
		}
		letSleep(attempt);

		TrustStrategy easyStrategy = new TrustStrategy() {
			@Override
			public boolean isTrusted(X509Certificate[] certificate,
					String authType) throws CertificateException {
				return true;
			}
		};

		// set up a TrustManager that trusts everything
		SSLSocketFactory sslsf = new SSLSocketFactory(easyStrategy, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		Scheme httpsScheme = new Scheme("https", 443, sslsf);
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(httpsScheme);
		schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
		HttpParams httpParams = new BasicHttpParams();
		// set timeout values
		HttpConnectionParams.setConnectionTimeout(httpParams, 50000);
		HttpConnectionParams.setSoTimeout(httpParams, new Integer(720000));
		ClientConnectionManager ccm = new ThreadSafeClientConnManager(schemeRegistry);
		DefaultHttpClient httpClient = new DefaultHttpClient(ccm, httpParams);
		httpClient.setRoutePlanner(new ProxySelectorRoutePlanner(schemeRegistry, ProxySelector.getDefault()));
		// Additions by lrt for tcia -
		// attempt to reduce errors going through a Coyote Point Equalizer load
		// balance switch
		httpClient.getParams().setParameter("http.socket.timeout", new Integer(12000));
		httpClient.getParams().setParameter("http.socket.receivebuffer", new Integer(16384));
		httpClient.getParams().setParameter("http.tcp.nodelay", true);
		httpClient.getParams() .setParameter("http.connection.stalecheck", false);
		// end lrt additions

		HttpPost httpPostMethod = new HttpPost(url.toString());
		List<BasicNameValuePair> postParams = new ArrayList<BasicNameValuePair>();	
		postParams.add(new BasicNameValuePair("userId", this.userId));
		postParams.add(new BasicNameValuePair("sopUids", this.sopUids));
		postParams.add(new BasicNameValuePair("seriesUid", this.seriesInstanceUid));
		postParams.add(new BasicNameValuePair("includeAnnotation", Boolean.toString(this.includeAnnotation)));
		postParams.add(new BasicNameValuePair("hasAnnotation", Boolean.toString(this.hasAnnotation)));
		// this is ignored
		postParams.add(new BasicNameValuePair("Range", "bytes=" + downloaded + "-"));
		postParams.add(new BasicNameValuePair("password", password));		
		postParams.add(new BasicNameValuePair("newFileNames", "Yes"));
		if (AbstractSeriesDownloader.isCheckMD5() ) {
			postParams.add(new BasicNameValuePair("getMD5Hash", "Yes"));
		} else {
			postParams.add(new BasicNameValuePair("getMD5Hash", "No"));
		}
		
		httpPostMethod.addHeader("password", password);
		HttpRequestRetryHandler myRetryHandler = new HttpRequestRetryHandler() {
			@Override
			public boolean retryRequest(IOException exception,
					int executionCount, HttpContext context) {
				if (executionCount >= noOfRetry) {
					// Do not retry if over max retry count
					additionalInfo.append("Reached max retry (" + noOfRetry + ") attempts using Request handler.\n");
					return false;
				}
				if (exception instanceof NoHttpResponseException) {
					// Retry on when server dropped connection
					return true;
				}
				if (exception instanceof SSLHandshakeException) {
					// Do not retry on SSL handshake exception
					return false;
				}
				if (exception instanceof java.net.SocketTimeoutException) {
					// Retry on socket timeout exception
					additionalInfo.append(getTimeStamp() + " Request Handler attempt").append(executionCount).append(" for SocketTimeOutException \n");
					letSleep(executionCount);
					return true;
				}
				if (exception instanceof java.net.SocketException) {
					// Retry on socket timeout exception
					additionalInfo.append(getTimeStamp() +" Request Handler attempt").append(executionCount) .append(" for SocketException \n");
					letSleep(executionCount);
					return true;
				}
				HttpRequest request = (HttpRequest) context.getAttribute(ExecutionContext.HTTP_REQUEST);
				boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
				if (idempotent) {
					// Retry if the request is considered idempotent
					return true;
				}
				return false;
			}
		};
		httpClient.setHttpRequestRetryHandler(myRetryHandler);
		UrlEncodedFormEntity query = new UrlEncodedFormEntity(postParams);
		httpPostMethod.setEntity(query);


		/* Connect to server. */
		try {
			HttpResponse response = httpClient.execute(httpPostMethod);
			int responseCode = response.getStatusLine().getStatusCode();
//			System.out.println("response code ="+responseCode);
			/* Make sure response code is in the 200 range. */
			if (responseCode / 100 != 2) {
				if (responseCode == HttpURLConnection.HTTP_FORBIDDEN) {
					additionalInfo
					.append("Not authorized to access series: "+ this.seriesInstanceUid);
					unauthorized();
					httpClient.getConnectionManager().shutdown();
					return;
				}
				else {
				// additionalInfo.append("incorrect response code");
				additionalInfo.append(getTimeStamp() + " retry attempt").append(attempt + 1).append("for incorrect response code \n");
				httpClient.getConnectionManager().shutdown();
				
				//it could be caused by the exclusion list is large than 1000 and result in sql execution error.
				// no more oracle
				/*if ( this.sopUidsList.size()>= 1000) {
					additionalInfo.append(getTimeStamp() + "redo whole series because server internal error.");
					this.sopUidsList.clear();
					this.sopUids="";
					this.downloaded = 0;
					this.progressUpdater.bytesCopied(0);
				}*/
				connectAndReadFromURL(url, attempt + 1);
				}
			}

			/*
			 * Set the size for this download if it hasn't been already set.
			 */
			if (size == -1) {
				status = NO_DATA;
				stateChanged();
			}
			readFromConnection(response.getEntity().getContent());	
		} catch (javax.net.ssl.SSLPeerUnverifiedException e) {
			// exclude downloading already download image.
			this.sopUids = StringUtil.encodeListEntriesWithSingleQuotes(this.sopUidsList);
//			System.out.println(getTimeStamp() +" SSLPeerUnverifiedException-- for series "+this.seriesInstanceUid +" @ attempt--"+ attempt);
			e.printStackTrace();
			additionalInfo.append(getTimeStamp() +" image retry attempt ").append(attempt + 1).append(" for SSLPeer Unverified Exception \n");
			connectAndReadFromURL(url, attempt + 1);
		} catch (SocketTimeoutException e) {
			// exclude downloading already download image.
			this.sopUids = StringUtil.encodeListEntriesWithSingleQuotes(this.sopUidsList);
//			System.out.println(getTimeStamp() +" SocketTimeoutException-- for series " + this.seriesInstanceUid +" @ attempt--"+ attempt);
			e.printStackTrace();
			additionalInfo.append(getTimeStamp() +" image retry attempt ").append(attempt + 1).append(" for Socket timeout exception \n");
			connectAndReadFromURL(url, attempt + 1);
		} catch (SocketException e) {
			// exclude downloading already download image.
			this.sopUids = StringUtil.encodeListEntriesWithSingleQuotes(this.sopUidsList);
//			System.out.println(getTimeStamp() +" SocketException-- for series " + this.seriesInstanceUid + " @ attempt--"+ attempt);
			e.printStackTrace();
			additionalInfo.append(getTimeStamp() +" image retry attempt ").append(attempt + 1).append(" for Socket exception \n");
			connectAndReadFromURL(url, attempt + 1);
		} catch (org.apache.http.MalformedChunkCodingException e) {
			// exclude downloading already download image.
			this.sopUids = StringUtil.encodeListEntriesWithSingleQuotes(this.sopUidsList);
//			System.out.println(getTimeStamp() +" MalformedChunkCodingException-- for series " + this.seriesInstanceUid +" @ attempt--"+ attempt);
			e.printStackTrace();
			additionalInfo.append(getTimeStamp() +" image retry attempt ").append(attempt + 1).append(" for MalformedChunkCodingException \n");
			connectAndReadFromURL(url, attempt + 1);
		} catch (Exception e) {
			if ((e instanceof java.net.SocketTimeoutException) || (e instanceof java.io.IOException)){
				// exclude downloading already download image.
				this.sopUids = StringUtil.encodeListEntriesWithSingleQuotes(this.sopUidsList);
//				System.out.println(getTimeStamp() +" java.net.SocketTimeoutException-- for series"+this.seriesInstanceUid  + "@ attempt--"+ attempt);
				e.printStackTrace();
				additionalInfo.append(getTimeStamp() +" image retry attempt").append(attempt + 1).append(" for Socket timeout exception \n");
				connectAndReadFromURL(url, attempt + 1);
			}
			else {
//				System.out.println(getTimeStamp() +"!!!!! cought exception for series "+ this.seriesInstanceUid +" @attempt--"+ attempt);
				e.printStackTrace();
			}
		}
		
		finally {
			httpClient.getConnectionManager().shutdown();
		}
	}

	private void readFromConnection(InputStream is) throws Exception {
		String location = createDirectory();

		TarArchiveInputStream zis = new TarArchiveInputStream(is);

		long imageCnt = 0;
		// Begin lrt additions
		imageCnt = sopUidsList.size(); // needed for pause/resume, and for error
										// recovery
		long downloadedImgSize = 0;
		long downloadedAnnoSize = 0;

		// End lrt additions
		try {
			// the pause button affects this loop
			// per tar entry, will check status... which pause button could
			// change... then we will drop out of downloading.
			while (status == DOWNLOADING) {
				TarArchiveEntry tarArchiveEntry = zis.getNextTarEntry();
				if (tarArchiveEntry == null) {
					status = COMPLETE;
					break;
				}
				// Begin lrt additions
				long fileSize = tarArchiveEntry.getSize();
				long startDownloaded = downloaded;

				OutputStream outputStream = null;
				String fileName = tarArchiveEntry.getName();
				String sop = null;
				boolean annotation = true;
				if (fileName.contains("^")) {
					annotation = false;
					int pos = fileName.indexOf("^");
					sop = fileName.substring(0, pos);
					String newFileName = fileName.substring(pos + 1);
					outputStream = new FileOutputStream(location + File.separator + newFileName);
				} else {
					int pos = fileName.indexOf(".dcm");
					if (pos > 0) {
						sop = fileName.substring(0, pos);
						annotation = false;
					} else {
						sop = fileName;
					}
					if (!annotation) {
						// sopUidsList.add(sop.substring(0, pos)); - lrt moved to
						// below, after file size check
						outputStream = new FileOutputStream(
								location + File.separator + StringUtil.displayAsSixDigitString(imageCnt) + ".dcm");
					} else {
						outputStream = new FileOutputStream(location + File.separator + sop);
					}
				}

				try {
					NBIAIOUtils.copy(zis, outputStream, progressUpdater);
				} finally {
					outputStream.flush();
					outputStream.close();
				}

				imageCnt += 1;

				// Begin lrt additions
				long bytesDownloaded = downloaded - startDownloaded;

				if (bytesDownloaded != fileSize) {
					additionalInfo.append(" file size mismatch for instance " + sop + "\n");
					error();
				} else {
					if (!annotation) { // image file
						downloadedImgSize += bytesDownloaded;
						sopUidsList.add(sop);
					} else { // annotation file
						if (!fileName.equals("md5hashes.csv"))
						downloadedAnnoSize += bytesDownloaded;
					}
				}

			}  //while
			
			if (status == COMPLETE) {
				if (downloadedImgSize != imagesSize) {
					additionalInfo.append(this.seriesInstanceUid + " total size of image files mismatch.  Was "
							+ downloadedImgSize + " should be " + imagesSize);
					error();
				}
				if (downloadedAnnoSize != annoSize) {
					additionalInfo.append(this.seriesInstanceUid + " total size of annotation files mismatch.  Was "
							+ downloadedAnnoSize + " should be " + annoSize + "\n");

					error();
				}										
				// End lrt additions				
		
			if (sopUidsList.size() != Integer.valueOf(numberOfImages).intValue()) {
				additionalInfo.append("Number of image files mismatch. It Was supposed to be " + numberOfImages
						+ " image files but we found " + sopUidsList.size() + " at server side\n");
				error();
			} 
			}
			
			if (status == COMPLETE) {
				downloaded = size;
				updateDownloadProgress(size);

				if (this.standalone) {
					writeToMetaData();
					if (checkMD5) {
						String validateResult = MD5Validator.verifyCheckSum(location);
						if (validateResult.length() != 0) {
							additionalInfo.append(validateResult);
							error();
						}						
						// else should report the md5 validation is successful;
					}
				}
			}	
		} finally {
			org.apache.commons.io.IOUtils.closeQuietly(zis);
		}		
	}
	
	
		
	private void writeToMetaData() {
		String fileName  = outputDirectory.getAbsolutePath() + File.separator
				+ System.getProperty("databasketId").replace(".tcia", "")
				+ File.separator + "metadata.csv";
		
		String newLine = this.seriesInstanceUid + "," +
				 this.collection + "," + 
				 this.thirdPartyAnalysis + "," + 
				 this.descriptionURI + "," + 
				 this.patientId + "," + 
				 this.studyInstanceUid + "," + 
				 this.studyDesc + "," + 
				 this.studyDate + "," + 
				 this.seriesDesc+ "," + 
				 this.manufacturer + "," + 
				 this.modality + "," + 
				 this.sopClassName + "," + 
				 this.sopClassUID + "," + 
				 this.numberOfImages + "," + 
				 bytesIntoHumanReadable(this.size) + "," + 
				 this.getRelativeDownloadDir() + "," + 
				 (LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
		
		File file = new File(fileName); 
		if (!file.exists()) {
			System.out.println("System Error: metadata file has not created.  It should be done already.Exit from the program.");
			System.exit(ERROR);
        }
        else {
			try(PrintWriter output = new PrintWriter(new FileWriter(fileName,true))) 
			{								 
			    output.printf("%s\r\n", newLine);
			}
			catch (FileNotFoundException fe) {
				if (fe.getMessage().contains("because it is being used by another process")) {
					JOptionPane.showMessageDialog(null,
						    "Can't write to metadata.csv because it is being used by another process.");
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
        }
	}

	private String createDownloadDir(boolean dirType){
		String fileLoc;
		String databasketId = System.getProperty("databasketId");
		String downloadRootDir;
		if (databasketId == null) {
			downloadRootDir = "NBIADownloadDir";
		}
		else {
			downloadRootDir = databasketId.replace(".tcia", "");
		}
		if (this.dirType) {				
			fileLoc = outputDirectory.getAbsolutePath() + File.separator
					+ downloadRootDir
					+ File.separator + this.collection + File.separator
					+ this.patientId + File.separator + this.studyInstanceUid
					+ File.separator + this.seriesInstanceUid;
		}
		else {		
			fileLoc = outputDirectory.getAbsolutePath() + File.separator
					+ downloadRootDir
					+ File.separator + this.collection + File.separator
					+ this.patientId + File.separator 
					+ getPartOfName(this.studyDate)
					+ getPartOfName(this.studyId)
					+ getDescPartOfName(this.studyDesc)
					+ this.studyInstanceUid.substring(this.studyInstanceUid.length()-5)	
					+ File.separator 
					+ getPartOfName(this.seriesNum)
					+ getDescPartOfName(this.seriesDesc)
					+ this.seriesInstanceUid.substring(this.seriesInstanceUid.length()-5);
		}
		return fileLoc;
	}
	
	private String getRelativeDownloadDir(){
		String fileLoc;
		if (this.dirType) {				
			fileLoc = "." + File.separator + this.collection + File.separator
					+ this.patientId + File.separator + this.studyInstanceUid
					+ File.separator + this.seriesInstanceUid;
		}
		else {		
			fileLoc = "." + File.separator + this.collection + File.separator
					+ this.patientId + File.separator 
					+ getPartOfName(this.studyDate)
					+ getPartOfName(this.studyId)
					+ getDescPartOfName(this.studyDesc)
					+ this.studyInstanceUid.substring(this.studyInstanceUid.length()-5)	
					+ File.separator 
					+ getPartOfName(this.seriesNum)
					+ getDescPartOfName(this.seriesDesc)
					+ this.seriesInstanceUid.substring(this.seriesInstanceUid.length()-5);
		}
		return fileLoc;
	}
	
	private String bytesIntoHumanReadable(long bytes) {
//        long kilobyte = 1024;
//        long megabyte = kilobyte * 1024;
//        long gigabyte = megabyte * 1024;
//        long terabyte = gigabyte * 1024;
        
        long kilobyte = 1000;
        long megabyte = kilobyte * 1000;
        long gigabyte = megabyte * 1000;
        long terabyte = gigabyte * 1000;        

        if ((bytes >= 0) && (bytes < kilobyte)) {
            return bytes + " B";

        } else if ((bytes >= kilobyte) && (bytes < megabyte)) {
            return String.format("%.2f", ((double)bytes / kilobyte)) + " KB";

        } else if ((bytes >= megabyte) && (bytes < gigabyte)) {
            return String.format("%.2f", ((double)bytes / megabyte)) + " MB";

        } else if ((bytes >= gigabyte) && (bytes < terabyte)) {
            return String.format("%.2f", ((double)bytes / gigabyte)) + " GB";

        } else if (bytes >= terabyte) {
            return String.format("%.2f", ((double)bytes / terabyte)) + " TB";

        } else {
            return bytes + " Bytes";
        }
    }	
	
	private String getDescPartOfName(String str) {
		if ((str != null) && (str.length() >= 1)) {
			str = str.replace('/', '-');
			if (str.equals("null") || str.equals("-"))
				return "";
			//Is it possible that the description will start with "/" which will be turned into "-"? It is legal but not recommended. 
			//Need to see the real cases to decide it we can remove or replace the first "/" in the description. So comment out the next two lines.
//			else if (str.startsWith("-"))
//				return str.substring(1, Math.min(str.length(), 54)) + "-";
			else
				return str.substring(0, Math.min(str.length(), 53)) + "-";
		} else
			return "";
	}

	private String getPartOfName(String str) {
		if ((str != null) && (str.length() >= 1)) {
			if (str.equals("null"))
				return "";
			else
				return str + "-";
		} else
			return "";
	}
	public void clearSopUidsList() {
		if (sopUidsList != null)
			sopUidsList.clear();
	}
	
    public void resetDownloadProgress() {
    	downloaded = 0;
    	status = NOT_STARTED;
    	sopUids = null;
    	clearSopUidsList();
    	stateChanged();
    }
    
    public void resetForResume() {
    	downloaded = 0;
    	sopUids = null;
    	clearSopUidsList();
    	status = NOT_STARTED;
    	stateChanged();
    }
}