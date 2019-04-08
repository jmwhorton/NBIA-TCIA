//To Test: http://localhost:8080/nbia-api/api/v1/getCollectionValues?format=json
//To Test: http://localhost:8080/nbia-api/api/v1/getCollectionValues?format=html
//To Test: http://localhost:8080/nbia-api/api/v1/getCollectionValues?format=xml
//To Test: http://localhost:8080/nbia-api/api/v1/getCollectionValues?format=csv


package gov.nih.nci.nbia.restAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import gov.nih.nci.nbia.lookup.*;
import gov.nih.nci.nbia.util.NCIAConfig;
import gov.nih.nci.nbia.util.SpringApplicationContext;
import gov.nih.nci.nbia.util.StringEncrypter;
import gov.nih.nci.nbia.security.*;
import gov.nih.nci.nbia.util.SiteData;
import gov.nih.nci.nbia.restUtil.AuthorizationUtil;
import gov.nih.nci.nbia.restUtil.ManifestMaker;
import gov.nih.nci.nbia.dto.CustomSeriesListDTO;
import gov.nih.nci.nbia.dto.SeriesDTO;
import gov.nih.nci.nbia.dao.CustomSeriesListDAO;
import gov.nih.nci.nbia.dao.GeneralSeriesDAO;
import gov.nih.nci.nbia.searchresult.SeriesSearchResult;
import gov.nih.nci.nbia.util.SeriesDTOConverter;

@Path("/getManifestForCollection")
public class GetManifestForCollection extends getData {
	public final static String TEXT_CSV = "text/csv";

	@Context
	private HttpServletRequest httpRequest;

	/**
	 * This method get a set of all collection names
	 *
	 * @return String - set of collection names
	 */
	@POST
	@Produces(MediaType.TEXT_PLAIN)

	public Response constructResponse(@FormParam("collection") String collection, @FormParam("visibility") String visibility, @FormParam("includeAnnotation") String includeAnnotation) {
		try {
			if (includeAnnotation == null || includeAnnotation.length() < 1) {
				includeAnnotation = "true";
			}

			if (visibility == null || visibility.length() < 1) {
				visibility = "1";
			} else if (visibility.equals("13")) {
				// 13 is "select all" or regardless the visibility
				visibility = "";
			}

			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm");
			LocalDateTime now = LocalDateTime.now();
			String manifestFileName = collection + "-" + dtf.format(now) + ".tcia";

			List<String> seriesList = null;

			GeneralSeriesDAO generalSeriesDAO = (GeneralSeriesDAO) SpringApplicationContext.getBean("generalSeriesDAO");
			List<String> seriesListOut = generalSeriesDAO.findSeriesByCollectionAndVisibility(collection, visibility);
			String manifest = ManifestMaker.getManifextFromSeriesIds(seriesListOut, includeAnnotation,
					manifestFileName);
			return Response.ok(manifest).type("application/x-nbia-manifest-file").build();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Response.status(500).entity("Server was not able to process your request").build();
	}

}