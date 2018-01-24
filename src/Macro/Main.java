macro "Main"{

/*
===============================================================================
                            MAIN VARIABLES
===============================================================================
*/

    //Path of the folder containing all scripts
    PathMACRO = getDirectory("macros")+"PLA"+File.separator;

    /*
        ALL KEY PARAMETERS ARE IN settings.txt File
        ORIGINAL VALUES:
        Minimum Surface for initial nuclei detection =5000
        Maximum Surface for initial nuclei detection =Infinity
        Maximum Surface of a single nucleus =15000
        Minimum Circularity of single nucleus =0.3
        Minimum Surface for PLA foci =5
        Maximum Surface for PLA foci =Infinity
        Extension of DAPI images =_w1DAPI.TIF
        Extension of PLA images =_w2RFP.TIF
    */

/*
===============================================================================
                    PREPARATION OF IMAGEJ ENVIRONMENT
===============================================================================
*/

    //setBatchMode(true);

    /*
        WELCOME AND SO
    */

    //Close all remaining images
    PathM1 = getDirectory("macros");
    PathM1 += "PLA"+File.separator;
    PathM1 += "Close_Images.java";
    runMacro(PathM1);

    //Remove all existing ROI
    roiManager("reset");

    //Retrieve folder to explore
    myTitle = "PLEASE CHOOSE THE FOLDER CONTAINING THE FILES TO PROCESS"
    PathFolderInput = getDirectory(myTitle);

    //Generate Finger Print
    getDateAndTime(year,
                    month,
                    dayOfWeek,
                    dayOfMonth,
                    hour,
                    minute,
                    second,
                    msec);
    FP = "" + year + "-" + month + "-" + dayOfMonth + "_";
    FP += "" + hour + "-" + minute + "_";

    //Created text Files
    myAnalysis = "" + FP + "Parameters_and_Files.txt";
    myResults = "" + FP + "Results.txt";

    //Launch the GUI
    ARG0 = PathFolderInput + "\t";
    ARG0 += FP;
    runMacro(PathMACRO + "GUI.java",
                ARG0);

    //Retrrieve the updated (if so) Parameters in the SEttings.txt file
    P = File.openAsString(PathMACRO+"Settings.txt");
    //Create Array of lines
    Params = split(P, "\n");

    //Get all single parmaters
    MinSize = parseFloat(getParm(Params, 0, 1));
    MaxSize = getParm(Params, 1, 1);
    if (MaxSize!="Infinity"){
        MaxSize = parseFloat(MaxSize);
    }
    MaxSizeSingle = parseFloat(getParm(Params, 2, 1));
    MinCircSingle = parseFloat(getParm(Params, 3, 1));
    MinSizePLA = parseFloat(getParm(Params, 4, 1));
    MaxSizePLA = getParm(Params, 5, 1);
    if (MaxSizePLA!="Infinity"){
        MaxSizePLA = parseFloat(MaxSizePLA);
    }
    ExtDAPI = getParm(Params, 6, 1);
    ExtRFP = getParm(Params, 7, 1);



/*
===============================================================================
                            MAIN PROGRAM
===============================================================================
*/

    //Extract the Files Names
    ResExplorer = File.openAsString(PathFolderInput + myAnalysis);
    Segments = split(ResExplorer, "*");
    myFiles = split(Segments[Segments.length-1],"\n");

    //Create Report File
    f = File.open(PathFolderInput + myResults);
    File.close(f);
    header = "File" + "\t";
    header += "Nuclei" + "\t";
    header += "Surface" + "\t";
    header += "PLA foci" + "\t";
    header += "Surface PLA" + "\t";
    header += "% of Nucleus surface\n";
    File.append(header, PathFolderInput + myResults);

    //Loop of analysis of ALL images
    for (im=1; im<myFiles.length; im++){

        //Path of the current image
        Path = myFiles[im];

        //Get the parent path
        myFolder = File.getParent(Path) + File.separator();

        //Get image name without extension
        myImageName = replace(File.getName(Path), ExtDAPI, "");

        //Update Report
        File.append(myImageName, PathFolderInput + myResults);

        //Create ouput folder
        OUTFolder = myFolder;
        OUTFolder += FP + "ANALYSIS_";
        OUTFolder += myImageName + File.separator();
        if (File.exists(OUTFolder)==0){
            File.makeDirectory(OUTFolder);
        }

        //Open the current DAPI image to be treated
        open(Path);
        //Rename for easy handeling of the image
        rename("DAPI");
        run("Duplicate...", "title=DAPIori");

        //Get the image properties
        W = getWidth();
        H = getHeight();

        //Open the corresponding RFP image
        open(myFolder + myImageName +ExtRFP);
        //Rename for easy handeling of the image
        rename("RFP");
        run("Duplicate...", "title=RFP2");
        run("Duplicate...", "title=RFPori");

    //Process the DAPI image

        ARG1 = OUTFolder + "\t";
        ARG1 += myImageName + "\t";
        ARG1 += "" + MinSize + "\t";
        ARG1 += "" + MaxSize + "\t";
        ARG1 += "" + MaxSizeSingle + "\t";
        ARG1 += "" + MinCircSingle;

        runMacro(PathMACRO + "Treat_DAPI.java",
                    ARG1);

    //Process the RFP image

        ARG2 = OUTFolder + "\t";
        ARG2 += myImageName + "\t";
        ARG2 += "" + MinSizePLA + "\t";
        ARG2 += "" + MaxSizePLA + "\t";
        ARG2 += PathFolderInput + myResults;

        runMacro(PathMACRO + "Treat_RFP.java",
                    ARG2);

    //Save OUTPUT files

        //Create image Bilan and save it
        imageCalculator("Average create",
                        "DAPI",
                        "RFP2");
        PathBILAN = OUTFolder;
        PathBILAN += myImageName + "_Bilan.jpg";
        saveAs("Jpeg", PathBILAN);

        //Save the DAPI image
        selectWindow("DAPI");
        PathDAPI = OUTFolder;
        PathDAPI += myImageName + "_Nuclei.jpg";
        saveAs("Jpeg", PathDAPI);


        //Save the RFP image
        selectWindow("RFP2");
        PathRFP = OUTFolder;
        PathRFP += myImageName + "_PLA.jpg";
        saveAs("Jpeg", PathRFP);

        //Create image Original and save it
        imageCalculator("Average create",
                        "DAPIori",
                        "RFPori");
        PathORI = OUTFolder;
        PathORI += myImageName + "_Original.jpg";
        saveAs("Jpeg", PathORI);

        //Close All images
        runMacro(PathMACRO + "Close_Images.java");

        //Add empty line in the Report
        File.append("", PathFolderInput + myResults);

    }//END LOOP OF ANALYSIS

    //Convert txt to csv
    Conv = File.rename(PathFolderInput + myResults,
                        PathFolderInput + FP + "Results.csv");

    waitForUser("PLA analysis is over.");

/*
===============================================================================
                            FUNCTIONS
===============================================================================
*/

function getParm(Params, index, what){
    //Split the line index of Settings.txt file at the "=" sign
    infos = split(Params[index], "=");
    //Return the value [1] or the description of the variable [0]
    return infos[what];
}

}//END MACRO
