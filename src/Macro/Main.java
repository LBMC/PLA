macro "Main"{

/*
===============================================================================
                            MAIN VARIABLES
===============================================================================
*/

    //Color map for drawing
    //Red
    CmapR = newArray(31, 255, 44, 214, 148, 140, 227, 127, 188, 23);
    //Green
    CmapG = newArray(119, 127, 160, 39, 103, 86, 119, 127, 189, 190);
    //Blue
    CmapB = newArray(180, 14, 44, 40, 189, 75, 194, 127, 34, 207);

    //Path of the folder containing all scripts
    PathMACRO = getDirectory("macros")+"PLA"+File.separator;

    //Parameters of the nuclei
    MinSize = 5000;
    MaxSize = "Infinity";
    MaxSizeSingle = 15000
    MinCircSingle = 0.3;

    //Parameters for PLA
    MinSizePLA = 5;
    MaxSizePLA = "Infinity"

/*
===============================================================================
                    PREPARATION OF IMAGEJ ENVIRONMENT
===============================================================================
*/

    //Close all remaining images
    PathM1 = getDirectory("macros");
    PathM1 += "Droplets"+File.separator;
    PathM1 += "Close_Images.java";
    runMacro(PathM1);

    //Remove all existing ROI
    roiManager("reset");

    /*
        MACRO GUI
        MACRO FOR AUTOMATED DETECTION
    */

    ExtDAPI = "_w1DAPI.TIF";
    ExtRFP = "_w2RFP.TIF";

/*
===============================================================================
                            MAIN PROGRAM
===============================================================================
*/

    //Choose image file
    //This will be replaced by automated detection and a loop will be generated
    Path = File.openDialog("Choose file");

    //Get the parent path
    myFolder = File.getParent(Path) + File.separator();

    //Get image name without extension
    myImageName = replace(File.getName(Path), ExtDAPI, "");

    //Create ouput folder
    OUTFolder= myFolder;
    OUTFolder += "ANALYSIS_";
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








}//END MACRO
