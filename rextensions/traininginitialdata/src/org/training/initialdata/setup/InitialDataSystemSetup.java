/*
 * [y] hybris Platform
 *
 * Copyright (c) 2017 SAP SE or an SAP affiliate company.  All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package org.training.initialdata.setup;

import de.hybris.platform.commerceservices.dataimport.impl.CoreDataImportService;
import de.hybris.platform.commerceservices.dataimport.impl.SampleDataImportService;
import de.hybris.platform.commerceservices.setup.AbstractSystemSetup;
import de.hybris.platform.commerceservices.setup.data.ImportData;
import de.hybris.platform.commerceservices.setup.events.CoreDataImportedEvent;
import de.hybris.platform.commerceservices.setup.events.SampleDataImportedEvent;
import de.hybris.platform.core.initialization.SystemSetup;
import de.hybris.platform.core.initialization.SystemSetup.Process;
import de.hybris.platform.core.initialization.SystemSetup.Type;
import de.hybris.platform.core.initialization.SystemSetupContext;
import de.hybris.platform.core.initialization.SystemSetupParameter;
import de.hybris.platform.core.initialization.SystemSetupParameterMethod;
import org.training.initialdata.constants.TrainingInitialDataConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;


/**
 * This class provides hooks into the system's initialization and update processes.
 */
@SystemSetup(extension = TrainingInitialDataConstants.EXTENSIONNAME)
public class InitialDataSystemSetup extends AbstractSystemSetup
{
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(InitialDataSystemSetup.class);

	private static final String IMPORT_CORE_DATA = "importCoreData";
	private static final String IMPORT_SAMPLE_DATA = "importSampleData";
	private static final String ACTIVATE_SOLR_CRON_JOBS = "activateSolrCronJobs";
	private static final String IMPORT_CORE_PRODUCT_DATA = "importCoreProductData";

	// Rack Room Shoes
	private static final String my_PRODUCT_CATALOG_NAME = "my";
	private static final String my_CONTENT_CATALOG_NAME = "trainingstore";
	private static final String my_STORE_NAME = "trainingstore";

	// Off Broadway Shoes

	private CoreDataImportService coreDataImportService;
	private SampleDataImportService sampleDataImportService;

	/**
	 * Generates the Dropdown and Multi-select boxes for the project data import
	 */
	@Override
	@SystemSetupParameterMethod
	public List<SystemSetupParameter> getInitializationOptions()
	{
		final List<SystemSetupParameter> params = new ArrayList<SystemSetupParameter>();

		params.add(createBooleanSystemSetupParameter(IMPORT_CORE_DATA, "Import Core Data", true));
		params.add(createBooleanSystemSetupParameter(IMPORT_SAMPLE_DATA, "Import Sample Data", true));
		params.add(createBooleanSystemSetupParameter(ACTIVATE_SOLR_CRON_JOBS, "Activate Solr Cron Jobs", true));
		// Add more Parameters here as you require

		return params;
	}

	/**
	 * Implement this method to create initial objects. This method will be called by system creator during
	 * initialization and system update. Be sure that this method can be called repeatedly.
	 * 
	 * @param context
	 *           the context provides the selected parameters and values
	 */
	@SystemSetup(type = Type.ESSENTIAL, process = Process.ALL)
	public void createEssentialData(final SystemSetupContext context)
	{
		// Add Essential Data here as you require
	}

	
	/**
	 * Implement this method to create data that is used in your project. This method will be called during the system
	 * initialization. <br>
	 * Add import data for each site you have configured
	 *
	 * <pre>
	 * final List<ImportData> importData = new ArrayList<ImportData>();
	 *
	 * final ImportData sampleImportData = new ImportData();
	 * sampleImportData.setProductCatalogName(SAMPLE_PRODUCT_CATALOG_NAME);
	 * sampleImportData.setContentCatalogNames(Arrays.asList(SAMPLE_CONTENT_CATALOG_NAME));
	 * sampleImportData.setStoreNames(Arrays.asList(SAMPLE_STORE_NAME));
	 * importData.add(sampleImportData);
	 *
	 * getCoreDataImportService().execute(this, context, importData);
	 * getEventService().publishEvent(new CoreDataImportedEvent(context, importData));
	 *
	 * getSampleDataImportService().execute(this, context, importData);
	 * getEventService().publishEvent(new SampleDataImportedEvent(context, importData));
	 * </pre>
	 *
	 * @param context
	 *           the context provides the selected parameters and values
	 */
	@SystemSetup(type = Type.PROJECT, process = Process.ALL)
	public void createProjectData(final SystemSetupContext context)
	{
		/*
		 * Add import data for each site you have configured
		 */
		final boolean coreData = getBooleanSystemSetupParameter(context, IMPORT_CORE_DATA);
		final boolean coreProductData = getBooleanSystemSetupParameter(context, IMPORT_CORE_PRODUCT_DATA);
		final boolean sampleData = getBooleanSystemSetupParameter(context, IMPORT_SAMPLE_DATA);

		if (coreData) {
			final List<ImportData> importData = new ArrayList<>();

			// Rack Room Shoes
			final ImportData my_coreImportData = new ImportData();
			my_coreImportData.setProductCatalogName(my_PRODUCT_CATALOG_NAME);
			my_coreImportData.setContentCatalogNames(Arrays.asList(my_CONTENT_CATALOG_NAME));
			my_coreImportData.setStoreNames(Arrays.asList(my_STORE_NAME));
			importData.add(my_coreImportData);


			// Process importData
			getCoreDataImportService().execute(this, context, importData);
			getEventService().publishEvent(new CoreDataImportedEvent(context, importData));
		}

		if (coreProductData) {
			// Core stuff here

		}

		if (sampleData) {
			final List<ImportData> sampleImportData = new ArrayList<>();

			// Rack Room Shoes
			final ImportData my_sampleImportData = new ImportData();
			my_sampleImportData.setProductCatalogName(my_PRODUCT_CATALOG_NAME);
			my_sampleImportData.setContentCatalogNames(Arrays.asList(my_CONTENT_CATALOG_NAME));
			my_sampleImportData.setStoreNames(Arrays.asList(my_STORE_NAME));
			sampleImportData.add(my_sampleImportData);


			// Process importData
			getSampleDataImportService().execute(this, context, sampleImportData);
			getEventService().publishEvent(new SampleDataImportedEvent(context, sampleImportData));

//			// Load Additional Access Rights
//			importAccessRights(context.getExtensionName());
//
//			// Load Product Workflows - Rack Room
//			importWorkflows(context.getExtensionName(), my_PRODUCT_CATALOG_NAME);
//
//			// Load Product Workflows - Off Broadway
//			importWorkflows(context.getExtensionName(), OBS_PRODUCT_CATALOG_NAME);
		}
		
	}

	public CoreDataImportService getCoreDataImportService()
	{
		return coreDataImportService;
	}

	@Required
	public void setCoreDataImportService(final CoreDataImportService coreDataImportService)
	{
		this.coreDataImportService = coreDataImportService;
	}

	public SampleDataImportService getSampleDataImportService()
	{
		return sampleDataImportService;
	}

	@Required
	public void setSampleDataImportService(final SampleDataImportService sampleDataImportService)
	{
		this.sampleDataImportService = sampleDataImportService;
	}
}
