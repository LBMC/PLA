macro "Installation_2018-01-17"{

version = "1.0a 2018-01-17";


//IJ version verification and close the macro's window 
selectWindow("Installation.ijm");			
run("Close");						
requires("1.49g");					

//Initialisation of the error counter
Errors=0;

//GUI Message
Dialog.create("Installation wizard for the PLA macro");
Dialog.addMessage("Version\n" + version);
Dialog.addMessage("Cluet David\nResearch Ingeneer,PHD\nCNRS, ENS-Lyon, LBMC");
Dialog.addMessage("This program will install the PLA macro.\nShortcut will be added in the Plugins/Macros menu.");
Dialog.show();

//Prepare key paths
PathSUM = getDirectory("macros")+File.separator+"StartupMacros.txt";	
PathFolderInput =File.directory+"Macro"+File.separator;					
PathOutput = getDirectory("macros")+"PLA"+File.separator;

    Listing = newArray("Main.java",
                        "Close_Images.java",
                        "Treat_DAPI.java",
                        "Treat_RFP.java",
                        "GUI.java",
                        "Explorer.java");


//Create the installation folder if required
if(File.exists(PathOutput)==0){
File.makeDirectory(getDirectory("macros")+File.separator+"PLA");
}

    //Protect settings
    if(File.exists(PathOutput+"Settings.txt")==0){
        Transfer=File.copy(PathFolderInput+"Settings.txt",
                            PathOutput+"Settings.txt");
    }else{
        waitForUser("Your setting file has been protected!");
    }



//Installation of all files of the listing
for(i=0; i<lengthOf(Listing); i++){
	if(File.exists(PathFolderInput+Listing[i])==0){
		waitForUser("" + PathFolderInput+Listing[i] + " file is missing");
		Errors = Errors + 1;
	}else{
		Transfer=File.copy(PathFolderInput+Listing[i], PathOutput+Listing[i]);
	}
}


//Create the shortcut in IJ macro menu for the first installation (Main program)
PCommandLine = PathFolderInput+ "Command_Line.txt"; 
SUM = File.openAsString(PathSUM);
pos =lastIndexOf(SUM, "//END OF PLA");
if(pos == -1){
	SUM = SUM + "\n\n" + File.openAsString(PCommandLine); 
	Startup = File.open(PathSUM);
	print(Startup, SUM);
	File.close(Startup);
}


//The program prompts the user of the success or failure of the installation.
if(Errors == 0){
waitForUser("Installation has been performed sucessfully!\nRestart your ImageJ program.");
} else {
waitForUser("Files were missing!\nInstallation is incomplete.");
}

}
