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

    //Parameters of the nuclei
    MinSize = 5000;
    MaxSize = "Infinity";
    MaxSizeSingle = 15000
    MinCircSingle = 0.3;

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
===============================================================================
                            MAIN PROGRAM
===============================================================================
*/

    //Choose image file
    //This will be replaced by automated detection and a loop will be generated
    Path = File.openDialog("Choose file");

    //Get the parent path
    myFolder = File.getParent(Path);

    //Get image name without extension
    myImageName = replace(File.getName(Path), "_w1DAPI.TIF", "");

    //Open the current image to be treated
    open(Path);

    //Rename for easy handeling of the images
    rename("DAPI");

    //Get the image properties
    W = getWidth();
    H = getHeight();

    /*
        Procedure to open the correspondingRFP
    */

//Process the DAPI image
    //Remove Background
    run("Subtract Background...", "rolling=100");

    //Threshold to find the whole surface of the nuclei
    setAutoThreshold("Huang dark");

    //Find the candidats Nuclei
    CMD1 = "size=" + MinSize + "-" + MaxSize;
    CMD1 += " show=Nothing";
    //The found Particles are added to the roiManager
    CMD1 += " add";
    run("Analyze Particles...", CMD1);

    //Create a new Image to display the good nuclei and treat the doublets
    newImage("Nuclei", "8-bit white", W, H, 1);

    //Processing of the candidats Nuclei
    for(candidat=0;
        candidat<roiManager("count");
        candidat++){

        selectWindow("Nuclei");
        roiManager("Select", candidat);

        //Measure the size and shape of the candidat
        List.setMeasurements;
        mySurface = List.getValue("Area");
        myCircularity = List.getValue("Circ.");

        //Fill the candidat Black on White
        setForegroundColor(0,0,0);
        run("Fill");

        //Treat the Candidat that is presumably a doublet
        if( (mySurface>MaxSizeSingle) && (myCircularity<MinCircSingle)){
            //Re-select the ROI
            roiManager("Select", candidat);
            //Separate the cells using Watershed
            run("Watershed");
        }

    }

    //Remove all ROI
    roiManager("reset");

    //Threshold to find the single nuclei
    makeRectangle(0, 0, W, H);
    setAutoThreshold("Huang");

    //Find all single nuclei
    CMD1 = "size=" + MinSize + "-" + MaxSize;
    CMD1 += " show=Nothing";
    //Nuclei touching the border of the image are excluded
    CMD1 += " exclude";
    //The found Particles are added to the roiManager    waitForUser("");
    CMD1 += " add";
    run("Analyze Particles...", CMD1);

    //Restore DAPI image and make it RGB
    selectWindow("DAPI");
    resetThreshold();
    run("RGB Color");

    //Rename explicitly all nuclei and draw on DAPI
    for(nucleus=0;
        nucleus<roiManager("count");
        nucleus++){

        selectWindow("DAPI");
        roiManager("Select", nucleus);
        roiManager("Rename", "NUCLEUS " + (nucleus+1));

        //Get an index between 0-9 whatever value
        index = ((nucleus / 10) - floor(nucleus / 10)) * 10;

        //Get the corresponding Cmap value (RGB)    waitForUser("");
        setForegroundColor(CmapR[index],
                            CmapG[index],
                            CmapB[index]);

        //Draw the nucleus on DAPI
        run("Line Width...", "line=2");
        run("Draw");
    }

    //Save the DAPI image
    PathDAPI = myFolder + File.separator();
    PathDAPI += myImageName + "_Nuclei.jpg";
    saveAs("Jpeg", PathDAPI);
    run("Close");

    //Save the Nuclei ROI
    PathNUCset = myFolder + File.separator();
    PathNUCset += myImageName + "_Nuclei.zip";
    roiManager("Save", PathNUCset);

    //Close the Nuclei window
    selectWindow("Nuclei");
    run("Close");








}//END MACRO
