package gov.nih.nci.nbia.restAPI;

import java.util.List;
import java.util.Arrays;

import javax.ws.rs.Path;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.FormParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import gov.nih.nci.nbia.util.SpringApplicationContext;
import gov.nih.nci.nbia.restUtil.ManifestMaker;
import gov.nih.nci.nbia.dao.GeneralSeriesDAO;

@Path("/getManifestFromPatientStudySeries")
public class GetManifestFromPatientStudySeries extends getData {
	public final static String TEXT_CSV = "text/csv";

	/**
	 * This method get a set of all collection names
	 *
	 * @return String - set of collection names
	 */
	@POST
	@Produces(MediaType.TEXT_PLAIN)

	public Response constructResponse(@FormParam("patientIDs") String patients, 
			@FormParam("studyUIDs") String studies, 
			@FormParam("seriesUIDs") String series, 
			@FormParam("anyOrAll") String anyOrAll, 
			@FormParam("includeAnnotation") String includeAnnotation) {
        if (includeAnnotation==null||includeAnnotation.length()<1) {
        	includeAnnotation="true";
        }
		long currentTimeMillis = System.currentTimeMillis();
		String manifestFileName = "manifest-" + currentTimeMillis + ".tcia";
		List<String> patientList=null;
		List<String> studyList=null;
		List<String> seriesList=null;
		if (patients!=null&&patients.length()>1) {
			patientList = (List<String>)Arrays.asList(patients.split("\\s*,\\s*"));
		}
		if (studies!=null&&studies.length()>1) {
			studyList = (List<String>)Arrays.asList(studies.split("\\s*,\\s*"));
		}
		if (series!=null&&series.length()>1) {
			seriesList = (List<String>)Arrays.asList(series.split("\\s*,\\s*"));
		}
		GeneralSeriesDAO generalSeriesDAO = (GeneralSeriesDAO)SpringApplicationContext.getBean("generalSeriesDAO");
		List<String> seriesListOut = generalSeriesDAO.getSeriesFromPatientStudySeriesUIDs(patientList, studyList, seriesList);
		String manifest=ManifestMaker.getManifextFromSeriesIds(seriesListOut, includeAnnotation, manifestFileName);
		return Response.ok(manifest).type("application/x-nbia-manifest-file").build();
	}

}