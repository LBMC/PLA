macro "Treat_DAPI"{

    Argument = getArgument();
    Arguments = split(Argument, "\t");

    OUTFolder = Arguments[0];
    myImageName = Arguments[1];
    MinSize = parseFloat(Arguments[2]);
    MaxSize = parseFloat(Arguments[3]);
    MaxSizeSingle = parseFloat(Arguments[4]);
    MinCircSingle = parseFloat(Arguments[5]);
    myProgress = parseFloat(Arguments[6]);
    showProgress(myProgress);

    //Color map for drawing
    //Red
    CmapR = newArray(31, 255, 44, 214, 148, 140, 227, 127, 188, 23);
    //Green
    CmapG = newArray(119, 127, 160, 39, 103, 86, 119, 127, 189, 190);
    //Blue
    CmapB = newArray(180, 14, 44, 40, 189, 75, 194, 127, 34, 207);

    Windows = newArray("DAPI", "DAPIori");

    for (w=0; w<Windows.length; w++){
        showProgress(myProgress);
        selectWindow(Windows[w]);
        //Remove Background
        run("Subtract Background...", "rolling=100");
        //Enhance Contrast
        run("Enhance Contrast", "saturated=0.35");
    }


    selectWindow("DAPI");

    //Get the image properties
    W = getWidth();
    H = getHeight();
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
    CMD2 = "size=" + MinSize + "-" + MaxSize;
    CMD2 += " show=Nothing";
    //Nuclei touching the border of the image are excluded
    CMD2 += " exclude";
    //The found Particles are added to the roiManager
    CMD2 += " add";
    run("Analyze Particles...", CMD2);

    //Close the Nuclei window
    selectWindow("Nuclei");
    run("Close");

    //Restore DAPI imageS and make THEM Blue and RGB
    selectWindow("DAPI");
    resetThreshold();

    for (w=0; w<Windows.length; w++){
        showProgress(myProgress);
        selectWindow(Windows[w]);
        run("Blue");
        run("RGB Color");
    }

    selectWindow("DAPI");

    //Rename explicitly all nuclei and draw on DAPI
    for(nucleus=0;
        nucleus<roiManager("count");
        nucleus++){
        showProgress(myProgress);
        selectWindow("DAPI");
        roiManager("Select", nucleus);
        /*
            Fitting with "Fit Elipse" does not give better results...
            2018/01/22
        */
        roiManager("Rename", "NUCLEUS " + (nucleus+1));

        //Get an index between 0-9 whatever value
        index = ((nucleus / 10) - floor(nucleus / 10)) * 10;

        //Get the corresponding Cmap value (RGB)
        setForegroundColor(CmapR[index],
                            CmapG[index],
                            CmapB[index]);

        //Draw the nucleus on DAPI
        run("Line Width...", "line=2");
        run("Draw");
    }


    //Save the Nuclei ROIs as text
    PathNUCset = OUTFolder;
    PathNUCset += myImageName + "_Nuclei.txt";
    ROIsave(PathNUCset, "append");

/*
===============================================================================
                            FUNCTIONS
===============================================================================
*/

function ROIsave(path, option){
    /*
        Recognized options:
        -"overwrite"
        -"append"

        Structure of the roi:
        "Name*X0;X1;...;Xn*Y0;Y1;...;Yn"
    */

    //create the file if not existing
    if(File.exists(path)==0){
        f = File.open(path);
        File.close(f);
    }

    //clear the file if overwriting is ordered
    if(option=="overwrite"){
        f = File.open(path);
        File.close(f);
    }

    //Loop of saving the ROIs
    for(roi=0; roi<roiManager("count"); roi++){
        showProgress(myProgress);
        roiManager("Select", roi);
        Roi.getCoordinates(xpoints, ypoints);
        Nom = Roi.getName();

        //Convert coordinates arrays as a single string
        X = "";
        Y = "";

        for(x=0; x<xpoints.length; x++){
            X += "" + xpoints[x];
            Y += "" + ypoints[x];

            if(x!=xpoints.length-1){
                X += ";";
                Y += ";";
            }
        }

        //Append the roi to the file
        File.append(Nom + "*" + X + "*" + Y,
                    path);
    }
}


}//END MACRO
