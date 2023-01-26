package gov.nih.nci.nbia.restAPI;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import javax.ws.rs.Path;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import gov.nih.nci.nbia.util.SpringApplicationContext;
import gov.nih.nci.nbia.restUtil.SearchUtil;
import gov.nih.nci.nbia.searchresult.*;
import gov.nih.nci.nbia.dao.*;
import gov.nih.nci.nbia.dto.SeriesDTO;

@Path("/getRestrictionsForTextSearch")
public class GetRestrictionsForTextSearch extends getData{
	public final static String TEXT_CSV = "text/csv";


	@POST
	@Produces(MediaType.TEXT_PLAIN)

	public Response constructResponse(@FormParam("textValue") String textValue) {

		try {	

	    SearchUtil util=new SearchUtil();
	    List<PatientSearchResult> patients=util.getPatients(textValue);
		List<String> list=new ArrayList<String>();
		for (PatientSearchResult patient:patients) {
			StudyIdentifiers[] studies=patient.getStudyIdentifiers();
			for (StudyIdentifiers study:studies) {
				GeneralSeriesDAO generalSeriesDAO = (GeneralSeriesDAO) SpringApplicationContext.getBean("generalSeriesDAO");
				if (study.getSeriesIdentifiers()!=null) {
					List<Integer> seriesList = Arrays.asList(study.getSeriesIdentifiers());
					List<SeriesDTO> seriesDTOs=generalSeriesDAO.findSeriesBySeriesPkId(seriesList);
					for (SeriesDTO seriesDTO:seriesDTOs) {
						if (seriesDTO.isCommercialRestrictions()) {
							return Response.ok("yes").type("application/text").build();
						}
					}
				}
				
			}	
		}

		return Response.ok("no").type("application/x-nbia-manifest-file").build();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Response.status(500)
				.entity("Server was not able to process your request").build();
	}
	

}
