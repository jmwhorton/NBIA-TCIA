//To Test: http://localhost:8080/nbia-api/api/v1/getCollectionValues?format=json
//To Test: http://localhost:8080/nbia-api/api/v1/getCollectionValues?format=html
//To Test: http://localhost:8080/nbia-api/api/v1/getCollectionValues?format=xml
//To Test: http://localhost:8080/nbia-api/api/v1/getCollectionValues?format=csv


package gov.nih.nci.nbia.restAPI;

import java.util.List;

import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import gov.nih.nci.nbia.dao.ImageDAO2;
import gov.nih.nci.nbia.util.SpringApplicationContext;
import gov.nih.nci.nbia.security.*;
import gov.nih.nci.nbia.util.SiteData;
import gov.nih.nci.nbia.restUtil.AuthorizationUtil;

@Path("/v2/getM5HashForImage")
public class V2_getM5HashForImage extends getData{
	/**
	 * 
	 *
	 * @return String - md5 hash if authorized
	 */
	@GET
	@Produces(MediaType.TEXT_PLAIN)

	public Response constructResponse(@QueryParam("SOPInstanceUid") String sopInstanceUid) {
		String results="";
//		Authentication authentication = SecurityContextHolder.getContext()
//				.getAuthentication();
//		String userName = (String) authentication.getPrincipal();
		String userName = getUserName(); 
		try {	
			List<SiteData> authorizedSiteData = AuthorizationUtil.getUserSiteData(userName);
			if (authorizedSiteData==null){
			     AuthorizationManager am = new AuthorizationManager(userName);
			     authorizedSiteData = am.getAuthorizedSites();
			     AuthorizationUtil.setUserSites(userName, authorizedSiteData);
			}
			ImageDAO2 tDao = (ImageDAO2)SpringApplicationContext.getBean("imageDAO2");
			try {
				results = tDao.getImage(sopInstanceUid, authorizedSiteData);
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
       
		return Response.ok(results).type(MediaType.TEXT_PLAIN)
				.build();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Response.status(500)
				.entity("Server was not able to process your request").build();
	}
	
}
	
