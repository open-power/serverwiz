package com.ibm.ServerWizard2;

import com.ibm.ServerWizard2.LibraryFile.FileTypes;


public class LibraryManager {
	LibraryFile files[] = new LibraryFile[5];

	public void loadModel(SystemModel model) throws Exception {
		for (LibraryFile libFile : files) {
			if (libFile.getType() == LibraryFile.FileTypes.ATTRIBUTE_TYPE_XML) {
				model.loadAttributes(new XmlHandler(), libFile.getPath());
			}
			if (libFile.getType() == LibraryFile.FileTypes.TARGET_TYPE_XML) {
				model.loadTargetTypes(new XmlHandler(), libFile.getPath());
			}
			if (libFile.getType() == LibraryFile.FileTypes.TARGET_INSTANCES_XML) {
				model.loadTargetInstances(libFile.getPath());
			}
		}
	}

	public void init() {
		files[0] = new LibraryFile("xml/attribute_types.xml",FileTypes.ATTRIBUTE_TYPE_XML);
		files[1] = new LibraryFile("xml/attribute_types_hb.xml",FileTypes.ATTRIBUTE_TYPE_XML);
		files[2] = new LibraryFile("xml/attribute_types_mrw.xml",FileTypes.ATTRIBUTE_TYPE_XML);
		files[3] = new LibraryFile("xml/target_types_mrw.xml",FileTypes.TARGET_TYPE_XML);
		files[4] = new LibraryFile("xml/target_instances_v3.xml",FileTypes.TARGET_INSTANCES_XML);
	}
}
