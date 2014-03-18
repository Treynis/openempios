/**
 *
 * Copyright (C) 2002-2012 "SYSNET International, Inc."
 * support@sysnetint.com [http://www.sysnetint.com]
 *
 * This file is part of OpenEMPI.
 *
 * OpenEMPI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.openempi.webapp.server;

import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.openempi.webapp.client.ProfileDataService;
import org.openempi.webapp.client.model.DataProfileWeb;
import org.openempi.webapp.client.model.DataProfileAttributeWeb;
import org.openempi.webapp.client.model.DataProfileAttributeValueWeb;
import org.openempi.webapp.server.util.ModelTransformer;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.model.DataProfile;
import org.openhie.openempi.model.DataProfileAttribute;
import org.openhie.openempi.model.DataProfileAttributeValue;
import org.openhie.openempi.profiling.DataProfileService;
import org.openhie.openempi.service.UserManager;

public class ProfileDataServiceImpl extends AbstractRemoteServiceServlet implements ProfileDataService
{
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

    public List<DataProfileWeb> getDataProfiles() throws Exception {

        authenticateCaller();
        try {
            DataProfileService profileService = Context.getDataProfileService();
            UserManager userService = Context.getUserManager();

            List<org.openhie.openempi.model.DataProfile> profiles = profileService.getDataProfiles();
            List<DataProfileWeb> dps = new java.util.ArrayList<DataProfileWeb>(profiles.size());
            for (DataProfile dp : profiles) {
                DataProfileWeb dpw = ModelTransformer.mapToDataProfile(dp, DataProfileWeb.class);
                if (dp.getDataSourceId() == -1) {
                    dpw.setDataSource("Repository Record Data Source");
                } else {
                    org.openhie.openempi.model.UserFile userFileFound = userService.getUserFile(dp.getDataSourceId());
                    if (userFileFound != null) {
                        dpw.setDataSource(userFileFound.getName());
                    }
                }
                dps.add(dpw);
            }
            return dps;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public String removeDataProfile(Integer dataProfileId) throws Exception {
        log.debug("Received request to delete the dada profile entry in the repository");

        authenticateCaller();

        String msg = "";
        try {
            DataProfileService profileService = Context.getDataProfileService();

            profileService.removeDataProfile(dataProfileId);

            return msg;

        } catch (Throwable t) {
            log.error("Failed while delete an dada profile entry: " + t, t);
            msg = t.getMessage();
            throw new Exception(t.getMessage());
        }
    }

	public List<DataProfileAttributeWeb> getDataProfileAttributes(Integer dataProfileId) throws Exception {

		authenticateCaller();
		try {
			DataProfileService profileService = Context.getDataProfileService();

			List<org.openhie.openempi.model.DataProfileAttribute> profileAttributes = profileService.getDataProfileAttributes(dataProfileId);
			List<DataProfileAttributeWeb> dpas = new java.util.ArrayList<DataProfileAttributeWeb>(profileAttributes.size());
			for (DataProfileAttribute dpa : profileAttributes) {
				DataProfileAttributeWeb dpaw = ModelTransformer.map(dpa, DataProfileAttributeWeb.class);
				dpas.add(dpaw);
			}

			return dpas;
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

	public List<DataProfileAttributeValueWeb> getDataProfileAttributeValues(Integer attributeId, int topCount) throws Exception {

		authenticateCaller();
		try {
			DataProfileService profileService = Context.getDataProfileService();
			List<org.openhie.openempi.model.DataProfileAttributeValue> profileAttributeValues = profileService.getTopDataProfileAttributeValues(attributeId, topCount);
			List<DataProfileAttributeValueWeb> dpavws = new java.util.ArrayList<DataProfileAttributeValueWeb>(profileAttributeValues.size());
			for (DataProfileAttributeValue dpav : profileAttributeValues) {
				DataProfileAttributeValueWeb dpavw = ModelTransformer.map(dpav, DataProfileAttributeValueWeb.class);
				dpavws.add(dpavw);
			}

			return dpavws;
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}
}
