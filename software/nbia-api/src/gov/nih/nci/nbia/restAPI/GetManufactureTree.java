package gov.nih.nci.nbia.restAPI;

import java.util.List;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import gov.nih.nci.ncia.criteria.*;
import gov.nih.nci.nbia.util.SpringApplicationContext;
import gov.nih.nci.nbia.security.*;
import gov.nih.nci.nbia.util.SiteData;
import gov.nih.nci.nbia.util.*;
import gov.nih.nci.nbia.restUtil.AuthorizationUtil;
import gov.nih.nci.nbia.restUtil.JSONUtil;
import gov.nih.nci.nbia.dao.ValueAndCountDAO;
import gov.nih.nci.ncia.criteria.ValuesAndCountsCriteria;

@Path("/getManufacturerTree")
public class GetManufactureTree extends getData{


	@Context private HttpServletRequest httpRequest;

	@GET
	@Produces(MediaType.APPLICATION_JSON)

	public Response constructResponse(@QueryParam("Collection") String collection, 
			@QueryParam("Modality") String modality, @QueryParam("BodyPartExamined") String bodyPart) {
		try {	
//			   Authentication authentication = SecurityContextHolder.getContext()
//						.getAuthentication();
//				String user = (String) authentication.getPrincipal();
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

				ValueAndCountDAO valueAndCountDAO = (ValueAndCountDAO)SpringApplicationContext.getBean("ValueAndCountDAO");
				ValuesAndCountsCriteria criteria=new ValuesAndCountsCriteria();
				criteria.setObjectType("MANUFACTURER_TREE");
				criteria.setAuth(auth);
				//List<ValuesAndCountsDTO> values = valueAndCountDAO.getValuesAndCounts(criteria);
				TreeNode values =valueAndCountDAO.manufacturerTreeQuery(criteria);
				
				return Response.ok(JSONUtil.getJSONforTreeNode(values)).type("application/json")
						.build();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return Response.status(500)
						.entity("Server was not able to process your request").build();
	}
}
