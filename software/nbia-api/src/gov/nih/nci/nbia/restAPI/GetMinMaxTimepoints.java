package gov.nih.nci.nbia.restAPI;

import java.util.List;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import gov.nih.nci.ncia.criteria.*;
import gov.nih.nci.nbia.util.SpringApplicationContext;
import gov.nih.nci.nbia.security.*;
import gov.nih.nci.nbia.util.SiteData;
import gov.nih.nci.nbia.restUtil.AuthorizationUtil;
import gov.nih.nci.nbia.restUtil.JSONUtil;

import gov.nih.nci.nbia.dao.StudyDAO;
import gov.nih.nci.nbia.dto.TimePointDTO;

import gov.nih.nci.nbia.security.UnauthorizedException;

@Path("/getMinMaxTimepoints")
public class GetMinMaxTimepoints extends getData{


	@Context private HttpServletRequest httpRequest;

	@GET
	@Produces(MediaType.APPLICATION_JSON)

	public Response constructResponse() throws Exception {

		String user = getUserName(); 

		List<SiteData> authorizedSiteData = AuthorizationUtil.getUserSiteData(user);
		if (authorizedSiteData==null){
		    AuthorizationManager am = new AuthorizationManager(user);
		    authorizedSiteData = am.getAuthorizedSites();
		    AuthorizationUtil.setUserSites(user, authorizedSiteData);
		}
		AuthorizationCriteria auth = new AuthorizationCriteria();
		auth.setSeriesSecurityGroups(new ArrayList<String>());
		auth.setSites(authorizedSiteData);
		List<String> seriesSecurityGroups = new ArrayList<String>();
		StudyDAO studyDAO = (StudyDAO)SpringApplicationContext.getBean("studyDAO");
		TimePointDTO values=studyDAO.getMinMaxTimepoints(auth);
		return Response.ok(JSONUtil.getJSONforTimepoint(values)).type("application/json")
				.build();
	}
}
