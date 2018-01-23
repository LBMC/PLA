macro "Refresh" {

    //Prepare key paths
    Path = File.separator()+"home.users";
    Path += File.separator()+"dcluet";
    Path += File.separator()+"Programmes";
    Path += File.separator()+"2017_Auboeuf_PLA";
    Path += File.separator()+"src";

    PathFolderInput = Path+File.separator+"Macro"+File.separator;
    PathOutput = getDirectory("macros")+"PLA"+File.separator;

    //Listing of the files to instal
    Listing = newArray("Main.java",
                        "Close_Images.java",
                        "Treat_DAPI.java",
                        "Treat_RFP.java",
                        "GUI.java");

    //Protect settings
    if(File.exists(PathOutput+"Settings.txt")==0){
        Transfer=File.copy(PathFolderInput+"Settings.txt",
                            PathOutput+"Settings.txt");
    }else{
        waitForUser("Your setting file has been protected!");
    }

    //Create the installation folder if required
    if(File.exists(PathOutput)==0){
        File.makeDirectory(getDirectory("macros")+File.separator+"PLA");
    }

    //Update files or create them
    for(i=0; i<lengthOf(Listing); i++){
    	if(File.exists(PathFolderInput+Listing[i])==0){
        	exit("" + PathFolderInput+Listing[i] + " file is missing");
    	}else{
			Transfer=File.copy(PathFolderInput+Listing[i],
                                PathOutput+Listing[i]);
		}
	}

    //Launch main macro
    Path = PathOutput + "Main.java";
    runMacro(Path);

}
