/*L
 *  Copyright SAIC, Ellumen and RSNA (CTP)
 *
 *
 *  Distributed under the OSI-approved BSD 3-Clause License.
 *  See http://ncip.github.com/national-biomedical-image-archive/LICENSE.txt for details.
 */

package gov.nih.nci.nbia.download;

import gov.nih.nci.nbia.util.NBIAIOUtils;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.Observable;

/**This class downloads each series.
 *
 * @author lethai
 *
 */
public abstract class AbstractSeriesDownloader extends Observable implements Runnable {
    /* These are the status names. */
    public static final String STATUSES[] = {"Not Started","Downloading",
        "Paused", "Complete", "Cancelled", "Error", "No Data", "Not Authorized"};

    /*These are the status codes.*/
    public static final int NOT_STARTED = 0;
    public static final int DOWNLOADING = 1;
    public static final int PAUSED = 2;
    public static final int COMPLETE = 3;
    public static final int CANCELLED = 4;
    public static final int ERROR = 5;
    public static final int NO_DATA =6;
    public static final int NOT_AUTHORIZED =7;    

    public static boolean checkMD5 = false;
 

	/* Constructor for SeriesDownloader */
    public AbstractSeriesDownloader() {
        status = NOT_STARTED;
    }
    
    public static boolean isCheckMD5() {
 		return checkMD5;
 	}

 	public static void setCheckMD5(boolean checkMD5) {
 		AbstractSeriesDownloader.checkMD5 = checkMD5;
 	}   

    /* Get this download's size.*/
    public long getSize() {
        return size;
    }

    /* Get this download's progress.*/
    public float getProgress(){
        return ((float) downloaded / size) * 100;
    }
    
    public long getDownloaded() {
    	return downloaded;
    }

    /* Get this download's status.*/
    public int getStatus(){
        return status;
    }

    /* Pause this download.*/
    public void pause() {
        status = PAUSED;
        stateChanged();
    }

    /* Resume this download.*/
    public void resume() {
        status = DOWNLOADING;        
        stateChanged();
    }

    /* Cancel this download.*/
    public void cancel() {
        status = CANCELLED;
        stateChanged();
    }

    /* Mark this download as having an error.*/
    protected void error() {
        int oldStatus = status;
        status = ERROR ;

        pcs.firePropertyChange("status", oldStatus, status);
        stateChanged();
    }
    
    protected void unauthorized() {
        int oldStatus = status;
        status = NOT_AUTHORIZED ;

        pcs.firePropertyChange("status", oldStatus, status);
        stateChanged();
    }  
    
    public void completed() {
        int oldStatus = status;
        status = COMPLETE;

        pcs.firePropertyChange("status", oldStatus, status);
        stateChanged();
        
//    	status = COMPLETE;
//    	downloaded= size;
//        stateChanged();
//        updateDownloadProgress(size);
    }        


    public void stateChanged(){
        setChanged();
        notifyObservers(this);
    }

    public String getCollection(){
        return collection;
    }

    public String getPatientId(){
        return patientId;
    }

    public String getStudyInstanceUid(){
        return studyInstanceUid;
    }

    public String getSeriesInstanceUid(){
        return seriesInstanceUid;
    }

    public boolean isIncludeAnnotation(){
        return includeAnnotation;
    }

    public boolean isHasAnnotation(){
        return hasAnnotation;
    }

    public String getNumberOfImages(){
        return this.numberOfImages;
    }

    /* (non-Javadoc)
     * @see com.javadude.beans.PropertyChangeNotifier#addPropertyChangeListener(java.beans.PropertyChangeListener)
    */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    /* (non-Javadoc)
     * @see com.javadude.beans.PropertyChangeNotifier#removePropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    public void setOutputDirectory(File outputDirectory) {
    	this.outputDirectory = outputDirectory;
    }

    /**
     * Must be called before run in order to initialize all data structures.
     */
    public void start(String serverUrl,
	                  String collection,
	                  String patientId,
	                  String studyInstanceUid,
	                  String seriesInstanceUid,
	                  boolean includeAnnotation,
	                  boolean hasAnnotation,
	                  String numberOfImages,
	                  String userId,
	                  String password,
	                  Long imagesSize,
	                  Long annoSize,
	                  String seriesIdentifier, Integer noOfRetry,
	                  String studyDate, String studyId, String studyDesc,
	                  String seriesNum, String seriesDesc){

		this.serverUrl = serverUrl;
		this.collection = collection;
		this.patientId = patientId;
		this.studyInstanceUid = studyInstanceUid;
		this.seriesInstanceUid = seriesInstanceUid;
		this.includeAnnotation = includeAnnotation;
		this.hasAnnotation = hasAnnotation;
		this.numberOfImages = numberOfImages;
		this.userId = userId;
		this.password = password;
		this.imagesSize = imagesSize;
		this.annoSize = annoSize;
		this.seriesIdentifier = seriesIdentifier;
		this.noOfRetry = noOfRetry;
		this.studyDate = studyDate;
		this.studyId = studyId;
		this.studyDesc = studyDesc;
		this.seriesNum = seriesNum;
		this.seriesDesc = seriesDesc;
				
		computeTotalSize();
		downloaded = 0;
		this.additionalInfo = new StringBuffer();
    }
    
    /**
     * Must be called before run in order to initialize all data structures.
     */
    public void start(String serverUrl,
	                  String collection,
	                  String patientId,
	                  String studyInstanceUid,
	                  String seriesInstanceUid,
	                  boolean includeAnnotation,
	                  boolean hasAnnotation,
	                  String numberOfImages,
	                  String userId,
	                  String password,
	                  Long imagesSize,
	                  Long annoSize,
	                  String seriesIdentifier, Integer noOfRetry,
	                  String studyDate, String studyId, String studyDesc,
	                  String seriesNum, String seriesDesc,
	                  String thirdPartyAnalysis,
	                  String descriptionURI,
	                  String manufacturer,	                  
	                  String modality,
	                  String sopClassUID,
	                  String sopClassName,
	                  String licenseName,
	                  String licenseUrl)
    {	
    	this.standalone = true;
		this.serverUrl = serverUrl;
		this.collection = collection;
		this.patientId = patientId;
		this.studyInstanceUid = studyInstanceUid;
		this.seriesInstanceUid = seriesInstanceUid;
		this.includeAnnotation = includeAnnotation;
		this.hasAnnotation = hasAnnotation;
		this.numberOfImages = numberOfImages;
		this.userId = userId;
		this.password = password;
		this.imagesSize = imagesSize;
		this.annoSize = annoSize;
		this.seriesIdentifier = seriesIdentifier;
		this.noOfRetry = noOfRetry;
		this.studyDate = studyDate;
		this.studyId = studyId;
		this.studyDesc = studyDesc;
		this.seriesNum = seriesNum;
		this.seriesDesc = seriesDesc;
		this.thirdPartyAnalysis = thirdPartyAnalysis;
		this.descriptionURI = descriptionURI;
		this.manufacturer = manufacturer;	                  
		this.modality = modality;
		this.sopClassUID = sopClassUID;
		this.sopClassName = sopClassName;
		this.licenseName = licenseName;
		this.licenseUrl = licenseUrl;
				
		computeTotalSize();
		downloaded = 0;
		this.additionalInfo = new StringBuffer();
    }   


    /**
     * Kick off the download for this series.
     */
    public final void run(){
    	System.out.println("AT THE TOP OF run for series:"+seriesInstanceUid);
        status = DOWNLOADING;
        stateChanged();

        long start = System.currentTimeMillis();
        try {

            runImpl();

            System.out.println("runImpl is done:"+status +" for series:"+seriesInstanceUid);
            //could be NO_DATA or ERROR i think
//            if (status == COMPLETE) {
//                downloaded= size;
//                stateChanged();
//                updateDownloadProgress(size);
//            }

        }
        catch (Exception e){
            //Changed by lrt for tcia -  we do wish to know what happened for debugging purposes.
            System.out.println("Exception: " + e);
            e.printStackTrace();
           // additionalInfo = e.getLocalizedMessage();
            error();
            // Changed by lrt for tcia -
            //    keep thread alive to work on other downloads, 
            //    but wait a moment in case there is a network problem
            //throw new RuntimeException(e);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
       }
        long end = System.currentTimeMillis();
        additionalInfo.append(" - total download time: " + (end - start)/1000 + "s.");
    }

    /**
     * An implementor must retrieve all the images for seriesInstanceUid.
     *
     * <p>The images must be written to a directory X, with the directory structure:
     * project/study/series/sop
     */
    public abstract void runImpl() throws Exception;




    ///////////////////////////////////////////////PROTECTED////////////////////////////////////////
    protected File outputDirectory;
    protected String seriesIdentifier;
    protected String collection;
    protected String patientId;
    protected String studyInstanceUid;
    protected String seriesInstanceUid;
    protected long size; // size of download in bytes
    protected long downloaded; // number of bytes downloaded
    protected int status; // current status of download
    protected boolean includeAnnotation;
    protected boolean hasAnnotation;
    protected String fileLocation;
    protected String serverUrl;
    protected String sopUids;
    protected String userId;
    protected String numberOfImages;
    protected String password;
    protected long imagesSize;
    protected long annoSize;
    protected int noOfRetry;
    protected StringBuffer additionalInfo;
    protected boolean dirType;
    protected String studyDate;
    protected String studyId;
    protected String studyDesc;
    protected String seriesNum;
    protected String seriesDesc;
    protected String thirdPartyAnalysis;
    protected String descriptionURI;
    protected String sopClassUID;
    protected String sopClassName;
    protected String modality;
    protected String manufacturer;
    protected boolean standalone = false; 
    protected String licenseName;
    protected String licenseUrl;
    
    protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    protected NBIAIOUtils.ProgressInterface progressUpdater = new ProgressUpdater();



    protected void computeTotalSize(){
        if(this.includeAnnotation && this.hasAnnotation){
            this.size = imagesSize + annoSize;
        }else{
            this.size = imagesSize;
        }
    }

    protected class ProgressUpdater implements NBIAIOUtils.ProgressInterface {
        public void bytesCopied(long n) {
             updateDownloadProgress(downloaded+n);
        }
    }

    protected void updateDownloadProgress(long bytesReceived) {
        downloaded = bytesReceived;
        stateChanged();
    }    

	/**
	 * @return the additionalInfo
	 */
	public StringBuffer getAdditionalInfo() {
		return additionalInfo;
	}

	/**
	 * @param additionalInfo the additionalInfo to set
	 */
	public void setAdditionalInfo(StringBuffer additionalInfo) {
		this.additionalInfo.append(additionalInfo);
	}
	
	public boolean isDirType() {
		return dirType;
	}

	public void setDirType(boolean dirType) {
		this.dirType = dirType;
	}
}