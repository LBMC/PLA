macro "Explorer"{

    //retrieve arguments
    Argument = getArgument();
    Arguments = split(Argument, "\t");

    Folder = Arguments[0];
    ExtensionDAPI = Arguments[1];
    ExtensionRFP = Arguments[2];
    Pathlist = Arguments[3];

    //Find the files and adding their path in the listing file
    listFiles(Folder, ExtensionDAPI, ExtensionRFP);

/*
===============================================================================
                            FUNCTIONS
===============================================================================
*/

function listFiles(folder, extension1, extension2) {

	list = getFileList(folder);
	for (i=0; i<list.length; i++) {
        if (File.isDirectory(folder+list[i])){
           	listFiles(""+folder+list[i], extension);
       	}

		if (endsWith(list[i], extension1)){
            fileRFP = replace(list[i],
                                extension1,
                                extension2);
            if (File.exists(""+folder+fileRFP)){
                //Only file with a RFP twin are added
                File.append(""+folder+list[i], Pathlist);
            }
		}
	}
}

}	//End of the macro
